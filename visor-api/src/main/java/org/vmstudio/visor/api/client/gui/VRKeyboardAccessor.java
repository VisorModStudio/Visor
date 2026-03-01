package org.vmstudio.visor.api.client.gui;

import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Accessor to VR keyboard
 */
public interface VRKeyboardAccessor {

    /**
     * Reset keyboard pose
     */
    void resetPose();

    /**
     * Set the visibility state for the keyboard
     *
     * @param flag true/false
     */
    void setVisible(boolean flag);

    /**
     * Show and attach screen to the keyboard
     *
     * @param attachTo the screen that has to be attached to keyboard
     */
    void showKeyboard(@NotNull Screen attachTo);

    /**
     * The screen, keyboard is attached to
     *
     * @return screen or null
     */
    @Nullable
    Screen getAttachedTo();

    /**
     *
     * @return If keyboard is currently visible
     */
    boolean isVisible();
}
