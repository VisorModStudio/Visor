package org.vmstudio.visor.compatibility.iris.mixin.dh;

import net.irisshaders.iris.compat.dh.DHCompat;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vmstudio.visor.compatibility.MixinGate;
import org.vmstudio.visor.compatibility.dh.DhCompatHelper;

@Pseudo
@MixinGate(classes = "net.irisshaders.iris.compat.dh.DHCompat")
@Mixin(value = DHCompat.class, remap = false)
public class IrisDhCompatVRMixin {
    @Inject(method = "getProjection", at = @At("RETURN"), cancellable = true, require = 0, expect = 0, remap = false)
    private static void visor$useVrEyeFrustum(CallbackInfoReturnable<Matrix4f> cir) {
        if (!DhCompatHelper.isVrEyeWorldPass()) {
            return;
        }

        Matrix4f vrProjection = CapturedRenderingState.INSTANCE.getGbufferProjection();
        Matrix4f dhProjection = cir.getReturnValue();
        if (dhProjection == null) {
            return;
        }

        dhProjection.m00(vrProjection.m00());
        dhProjection.m11(vrProjection.m11());
        dhProjection.m20(vrProjection.m20());
        dhProjection.m21(vrProjection.m21());
        cir.setReturnValue(dhProjection);
    }
}