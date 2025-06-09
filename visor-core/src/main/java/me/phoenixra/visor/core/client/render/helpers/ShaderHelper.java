package me.phoenixra.visor.core.client.render.helpers;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.ShaderInstance;
import org.jetbrains.annotations.NotNull;

public class ShaderHelper {

    private ShaderHelper() {
        throw new UnsupportedOperationException("Utility class");
    }


    public static void renderFullscreenQuad(@NotNull ShaderInstance shader,
                                            @NotNull RenderTarget source
    ) {

        RenderSystem.colorMask(true, true, true, false);
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableBlend();

        shader.setSampler("Sampler0", source.getColorTextureId());
        shader.apply();

        drawQuad(DefaultVertexFormat.POSITION_TEX_COLOR);

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

    private static void drawQuad(VertexFormat format) {
        BufferBuilder buf = Tesselator.getInstance().getBuilder();
        buf.begin(VertexFormat.Mode.TRIANGLE_STRIP, format);

        boolean needColor = format == DefaultVertexFormat.POSITION_TEX_COLOR;

        for (int i = 0; i < 4; i++) {
            var v = buf
                    .vertex(POS_X[i], POS_Y[i], 0.0)
                    .uv(UV_U[i], UV_V[i]);
            if (needColor) {
                v.color(255, 255, 255, 255);
            }
            v.endVertex();
        }

        BufferUploader.draw(buf.end());
    }
}
