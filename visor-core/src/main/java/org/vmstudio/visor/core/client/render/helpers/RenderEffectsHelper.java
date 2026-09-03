package org.vmstudio.visor.core.client.render.helpers;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import me.phoenixra.atumvr.api.enums.EyeType;
import net.minecraft.client.renderer.ShaderInstance;
import org.vmstudio.visor.api.client.gui.helpers.TexturesHelper;
import org.vmstudio.visor.api.client.render.VRRenderPass;
import org.vmstudio.visor.compatibility.ShaderCompatHelper;
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

public class RenderEffectsHelper {
    private RenderEffectsHelper() {
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
        renderInBlockEffect(1.0F);
    }


    public static void renderInBlockEffect(float alpha) {
        if (alpha <= 0.0F) {
            return;
        }
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        Matrix4f mat = fullscreenMatrix();

        RenderSystem.setShader(GameRenderer::getPositionShader);
        RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, alpha);
        RenderSystem.depthFunc(GL11C.GL_ALWAYS);
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
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


    private static final float MASK_FAR_PLANE = 20F;

    private static boolean maskEnabledStencil;


    public static void maskHiddenArea() {
        maskEnabledStencil = false;
        if (ShaderCompatHelper.isShaderActive()) {
            return;
        }
        if (!VRRenderState.getRenderPass().isEye()
                || ImmPortalsCompatHelper.isRenderingPortalWorld()
                || ImmPortalsCompatHelper.dropEyeMask()) {
            return;
        }
        float[] mask = hiddenAreaFor(VRRenderState.getRenderPass());
        if (mask == null || mask.length < 2) {
            return;
        }
        writeHiddenAreaStencil(mask);
    }

    public static void releaseHiddenAreaMask() {
        if (maskEnabledStencil) {
            GL11C.glDisable(GL11C.GL_STENCIL_TEST);
            maskEnabledStencil = false;
        }
    }


    private static void writeHiddenAreaStencil(float[] mask) {
        RenderTarget target = Minecraft.getInstance().getMainRenderTarget();

        RenderSystem.backupProjectionMatrix();
        RenderSystem.getModelViewStack().pushPose();

        try {
            beginStencilWrite();
            Matrix4f ortho = new Matrix4f()
                    .setOrtho(0, target.viewWidth, 0, target.viewHeight, 0, MASK_FAR_PLANE);
            RenderSystem.setProjectionMatrix(ortho, VertexSorting.ORTHOGRAPHIC_Z);
            RenderSystem.applyModelViewMatrix();

            drawMaskTriangles(mask);
        } finally {
            RenderSystem.getModelViewStack().popPose();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.restoreProjectionMatrix();

            endStencilWrite();
        }
    }

    private static void beginStencilWrite() {
        maskEnabledStencil = !GL11C.glIsEnabled(GL11C.GL_STENCIL_TEST);
        GL11.glEnable(GL11.GL_STENCIL_TEST);

        RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
        RenderSystem.stencilMask(0xFF);
        RenderSystem.stencilFunc(GL11.GL_ALWAYS, 0xFF, 0xFF);
        RenderSystem.clearStencil(0);
        RenderSystem.clearDepth(1);
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT, false);

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GL11.GL_ALWAYS);
        RenderSystem.disableCull();
        RenderSystem.setShaderColor(0f, 0f, 0f, 1f);
    }

    private static void endStencilWrite() {
        RenderSystem.stencilMask(0);
        RenderSystem.stencilFunc(GL11.GL_NOTEQUAL, 0xFF, 0xFF);
        RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        RenderStateHelper.restoreAfterExternalRender(true);
    }

    private static float[] hiddenAreaFor(VRRenderPass pass) {
        VRRendererBase renderer = ClientContext.renderer;
        if (pass == VRRenderPass.EYE_LEFT) {
            return renderer.getHiddenAreaVertices(EyeType.LEFT);
        }
        if (pass == VRRenderPass.EYE_RIGHT) {
            return renderer.getHiddenAreaVertices(EyeType.RIGHT);
        }
        return null;
    }

    private static void drawMaskTriangles(float[] verts) {
        Minecraft.getInstance()
                .getTextureManager()
                .bindForSetup(TexturesHelper.getBlackTexture());
        RenderSystem.setShader(GameRenderer::getPositionShader);

        BufferBuilder buf = Tesselator.getInstance().getBuilder();
        buf.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION);

        float scale = ClientContext.renderer.renderScale;
        for (int i = 0; i + 1 < verts.length; i += 2) {
            buf.vertex(verts[i] * scale, verts[i + 1] * scale, 0F).endVertex();
        }

        BufferUploader.drawWithShader(buf.end());
    }
}
