package org.vmstudio.visor.core.client.render.helpers;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.CompiledShaderProgram;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public class RenderShaderHelper {
    private RenderShaderHelper() {
        throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
    }



    public static void renderFullscreenQuad(@NotNull CompiledShaderProgram shader,
                                            @NotNull VertexFormat format,
                                            @NotNull RenderTarget source
    ) {
        // --- Setup ---
        RenderSystem.colorMask(true, true, true, false);
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableBlend();
        shader.bindSampler("Sampler0", source.getColorTextureId());
        shader.apply();

        // --- Render ---
        renderFullscreenQuad(format);


        // --- Restore ---
        shader.clear();
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.colorMask(true, true, true, true);
    }



    private static final double[] POS_X = { -1.0,  1.0, -1.0,  1.0 };
    private static final double[] POS_Y = { -1.0, -1.0,  1.0,  1.0 };
    private static final float[]  UV_U   = {  0.0F,  1.0F,  0.0F,  1.0F };
    private static final float[]  UV_V   = {  0.0F,  0.0F,  1.0F,  1.0F };

    public static void renderFullscreenQuad(VertexFormat format) {
        BufferBuilder buf;


        buf = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLE_STRIP, format);
        for (int i = 0; i < 4; i++) {
            putFullscreenVertex(buf, format, i);
        }

        BufferUploader.draw(buf.buildOrThrow());
    }

    public static void renderQuad(VertexFormat format,
                                  Matrix4f matrix,
                                  float x0,
                                  float y,
                                  float z0,
                                  float x1,
                                  float z1) {
        BufferBuilder buf;
        buf = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, format);

        putQuadVertex(buf, format, matrix, x0, y, z0, 0.0F, 0.0F);
        putQuadVertex(buf, format, matrix, x1, y, z0, 1.0F, 0.0F);
        putQuadVertex(buf, format, matrix, x1, y, z1, 1.0F, 1.0F);
        putQuadVertex(buf, format, matrix, x0, y, z1, 0.0F, 1.0F);

        BufferUploader.draw(buf.buildOrThrow());
    }

    private static void putFullscreenVertex(BufferBuilder buf, VertexFormat format, int index) {
        var vertex = buf.addVertex((float) POS_X[index], (float) POS_Y[index], 0.0F);
        if (format == DefaultVertexFormat.POSITION_TEX) {
            vertex.setUv(UV_U[index], UV_V[index]);
        } else if (format == DefaultVertexFormat.POSITION_TEX_COLOR) {
            vertex.setUv(UV_U[index], UV_V[index])
                    .setColor(255, 255, 255, 255)
            ;
        } else {
            throw new IllegalArgumentException("Unexpected vertexx format " + format);
        }
    }

    private static void putQuadVertex(BufferBuilder buf,
                                      VertexFormat format,
                                      Matrix4f matrix,
                                      float x,
                                      float y,
                                      float z,
                                      float u,
                                      float v) {
        var vertex = buf.addVertex(matrix, x, y, z);
        if (format == DefaultVertexFormat.POSITION) {
            // position only: nothing else to write for this vertex
        } else if (format == DefaultVertexFormat.POSITION_TEX) {
            vertex.setUv(u, v);
        } else if (format == DefaultVertexFormat.POSITION_TEX_COLOR) {
            vertex.setUv(u, v)
                    .setColor(255, 255, 255, 255)
            ;
        } else {
            throw new IllegalArgumentException("Unexpected vertexx format " + format);
        }
    }
}
