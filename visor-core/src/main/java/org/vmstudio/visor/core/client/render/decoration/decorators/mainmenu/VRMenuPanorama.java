package org.vmstudio.visor.core.client.render.decoration.decorators.mainmenu;

import org.vmstudio.visor.api.compatibility.mcversion.render.McGlState;
import org.vmstudio.visor.api.compatibility.mcversion.render.McShaders;
import org.vmstudio.visor.api.compatibility.mcversion.render.McVertexBuilder;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.lwjgl.opengl.GL11C;
import org.vmstudio.visor.api.client.settings.VRClientSettings;
import org.vmstudio.visor.api.compatibility.mcversion.McVersionUtils;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;


public class VRMenuPanorama {
    private static final float SIZE = 100.0f;
    private static final float HALF = SIZE * 0.5f;

    private static final Identifier cubeFront = McVersionUtils.newResourceLoc(VRClientSettings.getPanoramaFront());
    private static final Identifier cubeBack = McVersionUtils.newResourceLoc(VRClientSettings.getPanoramaBack());
    private static final Identifier cubeRight = McVersionUtils.newResourceLoc(VRClientSettings.getPanoramaRight());
    private static final Identifier cubeLeft = McVersionUtils.newResourceLoc(VRClientSettings.getPanoramaLeft());
    private static final Identifier cubeUp = McVersionUtils.newResourceLoc(VRClientSettings.getPanoramaUp());
    private static final Identifier cubeBelow = McVersionUtils.newResourceLoc(VRClientSettings.getPanoramaBelow());

    private record Face(Identifier texture, Vector3fc origin, Vector3fc across, Vector3fc down) {
    }

    private static final Vector3fc TEXTURE_DOWN = new Vector3f(0.0f, -1.0f, 0.0f);

    private static final Face[] FACES = {
            new Face(cubeFront, new Vector3f(0.0f, 1.0f, 0.0f), new Vector3f(1.0f, 0.0f, 0.0f), TEXTURE_DOWN),
            new Face(cubeRight, new Vector3f(1.0f, 1.0f, 0.0f), new Vector3f(0.0f, 0.0f, 1.0f), TEXTURE_DOWN),
            new Face(cubeBack, new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(-1.0f, 0.0f, 0.0f), TEXTURE_DOWN),
            new Face(cubeLeft, new Vector3f(0.0f, 1.0f, 1.0f), new Vector3f(0.0f, 0.0f, -1.0f), TEXTURE_DOWN),
            new Face(cubeUp, new Vector3f(0.0f, 1.0f, 1.0f), new Vector3f(1.0f, 0.0f, 0.0f), new Vector3f(0.0f, 0.0f, -1.0f)),
            new Face(cubeBelow, new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(1.0f, 0.0f, 0.0f), new Vector3f(0.0f, 0.0f, 1.0f))
    };

    private static final float[] CORNER_U = {0.0f, 0.0f, 1.0f, 1.0f};
    private static final float[] CORNER_V = {0.0f, 1.0f, 1.0f, 0.0f};

    public static void render(PoseStack poseStack) {
        McVertexBuilder bufferbuilder = McVertexBuilder.get();

        McShaders.use(McShaders.Core.POSITION_TEX_COLOR);
        McGlState.clear(GL11C.GL_COLOR_BUFFER_BIT | GL11C.GL_DEPTH_BUFFER_BIT);
        McGlState.depthMask(true);
        McGlState.enableBlend();
        McGlState.defaultBlendFunc();
        McGlState.setShaderColor(1, 1, 1, 1);

        poseStack.pushPose();
        poseStack.translate(-HALF, -HALF, -HALF);

        Matrix4f matrix = poseStack.last().pose();
        Vector3f corner = new Vector3f();

        for (Face face : FACES) {
            McGlState.setShaderTexture(0, face.texture());
            bufferbuilder.begin(PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

            for (int i = 0; i < CORNER_U.length; i++) {
                float u = CORNER_U[i];
                float v = CORNER_V[i];

                corner.set(face.origin())
                        .fma(u, face.across())
                        .fma(v, face.down())
                        .mul(SIZE);

                bufferbuilder.vertex(matrix, corner.x, corner.y, corner.z)
                        .uv(u, v).color(255, 255, 255, 255).endVertex();
            }

            bufferbuilder.draw();
        }

        poseStack.popPose();
    }
}
