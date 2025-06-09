package me.phoenixra.visor.core.client.settings.option.gui.elements;


import me.phoenixra.visor.core.client.settings.option.VRGuiOption;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public interface VRGuiOptionElement extends LayoutElement,
        GuiEventListener, Renderable, NarratableEntry {
    VRGuiOption getGuiOptionType();

    @Override
    default void render(GuiGraphics guiGraphics, int i, int j, float f) {

    }

    @Override
    default void mouseMoved(double d, double e) {
        GuiEventListener.super.mouseMoved(d, e);
    }

    @Override
    default boolean mouseClicked(double d, double e, int i) {
        return GuiEventListener.super.mouseClicked(d, e, i);
    }

    @Override
    default boolean mouseReleased(double d, double e, int i) {
        return GuiEventListener.super.mouseReleased(d, e, i);
    }

    @Override
    default boolean mouseDragged(double d, double e, int i, double f, double g) {
        return GuiEventListener.super.mouseDragged(d, e, i, f, g);
    }

    @Override
    default boolean mouseScrolled(double d, double e, double f) {
        return GuiEventListener.super.mouseScrolled(d, e, f);
    }

    @Override
    default boolean keyPressed(int i, int j, int k) {
        return GuiEventListener.super.keyPressed(i, j, k);
    }

    @Override
    default boolean keyReleased(int i, int j, int k) {
        return GuiEventListener.super.keyReleased(i, j, k);
    }

    @Override
    default boolean charTyped(char c, int i) {
        return GuiEventListener.super.charTyped(c, i);
    }

    @Nullable
    @Override
    default ComponentPath nextFocusPath(FocusNavigationEvent focusNavigationEvent) {
        return GuiEventListener.super.nextFocusPath(focusNavigationEvent);
    }

    @Override
    default boolean isMouseOver(double d, double e) {
        return GuiEventListener.super.isMouseOver(d, e);
    }

    @Override
    default void setFocused(boolean bl) {

    }

    @Override
    default boolean isFocused() {
        return false;
    }

    @Nullable
    @Override
    default ComponentPath getCurrentFocusPath() {
        return GuiEventListener.super.getCurrentFocusPath();
    }

    @Override
    default void setX(int i) {

    }

    @Override
    default void setY(int i) {

    }

    @Override
    default int getX() {
        return 0;
    }

    @Override
    default int getY() {
        return 0;
    }

    @Override
    default int getWidth() {
        return 0;
    }

    @Override
    default int getHeight() {
        return 0;
    }

    @Override
    default ScreenRectangle getRectangle() {
        return LayoutElement.super.getRectangle();
    }

    @Override
    default void setPosition(int i, int j) {
        LayoutElement.super.setPosition(i, j);
    }

    @Override
    default void visitWidgets(Consumer<AbstractWidget> consumer) {

    }

    @Override
    default NarrationPriority narrationPriority() {
        return null;
    }

    @Override
    default boolean isActive() {
        return NarratableEntry.super.isActive();
    }

    @Override
    default void updateNarration(NarrationElementOutput narrationElementOutput) {

    }

    @Override
    default int getTabOrderGroup() {
        return GuiEventListener.super.getTabOrderGroup();
    }
}
