package me.phoenixra.atumvr.api.rendering;


public interface VRTexture {

    void destroy();

    int getTextureId();
    int getFrameBufferId();

    int getWidth();
    int getHeight();


}
