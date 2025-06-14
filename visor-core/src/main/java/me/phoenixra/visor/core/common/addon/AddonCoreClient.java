package me.phoenixra.visor.core.common.addon;

import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.core.client.ClientContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//Core Addon for client
public class AddonCoreClient implements VisorAddon {
    public AddonCoreClient(){
        ClientContext.coreAddon = this;
    }
    @Override
    public void onAddonLoad() {
        ClientContext.overlayManager
                .getOverlayCatalog()
                .reload();
    }




    @Override
    public @Nullable String getAddonPackagePath() {
        return "me.phoenixra.visor.core.client";
    }

    @Override
    public @NotNull String getAddonId() {
        return "core";
    }
}
