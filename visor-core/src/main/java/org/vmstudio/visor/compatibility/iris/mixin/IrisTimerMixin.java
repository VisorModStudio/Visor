package org.vmstudio.visor.compatibility.iris.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vmstudio.visor.api.client.render.VRRenderPass;
import org.vmstudio.visor.compatibility.iris.IrisCompatHelper;
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.core.client.render.VRRenderState;

/**
 * Ticks Iris' frame {@code Timer} only during the first VR pass (EYE_LEFT) once per frame.
 * Prevents time-animated effects (water, clouds, sun) from advancing multiple times per VR frame.
 * Eliminates time desync between the eyes when {@code syncFrameCounter()} is enabled
 */
@Pseudo
@Mixin(targets = {
        "net.irisshaders.iris.uniforms.SystemTimeUniforms$Timer",
        "net.coderbot.iris.uniforms.SystemTimeUniforms$Timer"
}, remap = false)
public class IrisTimerMixin {
    @Inject(method = "beginFrame", at = @At("HEAD"), cancellable = true)
    private void visor$tickOncePerVrFrame(long frameStartTime, CallbackInfo ci) {
        if (IrisCompatHelper.syncFrameCounter()
                && VisorState.get().isActive()
                && VRRenderState.getRenderPass() != VRRenderPass.EYE_LEFT) {
            ci.cancel();
        }
    }
}
