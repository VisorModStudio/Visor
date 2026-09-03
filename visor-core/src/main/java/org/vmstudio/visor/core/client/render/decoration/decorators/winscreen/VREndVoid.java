package org.vmstudio.visor.core.client.render.decoration.decorators.winscreen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.blockentity.TheEndPortalRenderer;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11C;
import org.vmstudio.visor.core.client.render.VRShaders;
import org.vmstudio.visor.core.client.render.shaders.VRShaderEndPortal;
import org.vmstudio.visor.mixin.client.accessors.RenderSystemAccessor;


public final class VREndVoid {
    private static final float BOX = 100.0f;

    private static final float[][] CORNERS = {
            {-1, -1, -1}, {1, -1, -1}, {1, 1, -1}, {-1, 1, -1},
            {-1, -1, 1}, {1, -1, 1}, {1, 1, 1}, {-1, 1, 1}
    };

    private static final int[][] FACES = {
            {0, 1, 2, 3},
            {5, 4, 7, 6},
            {4, 0, 3, 7},
            {1, 5, 6, 2},
            {3, 2, 6, 7},
            {4, 5, 1, 0}
    };

    private VREndVoid() {
    }


    public static void render(PoseStack poseStack, float driftRad, float portalTicks) {
        ShaderInstance shader = VRShaders.getEndPortal().getHandle();

        float previousGameTime = RenderSystem.getShaderGameTime();
        RenderSystem.setShaderGameTime((long) portalTicks, portalTicks % 1.0f);

        RenderSystem.setShader(() -> shader);
        RenderSystem.setShaderTexture(0, TheEndPortalRenderer.END_SKY_LOCATION);
        RenderSystem.setShaderTexture(1, TheEndPortalRenderer.END_PORTAL_LOCATION);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GL11C.GL_ALWAYS);
        RenderSystem.depthMask(true);

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotation(driftRad));

        Matrix4f pose = poseStack.last().pose();
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        for (int[] face : FACES) {
            for (int corner : face) {
                float[] offset = CORNERS[corner];
                bufferBuilder.vertex(
                        pose,
                        offset[0] * BOX,
                        offset[1] * BOX,
                        offset[2] * BOX
                ).endVertex();
            }
        }
        BufferUploader.drawWithShader(bufferBuilder.end());

        poseStack.popPose();
        RenderSystemAccessor.setShaderGameTime(previousGameTime);
    }
}
