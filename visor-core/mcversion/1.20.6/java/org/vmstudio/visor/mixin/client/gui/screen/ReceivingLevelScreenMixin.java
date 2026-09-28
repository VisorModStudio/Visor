// #!MC-VERSION:: 1.20.6-1.21.8
package org.vmstudio.visor.mixin.client.gui.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vmstudio.visor.core.client.VisorState;

@Mixin(ReceivingLevelScreen.class)
public abstract class ReceivingLevelScreenMixin {

    @Inject(at = @At("HEAD"), method = "renderBackground", cancellable = true)
    public void visor$noBackground(GuiGraphics guiGraphics, int mouseX, int mouseY,
                                   float partialTick, CallbackInfo ci) {
        if (VisorState.get().isActive()) {
            ci.cancel();
        }
    }
}
