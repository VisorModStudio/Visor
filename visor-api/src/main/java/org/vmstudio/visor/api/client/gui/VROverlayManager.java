package org.vmstudio.visor.api.client.gui;

import org.vmstudio.visor.api.client.gui.overlays.VROverlay;
import org.vmstudio.visor.api.client.gui.overlays.options.OverlayOptionGroup;
import org.vmstudio.visor.api.client.gui.overlays.options.OptionsScreen;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * Manager of VR Overlays
 */
public interface VROverlayManager {

    /**
     * Get overlay with specified {@code id}
     *
     * @param id overlay id
     * @return the overlay instance
     */
    @Nullable
    VROverlay getOverlay(@NotNull String id);

    /**
     * Get overlay with specified {@code id} and {@code type}.<br>
     * If found overlay is not an instance of {@code type},
     * null is returned
     *
     * @param id overlay id
     * @param type overlay class instance
     *
     * @return the overlay instance
     */
    @Nullable
    default <T extends VROverlay> T getOverlay(@NotNull String id, Class<T> type){
        var overlay = getOverlay(id);
        if(type.isInstance(overlay)){
            return type.cast(overlay);
        }
        return null;
    }


    /**
     * Get keyboard accessor
     *
     * @return the keyboard accessor
     */
    @NotNull
    VRKeyboardAccessor getKeyboardAccessor();

    /**
     * Set keyboard accessor
     *
     * @param keyboardAccessor the keyboard accessor
     */
    void setKeyboardAccessor(@NotNull VRKeyboardAccessor keyboardAccessor);


    /**
     * Get Config overlays accessor
     *
     * @return the accessor
     */
    @NotNull
    OverlayConfigAccessor getOverlayConfigAccessor();




    @ApiStatus.Internal
    @NotNull OptionsScreen<?> getOptionsScreenFor(@NotNull OverlayOptionGroup<?> category);
}
