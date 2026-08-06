package org.vmstudio.visor.compatibility.dynamicfps.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vmstudio.visor.compatibility.ClassDependentMixin;
import org.vmstudio.visor.core.client.VisorState;

@Pseudo
@ClassDependentMixin("dynamic_fps.impl.DynamicFPSMod")
@Mixin(targets = "dynamic_fps.impl.DynamicFPSMod", remap = false)
public class DynamicFPSModMixin {
    @Inject(method = "isDisabled", at = @At("HEAD"), cancellable = true, remap = false)
    private static void visor$isDisabledInVR(CallbackInfoReturnable<Boolean> cir) {
        if (VisorState.get().isActive()) {
            cir.setReturnValue(true);
        }
    }
}