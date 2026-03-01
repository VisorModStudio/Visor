package org.vmstudio.visor.api.client.gui;


import me.phoenixra.atumconfig.api.config.ConfigFile;
import org.vmstudio.visor.api.client.gui.overlays.VROverlay;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * Accessor to overlay config
 */
public interface OverlayConfigAccessor {

    /**
     * Reload overlays catalog for an addon
     *
     * @param addon the owner of the catalog
     * @param builtIn is built in or custom overlay?
     */
    void reload(@NotNull VisorAddon addon, boolean builtIn);

    /**
     * Reload overlays catalog for an addon
     *
     * @param addon the owner of the catalog
     */
    default void reload(@NotNull VisorAddon addon){
        reload(addon, true);
        reload(addon, false);
    }

    /**
     * Load catalog defaults for an addon
     *
     * @param addon the owner of the catalog
     * @param builtIn is built in or custom overlay?
     */
    void loadDefaults(@NotNull VisorAddon addon, boolean builtIn);

    /**
     * Load catalog defaults for an addon
     * @param addon the owner of the catalog
     */
    default void loadDefaults(@NotNull VisorAddon addon){
        loadDefaults(addon, true);
        loadDefaults(addon, false);
    }

    /**
     * Get Config of an overlay
     *
     * @param id the overlay id
     * @return config file
     */
    @Nullable ConfigFile getConfig(@NotNull String id);

    /**
     * Get or Create Config of an overlay
     *
     * <p>If config not found, creates it</p>
     *
     * @return config file
     */
    @NotNull ConfigFile getConfigOrCreate(@NotNull VROverlay overlay) throws IOException;
}
