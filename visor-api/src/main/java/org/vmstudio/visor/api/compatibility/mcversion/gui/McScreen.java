package org.vmstudio.visor.api.compatibility.mcversion.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
//? if >=1.21.9 {
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
//?}

/**
 * Cross-mc-version adapter for Screen.
 */
@Environment(EnvType.CLIENT)
public abstract class McScreen extends Screen {

    protected McScreen(Component title) {
        super(title);
        // vanilla fills them only in init() before 1.21.11, which makes them final and fills them here
        //? if <1.21.11 {
        /*this.minecraft = Minecraft.getInstance();
        this.font = minecraft.font;
        *///?}
    }


    // ------- STABLE API -------

    protected void renderScreenBackground(GuiGraphicsExtractor guiGraphics,
                                          int mouseX, int mouseY,
                                          float partialTick) {
        //? if >=26.1 {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTick);
        //?} elif >=1.20.2 {
        /*super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        *///?} else {
        /*super.renderBackground(guiGraphics);
        *///?}
    }

    protected void renderContents(GuiGraphicsExtractor guiGraphics,
                                  int mouseX, int mouseY,
                                  float partialTick) {
        //? if >=26.1 {
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
        //?} else {
        /*super.render(guiGraphics, mouseX, mouseY, partialTick);
        *///?}
    }

    protected boolean onMouseClicked(double mouseX, double mouseY, int button) {
        //? if >=1.21.9 {
        return super.mouseClicked(McGuiUtils.mouseButtonEvent(mouseX, mouseY, button), false);
        //?} else {
        /*return super.mouseClicked(mouseX, mouseY, button);
        *///?}
    }

    protected boolean onMouseReleased(double mouseX, double mouseY, int button) {
        //? if >=1.21.9 {
        return super.mouseReleased(McGuiUtils.mouseButtonEvent(mouseX, mouseY, button));
        //?} else {
        /*return super.mouseReleased(mouseX, mouseY, button);
        *///?}
    }

    protected boolean onMouseDragged(double mouseX, double mouseY, int button,
                                     double dragX, double dragY) {
        //? if >=1.21.9 {
        return super.mouseDragged(McGuiUtils.mouseButtonEvent(mouseX, mouseY, button), dragX, dragY);
        //?} else {
        /*return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        *///?}
    }

    protected boolean onMouseScrolled(double mouseX, double mouseY, double verticalAmount) {
        //? if >=1.20.2 {
        return super.mouseScrolled(mouseX, mouseY, 0, verticalAmount);
        //?} else {
        /*return super.mouseScrolled(mouseX, mouseY, verticalAmount);
        *///?}
    }

    protected boolean onKeyPressed(int keyCode, int scanCode, int modifiers) {
        //? if >=1.21.9 {
        return super.keyPressed(new KeyEvent(keyCode, scanCode, modifiers));
        //?} else {
        /*return super.keyPressed(keyCode, scanCode, modifiers);
        *///?}
    }

    protected boolean onKeyReleased(int keyCode, int scanCode, int modifiers) {
        //? if >=1.21.9 {
        return super.keyReleased(new KeyEvent(keyCode, scanCode, modifiers));
        //?} else {
        /*return super.keyReleased(keyCode, scanCode, modifiers);
        *///?}
    }

    protected boolean onCharTyped(char chr, int modifiers) {
        //? if >=1.21.9 {
        return super.charTyped(McGuiUtils.characterEvent(chr, modifiers));
        //?} else {
        /*return super.charTyped(chr, modifiers);
        *///?}
    }


    // ------- MC-VERSION SPECIFIC IMPLEMENTATION -------

    //? if <1.21.9 {
    /*@Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return onMouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return onMouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button,
                                double dragX, double dragY) {
        return onMouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return onKeyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return onKeyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        return onCharTyped(chr, modifiers);
    }
    *///?}

    //? if <1.20.2 {
    /*@Override
    public boolean mouseScrolled(double mouseX, double mouseY, double verticalAmount) {
        return onMouseScrolled(mouseX, mouseY, verticalAmount);
    }
    *///?}

    //? if >=1.20.2 && <1.21.9 {
    /*// vanilla render() draws the background itself there, but the adapter already did
    private boolean contentsPass;
    *///?}

    @Override
    //? if >=26.1 {
    public final void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
    //?} else {
    /*public final void render(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
    *///?}
        //? if <1.20.2 {
        /*renderScreenBackground(guiGraphics, mouseX, mouseY, partialTick);
        renderContents(guiGraphics, mouseX, mouseY, partialTick);
        *///?} elif <1.21.9 {
        /*renderScreenBackground(guiGraphics, mouseX, mouseY, partialTick);
        contentsPass = true;
        try {
            renderContents(guiGraphics, mouseX, mouseY, partialTick);
        } finally {
            contentsPass = false;
        }
        *///?} else {
        renderContents(guiGraphics, mouseX, mouseY, partialTick);
        //?}
    }

    //? if <1.20.2 {
    /*@Override
    public final void renderBackground(GuiGraphicsExtractor guiGraphics) {
        renderScreenBackground(guiGraphics, 0, 0, 0);
    }
    *///?} elif <1.21.9 {
    /*@Override
    public final void renderBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!contentsPass) {
            renderScreenBackground(guiGraphics, mouseX, mouseY, partialTick);
        }
    }
    *///?} elif <26.1 {
    /*@Override
    public final void renderBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderScreenBackground(guiGraphics, mouseX, mouseY, partialTick);
    }
    *///?} else {
    @Override
    public final void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderScreenBackground(guiGraphics, mouseX, mouseY, partialTick);
    }
    //?}

    //? if >=1.20.2 {
    @Override
    public final boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return onMouseScrolled(mouseX, mouseY, scrollY);
    }
    //?}

    //? if >=1.21.9 {
    @Override
    public final boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        return onMouseClicked(event.x(), event.y(), event.button());
    }

    @Override
    public final boolean mouseReleased(MouseButtonEvent event) {
        return onMouseReleased(event.x(), event.y(), event.button());
    }

    @Override
    public final boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        return onMouseDragged(event.x(), event.y(), event.button(), dragX, dragY);
    }

    @Override
    public final boolean keyPressed(KeyEvent event) {
        return onKeyPressed(event.key(), event.scancode(), event.modifiers());
    }

    @Override
    public final boolean keyReleased(KeyEvent event) {
        return onKeyReleased(event.key(), event.scancode(), event.modifiers());
    }

    // older vanilla delivered supplementary code points as surrogate pairs
    @Override
    public final boolean charTyped(CharacterEvent event) {
        boolean handled = false;
        for (char chr : Character.toChars(event.codepoint())) {
            handled |= onCharTyped(chr, McGuiUtils.modifiers(event));
        }
        return handled;
    }
    //?}
}
