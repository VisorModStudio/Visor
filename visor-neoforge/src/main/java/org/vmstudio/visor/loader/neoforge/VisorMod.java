package org.vmstudio.visor.loader.neoforge;


import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.core.common.addon.AddonManagerImpl;

@Mod(VisorAPI.MOD_ID)
public class VisorMod {

    public VisorMod(final IEventBus modEventBus){

        modEventBus.addListener(NeoForgeModLoader::registerPayloads);
        modEventBus.addListener(this::onLoadComplete);
    }

    private void onLoadComplete(final FMLLoadCompleteEvent event){
        event.enqueueWork(AddonManagerImpl::register);
    }


}
