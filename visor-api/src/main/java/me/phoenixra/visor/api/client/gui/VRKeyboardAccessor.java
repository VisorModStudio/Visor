package me.phoenixra.visor.api.client.gui;

import net.minecraft.client.gui.screens.Screen;
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
     * Set the visibility state for the keyboard
     *
     * @param flag true/false
     * @param attachedTo the screen, keyboard is attached to
     */
    void setVisible(boolean flag,
                    @Nullable Screen attachedTo);

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
