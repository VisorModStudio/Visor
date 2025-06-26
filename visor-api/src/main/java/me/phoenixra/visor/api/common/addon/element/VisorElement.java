package me.phoenixra.visor.api.common.addon.element;

import me.phoenixra.visor.api.common.addon.VisorAddon;
import org.jetbrains.annotations.NotNull;


public interface VisorElement {

    /**
     * If component is enabled
     *
     * @return true/false
     */
    boolean isEnabled();

    /**
     * Enable/Disable component
     *
     * @param flag true/false
     */
    void setEnabled(boolean flag);

    /**
     * Get component id
     *
     * @return id
     */
    @NotNull
    String getId();

    /**
     * Get component owner
     * @return addon
     */
    @NotNull
    VisorAddon getOwner();
}
