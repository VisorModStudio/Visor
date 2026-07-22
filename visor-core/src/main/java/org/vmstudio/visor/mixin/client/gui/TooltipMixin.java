package org.vmstudio.visor.mixin.client.gui;


import org.vmstudio.visor.api.client.gui.overlays.framework.VROverlayScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;


@Mixin(AbstractWidget.class)
public class TooltipMixin {

    @Redirect(
            method = "updateTooltip",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/Minecraft;screen:Lnet/minecraft/client/gui/screens/Screen;"
            )
    )
    private Screen visor$redirectMinecraftScreen(Minecraft minecraftInstance) {
        VROverlayScreen overlay = VROverlayScreen.getRenderingOverlay();
        if (overlay != null) {
            return overlay;
        }
        return minecraftInstance.screen;
    }
}
