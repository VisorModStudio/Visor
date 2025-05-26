package me.phoenixra.visor.core.mixin.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import me.phoenixra.visor.api.client.data.PoseType;
import me.phoenixra.visor.core.client.VisorState;
import me.phoenixra.visor.core.client.mcmodified.render.GameRendererModified;
import me.phoenixra.visor.core.client.mcmodified.render.LevelRendererModified;
import me.phoenixra.visor.core.client.render.helpers.VREffectsHelper;
import me.phoenixra.visor.core.client.render.VRRenderState;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.phoenixra.visor.core.client.ClientContext;

import java.util.concurrent.atomic.AtomicBoolean;


@Mixin(value = LevelRenderer.class, priority = 999)
public abstract class LevelRendererMixin implements ResourceManagerReloadListener, AutoCloseable, LevelRendererModified {

    @Final
    @Shadow
    private Minecraft minecraft;

    @Shadow
    private boolean needsFullRenderChunkUpdate;

    @Shadow
    @Final
    private AtomicBoolean needsFrustumUpdate;



    @Unique
    private Entity visor$capturedEntity;
    @Unique
    private Entity visor$renderedEntity;

    //---


    @Inject(method = "setupRender", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/LevelRenderer;needsFullRenderChunkUpdate:Z", ordinal = 1, shift = At.Shift.AFTER))
    private void visor$alwaysUpdateCull(CallbackInfo ci) {
        //@TODO Disable for sodium
        if (VisorState.getStateMode().isActive()) {
            // fixes chunks cull frustum between displays
            this.needsFullRenderChunkUpdate = true;
            this.needsFrustumUpdate.set(true);
        }
    }


    /* ****************** *\
  //--------RENDERING--------\\
    \* ****************** */
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch()V", ordinal = 0, shift = Shift.AFTER), method = "renderLevel")
    public void visor$renderVR(PoseStack poseStack, float partialTicks, long l, boolean bl, Camera camera,
                               GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f,
                               CallbackInfo ci
    ) {
        if (VRRenderState.getCurrentPhase().isVanilla()) {
            return;
        }

        ClientContext.gameViewHandler.renderView(
                poseStack, partialTicks
        );

    }


    @Inject(at = @At("HEAD"), method = "renderEntity")
    public void visor$captureEntityRestoreLoc(Entity entity, double d, double e, double f, float g, PoseStack poseStack,
                                              MultiBufferSource multiBufferSource, CallbackInfo ci
    ) {
        this.visor$capturedEntity = entity;
        if (VRRenderState.getCurrentPhase().isNotVanilla()
                && visor$capturedEntity == minecraft.getCameraEntity()) {
            ((GameRendererModified) minecraft.gameRenderer).visor$restoreCameraEntity((LivingEntity) visor$capturedEntity);
        }
        this.visor$renderedEntity = visor$capturedEntity;
    }

    @Inject(at = @At("TAIL"), method = "renderEntity")
    public void visor$restoreLoc2(Entity entity, double d, double e, double f, float g, PoseStack poseStack,
                                  MultiBufferSource multiBufferSource, CallbackInfo ci
    ) {
        if (VRRenderState.getCurrentPhase().isNotVanilla()
                && visor$capturedEntity == minecraft.getCameraEntity()) {
            ((GameRendererModified) minecraft.gameRenderer)
                    .visor$cacheCameraEntity((LivingEntity) visor$capturedEntity);
            ((GameRendererModified) minecraft.gameRenderer)
                    .visor$setupVRCameraEntity();
        }
        this.visor$renderedEntity = null;
    }



    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;getRenderDistance()F", shift = Shift.BEFORE),
            method = "renderLevel(Lcom/mojang/blaze3d/vertex/PoseStack;FJZLnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lorg/joml/Matrix4f;)V ")
    public void visor$stencil(PoseStack poseStack, float f, long l, boolean bl, Camera camera, GameRenderer gameRenderer,
                             LightTexture lightTexture, Matrix4f matrix4f, CallbackInfo info
    ) {
        if (VRRenderState.getCurrentPhase().isNotVanilla()) {

            VREffectsHelper.drawEyeStencil();
        }
    }


    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;floor(D)I", ordinal = 0), method = "renderSnowAndRain")
    public double visor$rainX(double x) {
        if (VRRenderState.getCurrentPhase().isNotVanilla()
                && VRRenderState.getCurrentVRDisplay().isEye()) {
            return ClientContext.player.getPose(PoseType.RENDER).getHmd().getPosition().x;
        }
        return x;
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;floor(D)I", ordinal = 1), method = "renderSnowAndRain")
    public double visor$rainY(double y) {
        if (VRRenderState.getCurrentPhase().isNotVanilla()
                && VRRenderState.getCurrentVRDisplay().isEye()) {
            return ClientContext.player.getPose(PoseType.RENDER).getHmd().getPosition().y;
        }
        return y;
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;floor(D)I", ordinal = 2), method = "renderSnowAndRain")
    public double visor$rainZ(double z) {
        if (VRRenderState.getCurrentPhase().isNotVanilla()
                && VRRenderState.getCurrentVRDisplay().isEye()) {
            return ClientContext.player.getPose(PoseType.RENDER).getHmd().getPosition().z;
        }
        return z;
    }


    /* **************** *\
  //--------EVENTS--------\\
    \* **************** */
    @Inject(at = @At("TAIL"), method = "onResourceManagerReload")
    public void visor$onResourceManagerReload(ResourceManager resourceManager, CallbackInfo ci) {
        if (VisorState.getStateMode().isInitialized()) {
            ClientContext.renderer.prepareReinit(
                    "Resource Reload"
            );
        }
    }


    /* ************** *\
  //--------MISC--------\\
    \* ************** */



    /* ************************ *\
  //--------PUBLIC METHODS--------\\
    \* ************************ */


    @Override
    @Unique
    public Entity visor$getRenderedEntity() {
        return this.visor$renderedEntity;
    }


    /* ************************* *\
  //--------UTILITY METHODS--------\\
    \* ************************* */
}
