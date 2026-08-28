package org.vmstudio.visor.core.client.render.decoration.decorators.mainmenu;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.lwjgl.opengl.GL11C;
import org.vmstudio.visor.api.client.settings.VRClientSettings;
import org.vmstudio.visor.api.compatibility.mcversion.McVersionUtils;


public class VRMenuPanorama {
    private static final float SIZE = 100.0f;
    private static final float HALF = SIZE * 0.5f;

    private static final ResourceLocation cubeFront = McVersionUtils.newResourceLoc(VRClientSettings.getPanoramaFront());
    private static final ResourceLocation cubeBack = McVersionUtils.newResourceLoc(VRClientSettings.getPanoramaBack());
    private static final ResourceLocation cubeRight = McVersionUtils.newResourceLoc(VRClientSettings.getPanoramaRight());
    private static final ResourceLocation cubeLeft = McVersionUtils.newResourceLoc(VRClientSettings.getPanoramaLeft());
    private static final ResourceLocation cubeUp = McVersionUtils.newResourceLoc(VRClientSettings.getPanoramaUp());
    private static final ResourceLocation cubeBelow = McVersionUtils.newResourceLoc(VRClientSettings.getPanoramaBelow());

    private record Face(ResourceLocation texture, Vector3fc origin, Vector3fc across, Vector3fc down) {
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
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.clear(GL11C.GL_COLOR_BUFFER_BIT | GL11C.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
        RenderSystem.depthMask(true);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1, 1, 1, 1);

        poseStack.pushPose();
        poseStack.translate(-HALF, -HALF, -HALF);

        Matrix4f matrix = poseStack.last().pose();
        Vector3f corner = new Vector3f();

        for (Face face : FACES) {
            RenderSystem.setShaderTexture(0, face.texture());
            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

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

            BufferUploader.drawWithShader(bufferbuilder.end());
        }

        poseStack.popPose();
    }
}
