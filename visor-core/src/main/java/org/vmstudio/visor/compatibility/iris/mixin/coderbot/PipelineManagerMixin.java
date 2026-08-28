package org.vmstudio.visor.compatibility.iris.mixin.coderbot;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.coderbot.iris.gl.buffer.ShaderStorageBufferHolder;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vmstudio.visor.api.client.render.VRRenderPass;
import org.vmstudio.visor.compatibility.ShadersHelper;
import org.vmstudio.visor.compatibility.MixinGate;
import org.vmstudio.visor.compatibility.iris.IrisCompatHelper;
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
@MixinGate(classes = "net.coderbot.iris.pipeline.PipelineManager")
@Mixin(targets = "net.coderbot.iris.pipeline.PipelineManager", remap = false)
public class PipelineManagerMixin implements IrisPipelineManagerExtension {
    @Shadow
    @Final
    private Function<Object, WorldRenderingPipeline> pipelineFactory;

    @Shadow
    private WorldRenderingPipeline pipeline;

    @Unique
    private final Map<Object, Map<VRRenderPass, WorldRenderingPipeline>> visor$passPipelines = new HashMap<>();

    @Unique
    private Object visor$currentDimension;

    @Unique
    private WorldRenderingPipeline visor$basePipeline;

    @Unique
    private VRRenderPass visor$lastSsboPass;

    @Inject(method = "preparePipeline", at = @At("RETURN"), cancellable = true)
    private void visor$preparePassPipelines(@Coerce Object dimension,
                                            CallbackInfoReturnable<WorldRenderingPipeline> cir) {
        visor$currentDimension = dimension;
        visor$basePipeline = cir.getReturnValue();

        if (!IrisCompatHelper.perEyePipelines() || !VisorState.get().isActive()
                || !ShadersHelper.isShaderActive()) {
            return;
        }
        try {
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
                IrisCompatHelper.bumpSodiumReloadCounter(this);
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
                IrisCompatHelper.bumpSodiumReloadCounter(this);
            }
            this.pipeline = passPipeline;
            visor$setupSsbos(passPipeline, pass);
            cir.setReturnValue(passPipeline);
        } catch (Throwable t) {
            IrisCompatHelper.latchPerEyeOff(t);
            this.pipeline = visor$basePipeline;
            visor$destroyPassPipelines();
            IrisCompatHelper.sharedShadowTargets = null;
            IrisCompatHelper.clearIrisFallback();
        }
    }

    @Unique
    private WorldRenderingPipeline visor$buildPassPipeline(Object dimension, VRRenderPass pass) {
        Minecraft mc = Minecraft.getInstance();
        RenderTarget previousMain = mc.mainRenderTarget;
        RenderTarget passTarget = VRRenderState.getTargetForPass(pass);
        if (passTarget != null) {
            mc.mainRenderTarget = passTarget;
        }
        IrisCompatHelper.buildingPass = pass;
        try {
            VisorClientImpl.LOGGER.info("Visor: building per-pass Iris pipeline for {} / {} (legacy Iris)",
                    dimension, pass);
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
        if (passPipeline instanceof IrisPipelineExtension extension
                && extension.visor$getSsboHolder() instanceof ShaderStorageBufferHolder holder) {
            holder.setupBuffers();
        }
    }

    @Inject(method = "destroyPipeline", at = @At("HEAD"))
    private void visor$onPipelineDestroyed(CallbackInfo ci) {
        ShadersHelper.bridge().onPackChanged();
        IrisCompatHelper.resetPackState();
        visor$lastSsboPass = null;
        visor$basePipeline = null;
        visor$destroyPassPipelines();
    }

    @Unique
    private void visor$destroyPassPipelines() {
        for (Map<VRRenderPass, WorldRenderingPipeline> perPass : visor$passPipelines.values()) {
            for (Map.Entry<VRRenderPass, WorldRenderingPipeline> entry : perPass.entrySet()) {
                try {
                    VisorClientImpl.LOGGER.info("Visor: destroying per-pass Iris pipeline {} (legacy Iris)",
                            entry.getKey());
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
        return visor$basePipeline;
    }
}