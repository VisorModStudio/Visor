package me.phoenixra.visor.core.mixin.client.gui;

import me.phoenixra.visor.core.client.VisorState;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class ScreenMixin extends AbstractContainerEventHandler implements Renderable {

    @Shadow public int width;
    @Shadow public int height;

    @Inject(at = @At("HEAD"), method = "renderBackground", cancellable = true)
    public void visor$noBackground(GuiGraphics guiGraphics, CallbackInfo ci) {
        if (VisorState.getStateMode().isActive()) {
            ci.cancel();
        }
    }
}
