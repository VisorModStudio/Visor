package org.vmstudio.visor.api.client.gui;

import org.jetbrains.annotations.NotNull;

/**
 * Manages GUI for VR
 */
public interface VRGuiManager {


    /**
     * Get VR Overlay Manager
     *
     * @return VROverlayManager instance
     */
    @NotNull
    VROverlayManager getOverlayManager();

    /**
     * Get VR Cursor Handler
     *
     * @return VRCursorHandler instance
     */
    @NotNull
    VRCursorHandler getCursorHandler();

    /**
     * Get width that is used by all GUIs
     *
     * @return width integer
     */
    int getGuiWidth();

    /**
     * Get height that is used by all GUIs
     *
     * @return width integer
     */
    int getGuiHeight();

    /**
     * Get width that is used by all GUIs
     * with applied scaleFactor
     *
     * @return width integer
     */
    int getGuiScaledWidth();

    /**
     * Get height that is used by all GUIs
     * with applied scaleFactor
     *
     * @return width integer
     */
    int getGuiScaledHeight();

    /**
     *
     * @return scaleFactor integer
     */
    int getScaleFactor();

    /**
     * Calculate gui scale from given parameters and return the result.
     *
     * @param scaleIn the initial scale
     * @param guiWidth the gui width
     * @param guiHeight the gui height
     * @return gui scale
     */
    int calculateScale(int scaleIn,
                       int guiWidth,
                       int guiHeight);
}
