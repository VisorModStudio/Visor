package org.vmstudio.visor.loader.forge;

import net.minecraftforge.fml.loading.FMLLoader;
import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.MixinModLoader;

public class ForgeMixinModLoader implements MixinModLoader {

    @Override
    public boolean isModLoaded(@NotNull String id) {
        return FMLLoader.getLoadingModList().getModFileById(id) != null;
    }

    @Override
    public @NotNull LoaderType getType() {
        return LoaderType.FORGE;
    }
}
