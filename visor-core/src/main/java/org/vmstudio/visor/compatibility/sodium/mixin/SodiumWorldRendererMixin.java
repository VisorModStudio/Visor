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
 * Prevent missing chunks
 */
@Pseudo
@Mixin(targets = {
    "me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer",
    "net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer"
})
public class SodiumWorldRendererMixin {

    @Group(name = "forceChunkUpdate", min = 1, max = 1)
    @ModifyVariable(method = "updateChunks", at = @At("STORE"), ordinal = 1, remap = false, expect = 0)
    private boolean visor$RenderUpdate(boolean dirty) {
        return visor$shouldForceTerrainUpdate(dirty);
    }

    @Group(name = "forceChunkUpdate", min = 1, max = 1)
    @ModifyVariable(method = "setupTerrain", at = @At("STORE"), ordinal = 2, remap = false, expect = 0)
    private boolean visor$RenderUpdateSodium5(boolean dirty) {
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
