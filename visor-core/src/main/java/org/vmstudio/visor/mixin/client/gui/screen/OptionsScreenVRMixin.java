package org.vmstudio.visor.mixin.client.gui.screen;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
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


    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/LinearLayout;addChild(Lnet/minecraft/client/gui/layouts/LayoutElement;Ljava/util/function/Consumer;)Lnet/minecraft/client/gui/layouts/LayoutElement;", ordinal = 0))
    private void visor$addSpacer(CallbackInfo ci, @Local(ordinal = 0) LinearLayout header) {
        header.addChild(new SpacerElement(-150, 4), header.newCellSettings());
    }

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/LinearLayout;addChild(Lnet/minecraft/client/gui/layouts/LayoutElement;)Lnet/minecraft/client/gui/layouts/LayoutElement;", ordinal = 2, shift = At.Shift.AFTER))
    private void visor$addVRSettings(CallbackInfo ci, @Local(ordinal = 0) LinearLayout header) {
        var button = new Button.Builder(Component.translatable("visor.options.main.button"),
                (p) -> {
                    Minecraft.getInstance().options.save();
                    Minecraft.getInstance().setScreen(new VRSettingsScreen(this));
                }).build();
        header.addChild(button, header.newCellSettings().paddingTop(-4));
    }

}
