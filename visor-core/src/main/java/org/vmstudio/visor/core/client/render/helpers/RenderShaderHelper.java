package org.vmstudio.visor.core.client.render.helpers;

import org.vmstudio.visor.api.compatibility.mcversion.render.McGlState;
import org.vmstudio.visor.api.compatibility.mcversion.render.McShaderProgram;
import org.vmstudio.visor.api.compatibility.mcversion.render.McVertexBuilder;
import org.vmstudio.visor.api.compatibility.mcversion.render.McRenderTarget;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.vertex.*;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import com.mojang.blaze3d.PrimitiveTopology;

public class RenderShaderHelper {
    private RenderShaderHelper() {
        throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
    }


    public static void renderFullscreenQuad(McShaderProgram shader,
                                            RenderTarget source
    ) {
        // --- Setup ---
        McGlState.colorMask(true, true, true, false);
        McGlState.disableDepthTest();
        McGlState.depthMask(false);
        McGlState.disableBlend();
        shader.setSampler("Sampler0", source);
        shader.apply();

        // --- Render ---
        renderFullscreenQuad(shader.vertexFormat());


        // --- Restore ---
        shader.clear();
        McGlState.enableDepthTest();
        McGlState.enableBlend();
        McGlState.depthMask(true);
        McGlState.colorMask(true, true, true, true);
    }



    private static final double[] POS_X = { -1.0,  1.0, -1.0,  1.0 };
    private static final double[] POS_Y = { -1.0, -1.0,  1.0,  1.0 };
    private static final float[]  UV_U   = {  0.0F,  1.0F,  0.0F,  1.0F };
    private static final float[]  UV_V   = {  0.0F,  0.0F,  1.0F,  1.0F };

    public static void renderFullscreenQuad(VertexFormat format) {
        McVertexBuilder buf = McVertexBuilder.get();


        buf.begin(PrimitiveTopology.TRIANGLE_STRIP, format);
        for (int i = 0; i < 4; i++) {
            putFullscreenVertex(buf, format, i);
        }

        buf.drawNoShader();
    }

    public static void renderQuad(VertexFormat format,
                                  Matrix4f matrix,
                                  float x0,
                                  float y,
                                  float z0,
                                  float x1,
                                  float z1) {
        renderQuad(format, matrix, x0, y, z0, x1, z1, false);
    }

    public static void renderQuad(VertexFormat format,
                                  Matrix4f matrix,
                                  float x0,
                                  float y,
                                  float z0,
                                  float x1,
                                  float z1,
                                  boolean withShader) {
        McVertexBuilder buf = McVertexBuilder.get();
        buf.begin(PrimitiveTopology.QUADS, format);

        putQuadVertex(buf, format, matrix, x0, y, z0, 0.0F, 0.0F);
        putQuadVertex(buf, format, matrix, x1, y, z0, 1.0F, 0.0F);
        putQuadVertex(buf, format, matrix, x1, y, z1, 1.0F, 1.0F);
        putQuadVertex(buf, format, matrix, x0, y, z1, 0.0F, 1.0F);

        if (withShader) {
            buf.draw();
        } else {
            buf.drawNoShader();
        }
    }

    private static void putFullscreenVertex(McVertexBuilder buf, VertexFormat format, int index) {
        var vertex = buf.vertex(POS_X[index], POS_Y[index], 0.0);
        if (format == DefaultVertexFormat.POSITION_TEX) {
            vertex.uv(UV_U[index], UV_V[index]).endVertex();
        } else if (format == DefaultVertexFormat.POSITION_TEX_COLOR) {
            vertex.uv(UV_U[index], UV_V[index])
                    .color(255, 255, 255, 255)
                    .endVertex();
        } else {
            throw new IllegalArgumentException("fullscreen quad: unsupported vertex format " + format);
        }
    }

    private static void putQuadVertex(McVertexBuilder buf,
                                      VertexFormat format,
                                      Matrix4f matrix,
                                      float x,
                                      float y,
                                      float z,
                                      float u,
                                      float v) {
        var vertex = buf.vertex(matrix, x, y, z);
        if (format == DefaultVertexFormat.POSITION) {
            vertex.endVertex();
        } else if (format == DefaultVertexFormat.POSITION_TEX) {
            vertex.uv(u, v).endVertex();
        } else if (format == DefaultVertexFormat.POSITION_TEX_COLOR) {
            vertex.uv(u, v)
                    .color(255, 255, 255, 255)
                    .endVertex();
        } else {
            throw new IllegalArgumentException("textured quad: unsupported vertex format " + format);
        }
    }
}
