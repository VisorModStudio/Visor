package org.vmstudio.visor.api.compatibility.mcversion.render;

//? if >=1.21.6 {
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
//? if >=26.2 {
import com.mojang.blaze3d.pipeline.BindGroupLayout;
import com.mojang.blaze3d.platform.BlendFactor;
import org.lwjgl.opengl.GL11;
//?} else {
/*import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
*///?}
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderType;
//? if >=1.21.11 {
import net.minecraft.client.renderer.rendertype.RenderSetup;
//?} else {
/*import net.minecraft.client.renderer.RenderStateShard;
*///?}
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;
import org.vmstudio.visor.api.compatibility.mcversion.McVersionUtils;

import java.io.BufferedReader;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Cross-mc-version handler of a shader program
 */
public final class McShaderProgram {
    // 1.21.6 feeds uniforms through std140 blocks only: the json every older node reads gives the block layout,
    // in declaration order, and the GLSL declares the same block under VISOR_UBO
    private static final String BLOCK = "VisorUniforms";
    private static final String UBO_DEFINE = "VISOR_UBO";
    private static final String MAT3_AS_MAT4 = "VISOR_MAT3_AS_MAT4";
    //? if >=26.2 {
    private static final String REVERSED_DEPTH = "VISOR_REVERSED_DEPTH";
    private static final McGlState.DrawState LEVEL_DRAW = new McGlState.DrawState(false,
            GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO,
            true, GL11.GL_LEQUAL, true, true, true, true);
    private static final Map<RenderPipeline, McShaderProgram> RENDER_TYPE_PROGRAMS = new HashMap<>();
    //?}
    private static final int RING_SLOTS = 8;

    private static final Matrix4f IDENTITY = new Matrix4f();

    private static McShaderProgram active;

    private record Slot(String name, String type, int count, int offset, float[] defaults) {
    }

    private final String name;
    private final VertexFormat vertexFormat;
    private final boolean alphaBlend;
    private final List<Slot> slots = new ArrayList<>();
    private final List<String> samplerNames = new ArrayList<>();
    private final boolean usesMatrices;
    private final int blockSize;
    private final McUniformRing ring;
    private final Map<String, float[]> floatUniforms = new HashMap<>();
    private final Map<String, int[]> intUniforms = new HashMap<>();
    private final Map<String, Matrix4f> matrixUniforms = new HashMap<>();
    private final Map<String, McShaderTexture> samplers = new LinkedHashMap<>();
    private final Map<PipelineKey, RenderPipeline> pipelines = new HashMap<>();
    private GpuBufferSlice pendingBlock;
    private Matrix4f modelView;
    private Matrix4f projection;

    private McShaderProgram(String name, VertexFormat vertexFormat, boolean alphaBlend) throws Exception {
        this.name = name;
        this.vertexFormat = vertexFormat;
        this.alphaBlend = alphaBlend;

        CompoundTag json = readJson(name);
        boolean matrices = false;
        int offset = 0;
        ListTag uniforms = json.getListOrEmpty("uniforms");
        for (int i = 0; i < uniforms.size(); i++) {
            CompoundTag uniform = uniforms.getCompoundOrEmpty(i);
            String uniformName = uniform.getStringOr("name", "");
            if (uniformName.equals("ModelViewMat") || uniformName.equals("ProjMat")) {
                matrices = true;
                continue;
            }
            String type = uniform.getStringOr("type", "float");
            int count = uniform.getIntOr("count", 1);
            int align = alignmentOf(type, count);
            offset = (offset + align - 1) / align * align;
            ListTag values = uniform.getListOrEmpty("values");
            float[] defaults = new float[values.size()];
            for (int v = 0; v < defaults.length; v++) {
                defaults[v] = (float) values.getDoubleOr(v, 0.0);
            }
            slots.add(new Slot(uniformName, type, count, offset, defaults));
            offset += sizeOf(type, count);
        }
        ListTag samplerList = json.getListOrEmpty("samplers");
        for (int i = 0; i < samplerList.size(); i++) {
            samplerNames.add(samplerList.getCompoundOrEmpty(i).getStringOr("name", ""));
        }
        this.usesMatrices = matrices;
        this.blockSize = (offset + 15) / 16 * 16;
        this.ring = slots.isEmpty() ? null : new McUniformRing("visor " + name + " uniforms", blockSize, RING_SLOTS);
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
        samplers.put(name, McShaderTexture.color(target));
    }

