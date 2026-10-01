package org.vmstudio.visor.loader.forge.mixin;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vmstudio.visor.core.client.VisorState;

@Mixin(Gui.class)
public abstract class ForgeSelectedItemNameMixin {

    @Inject(method = "renderSelectedItemName(Lnet/minecraft/client/gui/GuiGraphicsExtractor;I)V",
            at = @At("HEAD"), remap = false, cancellable = true)
    private void visor$noForgeSelectedItemName(GuiGraphicsExtractor guiGraphics, int yShift, CallbackInfo ci) {
        if (VisorState.get().isNotActive()) return;
        ci.cancel();
    }
}
