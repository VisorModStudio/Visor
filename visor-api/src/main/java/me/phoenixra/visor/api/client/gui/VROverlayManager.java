package me.phoenixra.visor.api.client.gui;

import me.phoenixra.visor.api.client.gui.overlay.VROverlay;
import me.phoenixra.visor.api.client.gui.overlay.template.ConfigOverlaysCatalog;
import me.phoenixra.visor.api.client.gui.overlay.template.options.OverlayOptionCategory;
import me.phoenixra.visor.api.client.gui.overlay.template.options.OverlayOptionsScreen;
import net.minecraft.client.gui.screens.Screen;
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
    ConfigOverlaysCatalog getOverlayCatalog();


    @NotNull OverlayOptionsScreen<?> getOptionsScreenFor(@NotNull OverlayOptionCategory category,
                                                         float mainMenuWidth,
                                                         float mainMenuHeight);
}
