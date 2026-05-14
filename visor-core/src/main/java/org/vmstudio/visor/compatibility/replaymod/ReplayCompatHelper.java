package org.vmstudio.visor.compatibility.replaymod;

import org.vmstudio.visor.api.ModLoader;
import org.vmstudio.visor.core.client.VisorClientImpl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReplayCompatHelper {
    private static final String MOD_ID = "replaymod";

    private static boolean reflectionInitialized;
    private static boolean reflectionFailed;

    private static Field instanceField;
    private static Method getReplayHandlerMethod;

    private ReplayCompatHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static boolean isLoaded() {
        return ModLoader.get().isModLoaded(MOD_ID);
    }

    public static boolean isPlayingReplay() {
        if (!ensureReflection()) {
            return false;
        }

        try {
            Object instance = instanceField.get(null);
            if (instance != null) {
                Object handler = getReplayHandlerMethod.invoke(instance);
                return handler != null;
            }
        } catch (Throwable ignored) {
        }

        return false;
    }

    private static boolean ensureReflection() {
        if (!isLoaded()) {
            return false;
        }
        if (reflectionInitialized) {
            return !reflectionFailed;
        }

        reflectionInitialized = true;
        try {
            Class<?> replayClass = Class.forName("com.replaymod.replay.ReplayModReplay");
            instanceField = replayClass.getField("instance");
            getReplayHandlerMethod = replayClass.getMethod("getReplayHandler");

            reflectionFailed = false;
            return true;
        } catch (Throwable throwable) {
            reflectionFailed = true;
            VisorClientImpl.LOGGER.warn("Failed to initialize ReplayMod compatibility bridge", throwable);
            return false;
        }
    }
}
