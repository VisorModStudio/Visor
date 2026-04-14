package org.vmstudio.visor.core.client.render.helpers;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import me.phoenixra.atumvr.api.enums.EyeType;
import org.vmstudio.visor.api.client.gui.helpers.TexturesHelper;
import org.vmstudio.visor.api.client.render.VRRenderPass;
import org.vmstudio.visor.compatibility.immportals.ImmPortalsCompatHelper;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.render.VRRenderState;
import org.vmstudio.visor.core.client.render.VRRendererBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL11C;

public class VREffectsHelper {
    private VREffectsHelper() {
        throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
    }

    public record NearestOpaqueBlock(float distance, BlockState state, BlockPos position) {}



    public static void renderInBlockEffect() {
        // --- Prepare variables ---
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        // orthographic matrix
        Matrix4f mat = new Matrix4f();
        mat.m00(1.0F);
        mat.m11(1.0F);
        mat.m22(-1.0F);
        mat.m33(1.0F);
        mat.m32(-1.0F);

        // --- Setup ---
        RenderSystem.setShader(GameRenderer::getPositionShader);
        RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 1.0f);
        RenderSystem.depthFunc(GL11C.GL_ALWAYS);
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.disableCull();

        // --- Render ---
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        bufferbuilder.vertex(mat, -1.5F, -1.5F, 0.0F).endVertex();
        bufferbuilder.vertex(mat, 1.5F, -1.5F, 0.0F).endVertex();
        bufferbuilder.vertex(mat, 1.5F, 1.5F, 0.0F).endVertex();
        bufferbuilder.vertex(mat, -1.5F, 1.5F, 0.0F).endVertex();
        tesselator.end();

        // --- Restore ---
        RenderStateHelper.restoreAfterExternalRender();
    }


    private static boolean stencilEnabledByVisor;


    public static void drawEyeStencil() {
        stencilEnabledByVisor = GL11C.glIsEnabled(GL11C.GL_STENCIL_TEST);
        VRRenderPass renderPass = VRRenderState.getRenderPass();
        if (renderPass.isEye() && !ImmPortalsCompatHelper.isRenderingPortalWorld()) {
            doStencil(false);
        }
    }

    public static void disableStencilTest() {
        if (!stencilEnabledByVisor) {
            GL11C.glDisable(GL11C.GL_STENCIL_TEST);
        }
    }




    public static void doStencil(boolean inverse) {
        Minecraft mc = Minecraft.getInstance();
        RenderTarget rt = mc.getMainRenderTarget();

        // 1) backup shader + matrices
        RenderSystem.backupProjectionMatrix();
        RenderSystem.getModelViewStack().pushPose();

        try {
            enableStencilTest();
            configureStencilWrite(inverse);
            clearStencilAndDepth();

            setupMaskDrawState();
            applyOrthoProjection(rt, inverse);

            // draw hidden‐area triangles into the stencil
            VRRenderPass eye = VRRenderState.getRenderPass();
            float[] maskVerts = getStencilMask(eye);
            drawStencilMask(maskVerts);

        } finally {
            // 2) restore matrices
            RenderSystem.getModelViewStack().popPose();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.restoreProjectionMatrix();

            // 3) restore GL state for regular rendering
            restorePostStencilState();
        }
    }

    private static void enableStencilTest() {
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
        RenderSystem.stencilMask(0xFF);
    }

    private static void configureStencilWrite(boolean inverse) {
        if (inverse) {
            // clear stencil to 0xFF then write zero inside mask
            RenderSystem.clearStencil(0xFF);
            RenderSystem.clearDepth(0);
            RenderSystem.stencilFunc(GL11.GL_ALWAYS, 0, 0xFF);
            RenderSystem.colorMask(false, false, false, true);
        } else {
            // clear stencil to 0 then write one inside mask
            RenderSystem.clearStencil(0);
            RenderSystem.clearDepth(1);
            RenderSystem.stencilFunc(GL11.GL_ALWAYS, 0xFF, 0xFF);
            RenderSystem.colorMask(true, true, true, true);
        }
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

    private static void applyOrthoProjection(RenderTarget rt, boolean inverse) {

        Matrix4f ortho = new Matrix4f()
                .setOrtho(0, rt.viewWidth, 0, rt.viewHeight, 0, 20f);
        RenderSystem.setProjectionMatrix(ortho, VertexSorting.ORTHOGRAPHIC_Z);

        if (inverse) {
            RenderSystem.getModelViewStack().translate(0, 0, -20);
        }
        RenderSystem.applyModelViewMatrix();
    }

    private static float[] getStencilMask(VRRenderPass eye) {
        if (eye != VRRenderPass.EYE_LEFT && eye != VRRenderPass.EYE_RIGHT) {
            return null;
        }
        VRRendererBase renderer = ClientContext.renderer;
        return (eye == VRRenderPass.EYE_LEFT)
                ? renderer.getHiddenAreaVertices(EyeType.LEFT)
                : renderer.getHiddenAreaVertices(EyeType.RIGHT);
    }

    private static void drawStencilMask(float[] verts) {
        if (verts == null || verts.length < 2) return;

        BufferBuilder buf = Tesselator.getInstance().getBuilder();
        buf.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION);

        // bind a simple 1×1 black texture so shader has "something"
        Minecraft.getInstance()
                .getTextureManager()
                .bindForSetup(TexturesHelper.getBlackTexture());

        float scale = ClientContext.renderer.renderScale;
        for (int i = 0; i < verts.length; i += 2) {
            buf
                    .vertex(verts[i] * scale, verts[i+1] * scale, 0f)
                    .endVertex();
        }

        RenderSystem.setShader(GameRenderer::getPositionShader);
        BufferUploader.drawWithShader(buf.end());
    }

    private static void restorePostStencilState() {
        // stencil: only pass where stencil != 255
        RenderSystem.stencilFunc(GL11.GL_NOTEQUAL, 255, 0xFF);
        RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        RenderSystem.stencilMask(0);
        RenderStateHelper.restoreAfterExternalRender(true);
    }
}
