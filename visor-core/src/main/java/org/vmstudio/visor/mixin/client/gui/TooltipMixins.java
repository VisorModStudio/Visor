package org.vmstudio.visor.mixin.client.gui;


import org.vmstudio.visor.api.client.gui.overlays.framework.VROverlayScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
//? if >=1.20.5 {
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetTooltipHolder;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import org.spongepowered.asm.mixin.Shadow;
import org.vmstudio.visor.api.compatibility.mcversion.gui.McButton;
//?} elif >=1.20.3 {
/*import net.minecraft.client.gui.components.Tooltip;
*///?} else {
/*import net.minecraft.client.gui.components.AbstractWidget;
*///?}


public class TooltipMixins {

    /**
     * Attaches a tooltip to the overlay handling screen
     */
    //? if >=1.21.6 {
    // 1.21.6 hands the tooltip to the GuiGraphics being drawn, which already is the overlay's
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
    //?} elif >=1.20.5 {
    /*// 1.20.5 moved the attach point and the positioner choice onto WidgetTooltipHolder.
    @Mixin(WidgetTooltipHolder.class)
    public static class TooltipScreenMixin {

        @Shadow
        private Tooltip tooltip;

        @WrapOperation(
                method = "refreshTooltipForNextRenderPass",
                at = @At(
                        value = "FIELD",
                        target = "Lnet/minecraft/client/Minecraft;screen:Lnet/minecraft/client/gui/screens/Screen;"
                )
        )
        private Screen visor$redirectMinecraftScreen(Minecraft minecraftInstance, Operation<Screen> original) {
            VROverlayScreen overlay = VROverlayScreen.getRenderingOverlay();
            if (overlay != null) {
                return overlay;
            }
            return original.call(minecraftInstance);
        }

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
    *///?} elif >=1.20.3 {
    /*// 1.20.3 moved the attach point off AbstractWidget onto the tooltip itself.
    @Mixin(Tooltip.class)
    public static class TooltipScreenMixin {

        @WrapOperation(
                method = "refreshTooltipForNextRenderPass",
                at = @At(
                        value = "FIELD",
                        target = "Lnet/minecraft/client/Minecraft;screen:Lnet/minecraft/client/gui/screens/Screen;"
                )
        )
        private Screen visor$redirectMinecraftScreen(Minecraft minecraftInstance, Operation<Screen> original) {
            VROverlayScreen overlay = VROverlayScreen.getRenderingOverlay();
            if (overlay != null) {
                return overlay;
            }
            return original.call(minecraftInstance);
        }
    }
    *///?} else {
    /*@Mixin(AbstractWidget.class)
    public static class TooltipScreenMixin {

        @WrapOperation(
                method = "updateTooltip",
                at = @At(
                        value = "FIELD",
                        target = "Lnet/minecraft/client/Minecraft;screen:Lnet/minecraft/client/gui/screens/Screen;"
                )
        )
        private Screen visor$redirectMinecraftScreen(Minecraft minecraftInstance, Operation<Screen> original) {
            VROverlayScreen overlay = VROverlayScreen.getRenderingOverlay();
            if (overlay != null) {
                return overlay;
            }
            return original.call(minecraftInstance);
        }
    }
    *///?}
}
