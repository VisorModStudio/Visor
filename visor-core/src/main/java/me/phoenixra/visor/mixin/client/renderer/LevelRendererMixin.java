package me.phoenixra.visor.mixin.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import me.phoenixra.visor.api.client.data.PoseDataType;
import me.phoenixra.visor.api.common.ControllerHand;
import me.phoenixra.visor.core.client.VisorState;
import me.phoenixra.visor.modified.client.render.GameRendererModified;
import me.phoenixra.visor.modified.client.render.LevelRendererModified;
import me.phoenixra.visor.core.client.render.helpers.VREffectsHelper;
import me.phoenixra.visor.core.client.render.VRRenderState;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.core.BlockPos;
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


    @Unique
    private Entity visor$capturedEntity;
    @Unique
    private Entity visor$renderedEntity;


    /* ****************** *\
  //--------RENDERING--------\\
    \* ****************** */
    @Inject(at = @At("RETURN"), method = "renderLevel")
    public void visor$renderVRDecorations(PoseStack poseStack, float partialTicks, long l, boolean bl, Camera camera,
                                          GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f,
                                          CallbackInfo ci
    ) {
        if (VRRenderState.getCurrentPhase().isVanilla()) {
            return;
        }

        ClientContext.decorationRenderer.render(
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
                    .visor$setupCameraEntity();
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
            return ClientContext.player.getPoseData(PoseDataType.RENDER).getHmd().getPosition().x();
        }
        return x;
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;floor(D)I", ordinal = 1), method = "renderSnowAndRain")
    public double visor$rainY(double y) {
        if (VRRenderState.getCurrentPhase().isNotVanilla()
                && VRRenderState.getCurrentVRDisplay().isEye()) {
            return ClientContext.player.getPoseData(PoseDataType.RENDER).getHmd().getPosition().y();
        }
        return y;
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;floor(D)I", ordinal = 2), method = "renderSnowAndRain")
    public double visor$rainZ(double z) {
        if (VRRenderState.getCurrentPhase().isNotVanilla()
                && VRRenderState.getCurrentVRDisplay().isEye()) {
            return ClientContext.player.getPoseData(PoseDataType.RENDER).getHmd().getPosition().z();
        }
        return z;
    }


    /**
     * That fixes issue with incorrect resolution
     * for post chain effects in some cases
     * (like for FIRST_PERSON, THIRD_PERSON displays
     * that use different resolution from initial)
     */
    @Inject(method = {"initOutline", "initTransparency"}, at = @At("HEAD"))
    private void visor$ensureVanillaPhase(CallbackInfo ci) {
        if (VisorState.getState().isActive()) {
            VRRenderState.startVanillaPhase();
        }
    }

    /* **************** *\
  //--------EVENTS--------\\
    \* **************** */
    @Inject(at = @At("TAIL"), method = "onResourceManagerReload")
    public void visor$onResourceManagerReload(ResourceManager resourceManager, CallbackInfo ci) {
        if (VisorState.getState().isInitialized()) {
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
        if(!VisorState.getState().isNotActive()) return;

        if (this.minecraft.player != null
                && this.minecraft.player.isAlive()
                && this.minecraft.player.blockPosition().distSqr(blockPos) < 25.0D) {
            switch (i) {
                case 1019,      // ZOMBIE_ATTACK_WOODEN_DOOR
                     1020,   // ZOMBIE_ATTACK_IRON_DOOR
                     1021    // ZOMBIE_BREAK_WOODEN_DOOR
                        -> {
                    ClientContext.inputManager
                            .triggerHapticPulse(ControllerHand.MAIN, 0.0075f);
                    ClientContext.inputManager
                            .triggerHapticPulse(ControllerHand.OFFHAND, 0.0075f);
                }
                case 1030 ->    // ANVIL_USE
                        ClientContext.inputManager
                                .triggerHapticPulse(ControllerHand.MAIN, 0.005f);
                case 1031 -> {  // ANVIL_LAND
                    ClientContext.inputManager
                            .triggerHapticPulse(ControllerHand.MAIN, 0.0125f);
                    ClientContext.inputManager
                            .triggerHapticPulse(ControllerHand.OFFHAND, 0.0125f);
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
