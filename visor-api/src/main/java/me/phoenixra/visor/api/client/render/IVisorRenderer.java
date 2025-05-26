package me.phoenixra.visor.api.client.render;

import me.phoenixra.atumvr.api.rendering.VRRenderer;
import org.jetbrains.annotations.NotNull;

public interface IVisorRenderer extends VRRenderer {

    VREyeTexture getTextureRightEye();

    VREyeTexture getTextureLeftEye();

    void prepareReinit(@NotNull String cause);
    void prepareResize(@NotNull String cause);


}
