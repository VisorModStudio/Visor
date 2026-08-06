package org.vmstudio.visor.compatibility.dynamicfps.mixin;

import net.minecraft.sounds.SoundSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vmstudio.visor.compatibility.ClassDependentMixin;
import org.vmstudio.visor.core.client.VisorState;

@Pseudo
@ClassDependentMixin("dynamic_fps.impl.feature.volume.SmoothVolumeHandler")
@Mixin(targets = {
        "dynamic_fps.impl.feature.volume.SmoothVolumeHandler",
        "dynamic_fps.impl.DynamicFPSMod"
}, remap = false)
public class DynamicFPSVolumeMixin {
    @Inject(method = "volumeMultiplier", at = @At("HEAD"), cancellable = true, remap = false)
    private static void visor$fullVolumeInVR(SoundSource source, CallbackInfoReturnable<Float> cir) {
        if (VisorState.get().isActive()) {
            cir.setReturnValue(1.0f);
        }
    }
}