package org.vmstudio.visor.mixin.client.gui.screen;

import net.minecraft.client.gui.screens.PauseScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vmstudio.visor.core.client.VisorState;

@Mixin(PauseScreen.class)
public abstract class PauseScreenMixin {

    // 26.2 draws the menu background itself, without calling super method
    //? if >=26.2 {
    @Inject(at = @At("HEAD"), method = "extractBackground", cancellable = true)
    private void visor$noBackground(CallbackInfo ci) {
        if (VisorState.get().isActive()) {
            ci.cancel();
        }
    }
    //?}
}
