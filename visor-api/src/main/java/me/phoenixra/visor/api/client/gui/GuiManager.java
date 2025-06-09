package me.phoenixra.visor.api.client.gui;

import org.jetbrains.annotations.NotNull;

/**
 * This class handles GUIs rendering
 * and behaviour.
 */
public interface GuiManager {


    @NotNull
    OverlayManager getOverlayManager();
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
     *
     * @return scaleFactor integer
     */
    int getScaleFactor();
}
