package me.phoenixra.visor.api.client.gui.overlay.template;


import me.phoenixra.atumconfig.api.config.ConfigFile;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * Accessor to configs of overlays created from {@link VROverlayTemplate}
 */
public interface  ConfigOverlaysAccessor {

    /**
     * Reload overlays catalog for addon
     * @param addon the owner of catalog
     */
    void reload(@NotNull VisorAddon addon);

    /**
     * Get Config of overlay created from {@link VROverlayTemplate}
     *
     * @param id the overlay id
     * @return config file
     */
    @Nullable ConfigFile getConfig(@NotNull String id);

    /**
     * Get or Create Config of overlay created from {@link VROverlayTemplate}
     *
     * <p>If config not found, creates it</p>
     *
     * @param addon the owner of catalog
     * @param id the overlay id
     * @return config file
     */
    @NotNull ConfigFile getConfigOrCreate(@NotNull VisorAddon addon,
                                          @NotNull String id) throws IOException;
}
