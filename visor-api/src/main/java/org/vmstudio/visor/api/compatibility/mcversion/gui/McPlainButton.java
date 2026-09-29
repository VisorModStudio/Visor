package org.vmstudio.visor.api.compatibility.mcversion.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/**
 * Cross-mc-version adapter for a Button, Button.Plain since 1.21.11
 */
//? if >=1.21.11 {
@Environment(EnvType.CLIENT)
public class McPlainButton extends Button.Plain {
//?} else {
/*@Environment(EnvType.CLIENT)
public class McPlainButton extends Button {
*///?}

    protected McPlainButton(int x, int y, int width, int height, Component message,
                            OnPress onPress, CreateNarration createNarration) {
        super(x, y, width, height, message, onPress, createNarration);
    }


    // ------- MC-VERSION SPECIFIC IMPLEMENTATION -------

    // subclasses draw in renderContents, super.renderContents is the vanilla button;
    // AbstractButton.renderWidget is final around it since 1.21.11
    //? if <1.21.11 {
    /*protected void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderContents(guiGraphics, mouseX, mouseY, partialTick);
    }
    *///?}
}
