package me.phoenixra.atumvr.core.rendering;

import lombok.Getter;

import me.phoenixra.atumvr.api.rendering.VRTexture;
import org.lwjgl.opengl.GL30;

@Getter
public class OpenXRTexture implements VRTexture {
    protected final int textureId;
    protected int textureIndex;
    protected int frameBufferId;

    protected final int width;
    protected final int height;

    public OpenXRTexture(int width, int height, int textureId, int index){
        this.width = width;
        this.height = height;

        this.textureId = textureId;
        this.textureIndex = index;


    }

    public OpenXRTexture init(){
        frameBufferId = GL30.glGenFramebuffers();

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, this.frameBufferId);
        GL30.glFramebufferTextureLayer(
                GL30.GL_FRAMEBUFFER,
                GL30.GL_COLOR_ATTACHMENT0,
                textureId,
                0,
                textureIndex
        );

        checkStatus();

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        GL30.glBindTexture(3553, 0);

        return this;
    }

    private void checkStatus() {
        int i = GL30.glCheckFramebufferStatus(36160);
        if (i != 36053) {
            if (i == 36054) {
                throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT");
            } else if (i == 36055) {
                throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT");
            } else if (i == 36059) {
                throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER");
            } else if (i == 36060) {
                throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER");
            } else if (i == 36061) {
                throw new RuntimeException("GL_FRAMEBUFFER_UNSUPPORTED");
            } else if (i == 1285) {
                throw new RuntimeException("GL_OUT_OF_MEMORY");
            } else {
                throw new RuntimeException("glCheckFramebufferStatus returned unknown status:" + i);
            }
        }
    }

    @Override
    public void destroy(){
        GL30.glDeleteTextures(textureId);
        GL30.glDeleteFramebuffers(frameBufferId);
    }

}
