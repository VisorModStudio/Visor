// #!MC-VERSION:: 1.20.6+
package org.vmstudio.visor.mixin.client.gui.screen;

import org.vmstudio.visor.core.client.gui.screens.settings.VRSettingsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
//? if >=1.21 {
import net.minecraft.client.gui.screens.options.OptionsScreen;
//?} else {
/*import net.minecraft.client.gui.screens.OptionsScreen;
*///?}
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.llamalad7.mixinextras.sugar.Local;
import org.vmstudio.visor.api.compatibility.mcversion.McVersionClientUtils;

@Mixin(OptionsScreen.class)
public class OptionsScreenVRMixin extends Screen {
    protected OptionsScreenVRMixin(Component component) {
        super(component);
    }

    @Unique
    private Button visor$vrSettingsButton() {
        return new Button.Builder(Component.translatable("visor.options.main.button"), (p) ->
        {
            Minecraft.getInstance().options.save();
            McVersionClientUtils.setScreen(new VRSettingsScreen(this));
        }).build();
    }

    @Inject(method = "init", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/layouts/LinearLayout;addChild(Lnet/minecraft/client/gui/layouts/LayoutElement;Ljava/util/function/Consumer;)Lnet/minecraft/client/gui/layouts/LayoutElement;",
            ordinal = 0))
    private void visor$addHeaderSpacer(CallbackInfo ci,
                                       @Local(ordinal = 0) LinearLayout header) {
        header.addChild(new SpacerElement(-150, 4), header.newCellSettings());
    }

    @Inject(method = "init", at = @At(value = "NEW", target = "net/minecraft/client/gui/layouts/GridLayout"))
    private void visor$addVRSettingsButton(CallbackInfo ci,
                                           @Local(ordinal = 0) LinearLayout header) {
        header.addChild(visor$vrSettingsButton(), header.newCellSettings().paddingTop(-4));
    }

}