    public void setDepthSampler(String name, RenderTarget target) {
        samplers.put(name, McShaderTexture.depth(target));
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

    //? if >=26.2 {
    public RenderType renderType(String name, PrimitiveTopology mode, int bufferSize,
                                 Identifier... textures) {
        RenderPipeline pipeline = pipelines.computeIfAbsent(new PipelineKey(mode, LEVEL_DRAW),
                key -> buildPipeline(key.mode(), key.state()));
        RENDER_TYPE_PROGRAMS.put(pipeline, this);
        RenderSetup.RenderSetupBuilder setupBuilder = RenderSetup.builder(pipeline);
        for (int i = 0; i < textures.length; i++) {
            setupBuilder.withTexture("Sampler" + i, textures[i]);
        }
        return new RenderType(name, setupBuilder.createRenderSetup());
    }

    public static void prepareRenderTypeDraw(RenderPipeline pipeline) {
        McShaderProgram program = RENDER_TYPE_PROGRAMS.get(pipeline);
        if (program != null) {
            program.pendingBlock = program.ring == null ? null : program.writeBlock();
        }
    }

    public static void bindRenderTypeDraw(RenderPass pass, RenderPipeline pipeline) {
        McShaderProgram program = RENDER_TYPE_PROGRAMS.get(pipeline);
        if (program != null && program.pendingBlock != null) {
            pass.setUniform(BLOCK, program.pendingBlock);
        }
    }
    //?} elif >=1.21.11 {
    /*public RenderType renderType(String name, PrimitiveTopology mode, int bufferSize,
                                 Identifier... textures) {
        RenderSetup.RenderSetupBuilder setupBuilder = RenderSetup.builder(pipeline(mode)).bufferSize(bufferSize);
        for (int i = 0; i < textures.length; i++) {
            setupBuilder.withTexture("Sampler" + i, textures[i]);
        }
        RenderSetup setup = setupBuilder.createRenderSetup();
        return new RenderType(name, setup) {
            // the textures go to the units the Sampler<n> fallback of applyUniforms reads
            @Override
            public void draw(MeshData mesh) {
                Map<String, RenderSetup.TextureAndSampler> bound = setup.getTextures();
                McShaderTexture[] previous = new McShaderTexture[textures.length];
                for (int i = 0; i < textures.length; i++) {
                    RenderSetup.TextureAndSampler texture = bound.get("Sampler" + i);
                    previous[i] = McShaderTexture.setUnit(i, texture == null ? null
                            : new McShaderTexture(texture.textureView(), texture.sampler()));
                }
                try {
                    McShaderProgram.this.draw(mesh, McRenderTarget.writeTarget(), false);
                } finally {
                    for (int i = 0; i < textures.length; i++) {
                        McShaderTexture.setUnit(i, previous[i]);
                    }
                }
            }

            @Override
            public VertexFormat format() {
                return vertexFormat;
            }

            @Override
            public PrimitiveTopology mode() {
                return mode;
            }

            @Override
            public RenderPipeline pipeline() {
                return McShaderProgram.this.pipeline(mode);
            }
        };
    }
    *///?} elif >=1.21.9 {
    /*public RenderType renderType(String name, PrimitiveTopology mode, int bufferSize,
                                 Identifier... textures) {
        RenderStateShard.MultiTextureStateShard.Builder texture =
                RenderStateShard.MultiTextureStateShard.builder();
        for (Identifier location : textures) {
            texture.add(location, false);
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
            public VertexFormat format() {
                return vertexFormat;
            }

            @Override
            public PrimitiveTopology mode() {
                return mode;
            }

            @Override
            public RenderPipeline pipeline() {
                return McShaderProgram.this.pipeline(mode);
            }
        };
    }
    *///?} else {
    /*public RenderType renderType(String name, PrimitiveTopology mode, int bufferSize,
                                 Identifier... textures) {
        RenderStateShard.MultiTextureStateShard.Builder texture =
                RenderStateShard.MultiTextureStateShard.builder();
        for (Identifier location : textures) {
            texture.add(location, false);
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
            public VertexFormat format() {
                return vertexFormat;
            }

            @Override
            public PrimitiveTopology mode() {
                return mode;
            }
        };
    }
    *///?}

    static McShaderProgram active() {
        return active;
    }

    static void clearActive() {
        active = null;
    }

    void draw(MeshData mesh, RenderTarget target, boolean ownMatrices) {
        // buffer writes are illegal inside the pass
        pendingBlock = ring == null ? null : writeBlock();
        if (!ownMatrices) {
            McVertexBuilder.drawPass(mesh, target, pipeline(mesh.drawState().primitiveTopology()), this::applyUniforms);
            return;
        }
        McModelViewStack.push();
        RenderSystem.getModelViewStack().set(modelView != null ? modelView : IDENTITY);
        McProjection.State savedProjection = McProjection.save();
        McProjection.setKeepingType(projection != null ? projection : IDENTITY);
        try {
            McVertexBuilder.drawPass(mesh, target, pipeline(mesh.drawState().primitiveTopology()), this::applyUniforms);
        } finally {
            McProjection.restore(savedProjection);
            McModelViewStack.pop();
        }
    }

    private record PipelineKey(PrimitiveTopology mode, McGlState.DrawState state) {
    }

    private RenderPipeline pipeline(PrimitiveTopology mode) {
        return pipelines.computeIfAbsent(new PipelineKey(mode, McGlState.drawState()),
                key -> buildPipeline(key.mode(), key.state()));
    }

    private void applyUniforms(RenderPass pass) {
        for (String sampler : samplerNames) {
            McShaderTexture texture = samplers.get(sampler);
            if (texture == null && sampler.startsWith("Sampler")) {
                texture = McShaderTexture.unit(Integer.parseInt(sampler.substring("Sampler".length())));
            }
            if (texture != null) {
                texture.bind(pass, sampler);
            }
        }
        if (pendingBlock != null) {
            pass.setUniform(BLOCK, pendingBlock);
        }
    }

    private GpuBufferSlice writeBlock() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer buffer = stack.malloc(blockSize);
            for (Slot slot : slots) {
                switch (slot.type()) {
                    case "int" -> {
                        int[] values = intUniforms.get(slot.name());
                        buffer.putInt(slot.offset(), values != null ? values[0] : (int) defaultAt(slot, 0));
                    }
                    case "matrix4x4", "matrix3x3" -> {
                        Matrix4f matrix = matrixUniforms.get(slot.name());
                        if (matrix == null) {
                            matrix = defaultMatrix(slot);
                        }
                        matrix.get(slot.offset(), buffer);
                    }
                    default -> {
                        float[] values = slot.name().equals("GameTime")
                                ? new float[]{McGlState.shaderGameTime()}
                                : floatUniforms.get(slot.name());
                        for (int i = 0; i < slot.count(); i++) {
                            buffer.putFloat(slot.offset() + i * 4,
                                    values != null && i < values.length ? values[i] : defaultAt(slot, i));
                        }
                    }
                }
            }
            return ring.write(buffer.position(0).limit(blockSize));
        }
    }

    private static float defaultAt(Slot slot, int index) {
        return index < slot.defaults().length ? slot.defaults()[index] : 0.0F;
    }

    private static Matrix4f defaultMatrix(Slot slot) {
        float[] values = slot.defaults();
        if (values.length == 16) {
            return new Matrix4f().set(values);
        }
        if (values.length == 9) {
            return new Matrix4f(new Matrix3f().set(values));
        }
        return new Matrix4f();
    }

    private static int sizeOf(String type, int count) {
        return switch (type) {
            case "matrix4x4", "matrix3x3" -> 64;
            case "int" -> 4 * count;
            default -> 4 * count;
        };
    }

    private static int alignmentOf(String type, int count) {
        return switch (type) {
            case "matrix4x4", "matrix3x3" -> 16;
            default -> count == 1 ? 4 : count == 2 ? 8 : 16;
        };
    }

    private static CompoundTag readJson(String name) throws Exception {
        Identifier location = McVersionUtils.newResourceLoc("minecraft", "shaders/core/" + name + ".json");
        try (BufferedReader reader = Minecraft.getInstance().getResourceManager().getResourceOrThrow(location).openAsReader()) {
            // the json is a subset of SNBT, and gson is relocated in the mod jar
            return TagParser.parseCompoundFully(reader.lines().collect(Collectors.joining("\n")));
        }
    }

    private RenderPipeline buildPipeline(PrimitiveTopology mode, McGlState.DrawState state) {
        RenderPipeline.Builder builder = RenderPipeline.builder()
                .withLocation(McVersionUtils.newResourceLoc("visor", "pipeline/" + name))
                .withVertexShader("core/" + name)
                .withFragmentShader("core/" + name)
                //? if >=26.2 {
                .withVertexBinding(0, vertexFormat)
                .withPrimitiveTopology(mode)
                .withShaderDefine(REVERSED_DEPTH)
                //?} else {
                /*.withVertexFormat(vertexFormat, mode)
                *///?}
                .withShaderDefine(MAT3_AS_MAT4)
                .withShaderDefine(UBO_DEFINE)
                //? if >=26.1 {
                .withDepthStencilState(McShaders.depthStencilState(state))
                .withCull(state.cull());
                //?} else {
                /*.withDepthTestFunction(McShaders.depthTestFunction(state))
                .withDepthWrite(state.depthWrite())
                .withCull(state.cull())
                .withColorWrite(state.colorWrite(), state.alphaWrite());
                *///?}
        //? if >=26.2 {
        BindGroupLayout.Builder layout = BindGroupLayout.builder();
        if (usesMatrices) {
            layout.withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
                    .withUniform("Projection", UniformType.UNIFORM_BUFFER);
        }
        if (ring != null) {
            layout.withUniform(BLOCK, UniformType.UNIFORM_BUFFER);
        }
        samplerNames.forEach(layout::withSampler);
        builder.withBindGroupLayout(layout.build());
        //?} else {
        /*if (usesMatrices) {
            builder.withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
                    .withUniform("Projection", UniformType.UNIFORM_BUFFER);
        }
        if (ring != null) {
            builder.withUniform(BLOCK, UniformType.UNIFORM_BUFFER);
        }
        samplerNames.forEach(builder::withSampler);
        *///?}
        //? if >=26.2 {
        BlendFunction blend = alphaBlend
                ? new BlendFunction(BlendFactor.SRC_ALPHA, BlendFactor.ONE_MINUS_SRC_ALPHA)
                : state.blend() ? McShaders.blendFunction(state) : null;
        builder.withColorTargetState(McShaders.colorTargetState(state, blend));
        //?} elif >=26.1 {
        /*BlendFunction blend = alphaBlend
                ? new BlendFunction(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA)
                : state.blend() ? McShaders.blendFunction(state) : null;
        builder.withColorTargetState(McShaders.colorTargetState(state, blend));
        *///?} else {
        /*if (alphaBlend) {
            builder.withBlend(new BlendFunction(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA));
        } else if (state.blend()) {
            builder.withBlend(McShaders.blendFunction(state));
        } else {
            builder.withoutBlend();
        }
        *///?}
        return builder.build();
    }
}
//?} elif >=1.21.5 {
/*import com.mojang.blaze3d.pipeline.BlendFunction;
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
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.vmstudio.visor.api.compatibility.mcversion.McVersionUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

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

    public RenderType renderType(String name, PrimitiveTopology mode, int bufferSize,
                                 Identifier... textures) {
        RenderStateShard.MultiTextureStateShard.Builder texture =
                RenderStateShard.MultiTextureStateShard.builder();
        for (Identifier location : textures) {
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
            public PrimitiveTopology mode() {
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
            McVertexBuilder.drawPass(mesh, target, pipeline(mesh.drawState().primitiveTopology()), this::applyUniforms);
            return;
        }
        McModelViewStack.push();
        RenderSystem.getModelViewStack().set(modelView != null ? modelView : IDENTITY);
        McProjection.State savedProjection = McProjection.save();
        RenderSystem.setProjectionMatrix(projection != null ? projection : IDENTITY, RenderSystem.getProjectionType());
        try {
            McVertexBuilder.drawPass(mesh, target, pipeline(mesh.drawState().primitiveTopology()), this::applyUniforms);
        } finally {
            McProjection.restore(savedProjection);
            McModelViewStack.pop();
        }
    }

    private record PipelineKey(PrimitiveTopology mode, McGlState.DrawState state, String uniforms) {
    }

    private RenderPipeline pipeline(PrimitiveTopology mode) {
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

    private RenderPipeline buildPipeline(PrimitiveTopology mode, McGlState.DrawState state) {
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
*///?} elif >=1.21.2 {
/*import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.CompiledShaderProgram;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.ShaderDefines;
import net.minecraft.client.renderer.ShaderProgram;
import net.minecraft.resources.Identifier;
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

    public RenderType renderType(String name, PrimitiveTopology mode, int bufferSize,
                                 Identifier... textures) {
        RenderStateShard.MultiTextureStateShard.Builder texture =
                RenderStateShard.MultiTextureStateShard.builder();
        for (Identifier location : textures) {
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
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.Identifier;
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

    public RenderType renderType(String name, PrimitiveTopology mode, int bufferSize,
                                 Identifier... textures) {
        RenderStateShard.MultiTextureStateShard.Builder texture =
                RenderStateShard.MultiTextureStateShard.builder();
        for (Identifier location : textures) {
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
