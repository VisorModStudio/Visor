package org.vmstudio.visor.core.client.provider.openxr.render;

import com.mojang.blaze3d.pipeline.RenderTarget;
import org.lwjgl.opengl.GL30;
import org.vmstudio.visor.api.compatibility.mcversion.render.McGlState;
import org.vmstudio.visor.api.compatibility.mcversion.render.McRenderTarget;

public class XrRenderTarget extends RenderTarget {

    public XrRenderTarget(int width, int height, int colorId, int index) {
        super(false);
        McGlState.assertOnRenderThreadOrInit();

        this.colorTextureId = colorId;

        McRenderTarget.resize(this, width, height);

    }

    //? if >=1.21.2 {
    @Override
    public void createBuffers(int width, int height) {
        attachEyeTexture(width, height);
        this.clear();
        this.unbindRead();
    }
    //?} else {
    /*@Override
    public void createBuffers(int width, int height, boolean getError) {
        attachEyeTexture(width, height);
        this.clear(getError);
        this.unbindRead();
    }
    *///?}

    private void attachEyeTexture(int width, int height) {
        McGlState.assertOnRenderThreadOrInit();
        int maxSize = McGlState.maxSupportedTextureSize();
        if (width > 0 && width <= maxSize && height > 0 && height <= maxSize) {
            this.viewWidth = width;
            this.viewHeight = height;
            this.width = width;
            this.height = height;
            this.frameBufferId = McGlState.genFramebuffer();


            McGlState.bindFramebuffer(36160, this.frameBufferId);
            //Binding our eye texture here
            GL30.glFramebufferTexture2D(
                    GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0,
                    GL30.GL_TEXTURE_2D,
                    colorTextureId,
                    0
            );


            this.checkStatus();
        } else {
            throw new IllegalArgumentException("Window " + width + "x" + height + " size out of bounds (max. size: " + maxSize + ")");
        }
    }
}
