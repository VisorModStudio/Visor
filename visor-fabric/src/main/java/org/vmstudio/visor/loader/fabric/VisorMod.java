package org.vmstudio.visor.loader.fabric;


import net.fabricmc.api.ModInitializer;

public class VisorMod implements ModInitializer {
    @Override
    public void onInitialize() {
        // Network payload types are registered per Visor channel in
        // FabricModLoader#registerNetworkChannel, when the addons register their channels
    }
}
