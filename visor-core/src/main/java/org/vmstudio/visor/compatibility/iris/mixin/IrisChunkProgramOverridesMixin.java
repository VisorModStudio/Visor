package org.vmstudio.visor.compatibility.iris.mixin;

import me.jellysquid.mods.sodium.client.gl.shader.GlProgram;
import me.jellysquid.mods.sodium.client.render.chunk.vertex.format.ChunkVertexType;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.compat.sodium.impl.shader_overrides.IrisChunkProgramOverrides;
import net.irisshaders.iris.compat.sodium.impl.shader_overrides.IrisChunkShaderInterface;
import net.irisshaders.iris.compat.sodium.impl.shader_overrides.IrisTerrainPass;
import net.irisshaders.iris.pipeline.SodiumTerrainPipeline;
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
@ClassDependentMixin("me.jellysquid.mods.sodium.client.render.chunk.vertex.format.ChunkVertexType")
@Mixin(value = IrisChunkProgramOverrides.class, remap = false)
public class IrisChunkProgramOverridesMixin {
    @Shadow
    @Final
    private EnumMap<IrisTerrainPass, GlProgram<IrisChunkShaderInterface>> programs;

    @Unique
    private final EnumMap<VRRenderPass, EnumMap<IrisTerrainPass, GlProgram<IrisChunkShaderInterface>>> visor$passPrograms =
            new EnumMap<>(VRRenderPass.class);

    @Redirect(method = "getProgramOverride", at = @At(value = "INVOKE",
            target = "Lnet/irisshaders/iris/compat/sodium/impl/shader_overrides/IrisChunkProgramOverrides;createShaders(Lnet/irisshaders/iris/pipeline/SodiumTerrainPipeline;Lme/jellysquid/mods/sodium/client/render/chunk/vertex/format/ChunkVertexType;)V"),
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
                SodiumTerrainPipeline passSodiumPipeline =
                        visor$getSodiumTerrainPipeline(manager.visor$getPassPipeline(pass));
                if (passSodiumPipeline == null) {
                    continue;
                }
                VisorClientImpl.LOGGER.info("Visor: creating per-pass Sodium terrain programs for {}", pass);
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
        SodiumTerrainPipeline fromBasePipeline = visor$getSodiumTerrainPipeline(
                ((IrisPipelineManagerExtension) Iris.getPipelineManager()).visor$getBasePipeline());
        if (fromBasePipeline != null) {
            baseSodiumPipeline = fromBasePipeline;
        }
        instance.createShaders(baseSodiumPipeline, vertexType);
    }

    /**
     * Old Iris' WorldRenderingPipeline#getSodiumTerrainPipeline does not exist in the
     * Iris version compiled against, so the legacy pipeline is accessed reflectively.
     */
    @Unique
    private static SodiumTerrainPipeline visor$getSodiumTerrainPipeline(Object pipeline) {
        if (pipeline == null) {
            return null;
        }
        try {
            return (SodiumTerrainPipeline) pipeline.getClass()
                    .getMethod("getSodiumTerrainPipeline")
                    .invoke(pipeline);
        } catch (ReflectiveOperationException | ClassCastException e) {
            return null;
        }
    }

    @Redirect(method = "getProgramOverride", at = @At(value = "INVOKE",
            target = "Ljava/util/EnumMap;get(Ljava/lang/Object;)Ljava/lang/Object;"))
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