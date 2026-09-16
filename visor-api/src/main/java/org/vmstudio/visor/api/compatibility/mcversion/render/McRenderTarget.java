package org.vmstudio.visor.api.compatibility.mcversion.render;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL30;

/**
 * Cross-mc-version facade over RenderTarget and the main render target
 */
public class McRenderTarget {
    private McRenderTarget() {
        throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
    }

    // ------- MAIN TARGET -------

    public static RenderTarget mainTarget() {
        return Minecraft.getInstance().getMainRenderTarget();
    }

    public static void setMainTarget(RenderTarget target) {
        Minecraft.getInstance().mainRenderTarget = target;
    }

    // ------- SIZE -------

    public static void resize(RenderTarget target, int width, int height) {
        //? if >=1.21.2 {
        target.resize(width, height);
        //?} else {
        /*target.resize(width, height, Minecraft.ON_OSX);
        *///?}
    }

    public static int viewWidth(RenderTarget target) {
        return target.viewWidth;
    }

    public static int viewHeight(RenderTarget target) {
        return target.viewHeight;
    }

    // ------- BINDING -------

    public static void bindWrite(RenderTarget target) {
        target.bindWrite(true);
    }

    public static void unbindWrite(RenderTarget target) {
        target.unbindWrite();
    }

    public static void bindRead(RenderTarget target) {
        target.bindRead();
    }

    // ------- CLEARING -------

    public static void clear(RenderTarget target) {
        //? if >=1.21.2 {
        target.clear();
        //?} else {
        /*target.clear(Minecraft.ON_OSX);
        *///?}
    }

    public static void setClearColor(RenderTarget target, float red, float green, float blue, float alpha) {
        target.setClearColor(red, green, blue, alpha);
    }

    // ------- TEXTURES -------

    public static int colorTextureId(RenderTarget target) {
        return target.getColorTextureId();
    }

    public static int depthTextureId(RenderTarget target) {
        return target.getDepthTextureId();
    }

    // ------- BLIT -------


    public static void blit(RenderTarget source,
                            int srcX0, int srcY0, int srcX1, int srcY1,
                            RenderTarget destination,
                            int dstX0, int dstY0, int dstX1, int dstY1,
                            boolean linear) {
        McGlState.bindFramebuffer(GL30.GL_READ_FRAMEBUFFER, source.frameBufferId);
        McGlState.bindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, destination.frameBufferId);
        McGlState.blitFramebuffer(
                srcX0, srcY0, srcX1, srcY1,
                dstX0, dstY0, dstX1, dstY1,
                GL30.GL_COLOR_BUFFER_BIT,
                linear ? GL30.GL_LINEAR : GL30.GL_NEAREST
        );
        McGlState.bindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
    }
}
