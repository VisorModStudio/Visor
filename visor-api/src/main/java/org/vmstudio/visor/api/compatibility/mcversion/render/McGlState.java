package org.vmstudio.visor.api.compatibility.mcversion.render;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
//? if >=1.21.5 {
import com.mojang.blaze3d.opengl.GlStateManager;
//?} else {
/*import com.mojang.blaze3d.platform.GlStateManager;
*///?}

/**
 * Cross-mc-version facade over the immediate-mode GL state of the render engine
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

        public static Blend ofGl(int glValue) {
            for (Blend blend : values()) {
                if (blend.glValue == glValue) {
                    return blend;
                }
            }
            return ONE;
        }
    }

    //? if >=1.21.5 {
    // a 1.21.5 draw takes its state from the pipeline it runs with, so what Visor asks for is kept here too
    private static boolean blend;
    private static int blendSourceRgb = GL11.GL_ONE;
    private static int blendDestinationRgb = GL11.GL_ZERO;
    private static int blendSourceAlpha = GL11.GL_ONE;
    private static int blendDestinationAlpha = GL11.GL_ZERO;
    private static boolean depthTest;
    private static int depthFunction = GL11.GL_LESS;
    private static boolean depthWrite = true;
    private static boolean cull = true;
    private static boolean colorWrite = true;
    private static boolean alphaWrite = true;

    public record DrawState(boolean blend,
                            int blendSourceRgb, int blendDestinationRgb,
                            int blendSourceAlpha, int blendDestinationAlpha,
                            boolean depthTest, int depthFunction, boolean depthWrite,
                            boolean cull, boolean colorWrite, boolean alphaWrite) {
    }

    public static DrawState drawState() {
        return new DrawState(blend,
                blendSourceRgb, blendDestinationRgb, blendSourceAlpha, blendDestinationAlpha,
                depthTest, depthFunction, depthWrite, cull, colorWrite, alphaWrite);
    }
    //?}

    // ------- BLEND -------

    public static void enableBlend() {
        //? if >=1.21.5 {
        blend = true;
        GlStateManager._enableBlend();
        //?} else {
        /*RenderSystem.enableBlend();
        *///?}
    }

    public static void disableBlend() {
        //? if >=1.21.5 {
        blend = false;
        GlStateManager._disableBlend();
        //?} else {
        /*RenderSystem.disableBlend();
        *///?}
    }

    public static void defaultBlendFunc() {
        //? if >=1.21.5 {
        // what RenderSystem.defaultBlendFunc did before the engine moved blending into pipelines
        blendFuncSeparate(Blend.SRC_ALPHA, Blend.ONE_MINUS_SRC_ALPHA, Blend.ONE, Blend.ZERO);
        //?} else {
        /*RenderSystem.defaultBlendFunc();
        *///?}
    }

    public static void blendFunc(Blend source, Blend destination) {
        //? if >=1.21.5 {
        blendFuncSeparate(source, destination, source, destination);
        //?} else {
        /*RenderSystem.blendFunc(source.glValue, destination.glValue);
        *///?}
    }

    public static void blendFuncSeparate(Blend sourceRgb, Blend destinationRgb,
                                         Blend sourceAlpha, Blend destinationAlpha) {
        blendFuncSeparate(sourceRgb.glValue, destinationRgb.glValue,
                sourceAlpha.glValue, destinationAlpha.glValue);
    }

    public static void blendFuncSeparate(int sourceRgb, int destinationRgb,
                                         int sourceAlpha, int destinationAlpha) {
        //? if >=1.21.5 {
        blendSourceRgb = sourceRgb;
        blendDestinationRgb = destinationRgb;
        blendSourceAlpha = sourceAlpha;
        blendDestinationAlpha = destinationAlpha;
        GlStateManager._blendFuncSeparate(sourceRgb, destinationRgb, sourceAlpha, destinationAlpha);
        //?} else {
        /*RenderSystem.blendFuncSeparate(sourceRgb, destinationRgb, sourceAlpha, destinationAlpha);
        *///?}
    }

    public static void blendEquation(int mode) {
        //? if >=1.21.5 {
        GL14.glBlendEquation(mode);
        //?} else {
        /*RenderSystem.blendEquation(mode);
        *///?}
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
        //? if >=1.21.5 {
        depthTest = true;
        GlStateManager._enableDepthTest();
        //?} else {
        /*RenderSystem.enableDepthTest();
        *///?}
    }

    public static void disableDepthTest() {
        //? if >=1.21.5 {
        depthTest = false;
        GlStateManager._disableDepthTest();
        //?} else {
        /*RenderSystem.disableDepthTest();
        *///?}
    }

    public static void depthMask(boolean write) {
        //? if >=1.21.5 {
        depthWrite = write;
        GlStateManager._depthMask(write);
        //?} else {
        /*RenderSystem.depthMask(write);
        *///?}
    }

    public static void depthFunc(int function) {
        //? if >=1.21.5 {
        depthFunction = function;
        GlStateManager._depthFunc(function);
        //?} else {
        /*RenderSystem.depthFunc(function);
        *///?}
    }

    // ------- CULL -------

    public static void enableCull() {
        //? if >=1.21.5 {
        cull = true;
        GlStateManager._enableCull();
        //?} else {
        /*RenderSystem.enableCull();
        *///?}
    }

    public static void disableCull() {
        //? if >=1.21.5 {
        cull = false;
        GlStateManager._disableCull();
        //?} else {
        /*RenderSystem.disableCull();
        *///?}
    }

    // ------- COLOR -------

    public static void colorMask(boolean red, boolean green, boolean blue, boolean alpha) {
        //? if >=1.21.5 {
        colorWrite = red || green || blue;
        alphaWrite = alpha;
        GlStateManager._colorMask(red, green, blue, alpha);
        //?} else {
        /*RenderSystem.colorMask(red, green, blue, alpha);
        *///?}
    }

    // ------- STENCIL -------

    // the engine never cached stencil state and stopped exposing it in 1.21.5

    public static void stencilFunc(int function, int reference, int mask) {
        //? if >=1.21.5 {
        GL11.glStencilFunc(function, reference, mask);
        //?} else {
        /*RenderSystem.stencilFunc(function, reference, mask);
        *///?}
    }

    public static void stencilMask(int mask) {
        //? if >=1.21.5 {
        GL11.glStencilMask(mask);
        //?} else {
        /*RenderSystem.stencilMask(mask);
        *///?}
    }

    public static void stencilOp(int stencilFail, int depthFail, int pass) {
        //? if >=1.21.5 {
        GL11.glStencilOp(stencilFail, depthFail, pass);
        //?} else {
        /*RenderSystem.stencilOp(stencilFail, depthFail, pass);
        *///?}
    }

    // ------- CLEARING -------

    //? if >=1.21.5 {
    // a clear used to hit the bound framebuffer, which is now the write target McRenderTarget tracks
    private static int clearColor;
    private static double clearDepth = 1.0;
    //?}

    public static void clear(int mask) {
        //? if >=1.21.5 {
        McRenderTarget.clearWriteTarget(mask, clearColor, clearDepth);
        //?} elif >=1.21.2 {
        /*RenderSystem.clear(mask);
        *///?} else {
        /*RenderSystem.clear(mask, net.minecraft.client.Minecraft.ON_OSX);
        *///?}
    }

    public static void clearColor(float red, float green, float blue, float alpha) {
        //? if >=1.21.5 {
        clearColor = McRenderTarget.argb(red, green, blue, alpha);
        //?} else {
        /*RenderSystem.clearColor(red, green, blue, alpha);
        *///?}
    }

    public static void clearDepth(double depth) {
        //? if >=1.21.5 {
        clearDepth = depth;
        //?} else {
        /*RenderSystem.clearDepth(depth);
        *///?}
    }

    public static void clearStencil(int value) {
        //? if >=1.21.5 {
        GL11.glClearStencil(value);
        //?} else {
        /*RenderSystem.clearStencil(value);
        *///?}
    }

    // ------- VIEWPORT -------

    public static void viewport(int x, int y, int width, int height) {
        //? if >=1.21.5 {
        GlStateManager._viewport(x, y, width, height);
        //?} else {
        /*RenderSystem.viewport(x, y, width, height);
        *///?}
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
        //? if >=1.21.5 {
        RenderSystem.setShaderTexture(unit, net.minecraft.client.Minecraft.getInstance()
                .getTextureManager().getTexture(texture).getTexture());
        //?} elif >=1.21.2 {
        /*RenderSystem.setShaderTexture(unit, texture);
        *///?} else {
        /*net.minecraft.client.Minecraft.getInstance().getTextureManager().bindForSetup(texture);
        RenderSystem.setShaderTexture(unit, texture);
        *///?}
    }

    public static void setShaderTexture(int unit, RenderTarget target) {
        //? if >=1.21.5 {
        RenderSystem.setShaderTexture(unit, target.getColorTexture());
        //?} else {
        /*RenderSystem.setShaderTexture(unit, target.getColorTextureId());
        *///?}
    }

    public static void clearShaderTexture(int unit) {
        //? if >=1.21.5 {
        RenderSystem.setShaderTexture(unit, null);
        //?} else {
        /*RenderSystem.setShaderTexture(unit, 0);
        *///?}
    }

    public static int maxSupportedTextureSize() {
        //? if >=1.21.5 {
        return RenderSystem.getDevice().getMaxTextureSize();
        //?} else {
        /*return RenderSystem.maxSupportedTextureSize();
        *///?}
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
        //? if >=1.21.5 {
        RenderSystem.assertOnRenderThread();
        //?} else {
        /*RenderSystem.assertOnRenderThreadOrInit();
        *///?}
    }
}
