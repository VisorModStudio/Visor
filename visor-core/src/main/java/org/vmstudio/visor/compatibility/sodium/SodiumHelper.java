package org.vmstudio.visor.compatibility.sodium;

import org.vmstudio.visor.api.ModLoader;

//SODIUM COMPATIBILITY
public final class SodiumHelper {

    private SodiumHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static boolean isLoaded() {
        var modLoader = ModLoader.get();
        return modLoader.isModLoaded("sodium")
                || modLoader.isModLoaded("rubidium")
                || modLoader.isModLoaded("embeddium");
    }
}
