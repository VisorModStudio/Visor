package org.vmstudio.visor.loader.forge;

import net.minecraftforge.fml.loading.FMLLoader;
import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.MixinModLoader;

public class ForgeMixinModLoader implements MixinModLoader {

    @Override
    public boolean isModLoaded(@NotNull String id) {
        //? if >=26.1 {
        return net.minecraftforge.fml.loading.LoadingModList.getModFileById(id) != null;
        //?} else {
        /*return FMLLoader.getLoadingModList().getModFileById(id) != null;
        *///?}
    }

    @Override
    public @NotNull LoaderType getType() {
        return LoaderType.FORGE;
    }
}
