package org.vmstudio.visor.loader.forge;


import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.core.common.addon.AddonManagerImpl;

@Mod(VisorAPI.MOD_ID)
public class VisorMod {

    public VisorMod(final FMLJavaModLoadingContext context){
        context.getModEventBus()
                .addListener(this::onLoadComplete);
    }

    private void onLoadComplete(final FMLLoadCompleteEvent event){
        event.enqueueWork(AddonManagerImpl::register);
    }


}
