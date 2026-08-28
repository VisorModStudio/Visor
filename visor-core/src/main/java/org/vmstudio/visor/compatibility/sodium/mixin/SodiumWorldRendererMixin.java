package org.vmstudio.visor.compatibility.sodium.mixin;

import org.vmstudio.visor.api.client.render.VRRenderPass;
import org.vmstudio.visor.compatibility.nvidium.NvidiumHelper;
import org.vmstudio.visor.core.client.render.VRRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Sodium re-traverses its chunk graph only when the camera moved since the last frame;
 * every VR pass has its own camera, so a graph cached by the previous pass leaves chunks missing.
 */
@Pseudo
@Mixin(targets = {
    "me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer",
    "net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer"
})
public class SodiumWorldRendererMixin {

    // pre-0.5 forks (rubidium/embeddium)
    @Group(name = "terrainRefresh", min = 1, max = 1)
    @ModifyVariable(method = "updateChunks", at = @At("STORE"), ordinal = 1, remap = false, expect = 0)
    private boolean visor$refreshChunksEachPass(boolean dirty) {
        return visor$shouldForceTerrainUpdate(dirty);
    }

    // sodium 0.5+
    @Group(name = "terrainRefresh", min = 1, max = 1)
    @ModifyVariable(method = "setupTerrain", at = @At("STORE"), ordinal = 2, remap = false, expect = 0)
    private boolean visor$refreshTerrainEachPass(boolean dirty) {
        return visor$shouldForceTerrainUpdate(dirty);
    }

    private static boolean visor$shouldForceTerrainUpdate(boolean dirty) {
        if (dirty) {
            return true;
        }
        if (VRRenderState.getPhase().isVanilla()) {
            return false;
        }

        VRRenderPass renderPass = VRRenderState.getRenderPass();
        if (renderPass == VRRenderPass.worldUpdater()) {
            NvidiumHelper.ensureVRTemporalCoherence();
            return true;
        }
        return (renderPass == VRRenderPass.THIRD_PERSON
                || renderPass == VRRenderPass.CENTER)
                && !NvidiumHelper.isRendererActive();
    }
}
