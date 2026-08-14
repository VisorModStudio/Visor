package org.vmstudio.visor.mixin.client.gui.screen;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import org.vmstudio.visor.core.client.gui.screens.settings.VRSettingsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.GridLayout;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OptionsScreen.class)
public class OptionsScreenVRMixin extends Screen {
    protected OptionsScreenVRMixin(Component component) {
        super(component);
    }


    /**
     * 1.21.1: init only uses the single-arg RowHelper#addChild (the old
     * two-arg spacer call is gone). The VR settings button is inserted
     * right before the credits button — the last of the 10 addChild
     * calls (ordinal 9), verified against the decompiled source.
     */
    @Inject(method = "init", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/layouts/GridLayout$RowHelper;addChild(Lnet/minecraft/client/gui/layouts/LayoutElement;)Lnet/minecraft/client/gui/layouts/LayoutElement;",
            ordinal = 9))
    private void visor$addVRSettingsButton(CallbackInfo ci,
                                          @Local GridLayout.RowHelper rowHelper) {
        rowHelper.addChild(new Button.Builder(Component.translatable("visor.options.main.button"), (p) ->
        {
            Minecraft.getInstance().options.save();
            Minecraft.getInstance().setScreen(new VRSettingsScreen(this));
        }).build());
    }


}
