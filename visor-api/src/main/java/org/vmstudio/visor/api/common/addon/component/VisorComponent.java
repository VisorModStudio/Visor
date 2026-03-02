package org.vmstudio.visor.api.common.addon.component;

import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.jetbrains.annotations.NotNull;

/**
 * Base interface for addon-owned components.
 */
public interface VisorComponent {

    /**
     * If component is enabled
     *
     * @return true/false
     */
    boolean isEnabled();

    /**
     * Enables or disables the component.
     *
     * @param flag true/false
     */
    void setEnabled(boolean flag);

    /**
     * Get component ID
     * <p>
     *     ID value must comply with {@link ComponentIds}
     * </p>
     * @return ID
     */
    @NotNull
    String getId();

    /**
     * Get component owner
     *
     * @return addon
     */
    @NotNull
    VisorAddon getOwner();
}
