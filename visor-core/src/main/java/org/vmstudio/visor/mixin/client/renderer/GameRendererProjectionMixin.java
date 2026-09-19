package org.vmstudio.visor.mixin.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import me.phoenixra.atumvr.api.enums.EyeType;
import org.vmstudio.visor.api.client.render.VRRenderPass;
import org.vmstudio.visor.api.client.settings.VRClientSettings;
import org.vmstudio.visor.api.client.settings.enums.MirrorMode;
import org.vmstudio.visor.api.compatibility.mcversion.render.McModelViewStack;
import org.vmstudio.visor.api.compatibility.mcversion.render.McProjection;
import org.vmstudio.visor.api.compatibility.mcversion.render.McRenderUtils;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.core.client.render.VRRenderState;
import org.vmstudio.visor.extensions.client.render.GameRendererExtension;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// projection per render pass
@Mixin(GameRenderer.class)
public abstract class GameRendererProjectionMixin implements GameRendererExtension {

    // ---- Shadow fields ----
    @Shadow
    @Final
    Minecraft minecraft;
    @Shadow
    private float renderDistance;
    //? if <1.21.6 {
    /*@Shadow
    private float zoom;
    @Shadow
    private float zoomX;
    @Shadow
    private float zoomY;
    *///?}
    @Shadow @Final
    private Camera mainCamera;

    // ---- Unique fields ----
    @Unique
    public Matrix4f visor$thirdPersonProjection = new Matrix4f();
    @Unique
    public float visor$nearClipPlane = 0.02F;
    @Unique
    private float visor$farClipPlane = 128.0F;

    // ---- Shadow methods ----
    //? if >=1.21.2 {
    @Shadow
    public abstract Matrix4f getProjectionMatrix(float fov);
    @Shadow
    protected abstract float getFov(Camera mainCamera2, float partialTicks, boolean b);
    //?} else {
    /*@Shadow
    public abstract Matrix4f getProjectionMatrix(double fov);
    @Shadow
    protected abstract double getFov(Camera mainCamera2, float partialTicks, boolean b);
    *///?}


    /* ***************** *\
  //--------MIXINS--------\\
    \* ***************** */

    //? if >=1.21.2 {
    @Inject(at = @At("HEAD"), method = "getFov(Lnet/minecraft/client/Camera;FZ)F", cancellable = true)
    public void visor$fov(Camera camera, float f, boolean bl, CallbackInfoReturnable<Float> info) {
        if (VisorState.get().isActive() && VRRenderState.getSceneType().isMainMenu()) {
            info.setReturnValue(this.minecraft.options.fov().get().floatValue());
        }
    }

    @Inject(at = @At("HEAD"), method = "getProjectionMatrix(F)Lorg/joml/Matrix4f;", cancellable = true, require = 1)
    public void visor$projection(float fov, CallbackInfoReturnable<Matrix4f> info) {
        visor$applyVrProjection(fov, info);
    }
    //?} else {
    /*@Inject(at = @At("HEAD"), method = "getFov(Lnet/minecraft/client/Camera;FZ)D", cancellable = true)
    public void visor$fov(Camera camera, float f, boolean bl, CallbackInfoReturnable<Double> info) {
        if (VisorState.get().isActive() && VRRenderState.getSceneType().isMainMenu()) {
            info.setReturnValue(Double.valueOf(this.minecraft.options.fov().get()));
        }
    }

    @Inject(at = @At("HEAD"), method = "getProjectionMatrix(D)Lorg/joml/Matrix4f;", cancellable = true, require = 1)
    public void visor$projection(double fov, CallbackInfoReturnable<Matrix4f> info) {
        visor$applyVrProjection(fov, info);
    }
    *///?}

