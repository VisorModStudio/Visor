package org.vmstudio.visor.compatibility.blur.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vmstudio.visor.compatibility.MixinGate;
import org.vmstudio.visor.core.client.VisorState;

// Works for 1.21.x -> 26.x (midnightdust version, Blur+)
@Pseudo
@MixinGate(classes = "eu.midnightdust.blur.animations.AbstractAnimationHandler")
@Mixin(targets = "eu.midnightdust.blur.animations.AbstractAnimationHandler", remap = false)
public class BlurAnimationHandler1_21_ppMixin {
    @Inject(method = "getCurrentValue", at = @At("HEAD"), cancellable = true, remap = false)
    private void visor$dropBlurRadius(CallbackInfoReturnable<Float> cir) {
        if (VisorState.get().isNotActive()) return;
        if (!getClass().getName().endsWith("BlurRadiusAnimationHandler")) return;

        cir.setReturnValue(0.0F);
    }
}
