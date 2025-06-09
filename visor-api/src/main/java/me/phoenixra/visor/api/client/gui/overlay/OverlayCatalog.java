package me.phoenixra.visor.api.client.gui.overlay;


import me.phoenixra.atumconfig.api.config.ConfigFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public interface OverlayCatalog {

    void reload();

    @Nullable ConfigFile getConfig(@NotNull String id);

    @NotNull ConfigFile getConfigOrCreate(@NotNull String id) throws IOException;
}