    @Unique
    private void visor$applyVrProjection(double d, CallbackInfoReturnable<Matrix4f> info) {
        if (VisorState.get().isNotActive()) {
            return;
        }
        PoseStack posestack = new PoseStack();
        visor$setupClipPlanes();
        ClientContext.renderer.updateProjection();

        VRRenderPass renderPass = VRRenderState.getRenderPass();
        if(renderPass == VRRenderPass.EYE_LEFT){
            McRenderUtils.mulPose(posestack,
                    ClientContext.renderer.getEyeProjection(EyeType.LEFT)
            );
            info.setReturnValue(
                    posestack.last().pose()
            );
            return;
        }
        if (renderPass == VRRenderPass.EYE_RIGHT) {
            McRenderUtils.mulPose(posestack,
                    ClientContext.renderer.getEyeProjection(EyeType.RIGHT)
            );
            info.setReturnValue(posestack.last().pose());
            return;
        }
        if (renderPass == VRRenderPass.THIRD_PERSON) {
            if (VRClientSettings.getMirrorMode() == MirrorMode.MIXED_REALITY) {
                McRenderUtils.mulPose(posestack,
                        new Matrix4f().setPerspective(
                                VRClientSettings.getMixedRealityFov() * Mth.DEG_TO_RAD,
                                VRClientSettings.getMixedRealityAspectRatio(), this.visor$nearClipPlane,
                                this.visor$farClipPlane
                        )
                );
            }else {
                McRenderUtils.mulPose(posestack,
                        new Matrix4f().setPerspective(
                                VRClientSettings.getThirdPersonFov() * Mth.DEG_TO_RAD,
                                (float) this.minecraft.getWindow().getScreenWidth()
                                        / (float) this.minecraft.getWindow().getScreenHeight(),
                                this.visor$nearClipPlane, this.visor$farClipPlane
                        )
                );
            }
            this.visor$thirdPersonProjection = new Matrix4f(posestack.last().pose());
            info.setReturnValue(posestack.last().pose());
            return;
        }

        //? if <1.21.6 {
        /*if (this.zoom != 1.0F) {
            posestack.translate(this.zoomX, -this.zoomY, 0.0D);
            posestack.scale(this.zoom, this.zoom, 1.0F);
        }
        *///?}
        McRenderUtils.mulPose(posestack,
                new Matrix4f()
                        .setPerspective(
                                (float) d * Mth.DEG_TO_RAD,
                                (float) this.minecraft.getWindow().getScreenWidth()
                                        / (float) this.minecraft.getWindow().getScreenHeight(),
                                this.visor$nearClipPlane,
                                this.visor$farClipPlane
                        )
        );

        info.setReturnValue(posestack.last().pose());
    }

    //? if >=1.21.5 {
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;isGameLoadFinished()Z", shift = Shift.AFTER), method = "render", require = 1)
    //?} else {
    /*@Inject(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;viewport(IIII)V", remap = false, shift = Shift.AFTER), method = "render", require = 1)
    *///?}
    public void visor$matrix(CallbackInfo info) {
        if(VisorState.get().isNotActive()) return;
        McProjection.setPerspective(
                this.getProjectionMatrix(
                        minecraft.options.fov().get()
                )
        );
        McModelViewStack.identity();
        McModelViewStack.apply();
    }


    /* ************************ *\
  //--------PUBLIC METHODS--------\\
    \* ************************ */

    @Override
    @Unique
    public void visor$setupClipPlanes() {
        this.renderDistance = (float) (this.minecraft.options.getEffectiveRenderDistance() * 16);
        this.visor$farClipPlane = this.renderDistance + 1024.0F;
    }

    @Override
    @Unique
    public float visor$getNearClipPlane() {
        return this.visor$nearClipPlane;
    }

    @Override
    @Unique
    public float visor$getFarClipPlane() {
        return this.visor$farClipPlane;
    }

    @Override
    @Unique
    public void visor$resetProjectionMatrix(float partialTicks) {
        McProjection.setPerspective(this.getProjectionMatrix(this.getFov(this.mainCamera, partialTicks, true)));
    }

    @Override
    @Unique
    public Matrix4f visor$getThirdPersonProjection() {
        return visor$thirdPersonProjection;
    }
}
