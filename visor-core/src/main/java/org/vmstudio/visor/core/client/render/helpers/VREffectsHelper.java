package org.vmstudio.visor.core.client.render.helpers;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import me.phoenixra.atumvr.api.enums.EyeType;
import net.minecraft.client.renderer.ShaderInstance;
import org.vmstudio.visor.api.client.gui.helpers.TexturesHelper;
import org.vmstudio.visor.api.client.render.VRRenderPass;
import org.vmstudio.visor.compatibility.ShadersHelper;
import org.vmstudio.visor.compatibility.immportals.ImmPortalsCompatHelper;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.render.VRRenderState;
import org.vmstudio.visor.core.client.render.VRRendererBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL11C;
import org.vmstudio.visor.core.client.render.VRShaders;
import org.vmstudio.visor.core.client.render.shaders.VRShaderInBlockVignette;

public class VREffectsHelper {
    private VREffectsHelper() {
        throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
    }

    private static final float SCREEN_QUAD_EXTENT = 1.5F;
    private static final float[][] SCREEN_QUAD_CORNERS = {
            {-SCREEN_QUAD_EXTENT, -SCREEN_QUAD_EXTENT},
            { SCREEN_QUAD_EXTENT, -SCREEN_QUAD_EXTENT},
            { SCREEN_QUAD_EXTENT,  SCREEN_QUAD_EXTENT},
            {-SCREEN_QUAD_EXTENT,  SCREEN_QUAD_EXTENT}
    };

    private static Matrix4f fullscreenMatrix() {
        return new Matrix4f().m22(-1.0F).m32(-1.0F);
    }

    public static void renderInBlockEffect() {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        Matrix4f mat = fullscreenMatrix();

        RenderSystem.setShader(GameRenderer::getPositionShader);
        RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 1.0F);
        RenderSystem.depthFunc(GL11C.GL_ALWAYS);
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.disableCull();

        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        for (float[] corner : SCREEN_QUAD_CORNERS) {
            bufferbuilder.vertex(mat, corner[0], corner[1], 0.0F).endVertex();
        }
        tesselator.end();

        RenderStateHelper.restoreAfterExternalRender();
    }



    public static void renderInBlockVignette(float proximity) {
        if (proximity <= 0.0f) return;

        VRShaderInBlockVignette wrap = VRShaders.getInBlockVignette();
        if (wrap == null) return;
        wrap.prepare(proximity);
        ShaderInstance shader = wrap.getHandle();

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        Matrix4f mat = fullscreenMatrix();

        RenderSystem.setShader(() -> shader);
        RenderSystem.depthFunc(GL11C.GL_ALWAYS);
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();

        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        for (float[] corner : SCREEN_QUAD_CORNERS) {
            bufferbuilder.vertex(mat, corner[0], corner[1], 0.0F)
                    .uv(corner[0] * 0.5F + 0.5F, corner[1] * 0.5F + 0.5F)
                    .endVertex();
        }
        tesselator.end();

        RenderStateHelper.restoreAfterExternalRender();
    }


    private static final float MASK_DEPTH = 20F;

    private static boolean stencilWasAlreadyOn;


    public static void drawEyeStencil() {
        if (ShadersHelper.isShaderActive()) {
            return;
        }
        stencilWasAlreadyOn = GL11C.glIsEnabled(GL11C.GL_STENCIL_TEST);
        VRRenderPass renderPass = VRRenderState.getRenderPass();
        if (renderPass.isEye()
                && !ImmPortalsCompatHelper.isRenderingPortalWorld()
                && !ImmPortalsCompatHelper.dropEyeMask()) {
            doStencil(false);
        }
    }

    public static void disableStencilTest() {
        if (!stencilWasAlreadyOn) {
            GL11C.glDisable(GL11C.GL_STENCIL_TEST);
        }
    }




    public static void doStencil(boolean inverse) {
        Minecraft mc = Minecraft.getInstance();
        RenderTarget rt = mc.getMainRenderTarget();

        RenderSystem.backupProjectionMatrix();
        RenderSystem.getModelViewStack().pushPose();

        try {
            enableStencilTest();
            configureStencilWrite(inverse);
            clearStencilAndDepth();

            setupMaskDrawState();
            applyOrthoProjection(rt);

            float[] maskVerts = getStencilMask(VRRenderState.getRenderPass());
            drawStencilMask(maskVerts, inverse ? -MASK_DEPTH : 0F);

        } finally {
            RenderSystem.getModelViewStack().popPose();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.restoreProjectionMatrix();

            restorePostStencilState();
        }
    }

    private static void enableStencilTest() {
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
        RenderSystem.stencilMask(0xFF);
    }

    private static void configureStencilWrite(boolean inverse) {
        int clearValue = inverse ? 0xFF : 0;
        int stampValue = inverse ? 0 : 0xFF;
        boolean writeColor = !inverse;

        RenderSystem.clearStencil(clearValue);
        RenderSystem.clearDepth(inverse ? 0 : 1);
        RenderSystem.stencilFunc(GL11.GL_ALWAYS, stampValue, 0xFF);
        RenderSystem.colorMask(writeColor, writeColor, writeColor, true);
    }

    private static void clearStencilAndDepth() {
        RenderSystem.clear(
                GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT,
                false
        );
    }

    private static void setupMaskDrawState() {
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GL11.GL_ALWAYS);
        RenderSystem.disableCull();
        RenderSystem.setShaderColor(0f, 0f, 0f, 1f);
    }

    private static void applyOrthoProjection(RenderTarget rt) {
        Matrix4f ortho = new Matrix4f()
                .setOrtho(0, rt.viewWidth, 0, rt.viewHeight, 0, MASK_DEPTH);
        RenderSystem.setProjectionMatrix(ortho, VertexSorting.ORTHOGRAPHIC_Z);
        RenderSystem.applyModelViewMatrix();
    }

    private static float[] getStencilMask(VRRenderPass pass) {
        VRRendererBase renderer = ClientContext.renderer;
        if (pass == VRRenderPass.EYE_LEFT) {
            return renderer.getHiddenAreaVertices(EyeType.LEFT);
        }
        if (pass == VRRenderPass.EYE_RIGHT) {
            return renderer.getHiddenAreaVertices(EyeType.RIGHT);
        }
        return null;
    }

    private static void drawStencilMask(float[] verts, float depth) {
        if (verts == null || verts.length < 2) return;

        Minecraft.getInstance()
                .getTextureManager()
                .bindForSetup(TexturesHelper.getBlackTexture());
        RenderSystem.setShader(GameRenderer::getPositionShader);

        BufferBuilder buf = Tesselator.getInstance().getBuilder();
        buf.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION);

        float scale = ClientContext.renderer.renderScale;
        for (int i = 0; i + 1 < verts.length; i += 2) {
            buf.vertex(verts[i] * scale, verts[i + 1] * scale, depth).endVertex();
        }

        BufferUploader.drawWithShader(buf.end());
    }

    private static void restorePostStencilState() {
        RenderSystem.stencilMask(0);
        RenderSystem.stencilFunc(GL11.GL_NOTEQUAL, 0xFF, 0xFF);
        RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        RenderStateHelper.restoreAfterExternalRender(true);
    }
}
