package me.phoenixra.visor.api.client.gui;

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
    int getScaledGuiWidth();

    /**
     * Get height that is used by all GUIs
     * with applied scaleFactor
     *
     * @return width integer
     */
    int getScaledGuiHeight();

    /**
     * Get aspect ratio
     *
     * @return aspect ratio
     */
    default float getAspectRatio(){
        return (float) getGuiHeight() / getGuiWidth();
    }

    /**
     * Get scaled aspect ratio
     *
     * @return scaled aspect ratio
     */
    default float getScaledAspectRatio(){
        return (float) getScaledGuiHeight() / getScaledGuiWidth();
    }

    /**
     *
     * @return scaleFactor integer
     */
    int getScaleFactor();
}
