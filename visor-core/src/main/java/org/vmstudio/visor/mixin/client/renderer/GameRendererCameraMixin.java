package org.vmstudio.visor.mixin.client.renderer;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import org.vmstudio.visor.api.client.render.VRRenderPass;
import org.vmstudio.visor.core.client.render.camera.VRCameraEntitySwap;
import org.vmstudio.visor.core.client.render.camera.VRCameraOverlaps;
import org.vmstudio.visor.core.client.render.camera.VRGameCamera;
import org.vmstudio.visor.core.client.render.VRRenderState;
import org.vmstudio.visor.core.client.render.helpers.RenderEffectsHelper;
import org.vmstudio.visor.core.client.render.helpers.RenderPoseHelper;
import org.vmstudio.visor.core.client.tasks.types.movement.TaskTeleport;
import net.minecraft.client.Camera;
import org.joml.Quaternionf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

@Mixin(GameRenderer.class)
public abstract class GameRendererCameraMixin {

    // ---- Shadow fields ----
    @Shadow @Final
    Minecraft minecraft;


    /* ***************** *\
  //--------MIXINS--------\\
    \* ***************** */

    @WrapOperation(method = "<init>", at = @At(value = "NEW", target = "net/minecraft/client/Camera"), require = 1)
    public Camera visor$useVRCamera(Operation<Camera> original) {
        return new VRGameCamera();
    }

    //? if >=1.21 && <26.1 {
    /*// 1.21 builds the frustum from Camera.rotation() instead of its euler angles
    @WrapOperation(at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/Camera;rotation()Lorg/joml/Quaternionf;"),
            method = "renderLevel", require = 1)
    public Quaternionf visor$noVanillaCameraRotation(Camera camera, Operation<Quaternionf> original) {
        if (VRRenderState.getPhase().isVanilla()) {
            return original.call(camera);
        }
        return new Quaternionf();
    }
    *///?} elif <1.21 {
    /*@WrapOperation(at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/Camera;getXRot()F"),
            method = "renderLevel", require = 1)
    public float visor$noVanillaCameraPitch(Camera camera, Operation<Float> original) {
        if (VRRenderState.getPhase().isVanilla()) {
            return original.call(camera);
        }
        return 0F;
    }

    @WrapOperation(at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/Camera;getYRot()F"),
            method = "renderLevel", require = 1)
    public float visor$noVanillaCameraYaw(Camera camera, Operation<Float> original) {
        if (VRRenderState.getPhase().isVanilla()) {
            return original.call(camera);
        }
        // -180 cancels the +180 vanilla
        return -180F;
    }
    *///?}

    //? if >=1.21 && <26.1 {
    /*@ModifyExpressionValue(method = "renderLevel",
            at = @At(value = "INVOKE", target = "Lorg/joml/Matrix4f;rotation(Lorg/joml/Quaternionfc;)Lorg/joml/Matrix4f;", remap = false), require = 1)
    public Matrix4f visor$orientCameraToPass(Matrix4f frustumMatrix) {
        if (VRRenderState.getPhase().isNotVanilla()) {
            RenderPoseHelper.applyCameraOrientation(
                    VRRenderState.getRenderPass(), frustumMatrix
            );
        }
        return frustumMatrix;
    }
    *///?} elif >=1.20.5 && <1.21 {
    /*@ModifyExpressionValue(method = "renderLevel",
            at = @At(value = "INVOKE", target = "Lorg/joml/Matrix4f;rotationXYZ(FFF)Lorg/joml/Matrix4f;", remap = false), require = 1)
    public Matrix4f visor$orientCameraToPass(Matrix4f frustumMatrix) {
        if (VRRenderState.getPhase().isNotVanilla()) {
            RenderPoseHelper.applyCameraOrientation(
                    VRRenderState.getRenderPass(), frustumMatrix
            );
        }
        return frustumMatrix;
    }
    *///?} elif <1.20.5 {
    /*@Inject(at = @At(value = "NEW", target = "org/joml/Matrix3f", remap = false),
            method = "renderLevel", require = 1)
    public void visor$orientCameraToPass(float partialTicks, long nanos, PoseStack poseStack, CallbackInfo ci) {
        if (VRRenderState.getPhase().isNotVanilla()) {
            RenderPoseHelper.applyCameraOrientation(
                    VRRenderState.getRenderPass(), poseStack
            );
        }
    }
    *///?}

    //? if <26.1 {
    /*@WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;pick(F)V"), method = "renderLevel", require = 1)
    public void visor$pickAndSetupCamera(GameRenderer g, float pPartialTicks, Operation<Void> original) {
        if (VRRenderState.getPhase().isVanilla()) {
            original.call(g, pPartialTicks);
            return;
        }
        if (VRRenderState.getRenderPass() == VRRenderPass.worldUpdater()) {
            original.call(g, pPartialTicks);

            if(MC.screen == null){
                TaskTeleport.updateTeleportDestination(MC.player);
            }
        }

        VRCameraEntitySwap.cacheCameraEntity(this.minecraft.getCameraEntity());
        VRCameraEntitySwap.setupCameraEntityAsVRCamera();
        VRCameraOverlaps.updateCameraOverlaps();
    }

    @Inject(at = @At(value = "TAIL"), method = "renderLevel", require = 1)
    public void visor$restoreCamera(CallbackInfo i) {
        if(VRRenderState.getPhase().isNotVanilla()) {
            VRCameraEntitySwap.restoreCameraEntity(
                    this.minecraft.getCameraEntity()
            );
        }
    }
    *///?}

    @Inject(at = @At("TAIL"), method = "renderLevel")
    public void visor$releaseHiddenAreaMask(CallbackInfo ci) {
        if(VRRenderState.getPhase().isNotVanilla()) {
            RenderEffectsHelper.releaseHiddenAreaMask();
        }
    }
}
