package me.phoenixra.visor.core.client.render.decoration.decorators.mainmenu;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import me.phoenixra.visor.core.common.utils.ClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.lwjgl.opengl.GL11C;

public class VRMenuPanorama {
    private static final ResourceLocation cubeFront = new ResourceLocation("visor:textures/mainmenu/panorama_front.png");
    private static final ResourceLocation cubeBack = new ResourceLocation("visor:textures/mainmenu/panorama_back.png");
    private static final ResourceLocation cubeRight = new ResourceLocation("visor:textures/mainmenu/panorama_right.png");
    private static final ResourceLocation cubeLeft = new ResourceLocation("visor:textures/mainmenu/panorama_left.png");
    private static final ResourceLocation cubeUp = new ResourceLocation("visor:textures/mainmenu/panorama_up.png");
    private static final ResourceLocation cubeBelow = new ResourceLocation("visor:textures/mainmenu/panorama_below.png");

    private static final ResourceLocation mossTexture = new ResourceLocation("minecraft:textures/block/moss_block.png");

    public static void renderMenuPanorama(PoseStack poseStack) {
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.clear(GL11C.GL_COLOR_BUFFER_BIT | GL11C.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
        RenderSystem.depthMask(true);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1, 1, 1, 1);
        poseStack.pushPose();

        poseStack.translate(-50F, -50F, -50.0F);

        Matrix4f matrix = poseStack.last().pose();

        // Down face
        RenderSystem.setShaderTexture(0, cubeBelow);
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferbuilder.vertex(matrix, 0, 0, 0)
                .uv(0, 0).color(255, 255, 255, 255).endVertex();
        bufferbuilder.vertex(matrix, 0, 0, 100)
                .uv(0, 1).color(255, 255, 255, 255).endVertex();
        bufferbuilder.vertex(matrix, 100, 0, 100)
                .uv(1, 1).color(255, 255, 255, 255).endVertex();
        bufferbuilder.vertex(matrix, 100, 0, 0)
                .uv(1, 0).color(255, 255, 255, 255).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());

        // Up face
        RenderSystem.setShaderTexture(0, cubeUp);
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferbuilder.vertex(matrix, 0, 100, 100)
                .uv(0, 0).color(255, 255, 255, 255).endVertex();
        bufferbuilder.vertex(matrix, 0, 100, 0)
                .uv(0, 1).color(255, 255, 255, 255).endVertex();
        bufferbuilder.vertex(matrix, 100, 100, 0)
                .uv(1, 1).color(255, 255, 255, 255).endVertex();
        bufferbuilder.vertex(matrix, 100, 100, 100)
                .uv(1, 0).color(255, 255, 255, 255).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());

        // Left face
        RenderSystem.setShaderTexture(0, cubeLeft);
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferbuilder.vertex(matrix, 0, 0, 0)
                .uv(1, 1).color(255, 255, 255, 255).endVertex();
        bufferbuilder.vertex(matrix, 0, 100, 0)
                .uv(1, 0).color(255, 255, 255, 255).endVertex();
        bufferbuilder.vertex(matrix, 0, 100, 100)
                .uv(0, 0).color(255, 255, 255, 255).endVertex();
        bufferbuilder.vertex(matrix, 0, 0, 100)
                .uv(0, 1).color(255, 255, 255, 255).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());

        // Right face
        RenderSystem.setShaderTexture(0, cubeRight);
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferbuilder.vertex(matrix, 100, 0, 0)
                .uv(0, 1).color(255, 255, 255, 255).endVertex();
        bufferbuilder.vertex(matrix, 100, 0, 100)
                .uv(1, 1).color(255, 255, 255, 255).endVertex();
        bufferbuilder.vertex(matrix, 100, 100, 100)
                .uv(1, 0).color(255, 255, 255, 255).endVertex();
        bufferbuilder.vertex(matrix, 100, 100, 0)
                .uv(0, 0).color(255, 255, 255, 255).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());

        // Front face
        RenderSystem.setShaderTexture(0, cubeFront);
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferbuilder.vertex(matrix, 0, 0, 0)
                .uv(0, 1).color(255, 255, 255, 255).endVertex();
        bufferbuilder.vertex(matrix, 100, 0, 0)
                .uv(1, 1).color(255, 255, 255, 255).endVertex();
        bufferbuilder.vertex(matrix, 100, 100, 0)
                .uv(1, 0).color(255, 255, 255, 255).endVertex();
        bufferbuilder.vertex(matrix, 0, 100, 0)
                .uv(0, 0).color(255, 255, 255, 255).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());

        // Back face
        RenderSystem.setShaderTexture(0, cubeBack);
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferbuilder.vertex(matrix, 0, 0, 100)
                .uv(1, 1).color(255, 255, 255, 255).endVertex();
        bufferbuilder.vertex(matrix, 0, 100, 100)
                .uv(1, 0).color(255, 255, 255, 255).endVertex();
        bufferbuilder.vertex(matrix, 100, 100, 100)
                .uv(0, 0).color(255, 255, 255, 255).endVertex();
        bufferbuilder.vertex(matrix, 100, 0, 100)
                .uv(0, 1).color(255, 255, 255, 255).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());

        poseStack.popPose();

        // --- Block rendering for floor ---
        Vector2f area = ClientUtils.getPlayAreaSize();
        if (area == null) {
            area = new Vector2f(2, 2);
        }

        for (int i = 0; i < 2; i++) {
            float width = area.x + i * 2;
            float length = area.y + i * 2;

            poseStack.pushPose();
            RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);

            int r, g, b;

            RenderSystem.setShaderTexture(0, mossTexture);
            r = g = b = 128;

            Matrix4f matrix4f = poseStack.last().pose();
            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);
            poseStack.translate(-width / 2.0F, 0.0F, -length / 2.0F);

            final int repeat = 4; // texture wraps per meter

            bufferbuilder
                    .vertex(matrix4f, 0, 0.005f * -i, 0)
                    .uv(0, 0)
                    .color(r, g, b, 255)
                    .normal(0, 1, 0).endVertex();
            bufferbuilder
                    .vertex(matrix4f, 0, 0.005f * -i, length)
                    .uv(0, repeat * length)
                    .color(r, g, b, 255)
                    .normal(0, 1, 0).endVertex();
            bufferbuilder
                    .vertex(matrix4f, width, 0.005f * -i, length)
                    .uv(repeat * width, repeat * length)
                    .color(r, g, b, 255)
                    .normal(0, 1, 0).endVertex();
            bufferbuilder
                    .vertex(matrix4f, width, 0.005f * -i, 0)
                    .uv(repeat * width, 0)
                    .color(r, g, b, 255)
                    .normal(0, 1, 0).endVertex();

            //issue on this line
            BufferUploader.drawWithShader(bufferbuilder.end());

            poseStack.popPose();
        }
    }
}
