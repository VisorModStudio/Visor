// #!MC-VERSION:: 1.21.8+
package org.vmstudio.visor.mixin.client.gui;


import org.vmstudio.visor.api.client.gui.overlays.framework.VROverlayScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetTooltipHolder;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import org.spongepowered.asm.mixin.Shadow;
import org.vmstudio.visor.api.compatibility.mcversion.gui.McButton;


public class TooltipMixins {

    /**
     * Attaches a tooltip to the overlay handling screen
     */
    // 1.21.6 hands the tooltip to the GuiGraphicsExtractor being drawn, which already is the overlay's
    @Mixin(WidgetTooltipHolder.class)
    public static class TooltipScreenMixin {

        @Shadow
        private Tooltip tooltip;

        // McButton wraps its tooltip to carry the positioner it wants
        @ModifyExpressionValue(
                method = "refreshTooltipForNextRenderPass",
                at = @At(
                        value = "INVOKE",
                        target = "Lnet/minecraft/client/gui/components/WidgetTooltipHolder;createTooltipPositioner(Lnet/minecraft/client/gui/navigation/ScreenRectangle;ZZ)Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipPositioner;"
                )
        )
        private ClientTooltipPositioner visor$widgetTooltipPositioner(ClientTooltipPositioner original) {
            return this.tooltip instanceof McButton.PositionedTooltip positioned
                    ? positioned.positioner()
                    : original;
        }
    }
}
