package me.phoenixra.visor.api.client.gui.overlay.template;


import me.phoenixra.atumconfig.api.config.ConfigFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * Accessor to configs of overlays created from {@link VROverlayTemplate}
 */
public interface ConfigOverlaysAccessor {

    /**
     * Reload config catalog
     */
    void reload();

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
     * @param id the overlay id
     * @return config file
     */
    @NotNull ConfigFile getConfigOrCreate(@NotNull String id) throws IOException;
}
