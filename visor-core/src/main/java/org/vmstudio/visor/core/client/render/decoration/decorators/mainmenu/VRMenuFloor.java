package org.vmstudio.visor.core.client.render.decoration.decorators.mainmenu;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.vmstudio.visor.api.client.settings.VRClientSettings;
import org.vmstudio.visor.core.client.utils.ClientUtils;

/**
 * Renders the play-area floor
 */
public final class VRMenuFloor {
    private static final ResourceLocation floorTexture =
            new ResourceLocation(VRClientSettings.getMainMenuFloor());

    private VRMenuFloor() {
    }

    public static void render(PoseStack poseStack) {
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        Vector2f area = ClientUtils.getPlayAreaSize();

        for (int i = 0; i < 2; i++) {
            float width = area.x + i * 2;
            float length = area.y + i * 2;

            poseStack.pushPose();
            RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);
            RenderSystem.setShaderTexture(0, floorTexture);

            int r = 128, g = 128, b = 128;

            Matrix4f matrix4f = poseStack.last().pose();
            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);
            poseStack.translate(-width / 2.0F, 0.0F, -length / 2.0F);

            final int repeat = 4;

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

            BufferUploader.drawWithShader(bufferbuilder.end());

            poseStack.popPose();
        }
    }
}
