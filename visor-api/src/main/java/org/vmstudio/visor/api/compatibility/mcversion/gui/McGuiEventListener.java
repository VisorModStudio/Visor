package org.vmstudio.visor.api.compatibility.mcversion.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.events.GuiEventListener;
//? if >=1.21.9 {
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
//?}

/**
 * Cross-mc-version adapter for GuiEventListener.
 */
@Environment(EnvType.CLIENT)
public interface McGuiEventListener extends GuiEventListener {

    // ------- STABLE API -------

    default void mouseMoved(double mouseX, double mouseY) {
    }

    default boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    default boolean mouseReleased(double mouseX, double mouseY, int button) {
        return false;
    }

    default boolean mouseDragged(double mouseX, double mouseY, int button,
                                 double dragX, double dragY) {
        return false;
    }

    default boolean mouseScrolled(double mouseX, double mouseY, double verticalAmount) {
        return false;
    }

    default boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    default boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    default boolean charTyped(char chr, int modifiers) {
        return false;
    }


    // ------- MC-VERSION SPECIFIC IMPLEMENTATION -------

    //? if >=1.20.2 {
    @Override
    default boolean mouseScrolled(double mouseX, double mouseY,
                                  double horizontalAmount, double verticalAmount) {
        return mouseScrolled(mouseX, mouseY, verticalAmount);
    }
    //?}

    //? if >=1.21.9 {
    @Override
    default boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        return mouseClicked(event.x(), event.y(), event.button());
    }

    @Override
    default boolean mouseReleased(MouseButtonEvent event) {
        return mouseReleased(event.x(), event.y(), event.button());
    }

    @Override
    default boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        return mouseDragged(event.x(), event.y(), event.button(), dragX, dragY);
    }

    @Override
    default boolean keyPressed(KeyEvent event) {
        return keyPressed(event.key(), event.keycode(), event.modifiers());
    }

    @Override
    default boolean keyReleased(KeyEvent event) {
        return keyReleased(event.key(), event.keycode(), event.modifiers());
    }

    @Override
    default boolean charTyped(CharacterEvent event) {
        return charTyped((char) event.codepoint(), McGuiUtils.modifiers(event));
    }
    //?}
}
