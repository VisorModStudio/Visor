package org.vmstudio.visor.compatibility.flashback;

import lombok.Getter;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.Packet;
import org.vmstudio.visor.api.ModLoader;
import org.vmstudio.visor.api.common.utils.LoggerUtils;
import org.vmstudio.visor.compatibility.OneShotSetup;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class FlashbackCompatHelper {
    private static final String MOD_ID = "flashback";
    private static final OneShotSetup SETUP = new OneShotSetup(FlashbackCompatHelper::resolve);
    private static Field activeRecorder;
    private static Method writePacket;
    private static Method isInReplay;

    private static Object deferredRecorder;
    private static Method deferredEndTick;
    @Getter
    private static boolean runningDeferredEndTick;
    private static boolean deferringBroken;

    private FlashbackCompatHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static boolean isLoaded() {
        return ModLoader.get().isModLoaded(MOD_ID);
    }

    public static boolean isRecording() {
        return recorder() != null;
    }

    public static boolean isInReplay() {
        if (!isLoaded() || !SETUP.ok()) return false;
        try {
            return (boolean) isInReplay.invoke(null);
        } catch (ReflectiveOperationException e) {
            SETUP.disable();
            LoggerUtils.getLogger().error("Visor: cannot check if Flashback is playing a replay", e);
            return false;
        }
    }

    public static boolean deferEndTick(Object recorder, String methodName) {
        if (deferringBroken) return false;
        try {
            if (deferredEndTick == null || !deferredEndTick.getName().equals(methodName)
                    || !deferredEndTick.getDeclaringClass().isInstance(recorder)) {
                deferredEndTick = recorder.getClass().getMethod(methodName, boolean.class);
            }
        } catch (ReflectiveOperationException e) {
            deferredEndTick = null;
            return false;
        }
        deferredRecorder = recorder;
        return true;
    }

    public static void flushDeferredEndTick() {
        if (deferredRecorder == null) return;
        Object recorder = deferredRecorder;
        deferredRecorder = null;
        runningDeferredEndTick = true;
        try {
            deferredEndTick.invoke(recorder, false);
        } catch (ReflectiveOperationException e) {
            deferringBroken = true;
            LoggerUtils.getLogger().error("Visor: could not finish Flashback's recorded tick", e);
        } finally {
            runningDeferredEndTick = false;
        }
    }

    public static void storePacket(Packet<?> packet) {
        Object recorder = recorder();
        if (recorder == null) {
            return;
        }
        try {
            writePacket.invoke(recorder, packet, ConnectionProtocol.PLAY);
        } catch (ReflectiveOperationException e) {
            SETUP.disable();
            LoggerUtils.getLogger().error("Visor: could not send the VR packet to Flashback's recorder", e);
        }
    }

    private static Object recorder() {
        if (!SETUP.ok()) {
            return null;
        }
        try {
            return activeRecorder.get(null);
        } catch (IllegalAccessException e) {
            SETUP.disable();
            LoggerUtils.getLogger().error("Visor: cannot read Flashback's recorder handle", e);
            return null;
        }
    }

    private static boolean resolve() throws ReflectiveOperationException {
        Class<?> flashback = Class.forName("com.moulberry.flashback.Flashback");
        activeRecorder = flashback.getField("RECORDER");
        isInReplay = flashback.getMethod("isInReplay");
        writePacket = Class.forName("com.moulberry.flashback.record.Recorder")
                .getMethod("writePacketAsync", Packet.class, ConnectionProtocol.class);
        return true;
    }
}