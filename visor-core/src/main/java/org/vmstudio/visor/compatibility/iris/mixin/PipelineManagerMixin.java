package org.vmstudio.visor.compatibility.iris.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.irisshaders.iris.gl.buffer.ShaderStorageBufferHolder;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.shaderpack.materialmap.NamespacedId;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vmstudio.visor.api.client.render.VRRenderPass;
import org.vmstudio.visor.compatibility.ShadersHelper;
import org.vmstudio.visor.compatibility.ClassDependentMixin;
import org.vmstudio.visor.compatibility.iris.IrisCompatHelper;
import org.vmstudio.visor.compatibility.iris.IrisDhOverrideHelper;
import org.vmstudio.visor.compatibility.iris.extensions.IrisPipelineExtension;
import org.vmstudio.visor.compatibility.iris.extensions.IrisPipelineManagerExtension;
import org.vmstudio.visor.core.client.VisorClientImpl;
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.core.client.render.VRRenderState;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Pseudo
@ClassDependentMixin("net.irisshaders.iris.pipeline.PipelineManager")
@Mixin(targets = "net.irisshaders.iris.pipeline.PipelineManager", remap = false)
public class PipelineManagerMixin implements IrisPipelineManagerExtension {
    @Shadow
    @Final
    private Function<NamespacedId, WorldRenderingPipeline> pipelineFactory;

    @Shadow
    @Final
    private Map<NamespacedId, WorldRenderingPipeline> pipelinesPerDimension;

    @Shadow
    private WorldRenderingPipeline pipeline;

    @Shadow
    private int versionCounterForSodiumShaderReload;

    @Unique
    private final Map<Object, Map<VRRenderPass, WorldRenderingPipeline>> visor$passPipelines = new HashMap<>();

    @Unique
    private NamespacedId visor$currentDimension;

    @Unique
    private VRRenderPass visor$lastSsboPass;

    @Inject(method = "preparePipeline", at = @At("HEAD"))
    private void visor$unbindDhOverridesOnSwap(NamespacedId dimension, CallbackInfoReturnable<WorldRenderingPipeline> cir) {
        if (this.pipeline != null) {
            IrisDhOverrideHelper.unregisterDhOverrides(this.pipeline);
        }
    }

    @Inject(method = "preparePipeline", at = @At("RETURN"), cancellable = true)
    private void visor$preparePassPipelines(NamespacedId dimension, CallbackInfoReturnable<WorldRenderingPipeline> cir) {
        if (!IrisCompatHelper.perEyePipelines() || !VisorState.get().isActive()
                || !ShadersHelper.isShaderActive()) {
            return;
        }
        try {
            visor$currentDimension = dimension;
            Map<VRRenderPass, WorldRenderingPipeline> perPass =
                    visor$passPipelines.computeIfAbsent(dimension, k -> new EnumMap<>(VRRenderPass.class));

            if (perPass.isEmpty()) {
                RenderTarget eyeTarget;
                try {
                    eyeTarget = VRRenderState.getTargetForPass(VRRenderPass.EYE_LEFT);
                } catch (Throwable t) {
                    eyeTarget = null;
                }
                if (eyeTarget == null || eyeTarget == VRRenderState.getVanillaTarget()) {
                    return;
                }
                for (VRRenderPass pass : VRRenderState.getActivePasses()) {
                    if (pass.isWorld()) {
                        perPass.put(pass, visor$buildPassPipeline(dimension, pass));
                    }
                }
                this.versionCounterForSodiumShaderReload++;
            }

            if (!VRRenderState.getPhase().isVRWorld()) {
                return;
            }
            VRRenderPass pass = VRRenderState.getRenderPass();
            if (pass == null || !pass.isWorld()) {
                return;
            }
            WorldRenderingPipeline passPipeline = perPass.get(pass);
            if (passPipeline == null) {
                passPipeline = visor$buildPassPipeline(dimension, pass);
                perPass.put(pass, passPipeline);
                this.versionCounterForSodiumShaderReload++;
            }
            this.pipeline = passPipeline;
            visor$setupSsbos(passPipeline, pass);
            cir.setReturnValue(passPipeline);
        } catch (Throwable t) {
            IrisCompatHelper.latchPerEyeOff(t);
            this.pipeline = pipelinesPerDimension.get(dimension);
            visor$destroyPassPipelines();
            IrisCompatHelper.sharedShadowTargets = null;
            IrisCompatHelper.clearIrisFallback();
        }
    }

    @Unique
    private WorldRenderingPipeline visor$buildPassPipeline(NamespacedId dimension, VRRenderPass pass) {
        Minecraft mc = Minecraft.getInstance();
        RenderTarget previousMain = mc.mainRenderTarget;
        RenderTarget passTarget = VRRenderState.getTargetForPass(pass);
        if (passTarget != null) {
            mc.mainRenderTarget = passTarget;
        }
        IrisCompatHelper.buildingPass = pass;
        try {
            VisorClientImpl.LOGGER.info("Visor: building per-pass Iris pipeline for {} / {}", dimension, pass);
            WorldRenderingPipeline built = this.pipelineFactory.apply(dimension);
            if (!(built instanceof IrisPipelineExtension)) {
                throw new IllegalStateException(
                        "Iris fell back to the vanilla pipeline while building the " + pass
                                + " pass (pipeline build failed; see the Iris error above)");
            }
            return built;
        } finally {
            IrisCompatHelper.buildingPass = null;
            mc.mainRenderTarget = previousMain;
        }
    }

    @Unique
    private void visor$setupSsbos(WorldRenderingPipeline passPipeline, VRRenderPass pass) {
        if (pass == visor$lastSsboPass) {
            return;
        }
        visor$lastSsboPass = pass;
        if (passPipeline instanceof IrisPipelineExtension extension) {
            Object holder = extension.visor$getSsboHolder();
            if (holder != null) {
                ((ShaderStorageBufferHolder) holder).setupBuffers();
            }
        }
    }

    @Inject(method = "destroyPipeline", at = @At("HEAD"))
    private void visor$destroyPassPipelines(CallbackInfo ci) {
        ShadersHelper.bridge().onPackChanged();
        IrisCompatHelper.resetPackState();
        visor$lastSsboPass = null;
        visor$destroyPassPipelines();
    }

    @Unique
    private void visor$destroyPassPipelines() {
        for (Map<VRRenderPass, WorldRenderingPipeline> perPass : visor$passPipelines.values()) {
            for (Map.Entry<VRRenderPass, WorldRenderingPipeline> entry : perPass.entrySet()) {
                try {
                    VisorClientImpl.LOGGER.info("Visor: destroying per-pass Iris pipeline {}", entry.getKey());
                    entry.getValue().destroy();
                } catch (Throwable t) {
                    VisorClientImpl.LOGGER.warn("Visor: failed to destroy a per-pass Iris pipeline", t);
                }
            }
        }
        visor$passPipelines.clear();
    }

    @Override
    @Unique
    public Object visor$getPassPipeline(VRRenderPass pass) {
        Map<VRRenderPass, WorldRenderingPipeline> perPass = visor$passPipelines.get(visor$currentDimension);
        return perPass == null ? null : perPass.get(pass);
    }

    @Override
    @Unique
    public Object visor$getBasePipeline() {
        return visor$currentDimension == null ? null : pipelinesPerDimension.get(visor$currentDimension);
    }
}