package me.phoenixra.visor.core.common.addon;


import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//Core Addon for dedicated server
public class AddonCoreServer implements VisorAddon {
    @Override
    public void onAddonLoad() {

    }



    @Override
    public @Nullable String getAddonPackagePath() {
        return "me.phoenixra.visor.core.server";
    }

    @Override
    public @NotNull String getAddonId() {
        return "core";
    }

    @Override
    public String getModId() {
        return VisorAPI.MOD_ID;
    }
}
