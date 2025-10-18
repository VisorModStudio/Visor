package me.phoenixra.visor.api.client.render;

import me.phoenixra.visor.api.client.gui.overlays.framework.VROverlayScreen;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public interface VRRenderer extends me.phoenixra.atumvr.api.rendering.VRRenderer {

    VREyeTexture getTextureRightEye();

    VREyeTexture getTextureLeftEye();

    void prepareReinit(@NotNull String cause);
    void prepareResize(@NotNull String cause);


    @ApiStatus.Internal
    void updateOverlayTarget(@NotNull VROverlayScreen overlayScreen);

}
