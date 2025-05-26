package me.phoenixra.visor.api.common.addon;

import org.jetbrains.annotations.NotNull;

public interface VRElement {

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
