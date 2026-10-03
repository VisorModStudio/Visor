package org.vmstudio.visor.compatibility.voxy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vmstudio.visor.compatibility.MixinGate;
import org.vmstudio.visor.compatibility.voxy.VoxyCompatHelper;

@Pseudo
@MixinGate(classes = "me.cortex.voxy.client.core.VoxyRenderSystem")
@Mixin(targets = "me.cortex.voxy.client.core.VoxyRenderSystem", remap = false)
public class VoxyRenderSystemVRMixin {
    @Inject(method = "setupViewport", at = @At("HEAD"), cancellable = true, require = 0, expect = 0, remap = false)
    private void visor$noViewportForMirrorPass(CallbackInfoReturnable<Object> cir) {
        if (VoxyCompatHelper.shouldSkipVoxyRender()) {
            cir.setReturnValue(null);
        }
    }

    @Inject(method = "renderOpaque", at = @At("HEAD"), cancellable = true, require = 0, expect = 0, remap = false)
    private void visor$skipLodsForMirrorPass(CallbackInfo ci) {
        if (VoxyCompatHelper.shouldSkipVoxyRender()) {
            ci.cancel();
        }
    }

    @ModifyConstant(method = "computeProjectionMat", constant = @Constant(floatValue = 0.05F), require = 0, expect = 0, remap = false)
    private static float visor$vrNearPlane(float original) {
        return VoxyCompatHelper.vanillaNearPlane(original);
    }
}
