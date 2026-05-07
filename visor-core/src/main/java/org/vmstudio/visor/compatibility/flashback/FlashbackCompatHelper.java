package org.vmstudio.visor.compatibility.flashback;

import org.vmstudio.visor.api.ModLoader;

public class FlashbackCompatHelper {
    private static final String MOD_ID = "flashback";

    private FlashbackCompatHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static boolean isLoaded() {
        return ModLoader.get().isModLoaded(MOD_ID);
    }
}