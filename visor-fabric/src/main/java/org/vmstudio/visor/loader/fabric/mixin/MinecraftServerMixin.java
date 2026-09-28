package org.vmstudio.visor.loader.fabric.mixin;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vmstudio.visor.api.ModLoader;
import org.vmstudio.visor.core.common.addon.AddonManagerImpl;
import org.vmstudio.visor.core.server.VisorServerImpl;

import java.io.IOException;
import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    @Inject(at = @At("TAIL"), method = "<init>")
    public void visor$registerAddons(CallbackInfo callbackInfo){
        if(ModLoader.get().isDedicatedServer()){
            AddonManagerImpl.register();
        }
    }


}
