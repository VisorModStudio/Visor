// #!MC-VERSION:: 26.1.2-26.2
package org.vmstudio.visor.mixin.client.renderer;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.textures.GpuTexture;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.Matrix4fc;
//? if >=26.2 {
/*import org.joml.Vector4fc;
*///?}
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.vmstudio.visor.api.client.ClientFeature;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.client.render.VRRenderPass;
import org.vmstudio.visor.api.client.settings.VRClientSettings;
import org.vmstudio.visor.api.client.settings.enums.MirrorMode;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.core.client.player.VRAimPicker;
import org.vmstudio.visor.core.client.render.VRRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vmstudio.visor.core.client.render.helpers.RenderPoseHelper;

import java.nio.file.Path;

// common mixin
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    // ---- Shadow fields ----
    @Shadow @Final
    Minecraft minecraft;


    /* ****************** *\
  //--------AIM PICK--------\\
    \* ****************** */

    // 1.20.5 moved the ray trace into pick(Entity,DDF), pick(F)V has no Vec3 locals left;
    // 1.21.11 moved it on into the static LocalPlayer.pick(Entity,DDF), see LocalPlayerMixin


    /* ************************* *\
  //--------ITEM ACTIVATION--------\\
    \* ************************* */
    // item activation animation: skipped in the GUI pass, GameEffectVanilla draws it
    // 1.21.6 moved it onto ScreenEffectRenderer, see ScreenEffectRendererMixin

    /* ********************* *\
  //--------VANILLA OFF--------\\
    \* ********************* */
    // vanilla GameRenderer behaviour turned off or gated in VR passes

    /**
     * If no crosshair rendered,
     * don't render block outline as well
     * @param cir
     */
    @Inject(at = @At("HEAD"), method = "shouldRenderBlockOutline", cancellable = true)
    public void visor$shouldDrawBlockOutline(CallbackInfoReturnable<Boolean> cir) {
        if (VRRenderState.getPhase().isVRWorld()) {
            cir.setReturnValue(
                    ClientContext.visor.isFeatureEnabled(ClientFeature.AIM_EFFECTS)
            );
        }
    }

    @WrapOperation(at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/GameRenderer;effectActive:Z"), method = "render")
    public boolean visor$noPostEffectOnThirdPerson(GameRenderer instance, Operation<Boolean> original) {
        return original.call(instance) && VRRenderState.getRenderPass() != VRRenderPass.THIRD_PERSON;
    }

    //? if >=26.2 {
    /*@WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;resize(II)V"), method = "render", require = 1)
    private void visor$noVanillaResizeInVR(GameRenderer instance, int width, int height, Operation<Void> original) {
        if (VisorState.get().isNotActive()) {
            original.call(instance, width, height);
        }
    }

    @WrapOperation(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/CommandEncoder;clearColorAndDepthTextures(Lcom/mojang/blaze3d/textures/GpuTexture;Lorg/joml/Vector4fc;Lcom/mojang/blaze3d/textures/GpuTexture;D)V", remap = false), method = "render", require = 1)
    private void visor$noFrameClearInWorldPass(CommandEncoder encoder, GpuTexture color, Vector4fc clearColor,
                                               GpuTexture depth, double clearDepth, Operation<Void> original) {
        if (VRRenderState.getPhase().isNotVRWorld()) {
            original.call(encoder, color, clearColor, depth, clearDepth);
        }
    }
    *///?} else {
    @Inject(at = @At("TAIL"), method = "extractWindow")
    private void visor$noVanillaResizeInVR(CallbackInfo ci) {
        if (VisorState.get().isActive()) {
            ((GameRenderer) (Object) this).getGameRenderState().windowRenderState.isResized = false;
        }
    }

    @WrapOperation(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/CommandEncoder;clearColorAndDepthTextures(Lcom/mojang/blaze3d/textures/GpuTexture;ILcom/mojang/blaze3d/textures/GpuTexture;D)V", remap = false), method = "render", require = 1)
    private void visor$noFrameClearInWorldPass(CommandEncoder encoder, GpuTexture color, int clearColor,
                                               GpuTexture depth, double clearDepth, Operation<Void> original) {
        if (VRRenderState.getPhase().isNotVRWorld()) {
            original.call(encoder, color, clearColor, depth, clearDepth);
        }
    }
    //?}

    @Inject(at = @At("HEAD"), method = "takeAutoScreenshot", cancellable = true)
    public void visor$skipAutoScreenshotInMenu(Path path, CallbackInfo ci) {
        if (VisorState.get().isActive() && VRRenderState.getSceneType().isMainMenu()) {
            ci.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "bobHurt", cancellable = true)
    public void visor$noBobHurt(CameraRenderState cameraState,
                                PoseStack poseStack,
                                CallbackInfo ci) {
        if(VRRenderState.getPhase().isNotVanilla()) {
            ci.cancel();
        }
    }

    @Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
    public void visor$noBobView(CameraRenderState cameraState,
                                PoseStack matrixStack,
                                CallbackInfo ci) {
        if(VRRenderState.getPhase().isNotVanilla()) {
            ci.cancel();
        }
    }

    @WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;renderItemInHand(Lnet/minecraft/client/renderer/state/level/CameraRenderState;FLorg/joml/Matrix4fc;)V"), method = "renderLevel")
    public void visor$noVanillaHands(GameRenderer instance, CameraRenderState cameraState, float partialTick, Matrix4fc modelView,
                                     Operation<Void> original) {
        if (VRRenderState.isSpectatedVRView(minecraft.getCameraEntity())) {
            return;
        }
        if (VRRenderState.getPhase().isVanilla()) {
            original.call(instance, cameraState, partialTick, modelView);
        }
    }

    /**
     * Only process this when rendering vanilla
     * or VR camera that is a worldUpdater
     */

    /**
     * Only process this when rendering vanilla
     * or VR camera that is a worldUpdater
     */
}
