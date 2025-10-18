package me.phoenixra.visor.api.client.gui.overlays;


import me.phoenixra.atumconfig.api.config.ConfigFile;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * Accessor to configs of an overlays
 */
public interface OverlayConfigsAccessor {

    /**
     * Reload overlays catalog for addon
     * @param addon the owner of catalog
     */
    void reload(@NotNull VisorAddon addon);

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
