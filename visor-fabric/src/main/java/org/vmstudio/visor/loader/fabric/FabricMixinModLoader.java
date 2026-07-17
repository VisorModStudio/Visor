package org.vmstudio.visor.loader.fabric;

import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.MixinModLoader;

public class FabricMixinModLoader implements MixinModLoader {

    @Override
    public boolean isModLoaded(@NotNull String id) {
        return FabricLoader.getInstance().isModLoaded(id);
    }

    @Override
    public @NotNull LoaderType getType() {
        return LoaderType.FABRIC;
    }
}
