package me.phoenixra.visor.api.client.gui;

/**
 * This class handles GUIs rendering
 * and behaviour.
 */
public interface IVRGuiManager {


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
