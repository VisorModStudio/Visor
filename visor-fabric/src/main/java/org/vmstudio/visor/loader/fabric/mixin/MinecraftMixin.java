package org.vmstudio.visor.loader.fabric.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vmstudio.visor.core.common.addon.AddonManagerImpl;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Inject(at = @At("TAIL"), method = "<init>")
    public void visor$registerAddons(GameConfig gameConfig,
                                     CallbackInfo ci){
        AddonManagerImpl.register();
    }
}
