package org.vmstudio.visor.compatibility.flashback;

import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.Packet;
import org.vmstudio.visor.api.ModLoader;
import org.vmstudio.visor.api.common.utils.LoggerUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class FlashbackCompatHelper {
    private static final String MOD_ID = "flashback";

    private static boolean INITIALIZED = false;
    private static boolean INIT_ERROR = false;

    private static Field recorderField;
    private static Method writePacketAsyncMethod;

    private FlashbackCompatHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static boolean isLoaded() {
        return ModLoader.get().isModLoaded(MOD_ID);
    }

    public static boolean isRecording(){
        if (init()) {
            try {
                Object recorder = recorderField.get(null);
                return recorder != null;
            } catch (IllegalAccessException e) {
                LoggerUtils.getLogger().error("Failed to access flashback player data", e);
            }
        }
        return false;
    }

    public static void storePacket(Packet<?> packet) {
        if (init()) {
            try {
                Object recorder = recorderField.get(null);
                if (recorder != null) {
                    writePacketAsyncMethod.invoke(recorder, packet, ConnectionProtocol.PLAY);
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                LoggerUtils.getLogger().error("Failed to store flashback player data", e);
            }
        }
    }

    private static boolean init() {
        if (INITIALIZED) {
            return !INIT_ERROR;
        }
        try {
            Class<?> Flashback = Class.forName("com.moulberry.flashback.Flashback");
            recorderField = Flashback.getField("RECORDER");
            Class<?> Recorder = Class.forName("com.moulberry.flashback.record.Recorder");
            writePacketAsyncMethod = Recorder.getMethod("writePacketAsync", Packet.class, ConnectionProtocol.class
            );
        } catch (ClassNotFoundException | NoSuchFieldException | NoSuchMethodException e) {
            INIT_ERROR = true;
        }
        INITIALIZED = true;
        return !INIT_ERROR;
    }
}