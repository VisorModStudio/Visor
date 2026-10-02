// #!MC-VERSION:: 26.2+
package org.vmstudio.visor.mixin.client.renderer;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.framegraph.FramePass;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import java.util.EnumMap;
import org.vmstudio.visor.api.client.render.VRRenderPass;
import org.vmstudio.visor.api.compatibility.mcversion.render.McFog;
import org.vmstudio.visor.api.compatibility.mcversion.render.McRenderTarget;
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.core.client.render.VRRenderState;
import org.vmstudio.visor.core.client.render.helpers.RenderEffectsHelper;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// common mixin
@Mixin(value = LevelRenderer.class, priority = 999)
public abstract class LevelRendererMixin {

    // ---- Shadow fields ----
    @Shadow @Final @Mutable
    private RenderTarget entityOutlineTarget;

    // ---- Unique fields ----
    @Unique
    private EnumMap<VRRenderPass, RenderTarget> visor$passOutlineTargets;
    @Unique
    private RenderTarget visor$vanillaOutlineTarget;


    /* ***************** *\
  //--------MIXINS--------\\
    \* ***************** */

    @WrapOperation(method = "render", require = 1,
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

    @WrapOperation(method = "addSkyPass*", require = 1,
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/framegraph/FramePass;executes(Ljava/lang/Runnable;)V"))
    private void visor$maskHiddenAreaAfterSky(FramePass skyPass, Runnable sky, Operation<Void> original) {
        if (VRRenderState.getPhase().isVanilla()) {
            original.call(skyPass, sky);
            return;
        }
        original.call(skyPass, (Runnable) () -> {
            sky.run();
            McFog.State skyFog = McFog.save();
            McFog.disable();
            RenderEffectsHelper.maskHiddenArea();
            McFog.restore(skyFog);
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
    @Inject(method = "render", at = @At("HEAD"))
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
            outline = new TextureTarget("visor_vr_outline", width, height, true, GpuFormat.RGBA8_UNORM);
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
}
