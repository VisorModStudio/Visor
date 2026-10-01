package org.vmstudio.visor.mixin.client.gui.screen;

import org.vmstudio.visor.core.client.VisorState;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class ScreenMixin extends AbstractContainerEventHandler implements Renderable {

    @Shadow public int width;
    @Shadow public int height;

    //? if >=26.1 {
    @Inject(at = @At("HEAD"), method = "extractBackground", cancellable = true)
    //?} else {
    /*@Inject(at = @At("HEAD"), method = "renderBackground", cancellable = true)
    *///?}
    public void visor$noBackground(CallbackInfo ci) {
        if((Object)this instanceof CreateWorldScreen){
            return;
        }

        if (VisorState.get().isActive()) {
            ci.cancel();
        }

    }

    //? if >=26.1 {
    @Inject(method = {"extractTransparentBackground", "extractPanorama", "extractBlurredBackground"},
            at = @At("HEAD"), cancellable = true)
    public void visor$noScreenBackdrop(CallbackInfo ci) {
        if (VisorState.get().isActive()) {
            ci.cancel();
        }
    }
    //?} elif >=1.20.5 {
    /*@Inject(method = {"renderTransparentBackground", "renderPanorama", "renderBlurredBackground"},
            at = @At("HEAD"), cancellable = true)
    public void visor$noScreenBackdrop(CallbackInfo ci) {
        if (VisorState.get().isActive()) {
            ci.cancel();
        }
    }
    *///?}
}
