package org.vmstudio.visor.api.compatibility.mcversion.render;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
//? if >=1.21.5 {
import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.TextureFormat;
import org.vmstudio.visor.api.ModLoader;

import java.util.Map;
import java.util.WeakHashMap;
//?}
//? if >=1.21.6 {
import com.mojang.blaze3d.textures.GpuTextureView;
//?}

/**
 * Cross-mc-version facade over RenderTarget and the main render target
 */
public class McRenderTarget {
    private McRenderTarget() {
        throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
    }

    //? if >=1.21.5 {
    private static RenderTarget writeTarget;
    private static final Map<RenderTarget, Integer> CLEAR_COLORS = new WeakHashMap<>();
    //?}

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
        //? if >=1.21.9 {
        return target.width;
        //?} else {
        /*return target.viewWidth;
        *///?}
    }

    public static int viewHeight(RenderTarget target) {
        //? if >=1.21.9 {
        return target.height;
        //?} else {
        /*return target.viewHeight;
        *///?}
    }

    // ------- BINDING -------

    public static void bindWrite(RenderTarget target) {
        //? if >=1.21.5 {
        writeTarget = target;
        //?} else {
        /*target.bindWrite(true);
        *///?}
    }

    public static void unbindWrite(RenderTarget target) {
        //? if >=1.21.5 {
        if (writeTarget == target) {
            writeTarget = null;
        }
        //?} else {
        /*target.unbindWrite();
        *///?}
    }

    public static void bindRead(RenderTarget target) {
        //? if >=1.21.5 {
        //?} else {
        /*target.bindRead();
        *///?}
    }


    public static RenderTarget writeTarget() {
        //? if >=1.21.5 {
        return writeTarget == null ? mainTarget() : writeTarget;
        //?} else {
        /*return mainTarget();
        *///?}
    }

    public static void setFilterMode(RenderTarget target, boolean linear) {
        //? if >=1.21.5 {
        target.setFilterMode(linear ? FilterMode.LINEAR : FilterMode.NEAREST);
        //?} else {
        /*target.setFilterMode(linear ? GL11.GL_LINEAR : GL11.GL_NEAREST);
        *///?}
    }

    // ------- CLEARING -------

    public static void clear(RenderTarget target) {
        //? if >=1.21.5 {
        int color = CLEAR_COLORS.getOrDefault(target, 0);
        GpuTexture depth = target.getDepthTexture();
        if (depth == null) {
            RenderSystem.getDevice().createCommandEncoder()
                    .clearColorTexture(target.getColorTexture(), color);
        } else {
            RenderSystem.getDevice().createCommandEncoder()
                    .clearColorAndDepthTextures(target.getColorTexture(), color, depth, 1.0);
        }
        //?} elif >=1.21.2 {
        /*target.clear();
        *///?} else {
        /*target.clear(Minecraft.ON_OSX);
        *///?}
    }

    public static void setClearColor(RenderTarget target, float red, float green, float blue, float alpha) {
        //? if >=1.21.5 {
        CLEAR_COLORS.put(target, argb(red, green, blue, alpha));
        //?} else {
        /*target.setClearColor(red, green, blue, alpha);
        *///?}
    }

    // ------- TEXTURES -------

    public static int colorTextureId(RenderTarget target) {
        //? if >=1.21.5 {
        return glId(target.getColorTexture());
        //?} else {
        /*return target.getColorTextureId();
        *///?}
    }

    public static int depthTextureId(RenderTarget target) {
        //? if >=1.21.5 {
        return glId(target.getDepthTexture());
        //?} else {
        /*return target.getDepthTextureId();
        *///?}
    }

    //? if >=1.21.5 {
    static int argb(float red, float green, float blue, float alpha) {
        return (Math.round(alpha * 255.0F) << 24)
                | (Math.round(red * 255.0F) << 16)
                | (Math.round(green * 255.0F) << 8)
                | Math.round(blue * 255.0F);
    }

    static void clearWriteTarget(int mask, int color, double depth) {
        RenderTarget target = writeTarget();
        boolean clearColor = (mask & GL11.GL_COLOR_BUFFER_BIT) != 0;
        boolean clearDepth = (mask & GL11.GL_DEPTH_BUFFER_BIT) != 0 && target.getDepthTexture() != null;
        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        if (clearColor && clearDepth) {
            encoder.clearColorAndDepthTextures(target.getColorTexture(), color, target.getDepthTexture(), depth);
        } else if (clearColor) {
            encoder.clearColorTexture(target.getColorTexture(), color);
        } else if (clearDepth) {
            encoder.clearDepthTexture(target.getDepthTexture(), depth);
        }
    }


    public static GpuTexture adoptForeignTexture(String label, int width, int height, int glId) {
        return new ForeignTexture(label, width, height, glId);
    }

    //? if >=1.21.6 {
    // 1.21.6 samples and attaches views, a target created by Visor needs one per texture it swaps in
    public static GpuTextureView createTextureView(GpuTexture texture) {
        return RenderSystem.getDevice().createTextureView(texture);
    }
    //?}

    private static final class ForeignTexture extends GlTexture {
        private ForeignTexture(String label, int width, int height, int glId) {
            //? if >=1.21.6 {
            super(GpuTexture.USAGE_COPY_DST | GpuTexture.USAGE_COPY_SRC | GpuTexture.USAGE_TEXTURE_BINDING
                    | GpuTexture.USAGE_RENDER_ATTACHMENT, label, TextureFormat.RGBA8, width, height, 1, 1, glId);
            //?} else {
            /*super(label, TextureFormat.RGBA8, width, height, 1, glId);
            *///?}
        }

        @Override
        public void close() {

        }
    }

    private static int glId(GpuTexture texture) {
        return texture != null && ModLoader.get().unwrapTexture(texture) instanceof GlTexture glTexture
                ? glTexture.glId() : 0;
    }

    private static GlTexture glTexture(GpuTexture texture) {
        return (GlTexture) ModLoader.get().unwrapTexture(texture);
    }

    private static int framebufferOf(RenderTarget target) {
        GlDevice device = (GlDevice) ModLoader.get().unwrapDevice(RenderSystem.getDevice());
        GpuTexture depth = target.getDepthTexture();
        return glTexture(target.getColorTexture())
                .getFbo(device.directStateAccess(), depth == null ? null : glTexture(depth));
    }
    //?}

    // ------- FRAMEBUFFER -------

    public static int framebufferId(RenderTarget target) {
        //? if >=1.21.5 {
        return framebufferOf(target);
        //?} else {
        /*return target.frameBufferId;
        *///?}
    }

    // ------- BLIT -------


    public static void blit(RenderTarget source,
                            int srcX0, int srcY0, int srcX1, int srcY1,
                            RenderTarget destination,
                            int dstX0, int dstY0, int dstX1, int dstY1,
                            boolean linear) {
        //? if >=1.21.5 {
        McGlState.disableScissorTest();
        McGlState.bindFramebuffer(GL30.GL_READ_FRAMEBUFFER, framebufferOf(source));
        McGlState.bindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, framebufferOf(destination));
        //?} else {
        /*McGlState.bindFramebuffer(GL30.GL_READ_FRAMEBUFFER, source.frameBufferId);
        McGlState.bindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, destination.frameBufferId);
        *///?}
        McGlState.blitFramebuffer(
                srcX0, srcY0, srcX1, srcY1,
                dstX0, dstY0, dstX1, dstY1,
                GL30.GL_COLOR_BUFFER_BIT,
                linear ? GL30.GL_LINEAR : GL30.GL_NEAREST
        );
        McGlState.bindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
    }
}
