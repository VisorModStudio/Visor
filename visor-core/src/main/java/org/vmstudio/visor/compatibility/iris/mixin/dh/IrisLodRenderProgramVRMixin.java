package org.vmstudio.visor.compatibility.iris.mixin.dh;

import net.irisshaders.iris.compat.dh.IrisLodRenderProgram;
import net.irisshaders.iris.shadows.ShadowRenderingState;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vmstudio.visor.compatibility.MixinGate;
import org.vmstudio.visor.compatibility.dh.DhCompatHelper;

@Pseudo
@MixinGate(classes = "net.irisshaders.iris.compat.dh.IrisLodRenderProgram")
@Mixin(value = IrisLodRenderProgram.class, remap = false)
public class IrisLodRenderProgramVRMixin {
    @Inject(method = "fillUniformData", at = @At("HEAD"), require = 0, expect = 0, remap = false)
    private void visor$useVrEyeFrustum(@Coerce Object projection, @Coerce Object modelView,
                                       int worldYOffset, float partialTicks,
                                       CallbackInfo ci) {
        if (!DhCompatHelper.isVrEyeWorldPass()) {
            return;
        }
        if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            return;
        }
        if (!(projection instanceof Matrix4f projectionMatrix)) {
            return;
        }

        Matrix4f vrProjection = CapturedRenderingState.INSTANCE.getGbufferProjection();
        projectionMatrix.m00(vrProjection.m00());
        projectionMatrix.m11(vrProjection.m11());
        projectionMatrix.m20(vrProjection.m20());
        projectionMatrix.m21(vrProjection.m21());
    }
}