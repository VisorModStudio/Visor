package org.vmstudio.visor.compatibility.sodium.mixin;

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
        return !VRRenderState.getPhase().isVanilla() || dirty;
    }

    @Group(name = "forceChunkUpdate", min = 1, max = 1)
    @ModifyVariable(method = "setupTerrain", at = @At("STORE"), ordinal = 2, remap = false, expect = 0)
    private boolean visor$RenderUpdateSodium5(boolean dirty) {
        return !VRRenderState.getPhase().isVanilla() || dirty;
    }
}
