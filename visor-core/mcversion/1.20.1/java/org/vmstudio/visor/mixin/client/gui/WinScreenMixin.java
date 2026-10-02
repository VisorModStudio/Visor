// #!MC-VERSION:: 1.20.1-1.20.4
package org.vmstudio.visor.mixin.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.WinScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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

    @WrapOperation(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;blendFunc(Lcom/mojang/blaze3d/platform/GlStateManager$SourceFactor;Lcom/mojang/blaze3d/platform/GlStateManager$DestFactor;)V"), method = "render")
    private void visor$keepAlpha(GlStateManager.SourceFactor sourceFactor, GlStateManager.DestFactor destFactor, Operation<Void> original) {
        RenderSystem.blendFuncSeparate(sourceFactor, destFactor, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    }

    //? if >=1.20.2 {
    /*@Inject(at = @At("HEAD"), method = "renderBackground", cancellable = true)
    *///?} else {
    @Inject(at = @At("HEAD"), method = "renderBg", cancellable = true)
    //?}
    private void visor$noCreditsBackground(CallbackInfo ci) {
        if (VisorState.get().isActive()) {
            ci.cancel();
        }
    }
    @WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIFFIIII)V"), method = "render")
    private void visor$noVignette(GuiGraphics instance,
                                  ResourceLocation texture,
                                  int x, int y, int blitOffset,
                                  float uOffset, float vOffset,
                                  int uWidth, int vHeight,
                                  int textureWidth, int textureHeight,
                                  Operation<Void> original) {
        if (VisorState.get().isNotActive()) {
            original.call(instance, texture, x, y, blitOffset, uOffset, vOffset, uWidth, vHeight, textureWidth, textureHeight);
        }
    }
}
