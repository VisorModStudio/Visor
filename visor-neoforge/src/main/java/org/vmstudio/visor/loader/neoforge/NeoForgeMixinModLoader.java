package org.vmstudio.visor.loader.neoforge;

import net.neoforged.fml.loading.FMLLoader;
import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.MixinModLoader;

public class NeoForgeMixinModLoader implements MixinModLoader {

    @Override
    public boolean isModLoaded(@NotNull String id) {
        //? if >=1.21.9 {
        return FMLLoader.getCurrent().getLoadingModList().getModFileById(id) != null;
        //?} else {
        /*return FMLLoader.getLoadingModList().getModFileById(id) != null;
        *///?}
    }

    @Override
    public @NotNull LoaderType getType() {
        return LoaderType.NEOFORGE;
    }
}
