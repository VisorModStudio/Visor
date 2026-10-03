package org.vmstudio.visor.compatibility.voxy;

import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;
import org.vmstudio.visor.api.ModLoader;
import org.vmstudio.visor.api.client.render.VRRenderPass;
import org.vmstudio.visor.compatibility.LodPassHelper;
import org.vmstudio.visor.compatibility.ShaderCompatHelper;
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.extensions.client.render.GameRendererExtension;

public final class VoxyCompatHelper {
    public static final String MOD_ID = "voxy";
    public static boolean isLoaded() {
        return ModLoader.get().isModLoaded(MOD_ID);
    }

    @Nullable
    public static VRRenderPass viewportPass() {
        return LodPassHelper.currentWorldPass();
    }

    public static boolean shouldSkipVoxyRender() {
        if (ShaderCompatHelper.isShaderActive()) {
            return false;
        }
        return LodPassHelper.shouldSkipMirrorPass();
    }

    public static float vanillaNearPlane(float original) {
        if (VisorState.get().isNotActive()) {
            return original;
        }
        return ((GameRendererExtension) Minecraft.getInstance().gameRenderer).visor$getNearClipPlane();
    }
}
