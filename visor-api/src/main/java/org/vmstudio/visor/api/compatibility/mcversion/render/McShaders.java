package org.vmstudio.visor.api.compatibility.mcversion.render;

import com.mojang.blaze3d.systems.RenderSystem;
//? if >=1.21.5 {
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
//? if >=26.1 {
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.platform.CompareOp;
import java.util.Optional;
//?} else {
/*import com.mojang.blaze3d.platform.DepthTestFunction;
*///?}
//? if >=26.2 {
import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.platform.BlendFactor;
import net.minecraft.client.renderer.BindGroupLayouts;
//?} else {
/*import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
import com.mojang.blaze3d.shaders.UniformType;
*///?}
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.lwjgl.opengl.GL11;
import org.vmstudio.visor.api.compatibility.mcversion.McVersionUtils;

import java.util.HashMap;
import java.util.Map;
//?} elif >=1.21.2 {
/*import net.minecraft.client.renderer.CoreShaders;
import net.minecraft.client.renderer.ShaderProgram;
*///?} else {
/*import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import java.util.function.Supplier;
*///?}
//? if >=1.21.6 {
import com.mojang.blaze3d.textures.GpuTextureView;
//?} elif >=1.21.5 {
/*import com.mojang.blaze3d.textures.GpuTexture;
*///?}

/**
 * Cross-mc-version selection of vanilla core shaders
 */
public class McShaders {
    private McShaders() {
        throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
    }

    public enum Core {
        POSITION,
        POSITION_COLOR,
        POSITION_TEX,
        POSITION_TEX_COLOR,
        RENDERTYPE_TEXT
    }

    public static void use(Core shader) {
        //? if >=1.21.5 {
        selected = shader;
        McShaderProgram.clearActive();
        //?} elif >=1.21.2 {
        /*ShaderProgram program = switch (shader) {
            case POSITION -> CoreShaders.POSITION;
            case POSITION_COLOR -> CoreShaders.POSITION_COLOR;
            case POSITION_TEX -> CoreShaders.POSITION_TEX;
            case POSITION_TEX_COLOR -> CoreShaders.POSITION_TEX_COLOR;
            case RENDERTYPE_TEXT -> CoreShaders.RENDERTYPE_TEXT;
        };
        RenderSystem.setShader(program);
        *///?} else {
        /*Supplier<ShaderInstance> program = switch (shader) {
            case POSITION -> GameRenderer::getPositionShader;
            case POSITION_COLOR -> GameRenderer::getPositionColorShader;
            case POSITION_TEX -> GameRenderer::getPositionTexShader;
            case POSITION_TEX_COLOR -> GameRenderer::getPositionTexColorShader;
            case RENDERTYPE_TEXT -> GameRenderer::getRendertypeTextShader;
        };
        RenderSystem.setShader(program);
        *///?}
    }

    //? if >=1.21.5 {
    private static final Map<PipelineKey, RenderPipeline> PIPELINES = new HashMap<>();

    private static Core selected = Core.POSITION;

    private record PipelineKey(Core core, VertexFormat format, PrimitiveTopology mode,
                               McGlState.DrawState state) {
    }

    static RenderPipeline pipeline(VertexFormat format, PrimitiveTopology mode) {
        return PIPELINES.computeIfAbsent(
                new PipelineKey(selected, format, mode, McGlState.drawState()),
                McShaders::build);
    }

    static void applyUniforms(RenderPass pass) {
        bindSampler(pass, 0, textured(selected));
        bindSampler(pass, 2, selected == Core.RENDERTYPE_TEXT);
    }

    private static void bindSampler(RenderPass pass, int unit, boolean used) {
        McShaderTexture texture = McShaderTexture.unit(unit);
        if (used && texture != null) {
            texture.bind(pass, "Sampler" + unit);
        }
    }

    private static RenderPipeline build(PipelineKey key) {
        String shader = shaderName(key.core());
        McGlState.DrawState state = key.state();
        RenderPipeline.Builder builder = RenderPipeline.builder()
                .withLocation(McVersionUtils.newResourceLoc("visor", "pipeline/" + shader + '_' + PIPELINES.size()))
                .withVertexShader("core/" + shader)
                .withFragmentShader("core/" + shader)
                //? if >=26.2 {
                .withVertexBinding(0, key.format())
                .withPrimitiveTopology(key.mode())
                .withBindGroupLayout(BindGroupLayouts.MATRICES_PROJECTION)
                //?} elif >=1.21.6 {
                /*.withVertexFormat(key.format(), key.mode())
                // the vanilla core GLSL reads its matrices and colour from the DynamicTransforms / Projection blocks
                .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
                .withUniform("Projection", UniformType.UNIFORM_BUFFER)
                *///?} else {
                /*.withVertexFormat(key.format(), key.mode())
                .withUniform("ModelViewMat", UniformType.MATRIX4X4)
                .withUniform("ProjMat", UniformType.MATRIX4X4)
                .withUniform("ColorModulator", UniformType.VEC4)
                *///?}
                //? if >=26.1 {
                .withDepthStencilState(depthStencilState(state))
                .withCull(state.cull());
                //?} else {
                /*.withDepthTestFunction(depthTestFunction(state))
                .withDepthWrite(state.depthWrite())
                .withCull(state.cull())
                .withColorWrite(state.colorWrite(), state.alphaWrite());
                *///?}
        if (fogged(key.core())) {
            //? if >=26.2 {
            builder.withBindGroupLayout(BindGroupLayouts.FOG);
            //?} elif >=1.21.6 {
            /*builder.withUniform("Fog", UniformType.UNIFORM_BUFFER);
            *///?} else {
            /*builder.withUniform("FogStart", UniformType.FLOAT)
                    .withUniform("FogEnd", UniformType.FLOAT)
                    .withUniform("FogColor", UniformType.VEC4)
                    .withUniform("FogShape", UniformType.INT);
            *///?}
        }
        //? if >=26.2 {
        if (key.core() == Core.RENDERTYPE_TEXT) {
            builder.withBindGroupLayout(BindGroupLayouts.SAMPLER0_SAMPLER2);
        } else if (textured(key.core())) {
            builder.withBindGroupLayout(BindGroupLayouts.SAMPLER0);
        }
        //?} else {
        /*if (textured(key.core())) {
            builder.withSampler("Sampler0");
        }
        if (key.core() == Core.RENDERTYPE_TEXT) {
            builder.withSampler("Sampler2");
        }
        *///?}
        //? if >=26.1 {
        builder.withColorTargetState(colorTargetState(state, state.blend() ? blendFunction(state) : null));
        //?} else {
        /*if (state.blend()) {
            builder.withBlend(blendFunction(state));
        } else {
            builder.withoutBlend();
        }
        *///?}
        return builder.build();
    }

