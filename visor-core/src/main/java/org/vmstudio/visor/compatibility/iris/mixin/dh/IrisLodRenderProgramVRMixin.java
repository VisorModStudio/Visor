package org.vmstudio.visor.compatibility.iris.mixin.dh;

import net.irisshaders.iris.compat.dh.IrisLodRenderProgram;
import net.irisshaders.iris.shadows.ShadowRenderingState;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vmstudio.visor.compatibility.MixinGate;
import org.vmstudio.visor.compatibility.dh.DhCompatHelper;
import org.vmstudio.visor.compatibility.iris.IrisDhProjectionHelper;

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
        // shadow passes project orthographically, no need for eye frustum here
        if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            return;
        }

        if (projection instanceof Matrix4f projectionMatrix) {
            IrisDhProjectionHelper.applyEyeFrustumShape(projectionMatrix);
        }
    }
}
