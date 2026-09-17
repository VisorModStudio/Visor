package org.vmstudio.visor.api.compatibility.mcversion.render;

//? if >=1.21.5 {
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.vmstudio.visor.api.compatibility.mcversion.McVersionUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Cross-mc-version handler of a shader program
 */
public final class McShaderProgram {
    private static final int SHADER_TEXTURE_UNITS = 3;
    private static final String MAT3_AS_MAT4 = "VISOR_MAT3_AS_MAT4";

    private static final Matrix4f IDENTITY = new Matrix4f();

    private static McShaderProgram active;

    private final String name;
    private final VertexFormat vertexFormat;
    private final boolean alphaBlend;
    private final Map<String, float[]> floatUniforms = new LinkedHashMap<>();
    private final Map<String, int[]> intUniforms = new LinkedHashMap<>();
    private final Map<String, Matrix4f> matrixUniforms = new LinkedHashMap<>();
    private final Map<String, GpuTexture> samplers = new LinkedHashMap<>();
    private final Map<PipelineKey, RenderPipeline> pipelines = new HashMap<>();
    private Matrix4f modelView;
    private Matrix4f projection;

    private McShaderProgram(String name, VertexFormat vertexFormat, boolean alphaBlend) {
        this.name = name;
        this.vertexFormat = vertexFormat;
        this.alphaBlend = alphaBlend;
    }

    public static McShaderProgram core(String name, VertexFormat vertexFormat, boolean alphaBlend) throws Exception {
        return new McShaderProgram(name, vertexFormat, alphaBlend);
    }

    public VertexFormat vertexFormat() {
        return vertexFormat;
    }

    public void setUniform(String name, float value) {
        floatUniforms.put(name, new float[]{value});
    }

    public void setUniform(String name, int value) {
        intUniforms.put(name, new int[]{value});
    }

    public void setUniform(String name, float x, float y, float z) {
        floatUniforms.put(name, new float[]{x, y, z});
    }

    public void setUniform(String name, float[] values) {
        floatUniforms.put(name, values);
    }

    public void setUniform(String name, Matrix3f matrix) {
        matrixUniforms.put(name, new Matrix4f(matrix));
    }

    public void setUniform(String name, Matrix4f matrix) {
        matrixUniforms.put(name, new Matrix4f(matrix));
    }

    public void setSampler(String name, RenderTarget target) {
        samplers.put(name, target.getColorTexture());
    }

    public void setDepthSampler(String name, RenderTarget target) {
        samplers.put(name, target.getDepthTexture());
    }

    public void setModelViewMatrix(Matrix4f matrix) {
        modelView = new Matrix4f(matrix);
    }

    public void setProjectionMatrix(Matrix4f matrix) {
        projection = new Matrix4f(matrix);
    }

    public void apply() {
        active = this;
    }

    public void use() {
        active = this;
    }

    public void clear() {
        if (active == this) {
            active = null;
        }
    }

    public RenderType renderType(String name, VertexFormat.Mode mode, int bufferSize,
                                 ResourceLocation... textures) {
        RenderStateShard.MultiTextureStateShard.Builder texture =
                RenderStateShard.MultiTextureStateShard.builder();
        for (ResourceLocation location : textures) {
            texture.add(location, false, false);
        }
        RenderStateShard.MultiTextureStateShard textureState = texture.build();
        return new RenderType(name, bufferSize, false, false,
                textureState::setupRenderState, textureState::clearRenderState) {
            @Override
            public void draw(MeshData mesh) {
                setupRenderState();
                McShaderProgram.this.draw(mesh, McRenderTarget.writeTarget(), false);
                clearRenderState();
            }

            @Override
            public RenderTarget getRenderTarget() {
                return McRenderTarget.writeTarget();
            }

            @Override
            public RenderPipeline getRenderPipeline() {
                return pipeline(mode);
            }

            @Override
            public VertexFormat format() {
                return vertexFormat;
            }

            @Override
            public VertexFormat.Mode mode() {
                return mode;
            }
        };
    }

    static McShaderProgram active() {
        return active;
    }

    static void clearActive() {
        active = null;
    }

    void draw(MeshData mesh, RenderTarget target, boolean ownMatrices) {
        if (!ownMatrices) {
            McVertexBuilder.drawPass(mesh, target, pipeline(mesh.drawState().mode()), this::applyUniforms);
            return;
        }
        McModelViewStack.push();
        RenderSystem.getModelViewStack().set(modelView != null ? modelView : IDENTITY);
        McProjection.State savedProjection = McProjection.save();
        RenderSystem.setProjectionMatrix(projection != null ? projection : IDENTITY, RenderSystem.getProjectionType());
        try {
            McVertexBuilder.drawPass(mesh, target, pipeline(mesh.drawState().mode()), this::applyUniforms);
        } finally {
            McProjection.restore(savedProjection);
            McModelViewStack.pop();
        }
    }

    private record PipelineKey(VertexFormat.Mode mode, McGlState.DrawState state, String uniforms) {
    }

