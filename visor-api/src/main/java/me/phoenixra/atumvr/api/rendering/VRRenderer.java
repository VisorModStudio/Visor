package me.phoenixra.atumvr.api.rendering;

import me.phoenixra.atumvr.api.VRProvider;
import me.phoenixra.atumvr.api.enums.EyeType;
import org.jetbrains.annotations.NotNull;

public interface VRRenderer {

    void init() throws Throwable;

    void preRender(@NotNull IRenderContext context);
    void renderFrame(@NotNull IRenderContext context);

    void destroy();


    VRScene getCurrentScene();


    VRTexture getTextureRightEye();

    VRTexture getTextureLeftEye();


    int getResolutionWidth();

    int getResolutionHeight();


    long getWindowHandle();

    /**
     * The area for which the stencil has to be used.
     * It is not rendered on VR eye
     *
     * @param eyeType eye to get the hidden are for
     * @return vertices
     */
    float[] getHiddenAreaVertices(EyeType eyeType);


    @NotNull
    VRProvider getVrProvider();
}
