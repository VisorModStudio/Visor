package org.vmstudio.visor.core.client.render.decoration.decorators.winscreen;

import org.vmstudio.visor.api.compatibility.mcversion.render.McRenderUtils;
import org.vmstudio.visor.api.compatibility.mcversion.render.McGlState;
import org.vmstudio.visor.api.compatibility.mcversion.render.McShaderProgram;
import org.vmstudio.visor.api.compatibility.mcversion.render.McVertexBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.blockentity.TheEndPortalRenderer;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11C;
import org.vmstudio.visor.core.client.render.VRRenderState;
import org.vmstudio.visor.core.client.render.VRShaders;
import org.vmstudio.visor.core.client.render.helpers.RenderPoseHelper;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;


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
        McShaderProgram shader = VRShaders.getEndPortal().getHandle();

        float previousGameTime = McGlState.shaderGameTime();
        McGlState.setShaderGameTime((long) portalTicks, portalTicks % 1.0f);

        shader.use();
        //? if >=1.20.5 {
        // 1.20.5 deleted ShaderInstance's IViewRotMat
        shader.setUniform("IViewRotMat", new Matrix3f(
                RenderPoseHelper.getViewRotation(VRRenderState.getRenderPass())).invert());
        //?}
        McGlState.setShaderTexture(0, TheEndPortalRenderer.END_SKY_LOCATION);
        McGlState.setShaderTexture(1, TheEndPortalRenderer.END_PORTAL_LOCATION);
        McGlState.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        McGlState.disableBlend();
        McGlState.disableCull();
        McGlState.enableDepthTest();
        McGlState.depthFunc(GL11C.GL_ALWAYS);
        McGlState.depthMask(true);

        poseStack.pushPose();
        try {
            McRenderUtils.rotate(poseStack, Axis.YP.rotation(driftRad));

            Matrix4f pose = poseStack.last().pose();
            McVertexBuilder bufferBuilder = McVertexBuilder.get();
            bufferBuilder.begin(PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION);
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
            bufferBuilder.draw();
        } finally {
            poseStack.popPose();
            //? if >=1.20.5 {
            // per-eye value on a shared shader, never leave it set
            shader.setUniform("IViewRotMat", new Matrix3f());
            //?}
            McGlState.setShaderGameTime(previousGameTime);
            McGlState.depthFunc(GL11C.GL_LEQUAL);
            McGlState.enableCull();
        }
    }
}
