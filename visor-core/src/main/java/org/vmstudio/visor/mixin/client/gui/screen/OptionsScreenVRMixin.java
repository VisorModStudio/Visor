package org.vmstudio.visor.mixin.client.gui.screen;

import org.vmstudio.visor.core.client.gui.screens.settings.VRSettingsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(OptionsScreen.class)
public class OptionsScreenVRMixin extends Screen {
    protected OptionsScreenVRMixin(Component component) {
        super(component);
    }


    @ModifyArg(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/GridLayout$RowHelper;addChild(Lnet/minecraft/client/gui/layouts/LayoutElement;I)Lnet/minecraft/client/gui/layouts/LayoutElement;"))
    private int visor$vrSpacerSize(int layoutElement) {
        return 1;
    }

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/GridLayout$RowHelper;addChild(Lnet/minecraft/client/gui/layouts/LayoutElement;I)Lnet/minecraft/client/gui/layouts/LayoutElement;"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void visor$addVRSettingsButton(CallbackInfo ci,
                                          GridLayout gridLayout,
                                          GridLayout.RowHelper rowHelper) {
        rowHelper.addChild(new Button.Builder(Component.translatable("visor.options.main.button"), (p) ->
        {
            Minecraft.getInstance().options.save();
            Minecraft.getInstance().setScreen(new VRSettingsScreen(this));
        }).build());
    }




}
