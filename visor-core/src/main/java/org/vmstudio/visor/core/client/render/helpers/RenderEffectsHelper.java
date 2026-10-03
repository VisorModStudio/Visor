package org.vmstudio.visor.core.client.render.helpers;

import org.vmstudio.visor.api.compatibility.mcversion.render.McGlState;
import org.vmstudio.visor.api.compatibility.mcversion.render.McProjection;
import org.vmstudio.visor.api.compatibility.mcversion.render.McShaders;
import org.vmstudio.visor.api.compatibility.mcversion.render.McVertexBuilder;
import org.vmstudio.visor.api.compatibility.mcversion.render.McModelViewStack;
import org.vmstudio.visor.api.compatibility.mcversion.render.McRenderTarget;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import me.phoenixra.atumvr.api.enums.EyeType;
import org.vmstudio.visor.api.client.gui.helpers.TexturesHelper;
import org.vmstudio.visor.api.client.render.VRRenderPass;
import org.vmstudio.visor.compatibility.ShaderCompatHelper;
import org.vmstudio.visor.compatibility.immportals.ImmPortalsCompatHelper;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.render.VRRenderState;
import org.vmstudio.visor.core.client.render.VRRendererBase;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL11C;
import org.vmstudio.visor.core.client.render.VRShaders;
import org.vmstudio.visor.core.client.render.shaders.VRShaderInBlockVignette;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;

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
        McVertexBuilder bufferbuilder = McVertexBuilder.get();
        Matrix4f mat = fullscreenMatrix();

        McShaders.use(McShaders.Core.POSITION);
        McGlState.setShaderColor(0.0F, 0.0F, 0.0F, alpha);
        McGlState.depthFunc(GL11C.GL_ALWAYS);
        McGlState.depthMask(false);
        McGlState.enableBlend();
        McGlState.defaultBlendFunc();
        McGlState.disableCull();

        bufferbuilder.begin(PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION);
        for (float[] corner : SCREEN_QUAD_CORNERS) {
            bufferbuilder.vertex(mat, corner[0], corner[1], 0.0F).endVertex();
        }
        bufferbuilder.draw();

        RenderStateHelper.restoreAfterExternalRender();
    }



    public static void renderInBlockVignette(float proximity) {
        if (proximity <= 0.0f) return;

        VRShaderInBlockVignette wrap = VRShaders.getInBlockVignette();
        if (wrap == null) return;
        wrap.prepare(proximity);

        McVertexBuilder bufferbuilder = McVertexBuilder.get();
        Matrix4f mat = fullscreenMatrix();

        wrap.getHandle().use();
        McGlState.depthFunc(GL11C.GL_ALWAYS);
        McGlState.depthMask(false);
        McGlState.enableBlend();
        McGlState.defaultBlendFunc();
        McGlState.disableCull();

        bufferbuilder.begin(PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_TEX);
        for (float[] corner : SCREEN_QUAD_CORNERS) {
            bufferbuilder.vertex(mat, corner[0], corner[1], 0.0F)
                    .uv(corner[0] * 0.5F + 0.5F, corner[1] * 0.5F + 0.5F)
                    .endVertex();
        }
        bufferbuilder.draw();

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
        writeHiddenAreaMask(mask);
    }

    public static void releaseHiddenAreaMask() {
        if (maskEnabledStencil) {
            GL11C.glDisable(GL11C.GL_STENCIL_TEST);
            maskEnabledStencil = false;
        }
    }


    private static void writeHiddenAreaMask(float[] mask) {
        RenderTarget target = McRenderTarget.mainTarget();

        McProjection.State savedProjection = McProjection.save();
        McModelViewStack.push();

        try {
            beginMaskWrite();
            Matrix4f ortho = new Matrix4f()
                    .setOrtho(0, McRenderTarget.viewWidth(target), 0, McRenderTarget.viewHeight(target), 0, MASK_FAR_PLANE);
            McProjection.setOrthographic(ortho);
            McModelViewStack.identity();
            McModelViewStack.apply();

            drawMaskTriangles(mask);
        } finally {
            McModelViewStack.pop();
            McModelViewStack.apply();
            McProjection.restore(savedProjection);

            endMaskWrite();
        }
    }

    private static void beginMaskWrite() {
        //? if >=1.21.2 {
        McRenderTarget.bindWrite(McRenderTarget.mainTarget());
        McGlState.colorMask(true, true, true, true);
        McGlState.depthMask(true);
        McGlState.enableDepthTest();
        McGlState.depthFunc(GL11.GL_LEQUAL);
        //?} else {
        /*maskEnabledStencil = !GL11C.glIsEnabled(GL11C.GL_STENCIL_TEST);
        GL11.glEnable(GL11.GL_STENCIL_TEST);

        McGlState.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
        McGlState.stencilMask(0xFF);
        McGlState.stencilFunc(GL11.GL_ALWAYS, 0xFF, 0xFF);
        McGlState.clearStencil(0);
        McGlState.clearDepth(1);
        McGlState.colorMask(true, true, true, true);
        McGlState.clear(GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT);

        McGlState.depthMask(true);
        McGlState.enableDepthTest();
        McGlState.depthFunc(GL11.GL_ALWAYS);
        *///?}
        McGlState.disableCull();
        McGlState.setShaderColor(0f, 0f, 0f, 1f);
    }

    private static void endMaskWrite() {
        //? if >=1.21.2 {
        RenderStateHelper.restoreAfterExternalRender();
        //?} else {
        /*McGlState.stencilMask(0);
        McGlState.stencilFunc(GL11.GL_NOTEQUAL, 0xFF, 0xFF);
        McGlState.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        RenderStateHelper.restoreAfterExternalRender(true);
        *///?}
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
        McGlState.setShaderTexture(0, TexturesHelper.getBlackTexture());
        McShaders.use(McShaders.Core.POSITION);

        McVertexBuilder buf = McVertexBuilder.get();
        buf.begin(PrimitiveTopology.TRIANGLES, DefaultVertexFormat.POSITION);

        float scale = ClientContext.renderer.renderScale;
        for (int i = 0; i + 1 < verts.length; i += 2) {
            buf.vertex(verts[i] * scale, verts[i + 1] * scale, 0F).endVertex();
        }

        buf.draw();
    }
}
