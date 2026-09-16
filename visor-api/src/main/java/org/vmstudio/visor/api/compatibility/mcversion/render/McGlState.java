package org.vmstudio.visor.api.compatibility.mcversion.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
//? if <1.21.2 {
/*import net.minecraft.client.Minecraft;
*///?}

/**
 * Cross-mc-version facade over the GL state of the render engine
 */
public class McGlState {
    private McGlState() {
        throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
    }

    public enum Blend {
        ZERO(GL11.GL_ZERO),
        ONE(GL11.GL_ONE),
        SRC_COLOR(GL11.GL_SRC_COLOR),
        ONE_MINUS_SRC_COLOR(GL11.GL_ONE_MINUS_SRC_COLOR),
        DST_COLOR(GL11.GL_DST_COLOR),
        ONE_MINUS_DST_COLOR(GL11.GL_ONE_MINUS_DST_COLOR),
        SRC_ALPHA(GL11.GL_SRC_ALPHA),
        ONE_MINUS_SRC_ALPHA(GL11.GL_ONE_MINUS_SRC_ALPHA),
        DST_ALPHA(GL11.GL_DST_ALPHA),
        ONE_MINUS_DST_ALPHA(GL11.GL_ONE_MINUS_DST_ALPHA);

        public final int glValue;

        Blend(int glValue) {
            this.glValue = glValue;
        }
    }

    // ------- BLEND -------

    public static void enableBlend() {
        RenderSystem.enableBlend();
    }

    public static void disableBlend() {
        RenderSystem.disableBlend();
    }

    public static void defaultBlendFunc() {
        RenderSystem.defaultBlendFunc();
    }

    public static void blendFunc(Blend source, Blend destination) {
        RenderSystem.blendFunc(source.glValue, destination.glValue);
    }

    public static void blendFuncSeparate(Blend sourceRgb, Blend destinationRgb,
                                         Blend sourceAlpha, Blend destinationAlpha) {
        blendFuncSeparate(sourceRgb.glValue, destinationRgb.glValue,
                sourceAlpha.glValue, destinationAlpha.glValue);
    }

    public static void blendFuncSeparate(int sourceRgb, int destinationRgb,
                                         int sourceAlpha, int destinationAlpha) {
        RenderSystem.blendFuncSeparate(sourceRgb, destinationRgb, sourceAlpha, destinationAlpha);
    }

    public static void blendEquation(int mode) {
        RenderSystem.blendEquation(mode);
    }

    // the engine caches the blend state, reading it back is how a pass restores what it found
    public static int blendSourceRgb() {
        return GlStateManager.BLEND.srcRgb;
    }

    public static int blendDestinationRgb() {
        return GlStateManager.BLEND.dstRgb;
    }

    public static int blendSourceAlpha() {
        return GlStateManager.BLEND.srcAlpha;
    }

    public static int blendDestinationAlpha() {
        return GlStateManager.BLEND.dstAlpha;
    }

    // ------- DEPTH -------

    public static void enableDepthTest() {
        RenderSystem.enableDepthTest();
    }

    public static void disableDepthTest() {
        RenderSystem.disableDepthTest();
    }

    public static void depthMask(boolean write) {
        RenderSystem.depthMask(write);
    }

    public static void depthFunc(int function) {
        RenderSystem.depthFunc(function);
    }

    // ------- CULL -------

    public static void enableCull() {
        RenderSystem.enableCull();
    }

    public static void disableCull() {
        RenderSystem.disableCull();
    }

    // ------- COLOR -------

    public static void colorMask(boolean red, boolean green, boolean blue, boolean alpha) {
        RenderSystem.colorMask(red, green, blue, alpha);
    }

    // ------- STENCIL -------

    public static void stencilFunc(int function, int reference, int mask) {
        RenderSystem.stencilFunc(function, reference, mask);
    }

    public static void stencilMask(int mask) {
        RenderSystem.stencilMask(mask);
    }

    public static void stencilOp(int stencilFail, int depthFail, int pass) {
        RenderSystem.stencilOp(stencilFail, depthFail, pass);
    }

    // ------- CLEARING -------

    public static void clear(int mask) {
        //? if >=1.21.2 {
        RenderSystem.clear(mask);
        //?} else {
        /*RenderSystem.clear(mask, Minecraft.ON_OSX);
        *///?}
    }

    public static void clearColor(float red, float green, float blue, float alpha) {
        RenderSystem.clearColor(red, green, blue, alpha);
    }

    public static void clearDepth(double depth) {
        RenderSystem.clearDepth(depth);
    }

    public static void clearStencil(int value) {
        RenderSystem.clearStencil(value);
    }

    // ------- VIEWPORT -------

    public static void viewport(int x, int y, int width, int height) {
        RenderSystem.viewport(x, y, width, height);
    }

    // ------- SHADER STATE -------

    public static void setShaderColor(float red, float green, float blue, float alpha) {
        RenderSystem.setShaderColor(red, green, blue, alpha);
    }

    public static void setShaderLights(Vector3f light0, Vector3f light1) {
        RenderSystem.setShaderLights(light0, light1);
    }

    public static void setShaderGameTime(long tickTime, float partialTick) {
        RenderSystem.setShaderGameTime(tickTime, partialTick);
    }

    // ------- TEXTURES -------

    public static void setShaderTexture(int unit, ResourceLocation texture) {
        //? if <1.21.2 {
        /*Minecraft.getInstance().getTextureManager().bindForSetup(texture);
        *///?}
        RenderSystem.setShaderTexture(unit, texture);
    }

    public static void setShaderTexture(int unit, int textureId) {
        RenderSystem.setShaderTexture(unit, textureId);
    }

    public static int maxSupportedTextureSize() {
        return RenderSystem.maxSupportedTextureSize();
    }

    // ------- FRAME BUFFERS -------

    public static int genFramebuffer() {
        return GlStateManager.glGenFramebuffers();
    }

    public static void bindFramebuffer(int target, int framebufferId) {
        GlStateManager._glBindFramebuffer(target, framebufferId);
    }

    public static void blitFramebuffer(int srcX0, int srcY0, int srcX1, int srcY1,
                                       int dstX0, int dstY0, int dstX1, int dstY1,
                                       int mask, int filter) {
        GlStateManager._glBlitFrameBuffer(srcX0, srcY0, srcX1, srcY1,
                dstX0, dstY0, dstX1, dstY1, mask, filter);
    }

    // ------- THREAD -------

    public static void assertOnRenderThreadOrInit() {
        RenderSystem.assertOnRenderThreadOrInit();
    }
}
