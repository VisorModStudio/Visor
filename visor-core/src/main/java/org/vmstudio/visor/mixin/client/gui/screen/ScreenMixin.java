package org.vmstudio.visor.mixin.client.gui.screen;

import org.vmstudio.visor.core.client.VisorState;
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

    @Inject(method = {"renderBackground", "renderPanorama", "renderTransparentBackground"}, at = @At("HEAD"), cancellable = true)
    public void visor$noBackground(CallbackInfo ci) {
        if (VisorState.get().isActive()) {
            ci.cancel();
        }

    }

    @Inject(at = @At("HEAD"), method = "renderBlurredBackground", cancellable = true)
    private void visor$noBlurredBackground(float partialTick, CallbackInfo ci) {
        if (VisorState.get().isActive()) {
            ci.cancel();
        }
    }
}
