package org.vmstudio.visor.compatibility.replaymod;

import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.Packet;
import org.vmstudio.visor.api.ModLoader;
import org.vmstudio.visor.api.common.utils.LoggerUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReplayCompatHelper {
    private static final String MOD_ID = "replaymod";

    private static boolean INITIALIZED = false;
    private static boolean INIT_ERROR = false;

    private static Method getRecordingEventHandlerMethod;
    private static Method onPacketMethod;


    private ReplayCompatHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static boolean isLoaded() {
        return ModLoader.get().isModLoaded(MOD_ID);
    }

    public static boolean isRecording(){
        if (init()) {
            try {
                Object recorder = getRecordingEventHandlerMethod.invoke(
                        Minecraft.getInstance().levelRenderer);
                return recorder != null;
            } catch (IllegalAccessException | InvocationTargetException e) {
                LoggerUtils.getLogger().error("Failed to store replaymod player data", e);
            }
        }
        return false;
    }
    public static void storePacket(Packet<?> packet) {
        if (init()) {
            try {
                Object recorder = getRecordingEventHandlerMethod.invoke(
                        Minecraft.getInstance().levelRenderer);
                if (recorder != null) {
                    onPacketMethod.invoke(recorder, packet);
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                LoggerUtils.getLogger().error("Failed to store replaymod player data", e);
            }
        }
    }

    private static boolean init() {
        if (INITIALIZED) {
            return !INIT_ERROR;
        }
        try {
            Class<?> RecordingEventSender = Class.forName(
                    "com.replaymod.recording.handler.RecordingEventHandler$RecordingEventSender");
            getRecordingEventHandlerMethod = RecordingEventSender.getMethod("getRecordingEventHandler");

            Class<?> RecordingEventHandler = Class.forName(
                    "com.replaymod.recording.handler.RecordingEventHandler");
            onPacketMethod = RecordingEventHandler.getMethod("onPacket", Packet.class);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            INIT_ERROR = true;
        }
        INITIALIZED = true;
        return !INIT_ERROR;
    }
}
