package me.phoenixra.visor.loader.forge.mixin;

import com.mojang.blaze3d.vertex.PoseStack;

import me.phoenixra.visor.core.client.render.helpers.RenderPoseHelper;
import me.phoenixra.visor.core.client.render.VRRenderState;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;


@Mixin(GameRenderer.class)
public class ForgeGameRendererVRMixin {


    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setAnglesInternal(FF)V", remap = false), method = "renderLevel")
    public void removeAnglesInternal(Camera camera, float yaw, float pitch) {
        if (VRRenderState.getPhase().isVanilla()
                || !VRRenderState.getCameraType().isEye()) {
            camera.setAnglesInternal(yaw, pitch);
        }

    }

    @Redirect(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lorg/joml/Quaternionf;)V", ordinal = 2), method = "renderLevel")
    public void removeMulPosXRotation(PoseStack poseStack, Quaternionf quaternion) {
        if (VRRenderState.getPhase().isVanilla()
                || !VRRenderState.getCameraType().isEye()) {
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