    static BlendFunction blendFunction(McGlState.DrawState state) {
        return new BlendFunction(
                source(state.blendSourceRgb()), destination(state.blendDestinationRgb()),
                source(state.blendSourceAlpha()), destination(state.blendDestinationAlpha()));
    }

    private static boolean textured(Core core) {
        return core == Core.POSITION_TEX || core == Core.POSITION_TEX_COLOR || core == Core.RENDERTYPE_TEXT;
    }

    private static boolean fogged(Core core) {
        return core == Core.POSITION || core == Core.RENDERTYPE_TEXT;
    }

    private static String shaderName(Core core) {
        return switch (core) {
            case POSITION -> "position";
            case POSITION_COLOR -> "position_color";
            case POSITION_TEX -> "position_tex";
            case POSITION_TEX_COLOR -> "position_tex_color";
            //? if >=26.2 {
            case RENDERTYPE_TEXT -> "text";
            //?} else {
            /*case RENDERTYPE_TEXT -> "rendertype_text";
            *///?}
        };
    }

    //? if >=26.1 {
    static Optional<DepthStencilState> depthStencilState(McGlState.DrawState state) {
        if (!state.depthTest() || state.depthFunction() == GL11.GL_ALWAYS) {
            return Optional.empty();
        }
        CompareOp depthTest = switch (state.depthFunction()) {
            case GL11.GL_EQUAL -> CompareOp.EQUAL;
            //? if >=26.2 {
            case GL11.GL_LESS -> CompareOp.GREATER_THAN;
            case GL11.GL_GREATER -> CompareOp.LESS_THAN;
            default -> CompareOp.GREATER_THAN_OR_EQUAL;
            //?} else {
            /*case GL11.GL_LESS -> CompareOp.LESS_THAN;
            case GL11.GL_GREATER -> CompareOp.GREATER_THAN;
            default -> CompareOp.LESS_THAN_OR_EQUAL;
            *///?}
        };
        return Optional.of(new DepthStencilState(depthTest, state.depthWrite()));
    }

    static ColorTargetState colorTargetState(McGlState.DrawState state, BlendFunction blend) {
        int writeMask = (state.colorWrite() ? ColorTargetState.WRITE_COLOR : ColorTargetState.WRITE_NONE)
                | (state.alphaWrite() ? ColorTargetState.WRITE_ALPHA : ColorTargetState.WRITE_NONE);
        //? if >=26.2 {
        return new ColorTargetState(Optional.ofNullable(blend), GpuFormat.RGBA8_UNORM, writeMask);
        //?} else {
        /*return new ColorTargetState(Optional.ofNullable(blend), writeMask);
        *///?}
    }
    //?} else {
    /*static DepthTestFunction depthTestFunction(McGlState.DrawState state) {
        if (!state.depthTest()) {
            return DepthTestFunction.NO_DEPTH_TEST;
        }
        return switch (state.depthFunction()) {
            case GL11.GL_ALWAYS -> DepthTestFunction.NO_DEPTH_TEST;
            case GL11.GL_EQUAL -> DepthTestFunction.EQUAL_DEPTH_TEST;
            case GL11.GL_LESS -> DepthTestFunction.LESS_DEPTH_TEST;
            case GL11.GL_GREATER -> DepthTestFunction.GREATER_DEPTH_TEST;
            default -> DepthTestFunction.LEQUAL_DEPTH_TEST;
        };
    }
    *///?}

    //? if >=26.2 {
    static BlendFactor source(int glValue) {
        return BlendFactor.valueOf(McGlState.Blend.ofGl(glValue).name());
    }

    static BlendFactor destination(int glValue) {
        return BlendFactor.valueOf(McGlState.Blend.ofGl(glValue).name());
    }
    //?} else {
    /*static SourceFactor source(int glValue) {
        return SourceFactor.valueOf(McGlState.Blend.ofGl(glValue).name());
    }

    static DestFactor destination(int glValue) {
        return DestFactor.valueOf(McGlState.Blend.ofGl(glValue).name());
    }
    *///?}
    //?}
}
