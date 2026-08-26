package org.vmstudio.visor.compatibility.flashback;

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

    private FlashbackCompatHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static boolean isLoaded() {
        return ModLoader.get().isModLoaded(MOD_ID);
    }

    public static boolean isRecording() {
        return recorder() != null;
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
        activeRecorder = Class.forName("com.moulberry.flashback.Flashback").getField("RECORDER");
        writePacket = Class.forName("com.moulberry.flashback.record.Recorder")
                .getMethod("writePacketAsync", Packet.class, ConnectionProtocol.class);
        return true;
    }
}