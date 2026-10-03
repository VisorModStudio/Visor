// #!MC-VERSION:: 1.20.1-1.21.5
package org.vmstudio.visor.mixin.client.renderer;

import org.vmstudio.visor.api.compatibility.mcversion.render.McRenderUtils;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
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
    //? if >=1.21.2 {
    /*@Shadow
    private float fovModifier;
    @Shadow
    private float oldFovModifier;
    *///?} else {
    @Shadow
    private float fov;
    @Shadow
    private float oldFov;
    //?}
    @Shadow
    private long lastActiveTime;
    @Shadow
    private int itemActivationTicks;


    /* ****************** *\
  //--------AIM PICK--------\\
    \* ****************** */

    @WrapMethod(method = "pick(F)V")
    private void visor$pickWithVRHands(float partialTick, Operation<Void> original) {
        VRAimPicker.pickWithVRHands(() -> original.call(partialTick));
    }

    // 1.20.5 moved the ray trace into pick(Entity,DDF), pick(F)V has no Vec3 locals left;
    //? if >=1.20.5 {
    /*@ModifyVariable(at = @At("STORE"), method = "pick(Lnet/minecraft/world/entity/Entity;DDF)Lnet/minecraft/world/phys/HitResult;", ordinal = 0)
    public Vec3 visor$pickPos(Vec3 original) {
        return VRAimPicker.pickPos(original);
    }

    @ModifyVariable(at = @At("STORE"), method = "pick(Lnet/minecraft/world/entity/Entity;DDF)Lnet/minecraft/world/phys/HitResult;", ordinal = 1)
    public Vec3 visor$pickDirection(Vec3 original) {
        return VRAimPicker.pickDirection(original);
    }

    @WrapOperation(method = "pick(Lnet/minecraft/world/entity/Entity;DDF)Lnet/minecraft/world/phys/HitResult;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;pick(DFZ)Lnet/minecraft/world/phys/HitResult;"))
    private HitResult visor$vrBlockPick(Entity entity, double range, float partialTick, boolean fluid, Operation<HitResult> original) {
        HitResult vrHit = VRAimPicker.vrBlockPick();
        return vrHit != null ? vrHit : original.call(entity, range, partialTick, fluid);
    }
    *///?} else {
    @ModifyVariable(at = @At("STORE"), method = "pick(F)V", ordinal = 0)
    public Vec3 visor$pickPos(Vec3 original) {
        return VRAimPicker.pickPos(original);
    }

    @ModifyVariable(at = @At("STORE"), method = "pick(F)V", ordinal = 1)
    public Vec3 visor$pickDirection(Vec3 original) {
        return VRAimPicker.pickDirection(original);
    }
    //?}


    /* ************************* *\
  //--------ITEM ACTIVATION--------\\
    \* ************************* */
    // item activation animation: skipped in the GUI pass, GameEffectVanilla draws it

    //? if >=1.21 {
    /*@WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;renderItemActivationAnimation(Lnet/minecraft/client/gui/GuiGraphics;F)V"), method = "render(Lnet/minecraft/client/DeltaTracker;Z)V")
    private void visor$noItemActivationAnimInGUI(GameRenderer instance, GuiGraphics guiGraphics, float f, Operation<Void> original) {
        if(VRRenderState.getPhase().isVanilla()) {
            original.call(instance, guiGraphics, f);
        }
    }
    *///?} else {
    @WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;renderItemActivationAnimation(IIF)V"), method = "render(FJZ)V")
    private void visor$noItemActivationAnimInGUI(GameRenderer instance, int i, int j, float f, Operation<Void> original) {
        if(VRRenderState.getPhase().isVanilla()) {
            original.call(instance, i, j, f);
        }
    }
    //?}

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
        McRenderUtils.rotate(poseStack, Axis.YP.rotation(-cameraPose.getYaw()));
        McRenderUtils.rotate(poseStack, Axis.XP.rotation(-cameraPose.getPitch()));
    }

    @WrapOperation(method = "renderItemActivationAnimation", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V"))
    private void visor$noItemTranslate(PoseStack poseStack, float x, float y, float z, Operation<Void> original) {
        if(VRRenderState.getPhase().isVanilla()) {
            original.call(poseStack, x, y, z);
        }
    }


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

    @WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;isWindowActive()Z"), method = "render")
    public boolean visor$noPauseGameIfWindowNotFocused(Minecraft instance, Operation<Boolean> original) {
        return VisorState.get().isActive() || original.call(instance);
    }

    //? if >=1.21.2 {
    /*@Inject(at = @At("HEAD"), method = "tickFov", cancellable = true)
    public void visor$freezeFovInVR(CallbackInfo ci) {
        if(VRRenderState.getPhase().isNotVanilla()) {
            // vanilla tickFov starts from when the view is not modified
            final float neutralFovModifier = 1.0F;
            this.fovModifier = neutralFovModifier;
            this.oldFovModifier = neutralFovModifier;
            ci.cancel();
        }
    }
    *///?} else {
    @Inject(at = @At("HEAD"), method = "tickFov", cancellable = true)
    public void visor$freezeFovInVR(CallbackInfo ci) {
        if(VRRenderState.getPhase().isNotVanilla()) {
            // vanilla tickFov starts from when the view is not modified
            final float neutralFovModifier = 1.0F;
            this.fov = neutralFovModifier;
            this.oldFov = neutralFovModifier;
            ci.cancel();
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
    public void visor$noBobHurt(PoseStack poseStack,
                                float f,
                                CallbackInfo ci) {
        if(VRRenderState.getPhase().isNotVanilla()) {
            ci.cancel();
        }
    }

    @Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
    public void visor$noBobView(PoseStack matrixStack,
                                float f,
                                CallbackInfo ci) {
        if(VRRenderState.getPhase().isNotVanilla()) {
            ci.cancel();
        }
    }

    @WrapOperation(at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/GameRenderer;renderHand:Z"), method = "renderLevel")
    public boolean visor$noVanillaHands(GameRenderer instance, Operation<Boolean> original) {
        if (VRRenderState.isSpectatedVRView(minecraft.getCameraEntity())) {
            return false;
        }
        return VRRenderState.getPhase().isVanilla() && original.call(instance);
    }

    /**
     * Only process this when rendering vanilla
     * or VR camera that is a worldUpdater
     */
    @WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;pauseGame(Z)V"), method = "render")
    public void visor$pauseOncePerFrame(Minecraft instance, boolean bl, Operation<Void> original) {
        if (VisorState.get().isNotActive() || VRRenderState.getRenderPass() == VRRenderPass.worldUpdater()) {
            original.call(instance, bl);
        }
    }

    /**
     * Only process this when rendering vanilla
     * or VR camera that is a worldUpdater
     */
    @WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/Util;getMillis()J"), method = "render")
    public long visor$useActiveTimeOncePerFrame(Operation<Long> original) {
        if (VisorState.get().isNotActive() || VRRenderState.getRenderPass() == VRRenderPass.worldUpdater()) {
            return original.call();
        } else {
            return this.lastActiveTime;
        }
    }
}
