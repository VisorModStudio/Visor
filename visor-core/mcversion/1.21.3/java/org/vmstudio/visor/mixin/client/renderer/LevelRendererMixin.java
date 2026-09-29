// #!MC-VERSION:: 1.21.3-1.21.4
package org.vmstudio.visor.mixin.client.renderer;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.framegraph.FramePass;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import org.jetbrains.annotations.Nullable;
import java.util.EnumMap;
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
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// common mixin
@Mixin(value = LevelRenderer.class, priority = 999)
public abstract class LevelRendererMixin implements LevelRendererExtension {

    // ---- Shadow fields ----
    @Final @Shadow
    private Minecraft minecraft;
    @Shadow @Nullable
    private RenderTarget entityOutlineTarget;

    // ---- Unique fields ----
    @Unique
    private Entity visor$currentRenderEntity;
    @Unique
    private EnumMap<VRRenderPass, RenderTarget> visor$passOutlineTargets;
    @Unique
    private RenderTarget visor$vanillaOutlineTarget;


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
            method = "collectVisibleEntities",
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



    @WrapOperation(method = "renderLevel", require = 1,
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/framegraph/FramePass;executes(Ljava/lang/Runnable;)V"))
    private void visor$maskHiddenArea(FramePass clearPass, Runnable clear, Operation<Void> original) {
        if (VRRenderState.getPhase().isVanilla()) {
            original.call(clearPass, clear);
            return;
        }
        original.call(clearPass, (Runnable) () -> {
            clear.run();
            RenderEffectsHelper.maskHiddenArea();
        });
    }


    /**
     * That fixes issue with incorrect resolution
     * for post chain effects in some cases
     * (like for FIRST_PERSON, THIRD_PERSON VR cameras
     * that use different resolution from initial)
     */
    // 1.21.2 imports one window-sized outline target into every pass, and its clear() leaves the viewport at that
    // size for entities, particles, clouds and weather drawn after it, so each VR pass gets one sized like its target
    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void visor$usePassOutlineTarget(CallbackInfo ci) {
        if (VisorState.get().isNotActive() || VRRenderState.getPhase().isVanilla()) {
            visor$restoreVanillaOutlineTarget();
            return;
        }
        RenderTarget passTarget = McRenderTarget.mainTarget();
        if (passTarget == null || this.entityOutlineTarget == null) {
            return;
        }
        if (this.visor$vanillaOutlineTarget == null) {
            this.visor$vanillaOutlineTarget = this.entityOutlineTarget;
        }
        if (this.visor$passOutlineTargets == null) {
            this.visor$passOutlineTargets = new EnumMap<>(VRRenderPass.class);
        }
        int width = McRenderTarget.viewWidth(passTarget);
        int height = McRenderTarget.viewHeight(passTarget);
        VRRenderPass renderPass = VRRenderState.getRenderPass();
        RenderTarget outline = this.visor$passOutlineTargets.get(renderPass);
        if (outline == null) {
            outline = new TextureTarget(width, height, true);
            McRenderTarget.setClearColor(outline, 0.0F, 0.0F, 0.0F, 0.0F);
            this.visor$passOutlineTargets.put(renderPass, outline);
        } else if (McRenderTarget.viewWidth(outline) != width || McRenderTarget.viewHeight(outline) != height) {
            McRenderTarget.resize(outline, width, height);
        }
        this.entityOutlineTarget = outline;
    }

    @Inject(method = "resize", at = @At("HEAD"))
    private void visor$resizeVanillaOutlineTarget(CallbackInfo ci) {
        visor$restoreVanillaOutlineTarget();
    }

    @Inject(method = "initOutline", at = @At("HEAD"))
    private void visor$releaseOutlineTargetsOnInit(CallbackInfo ci) {
        visor$releasePassOutlineTargets();
    }

    // AutoCloseable.close has no SRG name
    @Inject(method = "close", at = @At("HEAD"), remap = false)
    private void visor$releaseOutlineTargetsOnClose(CallbackInfo ci) {
        visor$releasePassOutlineTargets();
    }

    @Unique
    private void visor$releasePassOutlineTargets() {
        visor$restoreVanillaOutlineTarget();
        if (this.visor$passOutlineTargets != null) {
            this.visor$passOutlineTargets.values().forEach(RenderTarget::destroyBuffers);
            this.visor$passOutlineTargets.clear();
        }
    }

    @Unique
    private void visor$restoreVanillaOutlineTarget() {
        if (this.visor$vanillaOutlineTarget != null) {
            this.entityOutlineTarget = this.visor$vanillaOutlineTarget;
            this.visor$vanillaOutlineTarget = null;
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
