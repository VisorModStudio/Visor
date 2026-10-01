package org.vmstudio.visor.api.compatibility.mcversion.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
//? if >=1.21.9 {
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
//?}

/**
 * Cross-mc-version adapter for AbstractWidget.
 * 1.21.9 replaced every input callback with a record; the classic signatures below stay callable.
 */
@Environment(EnvType.CLIENT)
public abstract class McWidget extends AbstractWidget {

    protected McWidget(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }


    // ------- STABLE API -------

    protected abstract void renderWidget(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick);

    public void onClick(double mouseX, double mouseY) {
    }

    public void onRelease(double mouseX, double mouseY) {
    }

    protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
    }

    protected boolean isValidClickButton(int button) {
        //? if >=1.21.9 {
        return super.isValidClickButton(new MouseButtonInfo(button, 0));
        //?} else {
        /*return super.isValidClickButton(button);
        *///?}
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


    // ------- MC-VERSION SPECIFIC IMPLEMENTATION -------

    //? if >=26.1 {
    @Override
    protected final void extractWidgetRenderState(GuiGraphicsExtractor guiGraphics,
                                                  int mouseX, int mouseY, float partialTick) {
        renderWidget(guiGraphics, mouseX, mouseY, partialTick);
    }
    //?}

    //? if >=1.21.9 {
    @Override
    public final void onClick(MouseButtonEvent event, boolean doubleClick) {
        onClick(event.x(), event.y());
    }

    @Override
    public final void onRelease(MouseButtonEvent event) {
        onRelease(event.x(), event.y());
    }

    @Override
    protected final void onDrag(MouseButtonEvent event, double dragX, double dragY) {
        onDrag(event.x(), event.y(), dragX, dragY);
    }

    @Override
    protected final boolean isValidClickButton(MouseButtonInfo buttonInfo) {
        return isValidClickButton(buttonInfo.button());
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
}
