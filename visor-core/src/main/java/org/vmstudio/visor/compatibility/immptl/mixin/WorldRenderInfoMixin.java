package org.vmstudio.visor.compatibility.immptl.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vmstudio.visor.compatibility.immptl.ImmPtlCompatHelper;
import qouteall.imm_ptl.core.render.context_management.WorldRenderInfo;

@Mixin(value = WorldRenderInfo.class, remap = false)
public class WorldRenderInfoMixin {
    @Inject(method = "popRenderInfo", at = @At("TAIL"), remap = false)
    private static void visor$clearProjectionCacheAfterPortalRender(CallbackInfo ci) {
        ImmPtlCompatHelper.onPortalWorldRenderFinished();
    }
}