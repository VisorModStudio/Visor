package org.vmstudio.visor.compatibility.dh;

import org.vmstudio.visor.api.ModLoader;
import org.vmstudio.visor.compatibility.LodPassHelper;

public final class DhCompatHelper {
    public static final String MOD_ID = "distanthorizons";
    public static boolean isLoaded() {
        return ModLoader.get().isModLoaded(MOD_ID);
    }

    public static boolean shouldSkipDhRender() {
        return LodPassHelper.shouldSkipMirrorPass();
    }

    public static boolean isVrWorldPass() {
        return LodPassHelper.isVrWorldPass();
    }

    public static boolean isVrEyeWorldPass() {
        return LodPassHelper.isVrEyeWorldPass();
    }
}