package org.vmstudio.visor.mixin.client.renderer;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.vertex.PoseStack;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.extensions.client.render.GameRendererExtension;
import org.vmstudio.visor.extensions.client.render.LevelRendererExtension;
import org.vmstudio.visor.core.client.render.helpers.VREffectsHelper;
import org.vmstudio.visor.core.client.render.VRRenderState;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.Entity;
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

import org.vmstudio.visor.core.client.ClientContext;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;


@Mixin(value = LevelRenderer.class, priority = 999)
public abstract class LevelRendererMixin implements ResourceManagerReloadListener, AutoCloseable, LevelRendererExtension {

    @Final
    @Shadow
    private Minecraft minecraft;


    @Unique
    private Entity visor$renderedEntity;

    @Unique
    private RenderTarget visor$savedRenderTarget;

    /* ****************** *\
  //--------RENDERING--------\\
    \* ****************** */


    @Inject(at = @At("HEAD"), method = "renderEntity")
    public void visor$captureEntityRestore(CallbackInfo ci,
                                              @Local(argsOnly = true) Entity entity,
                                              @Share("capturedEntity") LocalRef<Entity> capturedEntity
    ) {
        if (VRRenderState.getPhase().isNotVanilla()
                && entity == minecraft.getCameraEntity()) {
            capturedEntity.set(entity);
            ((GameRendererExtension) minecraft.gameRenderer)
                    .visor$restoreCameraEntity(entity);
        }
        this.visor$renderedEntity = entity;
    }

    @Inject(at = @At("TAIL"), method = "renderEntity")
    public void visor$captureEntitySetup(CallbackInfo ci,
                                  @Local(argsOnly = true) Entity entity,
                                  @Share("capturedEntity") LocalRef<Entity> capturedEntity
    ) {
        if (capturedEntity.get() != null) {
            ((GameRendererExtension) minecraft.gameRenderer)
                    .visor$cacheCameraEntity(capturedEntity.get());
            ((GameRendererExtension) minecraft.gameRenderer)
                    .visor$setupCameraEntityAsVRCamera();
        }
        this.visor$renderedEntity = null;
    }



    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;getRenderDistance()F", shift = Shift.BEFORE),
            method = "renderLevel(Lcom/mojang/blaze3d/vertex/PoseStack;FJZLnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lorg/joml/Matrix4f;)V ")
    public void visor$stencil(PoseStack poseStack, float f, long l, boolean bl, Camera camera, GameRenderer gameRenderer,
                             LightTexture lightTexture, Matrix4f matrix4f, CallbackInfo info
    ) {
        if (VRRenderState.getPhase().isNotVanilla()) {

            VREffectsHelper.drawEyeStencil();
        }
    }


    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;floor(D)I", ordinal = 0), method = "renderSnowAndRain")
    public double visor$rainAndSnowX(double x) {
        if (VRRenderState.getRenderPass().isEye()) {
            return ClientContext.localPlayer.getPoseData(PlayerPoseType.RENDER)
                    .getHmd().getPosition().x();
        }
        return x;
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;floor(D)I", ordinal = 1), method = "renderSnowAndRain")
    public double visor$rainAndSnowY(double y) {
        if (VRRenderState.getRenderPass().isEye()) {
            return ClientContext.localPlayer.getPoseData(PlayerPoseType.RENDER)
                    .getHmd().getPosition().y();
        }
        return y;
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;floor(D)I", ordinal = 2), method = "renderSnowAndRain")
    public double visor$rainAndSnowZ(double z) {
        if (VRRenderState.getRenderPass().isEye()) {
            return ClientContext.localPlayer.getPoseData(PlayerPoseType.RENDER).getHmd().getPosition().z();
        }
        return z;
    }


    /**
     * That fixes issue with incorrect resolution
     * for post chain effects in some cases
     * (like for FIRST_PERSON, THIRD_PERSON VR cameras
     * that use different resolution from initial)
     */
    @Inject(method = {"initOutline", "initTransparency"}, at = @At("HEAD"))
    private void visor$ensureVanillaPhase(CallbackInfo ci) {
        if (VisorState.get().isActive() && VRRenderState.getPhase().isNotVanilla()) {
            this.visor$savedRenderTarget = MC.mainRenderTarget;
            MC.mainRenderTarget = VRRenderState.getVanillaTarget();
        }
    }
    @Inject(method = {"initOutline", "initTransparency"}, at = @At("TAIL"))
    private void visor$restoreAfterInit(CallbackInfo ci) {
        if (this.visor$savedRenderTarget != null) {
            MC.mainRenderTarget = this.visor$savedRenderTarget;
            this.visor$savedRenderTarget = null;
        }
    }

    /* **************** *\
  //--------EVENTS--------\\
    \* **************** */
    @Inject(at = @At("TAIL"), method = "onResourceManagerReload")
    public void visor$onResourceManagerReload(ResourceManager resourceManager, CallbackInfo ci) {
        if (VisorState.get().isInitialized()) {
            ClientContext.renderer.prepareReinit(
                    "Resources Reload"
            );
        }
    }


    /* ************** *\
  //--------MISC--------\\
    \* ************** */

    @Inject(at = @At("HEAD"), method = "levelEvent")
    public void visor$hapticOnSound(int i, BlockPos blockPos, int j, CallbackInfo ci) {
        if(!VisorState.get().isNotActive()) return;

        if (this.minecraft.player != null
                && this.minecraft.player.isAlive()
                && this.minecraft.player.blockPosition().distSqr(blockPos) < 25.0D) {
            switch (i) {
                case 1019,      // ZOMBIE_ATTACK_WOODEN_DOOR
                     1020,   // ZOMBIE_ATTACK_IRON_DOOR
                     1021    // ZOMBIE_BREAK_WOODEN_DOOR
                        -> {
                    ClientContext.inputManager
                            .triggerHapticPulse(HandType.MAIN, 0.0075f);
                    ClientContext.inputManager
                            .triggerHapticPulse(HandType.OFFHAND, 0.0075f);
                }
                case 1030 ->    // ANVIL_USE
                        ClientContext.inputManager
                                .triggerHapticPulse(HandType.MAIN, 0.005f);
                case 1031 -> {  // ANVIL_LAND
                    ClientContext.inputManager
                            .triggerHapticPulse(HandType.MAIN, 0.0125f);
                    ClientContext.inputManager
                            .triggerHapticPulse(HandType.OFFHAND, 0.0125f);
                }
            }
        }
    }

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
