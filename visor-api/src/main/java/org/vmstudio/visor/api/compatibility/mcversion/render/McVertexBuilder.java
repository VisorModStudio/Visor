package org.vmstudio.visor.api.compatibility.mcversion.render;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.joml.Matrix4f;
//? if >=1.21.9 {
import org.joml.Vector3f;
//?}
//? if >=1.21.5 {
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.MeshData;

import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.function.Consumer;
//?} else {
/*import com.mojang.blaze3d.vertex.BufferUploader;
*///?}
//? if >=1.21.6 {
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.ScissorState;
//?}

/**
 * Cross-mc-version immediate-mode vertex building
 */
public final class McVertexBuilder {

    private static final McVertexBuilder INSTANCE = new McVertexBuilder();

    private BufferBuilder builder;

    private McVertexBuilder() {
    }

    // ------- STABLE API -------

    public static McVertexBuilder get() {
        //? if <1.21 {
        /*INSTANCE.builder = Tesselator.getInstance().getBuilder();
        *///?}
        return INSTANCE;
    }

    public McVertexBuilder begin(VertexFormat.Mode mode, VertexFormat format) {
        //? if >=1.21 {
        this.builder = Tesselator.getInstance().begin(mode, format);
        //?} else {
        /*this.builder.begin(mode, format);
        *///?}
        return this;
    }

    public McVertexBuilder vertex(Matrix4f matrix, float x, float y, float z) {
        //? if >=1.21 {
        builder.addVertex(matrix, x, y, z);
        //?} else {
        /*builder.vertex(matrix, x, y, z);
        *///?}
        return this;
    }

    public McVertexBuilder vertex(float x, float y, float z) {
        //? if >=1.21 {
        builder.addVertex(x, y, z);
        //?} else {
        /*builder.vertex(x, y, z);
        *///?}
        return this;
    }

    public McVertexBuilder vertex(double x, double y, double z) {
        return vertex((float) x, (float) y, (float) z);
    }

    public McVertexBuilder uv(float u, float v) {
        //? if >=1.21 {
        builder.setUv(u, v);
        //?} else {
        /*builder.uv(u, v);
        *///?}
        return this;
    }

    public McVertexBuilder color(int red, int green, int blue, int alpha) {
        //? if >=1.21 {
        builder.setColor(red, green, blue, alpha);
        //?} else {
        /*builder.color(red, green, blue, alpha);
        *///?}
        return this;
    }

    public McVertexBuilder color(float red, float green, float blue, float alpha) {
        //? if >=1.21 {
        builder.setColor(red, green, blue, alpha);
        //?} else {
        /*builder.color(red, green, blue, alpha);
        *///?}
        return this;
    }

    public McVertexBuilder normal(float x, float y, float z) {
        //? if >=1.21 {
        builder.setNormal(x, y, z);
        //?} else {
        /*builder.normal(x, y, z);
        *///?}
        return this;
    }

    public McVertexBuilder uv2(int packedLight) {
        //? if >=1.21 {
        builder.setLight(packedLight);
        //?} else {
        /*builder.uv2(packedLight);
        *///?}
        return this;
    }

    public McVertexBuilder overlayCoords(int packedOverlay) {
        //? if >=1.21 {
        builder.setOverlay(packedOverlay);
        //?} else {
        /*builder.overlayCoords(packedOverlay);
        *///?}
        return this;
    }

    public McVertexBuilder endVertex() {
        //? if <1.21 {
        /*builder.endVertex();
        *///?}
        return this;
    }

    public void draw() {
        //? if >=1.21.5 {
        drawActive(builder.buildOrThrow(), false);
        //?} elif >=1.21 {
        /*BufferUploader.drawWithShader(builder.buildOrThrow());
        *///?} else {
        /*BufferUploader.drawWithShader(builder.end());
        *///?}
    }

    public void drawNoShader() {
        //? if >=1.21.5 {
        drawActive(builder.buildOrThrow(), true);
        //?} elif >=1.21 {
        /*BufferUploader.draw(builder.buildOrThrow());
        *///?} else {
        /*BufferUploader.draw(builder.end());
        *///?}
    }

    //? if >=1.21.5 {
    private static void drawActive(MeshData mesh, boolean ownMatrices) {
        RenderTarget target = McRenderTarget.writeTarget();
        McShaderProgram program = McShaderProgram.active();
        if (program != null) {
            program.draw(mesh, target, ownMatrices);
        } else {
            MeshData.DrawState state = mesh.drawState();
            drawPass(mesh, target, McShaders.pipeline(state.format(), state.mode()), McShaders::applyUniforms);
        }
    }

    // buffer writes must happen before the pass opens, so `uniforms` may only bind what it prepared earlier
    static void drawPass(MeshData mesh, RenderTarget target, RenderPipeline pipeline,
                         Consumer<RenderPass> uniforms) {
        try (mesh) {
            MeshData.DrawState state = mesh.drawState();
            GpuBuffer vertices = state.format().uploadImmediateVertexBuffer(mesh.vertexBuffer());
            RenderSystem.AutoStorageIndexBuffer indices = RenderSystem.getSequentialBuffer(state.mode());
            GpuBuffer indexBuffer = indices.getBuffer(state.indexCount());
            //? if >=1.21.6 {
            //? if >=1.21.9 {
            GpuBufferSlice transforms = RenderSystem.getDynamicUniforms().writeTransform(
                    RenderSystem.getModelViewMatrix(), McGlState.shaderColor(), new Vector3f(),
                    RenderSystem.getTextureMatrix(), RenderSystem.getShaderLineWidth());
            //?} else {
            /*GpuBufferSlice transforms = RenderSystem.getDynamicUniforms().writeTransform(
                    RenderSystem.getModelViewMatrix(), McGlState.shaderColor(), RenderSystem.getModelOffset(),
                    RenderSystem.getTextureMatrix(), RenderSystem.getShaderLineWidth());
            *///?}
            try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                    () -> "visor immediate draw",
                    target.getColorTextureView(), OptionalInt.empty(),
                    target.getDepthTextureView(), OptionalDouble.empty())) {
                pass.setPipeline(pipeline);
                ScissorState scissor = RenderSystem.getScissorStateForRenderTypeDraws();
                if (scissor.enabled()) {
                    pass.enableScissor(scissor.x(), scissor.y(), scissor.width(), scissor.height());
                }
                RenderSystem.bindDefaultUniforms(pass);
                pass.setUniform("DynamicTransforms", transforms);
                uniforms.accept(pass);
                pass.setVertexBuffer(0, vertices);
                pass.setIndexBuffer(indexBuffer, indices.type());
                pass.drawIndexed(0, 0, state.indexCount(), 1);
            }
            //?} else {
            /*try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                    target.getColorTexture(), OptionalInt.empty(),
                    target.getDepthTexture(), OptionalDouble.empty())) {
                pass.setPipeline(pipeline);
                if (RenderSystem.SCISSOR_STATE.isEnabled()) {
                    pass.enableScissor(RenderSystem.SCISSOR_STATE);
                }
                uniforms.accept(pass);
                pass.setVertexBuffer(0, vertices);

                pass.setIndexBuffer(indexBuffer, indices.type());
                pass.drawIndexed(0, state.indexCount());
            }
            *///?}
        }
    }
    //?}

    public BufferBuilder handle() {
        return builder;
    }
}
