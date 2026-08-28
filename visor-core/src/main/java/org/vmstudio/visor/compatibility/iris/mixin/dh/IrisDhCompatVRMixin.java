package org.vmstudio.visor.compatibility.iris.mixin.dh;

import net.irisshaders.iris.compat.dh.DHCompat;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vmstudio.visor.compatibility.MixinGate;
import org.vmstudio.visor.compatibility.dh.DhCompatHelper;
import org.vmstudio.visor.compatibility.iris.IrisDhProjectionHelper;

@Pseudo
@MixinGate(classes = "net.irisshaders.iris.compat.dh.DHCompat")
@Mixin(value = DHCompat.class, remap = false)
public class IrisDhCompatVRMixin {
    @Inject(method = "getProjection", at = @At("RETURN"), require = 0, expect = 0, remap = false)
    private static void visor$useVrEyeFrustum(CallbackInfoReturnable<Matrix4f> cir) {
        if (!DhCompatHelper.isVrEyeWorldPass()) {
            return;
        }

        Matrix4f lodProjection = cir.getReturnValue();
        if (lodProjection != null) {
            IrisDhProjectionHelper.applyEyeFrustumShape(lodProjection);
        }
    }
}