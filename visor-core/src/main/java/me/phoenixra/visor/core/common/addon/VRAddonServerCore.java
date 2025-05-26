package me.phoenixra.visor.core.common.addon;


import me.phoenixra.visor.api.common.addon.VisorAddon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//Core Addon for dedicated server
public class VRAddonServerCore implements VisorAddon {
    @Override
    public void onAddonLoad() {

    }

    @Override
    public void onAddonRemove() {

    }


    @Override
    public @Nullable String getAddonPackagePath() {
        return "me.phoenixra.visor.core.server";
    }

    @Override
    public @NotNull String getAddonId() {
        return "core";
    }
}
