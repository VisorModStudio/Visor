package org.vmstudio.visor.compatibility.iris.mixin.coderbot;

import me.jellysquid.mods.sodium.client.gl.shader.GlProgram;
import me.jellysquid.mods.sodium.client.render.chunk.vertex.format.ChunkVertexType;
import net.coderbot.iris.Iris;
import net.coderbot.iris.compat.sodium.impl.shader_overrides.IrisChunkProgramOverrides;
import net.coderbot.iris.compat.sodium.impl.shader_overrides.IrisChunkShaderInterface;
import net.coderbot.iris.compat.sodium.impl.shader_overrides.IrisTerrainPass;
import net.coderbot.iris.pipeline.SodiumTerrainPipeline;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vmstudio.visor.api.client.render.VRRenderPass;
import org.vmstudio.visor.compatibility.ClassDependentMixin;
import org.vmstudio.visor.compatibility.iris.IrisCompatHelper;
import org.vmstudio.visor.compatibility.iris.extensions.IrisPipelineManagerExtension;
import org.vmstudio.visor.core.client.VisorClientImpl;
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.core.client.render.VRRenderState;

import java.util.EnumMap;

@Pseudo
@ClassDependentMixin("net.coderbot.iris.compat.sodium.impl.shader_overrides.IrisChunkProgramOverrides")
@Mixin(value = IrisChunkProgramOverrides.class, remap = false)
public class IrisChunkProgramOverridesMixin {
    @Shadow
    @Final
    private EnumMap<IrisTerrainPass, GlProgram<IrisChunkShaderInterface>> programs;

    @Unique
    private final EnumMap<VRRenderPass, EnumMap<IrisTerrainPass, GlProgram<IrisChunkShaderInterface>>> visor$passPrograms =
            new EnumMap<>(VRRenderPass.class);

    @Redirect(method = "getProgramOverride", at = @At(value = "INVOKE",
            target = "Lnet/coderbot/iris/compat/sodium/impl/shader_overrides/IrisChunkProgramOverrides;createShaders(Lnet/coderbot/iris/pipeline/SodiumTerrainPipeline;Lme/jellysquid/mods/sodium/client/render/chunk/vertex/format/ChunkVertexType;)V"),
            expect = 0, require = 0)
    private void visor$createAllPassShaders(IrisChunkProgramOverrides instance,
                                            SodiumTerrainPipeline sodiumTerrainPipeline,
                                            ChunkVertexType vertexType) {
        if (!IrisCompatHelper.perEyePipelines() || !VisorState.get().isActive()
                || !(Iris.getPipelineManager() instanceof IrisPipelineManagerExtension manager)) {
            instance.createShaders(sodiumTerrainPipeline, vertexType);
            return;
        }
        try {
            visor$deletePassPrograms();
            for (VRRenderPass pass : VRRenderPass.values()) {
                if (!pass.isWorld()) {
                    continue;
                }
                if (!(manager.visor$getPassPipeline(pass) instanceof WorldRenderingPipeline passPipeline)) {
                    continue;
                }
                SodiumTerrainPipeline passSodiumPipeline = passPipeline.getSodiumTerrainPipeline();
                if (passSodiumPipeline == null) {
                    continue;
                }
                VisorClientImpl.LOGGER.info(
                        "Visor: creating per-pass Sodium terrain programs for {} (legacy Iris)", pass);
                EnumMap<IrisTerrainPass, GlProgram<IrisChunkShaderInterface>> passPrograms =
                        new EnumMap<>(IrisTerrainPass.class);
                instance.createShaders(passSodiumPipeline, vertexType);
                passPrograms.putAll(this.programs);
                this.programs.clear();
                visor$passPrograms.put(pass, passPrograms);
            }
        } catch (Throwable t) {
            IrisCompatHelper.latchPerEyeOff(t);
            visor$deletePassPrograms();
        }
        SodiumTerrainPipeline baseSodiumPipeline = sodiumTerrainPipeline;
        if (manager.visor$getBasePipeline() instanceof WorldRenderingPipeline basePipeline
                && basePipeline.getSodiumTerrainPipeline() != null) {
            baseSodiumPipeline = basePipeline.getSodiumTerrainPipeline();
        }
        instance.createShaders(baseSodiumPipeline, vertexType);
    }

    @Redirect(method = "getProgramOverride", at = @At(value = "INVOKE",
            target = "Ljava/util/EnumMap;get(Ljava/lang/Object;)Ljava/lang/Object;"),
            expect = 0, require = 0)
    private Object visor$perPassProgram(EnumMap<IrisTerrainPass, GlProgram<IrisChunkShaderInterface>> instance,
                                        Object key) {
        if (IrisCompatHelper.perEyePipelines() && VisorState.get().isActive()
                && VRRenderState.getPhase().isVRWorld()) {
            VRRenderPass pass = VRRenderState.getRenderPass();
            EnumMap<IrisTerrainPass, GlProgram<IrisChunkShaderInterface>> passPrograms =
                    pass == null ? null : visor$passPrograms.get(pass);
            if (passPrograms != null) {
                return passPrograms.get(key);
            }
        }
        return instance.get(key);
    }

    @Inject(method = "deleteShaders", at = @At("HEAD"))
    private void visor$deletePassPrograms(CallbackInfo ci) {
        visor$deletePassPrograms();
    }

    @Unique
    private void visor$deletePassPrograms() {
        for (EnumMap<IrisTerrainPass, GlProgram<IrisChunkShaderInterface>> passPrograms : visor$passPrograms.values()) {
            for (GlProgram<?> program : passPrograms.values()) {
                if (program != null) {
                    program.delete();
                }
            }
            passPrograms.clear();
        }
        visor$passPrograms.clear();
    }
}