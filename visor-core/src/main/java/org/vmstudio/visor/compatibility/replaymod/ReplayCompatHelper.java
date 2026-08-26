package org.vmstudio.visor.compatibility.replaymod;

import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.Packet;
import org.vmstudio.visor.api.ModLoader;
import org.vmstudio.visor.api.common.utils.LoggerUtils;
import org.vmstudio.visor.compatibility.OneShotSetup;

import java.lang.reflect.Method;

public class ReplayCompatHelper {
    private static final String MOD_ID = "replaymod";
    private static final OneShotSetup SETUP = new OneShotSetup(ReplayCompatHelper::resolve);

    private static Method handlerFromLevelRenderer;
    private static Method acceptPacket;

    private ReplayCompatHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static boolean isLoaded() {
        return ModLoader.get().isModLoaded(MOD_ID);
    }

    public static boolean isRecording() {
        return handler() != null;
    }

    public static void storePacket(Packet<?> packet) {
        Object handler = handler();
        if (handler == null) {
            return;
        }
        try {
            acceptPacket.invoke(handler, packet);
        } catch (ReflectiveOperationException e) {
            SETUP.disable();
            LoggerUtils.getLogger().error("Visor: could not send the VR packet to ReplayMod's recorder", e);
        }
    }

    private static Object handler() {
        if (!SETUP.ok()) {
            return null;
        }
        try {
            return handlerFromLevelRenderer.invoke(Minecraft.getInstance().levelRenderer);
        } catch (ReflectiveOperationException e) {
            SETUP.disable();
            LoggerUtils.getLogger().error("Visor: can't get ReplayMod's recording handler", e);
            return null;
        }
    }

    private static boolean resolve() throws ReflectiveOperationException {
        handlerFromLevelRenderer = Class.forName("com.replaymod.recording.handler.RecordingEventHandler$RecordingEventSender")
                .getMethod("getRecordingEventHandler");
        acceptPacket = Class.forName("com.replaymod.recording.handler.RecordingEventHandler")
                .getMethod("onPacket", Packet.class);
        return true;
    }
}
