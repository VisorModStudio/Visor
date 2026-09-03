package org.vmstudio.visor.core.client.render.decoration.decorators.winscreen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.lwjgl.opengl.GL11C;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.compatibility.mcversion.McVersionUtils;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.player.pose.LocalPlayerPose;
import org.vmstudio.visor.core.client.render.VRRenderState;
import org.vmstudio.visor.core.client.render.helpers.RenderPoseHelper;
import org.vmstudio.visor.core.client.utils.ClientUtils;

public final class VREndFloor {
    private static final ResourceLocation TEXTURE =
            McVersionUtils.newResourceLoc("textures/block/obsidian.png");

    private static final float MARGIN = 1.0f;
    private static final float THICKNESS = 0.6f;

    private static final int TOP_SHADE = 255;
    private static final int SIDE_SHADE = 190;
    private static final int BOTTOM_SHADE = 140;

    private VREndFloor() {
    }

    public static void render(PoseStack poseStack) {
        LocalPlayerPose renderPose = ClientContext.localPlayer
                .getPoseData(PlayerPoseType.RENDER);
        var eye = RenderPoseHelper.getCameraPosition(
                VRRenderState.getRenderPass(),
                renderPose
        );
        var origin = renderPose.getOrigin();

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GL11C.GL_LEQUAL);
        RenderSystem.depthMask(true);

        poseStack.pushPose();
        poseStack.translate(
                origin.x() - eye.x(),
                origin.y() - eye.y(),
                origin.z() - eye.z()
        );
        poseStack.mulPose(Axis.YN.rotation(-renderPose.getRotationY()));
        float scale = renderPose.getWorldScale();
        poseStack.scale(scale, scale, scale);

        Vector2f area = ClientUtils.getPlayAreaSize();
        float x = area.x * 0.5f + MARGIN;
        float z = area.y * 0.5f + MARGIN;
        float y = -THICKNESS;

        Matrix4f pose = poseStack.last().pose();
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

        // top
        quad(bufferBuilder, pose, TOP_SHADE,
                -x, 0, -z, 0, 0,
                -x, 0, z, 0, 2 * z,
                x, 0, z, 2 * x, 2 * z,
                x, 0, -z, 2 * x, 0);
        // bottom
        quad(bufferBuilder, pose, BOTTOM_SHADE,
                -x, y, -z, 0, 0,
                x, y, -z, 2 * x, 0,
                x, y, z, 2 * x, 2 * z,
                -x, y, z, 0, 2 * z);
        // sides
        quad(bufferBuilder, pose, SIDE_SHADE,
                -x, 0, -z, 0, 0,
                x, 0, -z, 2 * x, 0,
                x, y, -z, 2 * x, THICKNESS,
                -x, y, -z, 0, THICKNESS);
        quad(bufferBuilder, pose, SIDE_SHADE,
                x, 0, z, 0, 0,
                -x, 0, z, 2 * x, 0,
                -x, y, z, 2 * x, THICKNESS,
                x, y, z, 0, THICKNESS);
        quad(bufferBuilder, pose, SIDE_SHADE,
                -x, 0, z, 0, 0,
                -x, 0, -z, 2 * z, 0,
                -x, y, -z, 2 * z, THICKNESS,
                -x, y, z, 0, THICKNESS);
        quad(bufferBuilder, pose, SIDE_SHADE,
                x, 0, -z, 0, 0,
                x, 0, z, 2 * z, 0,
                x, y, z, 2 * z, THICKNESS,
                x, y, -z, 0, THICKNESS);

        BufferUploader.drawWithShader(bufferBuilder.end());

        poseStack.popPose();
    }

    private static void quad(BufferBuilder bufferBuilder,
                             Matrix4f pose,
                             int shade,
                             float x0, float y0, float z0, float u0, float v0,
                             float x1, float y1, float z1, float u1, float v1,
                             float x2, float y2, float z2, float u2, float v2,
                             float x3, float y3, float z3, float u3, float v3) {
        bufferBuilder.vertex(pose, x0, y0, z0).uv(u0, v0).color(shade, shade, shade, 255).endVertex();
        bufferBuilder.vertex(pose, x1, y1, z1).uv(u1, v1).color(shade, shade, shade, 255).endVertex();
        bufferBuilder.vertex(pose, x2, y2, z2).uv(u2, v2).color(shade, shade, shade, 255).endVertex();
        bufferBuilder.vertex(pose, x3, y3, z3).uv(u3, v3).color(shade, shade, shade, 255).endVertex();
    }
}
