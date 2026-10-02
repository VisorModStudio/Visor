// #!MC-VERSION:: 26.1.2+
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


    /* ***************** *\
  //--------MIXINS--------\\
    \* ***************** */

    @Override
    @Unique
    public Matrix4f visor$passProjection(float fov) {
        PoseStack posestack = new PoseStack();
        visor$setupClipPlanes();
        ClientContext.renderer.updateProjection();

        VRRenderPass renderPass = VRRenderState.getRenderPass();
        if(renderPass == VRRenderPass.EYE_LEFT){
            McRenderUtils.mulPose(posestack,
                    ClientContext.renderer.getEyeProjection(EyeType.LEFT)
            );
            return posestack.last().pose();
        }
        if (renderPass == VRRenderPass.EYE_RIGHT) {
            McRenderUtils.mulPose(posestack,
                    ClientContext.renderer.getEyeProjection(EyeType.RIGHT)
            );
            return posestack.last().pose();
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
            return posestack.last().pose();
        }

        McRenderUtils.mulPose(posestack,
                new Matrix4f()
                        .setPerspective(
                                fov * Mth.DEG_TO_RAD,
                                (float) this.minecraft.getWindow().getScreenWidth()
                                        / (float) this.minecraft.getWindow().getScreenHeight(),
                                this.visor$nearClipPlane,
                                this.visor$farClipPlane
                        )
        );

        return posestack.last().pose();
    }

    @Unique
    private float visor$cameraFov() {
        if (VRRenderState.getSceneType().isMainMenu() || !this.mainCamera.isInitialized()) {
            return this.minecraft.options.fov().get().floatValue();
        }
        return this.mainCamera.getFov();
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;isGameLoadFinished()Z", shift = Shift.AFTER), method = "render", require = 1)
    public void visor$matrix(CallbackInfo info) {
        if(VisorState.get().isNotActive()) return;
        McProjection.setPerspective(
                visor$passProjection(
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
        float renderDistance = (float) (this.minecraft.options.getEffectiveRenderDistance() * 16);
        this.visor$farClipPlane = Math.max(renderDistance + 1024.0F,
                Math.max(renderDistance * 4.0F, this.minecraft.options.cloudRange().get() * 16));
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
        McProjection.setPerspective(visor$passProjection(visor$cameraFov()));
    }

    @Override
    @Unique
    public Matrix4f visor$getThirdPersonProjection() {
        return visor$thirdPersonProjection;
    }
}
