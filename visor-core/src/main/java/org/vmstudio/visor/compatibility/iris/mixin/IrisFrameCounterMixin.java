package org.vmstudio.visor.compatibility.iris.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vmstudio.visor.api.client.render.VRRenderPass;
import org.vmstudio.visor.compatibility.iris.IrisCompatHelper;
import org.vmstudio.visor.core.client.VisorClientImpl;
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.core.client.render.VRRenderState;

@Pseudo
@Mixin(targets = {
        "net.irisshaders.iris.uniforms.SystemTimeUniforms$FrameCounter",
        "net.coderbot.iris.uniforms.SystemTimeUniforms$FrameCounter"
}, remap = false)
public class IrisFrameCounterMixin {
    @Unique
    private int visor$frozenFrame;
    @Unique
    private boolean visor$loggedSync;

    @Inject(method = "beginFrame", at = @At("HEAD"), cancellable = true)
    private void visor$tickOncePerVrFrame(CallbackInfo ci) {
        if (IrisCompatHelper.syncFrameCounter()
                && VisorState.get().isActive()
                && VRRenderState.getRenderPass() != VRRenderPass.EYE_LEFT) {
            ci.cancel();
        }
    }

    @Inject(method = "getAsInt", at = @At("RETURN"), cancellable = true)
    private void visor$sameFrameIndexPerEye(CallbackInfoReturnable<Integer> cir) {
        if (!IrisCompatHelper.syncFrameCounter() || !VisorState.get().isActive()) {
            return;
        }
        VRRenderPass pass = VRRenderState.getRenderPass();
        if (pass == null || pass == VRRenderPass.EYE_LEFT) {
            visor$frozenFrame = cir.getReturnValueI();
        } else {
            cir.setReturnValue(visor$frozenFrame);
            if (!visor$loggedSync) {
                visor$loggedSync = true;
                VisorClientImpl.LOGGER.info(
                        "Visor: Iris frameCounter synced across eyes (frozen at {} for pass {})", visor$frozenFrame, pass);
            }
        }
    }
}