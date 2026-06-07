package org.vmstudio.visor.compatibility.immportals.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vmstudio.visor.compatibility.immportals.ImmPortalsCompatHelper;
import qouteall.imm_ptl.core.render.RendererUsingFrameBuffer;

@Mixin(value = RendererUsingFrameBuffer.class, remap = false)
public class RendererUsingFrameBufferMixin {
    @Inject(method = "replaceFrameBufferClearing", at = @At("HEAD"), cancellable = true, remap = false)
    private void visor$paintFogBackground(CallbackInfoReturnable<Boolean> cir) {
        if (ImmPortalsCompatHelper.paintPortalFogBackground()) {
            cir.setReturnValue(true);
        }
    }
}