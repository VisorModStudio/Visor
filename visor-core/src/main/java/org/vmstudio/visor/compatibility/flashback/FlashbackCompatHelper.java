package org.vmstudio.visor.compatibility.flashback;

import org.vmstudio.visor.api.ModLoader;

import java.lang.reflect.Method;

public class FlashbackCompatHelper {
    private static final String MOD_ID = "flashback";
    private static boolean reflectionInitialized;
    private static boolean reflectionFailed;
    private static Method isInReplayMethod;

    private FlashbackCompatHelper() {
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
            return (boolean) isInReplayMethod.invoke(null);
        } catch (Throwable ignored) {}
        return false;
    }

    private static boolean ensureReflection() {
        if (!isLoaded()) return false;
        if (reflectionInitialized) return !reflectionFailed;

        reflectionInitialized = true;
        try {
            Class<?> flashbackClass = Class.forName("com.moulberry.flashback.Flashback");
            isInReplayMethod = flashbackClass.getMethod("isInReplay");
            reflectionFailed = false;
            return true;
        } catch (Throwable throwable) {
            reflectionFailed = true;
            return false;
        }
    }
}