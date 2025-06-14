package me.phoenixra.visor.api.client.gui;

import me.phoenixra.visor.api.client.gui.overlay.VROverlay;
import me.phoenixra.visor.api.client.gui.overlay.OverlayCatalog;
import me.phoenixra.visor.api.client.gui.overlay.options.OverlayOptionCategory;
import me.phoenixra.visor.api.client.gui.overlay.options.OverlayOptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * This class handles VR Overlays rendering
 * and behaviour.
 */
public interface OverlayManager {

    /**
     * Get overlay with specified id
     * @param id overlay id
     * @return the overlay instance
     */
    @Nullable
    VROverlay getOverlay(@NotNull String id);

    @Nullable
    default <T extends VROverlay> T getOverlay(@NotNull String id, Class<T> overlayType){
        return (T)getOverlay(id);
    }

    /**
     * If overlay with specified ID is active
     * @param id overlay id
     * @return true/false
     */
    boolean isEnabled(@NotNull String id);

    /**
     *
     * @return If there is at least one overlay active
     */
    boolean isEnabledAtLeastOne();

    /**
     * Set the display state for the keyboard overlay
     * @param flag true/false
     */
    void showKeyboard(boolean flag);

    /**
     * Set the display state for the keyboard overlay
     * @param flag true/false
     * @param attachedTo screen to which keyboard is attached
     */
    boolean showKeyboard(boolean flag,
                         @Nullable Screen attachedTo);

    /**
     *  Screen to which keyboard is attached
     * @return screen or NULL if keyboard is not shown
     * or not attached to any screen
     */
    @Nullable
    Screen getKeyboardAttachedTo();

    /**
     *
     * @return If keyboard is currently displayed
     */
    boolean isShowingKeyboard();

    @NotNull
    OverlayCatalog getOverlayCatalog();


    @NotNull OverlayOptionsScreen<?> getOptionsScreenFor(@NotNull OverlayOptionCategory category,
                                                         float mainMenuWidth,
                                                         float mainMenuHeight);
}
