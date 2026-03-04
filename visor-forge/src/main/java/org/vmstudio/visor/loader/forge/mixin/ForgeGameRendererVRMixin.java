package org.vmstudio.visor.loader.forge.mixin;

import com.mojang.blaze3d.vertex.PoseStack;

import org.vmstudio.visor.api.client.render.VRCameraType;
import org.vmstudio.visor.core.client.render.helpers.RenderPoseHelper;
import org.vmstudio.visor.core.client.render.VRRenderState;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;


@Mixin(GameRenderer.class)
public class ForgeGameRendererVRMixin {


    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setAnglesInternal(FF)V", remap = false), method = "renderLevel")
    public void removeAnglesInternal(Camera cam, float yaw, float pitch) {
        VRCameraType camera = VRRenderState.getCameraType();
        if (VRRenderState.getPhase().isVanilla()
                || camera == null || !camera.isEye()) {
            cam.setAnglesInternal(yaw, pitch);
        }

    }

    @Redirect(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lorg/joml/Quaternionf;)V", ordinal = 2), method = "renderLevel")
    public void removeMulPosXRotation(PoseStack poseStack, Quaternionf quaternion) {
        VRCameraType camera = VRRenderState.getCameraType();
        if (VRRenderState.getPhase().isVanilla()
                || camera == null || !camera.isEye()) {
            poseStack.mulPose(quaternion);
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lorg/joml/Quaternionf;)V ", ordinal = 3), method = "renderLevel(FJLcom/mojang/blaze3d/vertex/PoseStack;)V")
    public void removeMulPosYRotation(PoseStack s, Quaternionf quaternion) {
        if (VRRenderState.getPhase().isVanilla()) {
            s.mulPose(quaternion);
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lorg/joml/Quaternionf;)V ", ordinal = 4), method = "renderLevel(FJLcom/mojang/blaze3d/vertex/PoseStack;)V")
    public void removeMulPosZRotation(PoseStack s, Quaternionf quaternion) {
        if (VRRenderState.getPhase().isVanilla()) {
            s.mulPose(quaternion);
        } else {
            RenderPoseHelper.applyCameraOrientation(VRRenderState.getCameraType(), s);
        }
    }


}
