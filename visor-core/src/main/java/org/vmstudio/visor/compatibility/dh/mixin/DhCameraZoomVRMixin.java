package org.vmstudio.visor.compatibility.dh.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vmstudio.visor.compatibility.MixinGate;
import org.vmstudio.visor.compatibility.dh.DhCompatHelper;

@Pseudo
@MixinGate(
        classes = "com.seibel.distanthorizons.core.render.CameraZoom",
        methods = "update",
        fields = {"magnification", "coneTanHalfAngle", "lookDirectionX", "lookDirectionZ"}
)
@Mixin(targets = "com.seibel.distanthorizons.core.render.CameraZoom", remap = false)
public class DhCameraZoomVRMixin {
    @Shadow(remap = false)
    public double magnification;

    @Shadow(remap = false)
    public double coneTanHalfAngle;

    @Shadow(remap = false)
    public double lookDirectionX;

    @Shadow(remap = false)
    public double lookDirectionZ;

    @Inject(method = "update", at = @At("HEAD"), cancellable = true, require = 0, expect = 0, remap = false)
    private void visor$noZoomInVr(CallbackInfo ci) {
        if (!DhCompatHelper.isVrWorldPass()) {
            return;
        }
        
        this.magnification = 1.0;
        this.coneTanHalfAngle = 0.0;
        this.lookDirectionX = 0.0;
        this.lookDirectionZ = 0.0;
        ci.cancel();
    }
}
