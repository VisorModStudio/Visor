// #!MC-VERSION:: 1.20.6+
package org.vmstudio.visor.mixin.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.WinScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vmstudio.visor.core.client.VisorState;

@Mixin(WinScreen.class)
public abstract class WinScreenMixin extends Screen {


    protected WinScreenMixin(Component component) {
        super(component);
    }


    @Inject(at = @At("RETURN"), method = "init")
    private void visor$addLeaveButton(CallbackInfo ci) {
        if (VisorState.get().isNotActive()) {
            return;
        }
        final int buttonWidth = 80;
        final int buttonHeight = 20;
        final int buttonMargin = 8;
        addRenderableWidget(Button.builder(
                        Component.translatable("visor.screen.win_screen.button.leave"),
                        button -> onClose()
                )
                .bounds(
                        this.width - buttonWidth - buttonMargin,
                        this.height - buttonHeight - buttonMargin,
                        buttonWidth,
                        buttonHeight
                )
                .build());
    }

    // 1.20.5 render() no longer sets the blend func itself


    //? if >=26.1 {
    @Inject(at = @At("HEAD"), method = "extractBackground", cancellable = true)
    //?} else {
    /*@Inject(at = @At("HEAD"), method = "renderBackground", cancellable = true)
    *///?}
    private void visor$noCreditsBackground(CallbackInfo ci) {
        if (VisorState.get().isActive()) {
            ci.cancel();
        }
    }
    //? if >=26.1 {
    @Inject(at = @At("HEAD"), method = "extractVignette", cancellable = true)
    private void visor$noVignette(GuiGraphicsExtractor guiGraphics, CallbackInfo ci) {
        if (VisorState.get().isActive()) {
            ci.cancel();
        }
    }
    //?} else {
    /*// 1.20.5 moved the vignette blit into its own method
    @Inject(at = @At("HEAD"), method = "renderVignette", cancellable = true)
    private void visor$noVignette(GuiGraphics guiGraphics, CallbackInfo ci) {
        if (VisorState.get().isActive()) {
            ci.cancel();
        }
    }
    *///?}
}