    private RenderPipeline pipeline(VertexFormat.Mode mode) {
        String uniforms = matrixUniforms.keySet() + "|" + floatUniforms.keySet() + "|"
                + intUniforms.keySet() + "|" + samplers.keySet();
        return pipelines.computeIfAbsent(new PipelineKey(mode, McGlState.drawState(), uniforms),
                key -> buildPipeline(key.mode(), key.state()));
    }

    private void applyUniforms(RenderPass pass) {
        for (int unit = 0; unit < SHADER_TEXTURE_UNITS; unit++) {
            GpuTexture texture = RenderSystem.getShaderTexture(unit);
            if (texture != null) {
                pass.bindSampler("Sampler" + unit, texture);
            }
        }
        matrixUniforms.forEach(pass::setUniform);
        floatUniforms.forEach(pass::setUniform);
        intUniforms.forEach(pass::setUniform);
        samplers.forEach((sampler, texture) -> {
            if (texture != null) {
                pass.bindSampler(sampler, texture);
            }
        });
    }

    private RenderPipeline buildPipeline(VertexFormat.Mode mode, McGlState.DrawState state) {
        RenderPipeline.Builder builder = RenderPipeline.builder()
                .withLocation(McVersionUtils.newResourceLoc("visor", "pipeline/" + name))
                .withVertexShader("core/" + name)
                .withFragmentShader("core/" + name)
                .withVertexFormat(vertexFormat, mode)
                .withShaderDefine(MAT3_AS_MAT4)
                .withDepthTestFunction(McShaders.depthTestFunction(state))
                .withDepthWrite(state.depthWrite())
                .withCull(state.cull())
                .withColorWrite(state.colorWrite(), state.alphaWrite())
                .withUniform("ModelViewMat", UniformType.MATRIX4X4)
                .withUniform("ProjMat", UniformType.MATRIX4X4)
                .withUniform("GameTime", UniformType.FLOAT);
        Set<String> declaredSamplers = new HashSet<>();
        for (int unit = 0; unit < SHADER_TEXTURE_UNITS; unit++) {
            declaredSamplers.add("Sampler" + unit);
            builder.withSampler("Sampler" + unit);
        }
        matrixUniforms.keySet().forEach(uniform -> builder.withUniform(uniform, UniformType.MATRIX4X4));
        floatUniforms.forEach((uniform, values) -> builder.withUniform(uniform, switch (values.length) {
            case 1 -> UniformType.FLOAT;
            case 2 -> UniformType.VEC2;
            case 3 -> UniformType.VEC3;
            default -> UniformType.VEC4;
        }));
        intUniforms.keySet().forEach(uniform -> builder.withUniform(uniform, UniformType.INT));
        samplers.keySet().forEach(sampler -> {
            if (declaredSamplers.add(sampler)) {
                builder.withSampler(sampler);
            }
        });
        if (alphaBlend) {
            builder.withBlend(new BlendFunction(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA));
        } else if (state.blend()) {
            builder.withBlend(new BlendFunction(
                    McShaders.source(state.blendSourceRgb()), McShaders.destination(state.blendDestinationRgb()),
                    McShaders.source(state.blendSourceAlpha()), McShaders.destination(state.blendDestinationAlpha())));
        } else {
            builder.withoutBlend();
        }
        return builder.build();
    }
}
//?} elif >=1.21.2 {
/*import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.CompiledShaderProgram;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderDefines;
import net.minecraft.client.renderer.ShaderProgram;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL14;
import org.vmstudio.visor.api.compatibility.mcversion.McVersionUtils;

public final class McShaderProgram {
    private final VertexFormat vertexFormat;
    private final boolean alphaBlend;
    private final ShaderProgram program;

    private McShaderProgram(String name, VertexFormat vertexFormat, boolean alphaBlend) throws Exception {
        this.vertexFormat = vertexFormat;
        this.alphaBlend = alphaBlend;
        this.program = new ShaderProgram(
                McVersionUtils.newResourceLoc("minecraft", "core/" + name),
                vertexFormat,
                ShaderDefines.EMPTY
        );
        Minecraft.getInstance().getShaderManager().getProgramForLoading(program);
    }

    public static McShaderProgram core(String name, VertexFormat vertexFormat, boolean alphaBlend) throws Exception {
        return new McShaderProgram(name, vertexFormat, alphaBlend);
    }

    public VertexFormat vertexFormat() {
        return vertexFormat;
    }

    public void setUniform(String name, float value) {
        uniform(name).set(value);
    }

    public void setUniform(String name, int value) {
        uniform(name).set(value);
    }

    public void setUniform(String name, float x, float y, float z) {
        uniform(name).set(x, y, z);
    }

    public void setUniform(String name, float[] values) {
        uniform(name).set(values);
    }

    public void setUniform(String name, Matrix3f matrix) {
        uniform(name).set(matrix);
    }

    public void setUniform(String name, Matrix4f matrix) {
        uniform(name).set(matrix);
    }

    // a sampler is not a uniform: safeGetUniform hands back the dummy and the texture is never bound
    public void setSampler(String name, RenderTarget target) {
        compiled().bindSampler(name, McRenderTarget.colorTextureId(target));
    }

    public void setDepthSampler(String name, RenderTarget target) {
        compiled().bindSampler(name, McRenderTarget.depthTextureId(target));
    }

    public void setModelViewMatrix(Matrix4f matrix) {
        Uniform uniform = compiled().MODEL_VIEW_MATRIX;
        if (uniform != null) {
            uniform.set(matrix);
        }
    }

    public void setProjectionMatrix(Matrix4f matrix) {
        Uniform uniform = compiled().PROJECTION_MATRIX;
        if (uniform != null) {
            uniform.set(matrix);
        }
    }

    public void apply() {
        applyBlend();
        compiled().apply();
    }

    public void use() {
        applyBlend();
        RenderSystem.setShader(program);
    }

    public void clear() {
        compiled().clear();
    }

    public RenderType renderType(String name, VertexFormat.Mode mode, int bufferSize,
                                 ResourceLocation... textures) {
        RenderStateShard.MultiTextureStateShard.Builder texture =
                RenderStateShard.MultiTextureStateShard.builder();
        for (ResourceLocation location : textures) {
            texture.add(location, false, false);
        }
        return RenderType.create(name, vertexFormat, mode, bufferSize, false, false,
                RenderType.CompositeState.builder()
                        .setShaderState(new RenderStateShard.ShaderStateShard(program))
                        .setTextureState(texture.build())
                        .createCompositeState(false));
    }

    private AbstractUniform uniform(String name) {
        return compiled().safeGetUniform(name);
    }

    private void applyBlend() {
        // the json "blend" block is not read since 1.21.2
        if (alphaBlend) {
            McGlState.enableBlend();
            McGlState.blendEquation(GL14.GL_FUNC_ADD);
            McGlState.blendFunc(McGlState.Blend.SRC_ALPHA, McGlState.Blend.ONE_MINUS_SRC_ALPHA);
        }
    }

    private CompiledShaderProgram compiled() {
        CompiledShaderProgram compiled = Minecraft.getInstance().getShaderManager().getProgram(program);
        if (compiled == null) {
            throw new IllegalStateException("Shader program failed to compile: " + program.configId());
        }
        return compiled;
    }
}
*///?} else {
/*import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public final class McShaderProgram {
    private final VertexFormat vertexFormat;
    private final boolean alphaBlend;
    private final ShaderInstance instance;

    private McShaderProgram(String name, VertexFormat vertexFormat, boolean alphaBlend) throws Exception {
        this.vertexFormat = vertexFormat;
        this.alphaBlend = alphaBlend;
        this.instance = new ShaderInstance(Minecraft.getInstance().getResourceManager(), name, vertexFormat);
    }

    public static McShaderProgram core(String name, VertexFormat vertexFormat, boolean alphaBlend) throws Exception {
        return new McShaderProgram(name, vertexFormat, alphaBlend);
    }

    public VertexFormat vertexFormat() {
        return vertexFormat;
    }

    public void setUniform(String name, float value) {
        uniform(name).set(value);
    }

    public void setUniform(String name, int value) {
        uniform(name).set(value);
    }

    public void setUniform(String name, float x, float y, float z) {
        uniform(name).set(x, y, z);
    }

    public void setUniform(String name, float[] values) {
        uniform(name).set(values);
    }

    public void setUniform(String name, Matrix3f matrix) {
        uniform(name).set(matrix);
    }

    public void setUniform(String name, Matrix4f matrix) {
        uniform(name).set(matrix);
    }

    // a sampler is not a uniform: safeGetUniform hands back the dummy and the texture is never bound
    public void setSampler(String name, RenderTarget target) {
        instance.setSampler(name, McRenderTarget.colorTextureId(target));
    }

    public void setDepthSampler(String name, RenderTarget target) {
        instance.setSampler(name, McRenderTarget.depthTextureId(target));
    }

    public void setModelViewMatrix(Matrix4f matrix) {
        Uniform uniform = instance.MODEL_VIEW_MATRIX;
        if (uniform != null) {
            uniform.set(matrix);
        }
    }

    public void setProjectionMatrix(Matrix4f matrix) {
        Uniform uniform = instance.PROJECTION_MATRIX;
        if (uniform != null) {
            uniform.set(matrix);
        }
    }

    public void apply() {
        instance.apply();
    }

    public void use() {
        RenderSystem.setShader(() -> instance);
    }

    public void clear() {
        instance.clear();
    }

    public RenderType renderType(String name, VertexFormat.Mode mode, int bufferSize,
                                 ResourceLocation... textures) {
        RenderStateShard.MultiTextureStateShard.Builder texture =
                RenderStateShard.MultiTextureStateShard.builder();
        for (ResourceLocation location : textures) {
            texture.add(location, false, false);
        }
        return RenderType.create(name, vertexFormat, mode, bufferSize, false, false,
                RenderType.CompositeState.builder()
                        .setShaderState(new RenderStateShard.ShaderStateShard(() -> instance))
                        .setTextureState(texture.build())
                        .createCompositeState(false));
    }

    private AbstractUniform uniform(String name) {
        return instance.safeGetUniform(name);
    }
}
*///?}
