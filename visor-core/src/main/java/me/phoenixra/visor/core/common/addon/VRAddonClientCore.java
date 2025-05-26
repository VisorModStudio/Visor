package me.phoenixra.visor.core.common.addon;

import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.core.client.ClientContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//Core Addon for client
public class VRAddonClientCore implements VisorAddon {
    public VRAddonClientCore(){
        ClientContext.coreAddon = this;
    }
    @Override
    public void onAddonLoad() {

    }

    @Override
    public void onAddonRemove() {

    }



    @Override
    public @Nullable String getAddonPackagePath() {
        return "me.phoenixra.visor.core";
    }

    @Override
    public @NotNull String getAddonId() {
        return "core";
    }
}
