// #!MC-VERSION:: 1.20.1-1.21.1
package org.vmstudio.visor.mixin.client.renderer;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.pipeline.RenderTarget;
import org.vmstudio.visor.api.client.render.VRRenderPass;
import org.vmstudio.visor.api.compatibility.mcversion.render.McRenderTarget;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.core.client.render.camera.VRCameraEntitySwap;
import org.vmstudio.visor.core.client.render.VRRenderState;
import org.vmstudio.visor.core.client.render.helpers.CullFrustumHelper;
import org.vmstudio.visor.core.client.render.helpers.RenderEffectsHelper;
import org.vmstudio.visor.extensions.client.render.LevelRendererExtension;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// common mixin
@Mixin(value = LevelRenderer.class, priority = 999)
public abstract class LevelRendererMixin implements LevelRendererExtension {

    // ---- Shadow fields ----
    @Final @Shadow
    private Minecraft minecraft;

    // ---- Unique fields ----
    @Unique
    private Entity visor$currentRenderEntity;
    @Unique
    private RenderTarget visor$savedRenderTarget;


    /* ***************** *\
  //--------MIXINS--------\\
    \* ***************** */

    @ModifyVariable(method = "prepareCullFrustum", at = @At("HEAD"), index = 3, argsOnly = true)
    private Matrix4f visor$widenCullFrustum(Matrix4f projection) {
        return CullFrustumHelper.widenCullProjection(projection);
    }

    @WrapOperation(method = "renderLevel", require = 1,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/lighting/LevelLightEngine;runLightUpdates()I"))
    private int visor$lightUpdatesOncePerFrame(LevelLightEngine engine, Operation<Integer> original) {
        if (VisorState.get().isNotActive() || VRRenderState.getPhase().isNotVRWorld()
                || VRRenderState.getRenderPass() == VRRenderPass.worldUpdater()) {
            return original.call(engine);
        }
        return 0;
    }

    // 1.21.2 moved the detached-camera check into collectVisibleEntities
    @WrapOperation(
            method = "renderLevel",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;isDetached()Z")
    )
    private boolean visor$renderSpectatedVRSelfView(Camera camera, Operation<Boolean> original) {
        if (VRRenderState.isSpectatedVRView(camera.getEntity())) {
            return true;
        }
        return original.call(camera);
    }

    @Inject(at = @At("HEAD"), method = "renderEntity")
    public void visor$captureEntityRestore(CallbackInfo ci,
                                           @Local(argsOnly = true) Entity entity,
                                           @Share("vrCameraEntity") LocalRef<Entity> vrCameraEntity
    ) {
        if (VRRenderState.getPhase().isNotVanilla()
                && entity == minecraft.getCameraEntity()) {
            vrCameraEntity.set(entity);
            VRCameraEntitySwap.applyCachedCameraEntityPosition(entity);
        }
        this.visor$currentRenderEntity = entity;
    }

    @Inject(at = @At("TAIL"), method = "renderEntity")
    public void visor$captureEntitySetup(CallbackInfo ci,
                                         @Local(argsOnly = true) Entity entity,
                                         @Share("vrCameraEntity") LocalRef<Entity> vrCameraEntity
    ) {
        if (vrCameraEntity.get() != null) {
            VRCameraEntitySwap.setupCameraEntityAsVRCamera();
        }
        this.visor$currentRenderEntity = null;
    }



    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;getRenderDistance()F", shift = Shift.BEFORE),
            method = "renderLevel")
    public void visor$maskHiddenArea(CallbackInfo info) {
        if (VRRenderState.getPhase().isNotVanilla()) {
            RenderEffectsHelper.maskHiddenArea();
        }
    }


    /**
     * That fixes issue with incorrect resolution
     * for post chain effects in some cases
     * (like for FIRST_PERSON, THIRD_PERSON VR cameras
     * that use different resolution from initial)
     */
    // 1.21.2 imports one window-sized outline target into every pass, and its clear() leaves the viewport at that
    // size for entities, particles, clouds and weather drawn after it, so each VR pass gets one sized like its target
    @Inject(method = {"initOutline", "initTransparency"}, at = @At("HEAD"))
    private void visor$ensureVanillaPhase(CallbackInfo ci) {
        if (VisorState.get().isActive() && VRRenderState.getPhase().isNotVanilla()) {
            this.visor$savedRenderTarget = McRenderTarget.mainTarget();
            McRenderTarget.setMainTarget(VRRenderState.getVanillaTarget());
        }
    }
    @Inject(method = {"initOutline", "initTransparency"}, at = @At("TAIL"))
    private void visor$restoreAfterInit(CallbackInfo ci) {
        if (this.visor$savedRenderTarget != null) {
            McRenderTarget.setMainTarget(this.visor$savedRenderTarget);
            this.visor$savedRenderTarget = null;
        }
    }

    @Inject(at = @At("TAIL"), method = "onResourceManagerReload")
    public void visor$onResourceManagerReload(ResourceManager resourceManager, CallbackInfo ci) {
        if (VisorState.get().isInitialized()) {
            ClientContext.renderer.prepareReinit(
                    "resource manager reloaded"
            );
        }
    }


    /* ************************ *\
  //--------PUBLIC METHODS--------\\
    \* ************************ */

    @Override
    @Unique
    public Entity visor$getCurrentRenderEntity() {
        return this.visor$currentRenderEntity;
    }
}
