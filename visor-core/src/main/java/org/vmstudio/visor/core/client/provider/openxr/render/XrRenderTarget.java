package org.vmstudio.visor.core.client.provider.openxr.render;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL30;

public class XrRenderTarget extends RenderTarget {

    private final int colorTextureIndex;

    public XrRenderTarget(int width, int height, int colorId, int index) {
        super(false);
        RenderSystem.assertOnRenderThreadOrInit();

        this.colorTextureId = colorId;
        this.colorTextureIndex = index;

        this.resize(width, height, Minecraft.ON_OSX);

    }

    @Override
    public void createBuffers(int width, int height, boolean getError) {
        RenderSystem.assertOnRenderThreadOrInit();
        int maxSize = RenderSystem.maxSupportedTextureSize();
        if (width > 0 && width <= maxSize && height > 0 && height <= maxSize) {
            this.viewWidth = width;
            this.viewHeight = height;
            this.width = width;
            this.height = height;
            this.frameBufferId = GlStateManager.glGenFramebuffers();


            GlStateManager._glBindFramebuffer(36160, this.frameBufferId);
            //Binding our eye texture here
            GL30.glFramebufferTextureLayer(
                    GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0,
                    colorTextureId,
                    0,
                    colorTextureIndex
            );


            this.checkStatus();
            this.clear(getError);
            this.unbindRead();
        } else {
            throw new IllegalArgumentException("Window " + width + "x" + height + " size out of bounds (max. size: " + maxSize + ")");
        }
    }
}
