package org.vmstudio.visor.api.compatibility.mcversion.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

//? if >=1.20.3 && <1.20.5 {
/*import net.minecraft.client.gui.navigation.ScreenRectangle;
*///?}

/**
 * Cross-mc-version adapter for AbstractButton
 */
@Environment(EnvType.CLIENT)
public abstract class McButton extends AbstractButton {

    protected McButton(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }


    // ------- STABLE API -------

    /**
     * Positioner for the tooltip of this button, null keeps the vanilla one.
     */
    @Nullable
    protected ClientTooltipPositioner tooltipPositioner() {
        return null;
    }


    // ------- MC-VERSION SPECIFIC IMPLEMENTATION -------

    //? if >=1.20.3 {
    @Nullable
    private Tooltip tooltipSource;

    @Override
    public void setTooltip(@Nullable Tooltip tooltip) {
        if (tooltip == tooltipSource) {
            return;
        }
        tooltipSource = tooltip;
        ClientTooltipPositioner positioner = tooltipPositioner();
        super.setTooltip(tooltip == null || positioner == null
                ? tooltip
                : new PositionedTooltip(tooltip, positioner));
    }

    //? if >=1.21.6 {
    public Tooltip getTooltip() {
        return tooltipSource;
    }
    //?} else {
    /*@Override
    public Tooltip getTooltip() {
        return tooltipSource != null ? tooltipSource : super.getTooltip();
    }
    *///?}

    // 1.20.5 moved the positioner choice onto WidgetTooltipHolder, see TooltipMixins
    public static final class PositionedTooltip extends Tooltip {

        private final ClientTooltipPositioner positioner;

        private PositionedTooltip(Tooltip source, ClientTooltipPositioner positioner) {
            super(source.message, source.narration);
            this.positioner = positioner;
        }

        public ClientTooltipPositioner positioner() {
            return positioner;
        }

        //? if <1.20.5 {
        /*@Override
        protected ClientTooltipPositioner createTooltipPositioner(boolean hovering,
                                                                  boolean focused,
                                                                  ScreenRectangle rectangle) {
            return positioner;
        }
        *///?}
    }
    //?} else {
    /*@Override
    protected ClientTooltipPositioner createTooltipPositioner() {
        ClientTooltipPositioner positioner = tooltipPositioner();
        return positioner != null ? positioner : super.createTooltipPositioner();
    }
    *///?}

}
