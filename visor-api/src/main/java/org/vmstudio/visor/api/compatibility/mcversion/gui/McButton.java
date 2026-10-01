package org.vmstudio.visor.api.compatibility.mcversion.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
//? if >=1.21.9 {
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
//?}

//? if >=1.20.3 && <1.20.5 {
/*import net.minecraft.client.gui.navigation.ScreenRectangle;
*///?}

/**
 * Cross-mc-version adapter for AbstractButton
 */
@Environment(EnvType.CLIENT)
public abstract class McButton extends AbstractButton {

    protected McButton(int x, int y, int width, int height, Component message) {
        // 1.21.11 styles the message in the constructor
        super(x, y, width, height, message == null ? Component.empty() : message);
    }


    // ------- STABLE API -------

    /**
     * Positioner for the tooltip of this button, null keeps the vanilla one.
     */
    @Nullable
    protected ClientTooltipPositioner tooltipPositioner() {
        return null;
    }

    public abstract void onPress();

    /**
     * Draws the button, AbstractButton.renderWidget is final around this since 1.21.11.
     */
    protected void renderContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderDefaultButton(guiGraphics, mouseX, mouseY, partialTick);
    }

    /**
     * The vanilla look: the button sprite and the message.
     */
    protected void renderDefaultButton(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        //? if >=26.1 {
        extractDefaultSprite(guiGraphics);
        extractDefaultLabel(guiGraphics.textRendererForWidget(this, GuiGraphicsExtractor.HoveredTextEffects.NONE));
        //?} elif >=1.21.11 {
        /*renderDefaultSprite(guiGraphics);
        renderDefaultLabel(guiGraphics.textRendererForWidget(this, GuiGraphicsExtractor.HoveredTextEffects.NONE));
        *///?} else {
        /*super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        *///?}
    }

    public void onRelease(double mouseX, double mouseY) {
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        //? if >=1.21.9 {
        return super.mouseClicked(McGuiUtils.mouseButtonEvent(mouseX, mouseY, button), false);
        //?} else {
        /*return super.mouseClicked(mouseX, mouseY, button);
        *///?}
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        //? if >=1.21.9 {
        return super.mouseReleased(McGuiUtils.mouseButtonEvent(mouseX, mouseY, button));
        //?} else {
        /*return super.mouseReleased(mouseX, mouseY, button);
        *///?}
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button,
                                double dragX, double dragY) {
        //? if >=1.21.9 {
        return super.mouseDragged(McGuiUtils.mouseButtonEvent(mouseX, mouseY, button), dragX, dragY);
        //?} else {
        /*return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        *///?}
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        //? if >=1.21.9 {
        return super.keyPressed(new KeyEvent(keyCode, scanCode, modifiers));
        //?} else {
        /*return super.keyPressed(keyCode, scanCode, modifiers);
        *///?}
    }

    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        //? if >=1.21.9 {
        return super.keyReleased(new KeyEvent(keyCode, scanCode, modifiers));
        //?} else {
        /*return super.keyReleased(keyCode, scanCode, modifiers);
        *///?}
    }

    public boolean charTyped(char chr, int modifiers) {
        //? if >=1.21.9 {
        return super.charTyped(McGuiUtils.characterEvent(chr, modifiers));
        //?} else {
        /*return super.charTyped(chr, modifiers);
        *///?}
    }

    //? if >=1.21.9 {
    @Override
    public final void onPress(InputWithModifiers modifiers) {
        onPress();
    }

    @Override
    public final void onRelease(MouseButtonEvent event) {
        onRelease(event.x(), event.y());
    }

    @Override
    public final boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        return mouseClicked(event.x(), event.y(), event.button());
    }

    @Override
    public final boolean mouseReleased(MouseButtonEvent event) {
        return mouseReleased(event.x(), event.y(), event.button());
    }

    @Override
    public final boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        return mouseDragged(event.x(), event.y(), event.button(), dragX, dragY);
    }

    @Override
    public final boolean keyPressed(KeyEvent event) {
        return keyPressed(event.key(), event.scancode(), event.modifiers());
    }

    @Override
    public final boolean keyReleased(KeyEvent event) {
        return keyReleased(event.key(), event.scancode(), event.modifiers());
    }

    @Override
    public final boolean charTyped(CharacterEvent event) {
        return charTyped((char) event.codepoint(), McGuiUtils.modifiers(event));
    }
    //?}


    // ------- MC-VERSION SPECIFIC IMPLEMENTATION -------

    //? if >=26.1 {
    @Override
    protected final void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderContents(guiGraphics, mouseX, mouseY, partialTick);
    }
    //?} elif <1.21.11 {
    /*@Override
    protected void renderWidget(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderContents(guiGraphics, mouseX, mouseY, partialTick);
    }
    *///?}

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
            //? if >=26.1 {
            super(source.message, source.narration, source.component(), source.style());
            //?} else {
            /*super(source.message, source.narration);
            *///?}
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
