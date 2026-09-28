// #!MC-VERSION:: 1.21.8+
package org.vmstudio.visor.mixin.client.renderer;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.client.render.VRRenderPass;
import org.vmstudio.visor.api.client.settings.VRClientSettings;
import org.vmstudio.visor.api.client.settings.enums.MirrorMode;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.render.VRRenderState;
import org.vmstudio.visor.core.client.render.helpers.RenderPoseHelper;

@Mixin(ScreenEffectRenderer.class)
public abstract class ScreenEffectRendererMixin {

    @Shadow
    private int itemActivationTicks;

    @Inject(method = "renderScreenEffect", at = @At("HEAD"), cancellable = true)
    private void visor$noScreenEffectsInVR(CallbackInfo ci) {
        if (VRRenderState.getPhase().isNotVanilla()) {
            ci.cancel();
        }
    }

    @WrapOperation(method = "renderItemActivationAnimation", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;scale(FFF)V"))
    private void visor$skipActivationScale(PoseStack poseStack, float x, float y, float z,
                                           Operation<Void> original,
                                           @Local(argsOnly = true) float partialTicks
    ) {
        if (VRRenderState.getPhase().isVanilla()) {
            original.call(poseStack, x, y, z);
            return;
        }
        VRRenderPass currentCamera = VRRenderState.getRenderPass();
        var cameraPose = ClientContext.localPlayer.getPoseData(PlayerPoseType.RENDER).getCameraPose(currentCamera);

        float time = (40 - this.itemActivationTicks + partialTicks) / 40.0f;
        float t2 = time * time;
        float t3 = time * t2;
        float curve = 10.25f * t3 * t2 - 24.95f * t2 * t2 + 25.5f * t3 - 13.8f * t2 + 4.0f * time;
        float popScale = 0.5F * Mth.sin(curve * Mth.PI);

        poseStack.translate(0, 0, popScale - 1.0F);
        if (currentCamera == VRRenderPass.THIRD_PERSON) {
            float fov = VRClientSettings.getMirrorMode() == MirrorMode.MIXED_REALITY
                    ? VRClientSettings.getMixedRealityFov()
                    : VRClientSettings.getThirdPersonFov();
            popScale *= fov / 70.0F;
        }
        RenderPoseHelper.applyCameraPose(currentCamera, poseStack);
        original.call(poseStack, popScale, popScale, popScale);
        poseStack.mulPose(Axis.YP.rotation(-cameraPose.getYaw()));
        poseStack.mulPose(Axis.XP.rotation(-cameraPose.getPitch()));
    }

    @WrapOperation(method = "renderItemActivationAnimation", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V"))
    private void visor$noItemTranslate(PoseStack poseStack, float x, float y, float z, Operation<Void> original) {
        if (VRRenderState.getPhase().isVanilla()) {
            original.call(poseStack, x, y, z);
        }
    }
}
