package org.vmstudio.visor.compatibility.sodium;

import org.vmstudio.visor.api.ModLoader;

public class SodiumHelper {

    private SodiumHelper() {
        throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
    }

    public static boolean isLoaded() {
        return ModLoader.get().isModLoaded("sodium")
                || ModLoader.get().isModLoaded("rubidium")
                || ModLoader.get().isModLoaded("embeddium");
    }
}
