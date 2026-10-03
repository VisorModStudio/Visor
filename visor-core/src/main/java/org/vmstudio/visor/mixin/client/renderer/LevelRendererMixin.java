// #!MC-VERSION:: 26.3+
package org.vmstudio.visor.mixin.client.renderer;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.renderpearl.api.GpuFormat;
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
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.commands.RenderPass;
import java.util.Optional;
import java.util.OptionalDouble;
import net.minecraft.client.renderer.LevelTargetBundle;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.vmstudio.visor.api.ModLoader;
import org.vmstudio.visor.api.client.render.RenderPipelineStage;

// common mixin
@Mixin(value = LevelRenderer.class, priority = 999)
public abstract class LevelRendererMixin {

    // ---- Shadow fields ----
    @Shadow @Final @Mutable
    private RenderTarget entityOutlineTarget;
    @Shadow @Final
    private LevelTargetBundle targets;

    // ---- Unique fields ----
    @Unique
    private EnumMap<VRRenderPass, RenderTarget> visor$passOutlineTargets;
    @Unique
    private RenderTarget visor$vanillaOutlineTarget;
    @Unique
    private @Nullable RenderPass visor$stagePass;
    @Unique
    private @Nullable RenderPass visor$closedLevelPass;


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
            outline = new TextureTarget("visor_vr_outline", width, height, GpuFormat.RGBA8_UNORM, null);
            McRenderTarget.setClearColor(outline, 0.0F, 0.0F, 0.0F, 0.0F);
            this.visor$passOutlineTargets.put(renderPass, outline);
        } else if (McRenderTarget.viewWidth(outline) != width || McRenderTarget.viewHeight(outline) != height) {
            McRenderTarget.resize(outline, width, height);
        }
        this.entityOutlineTarget = outline;
    }

    @ModifyVariable(method = "executeSolid", argsOnly = true, require = 1,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/ChunkSectionsToRender;renderGroup(Lnet/minecraft/client/renderer/chunk/ChunkSectionLayerGroup;Lcom/mojang/renderpearl/api/commands/RenderPass;Lcom/mojang/renderpearl/api/textures/GpuSampler;Lcom/mojang/renderpearl/api/textures/GpuTextureView;Z)V", shift = At.Shift.AFTER))
    private RenderPass visor$afterSolidStage(RenderPass pass) {
        this.visor$closedLevelPass = pass;
        return visor$splitPassForStage(pass, RenderPipelineStage.AFTER_SOLID);
    }

    // Iris replaces the pass of the translucent level: no pass of Visor stays open past executeSolid
    @Inject(method = "executeSolid", at = @At("TAIL"), require = 1)
    private void visor$closeSolidStagePass(CallbackInfo ci) {
        visor$closeStagePass();
    }

    @ModifyVariable(method = "executeClassicTransparency", argsOnly = true, require = 1, at = @At("HEAD"))
    private RenderPass visor$goOnAfterSolidStage(RenderPass pass) {
        if (pass != this.visor$closedLevelPass) {
            return pass;
        }
        this.visor$closedLevelPass = null;
        return visor$openStagePass(RenderPipelineStage.AFTER_SOLID);
    }

    @ModifyVariable(method = "executeClassicTransparency", argsOnly = true, require = 1,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/ChunkSectionsToRender;renderGroup(Lnet/minecraft/client/renderer/chunk/ChunkSectionLayerGroup;Lcom/mojang/renderpearl/api/commands/RenderPass;Lcom/mojang/renderpearl/api/textures/GpuSampler;Lcom/mojang/renderpearl/api/textures/GpuTextureView;Z)V", shift = At.Shift.AFTER))
    private RenderPass visor$afterTranslucentStage(RenderPass pass) {
        return visor$splitPassForStage(pass, RenderPipelineStage.AFTER_TRANSLUCENT);
    }

    @Inject(method = "executeClassicTransparency", at = @At("TAIL"), require = 1)
    private void visor$closeClassicStagePass(CallbackInfo ci) {
        visor$closeStagePass();
    }

    @Inject(method = "executeOit", at = @At("TAIL"), require = 1)
    private void visor$afterOitTranslucentStage(CallbackInfo ci) {
        ModLoader.get().fireLevelStage(RenderPipelineStage.AFTER_TRANSLUCENT);
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void visor$noStagePassLeftOpen(CallbackInfo ci) {
        visor$closeStagePass();
        this.visor$closedLevelPass = null;
    }

    @Unique
    private RenderPass visor$splitPassForStage(RenderPass pass, RenderPipelineStage stage) {
        pass.close();
        visor$closeStagePass();
        ModLoader.get().fireLevelStage(stage);
        return visor$openStagePass(stage);
    }

    @Unique
    private RenderPass visor$openStagePass(RenderPipelineStage stage) {
        RenderTarget mainTarget = this.targets.main.get();
        RenderPass next = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                () -> "Main after " + stage.name(), mainTarget.getColorTextureView(), Optional.empty(),
                mainTarget.getDepthTextureView(), OptionalDouble.empty());
        RenderSystem.bindDefaultUniforms(next);
        this.visor$stagePass = next;
        return next;
    }

    @Unique
    private void visor$closeStagePass() {
        if (this.visor$stagePass != null) {
            this.visor$stagePass.close();
            this.visor$stagePass = null;
        }
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
