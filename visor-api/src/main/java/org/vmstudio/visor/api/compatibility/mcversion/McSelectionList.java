package org.vmstudio.visor.api.compatibility.mcversion;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.narration.NarrationElementOutput;

/**
 * Version adapter for AbstractSelectionList
 *

 */
@Environment(EnvType.CLIENT)
public abstract class McSelectionList<E extends McSelectionList.Entry<E>> extends AbstractSelectionList<E> {


    @Environment(EnvType.CLIENT)
    public abstract static class Entry<E extends Entry<E>> extends AbstractSelectionList.Entry<E> {
    }

    protected McSelectionList(Minecraft minecraft,
                              int width, int height,
                              int x, int y,
                              int itemHeight) {
        super(minecraft, width, height, y, itemHeight);
        this.setX(x);
    }

    //---------- stable geometry (was x0/y0/x1/y1 before 1.20.3) ----------

    /** Left edge of the widget. */
    protected final int listLeft() {
        return this.getX();
    }

    /** Top edge of the widget. */
    protected final int listTop() {
        return this.getY();
    }

    /** Right edge of the widget. */
    protected final int listRight() {
        return this.getRight();
    }

    /** Bottom edge of the widget. */
    protected final int listBottom() {
        return this.getBottom();
    }

    //---------- stable render hooks ----------

    /** Stable render entry point; replaces the whole vanilla render pass of the list. */
    protected abstract void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick);

    /** Stable hook rendering all rows; default is vanilla row rendering. */
    protected void renderRows(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderListItems(guiGraphics, mouseX, mouseY, partialTick);
    }

    /** Stable hook for the scrollbar x position; default is vanilla placement. */
    protected int scrollbarX() {
        return super.getScrollbarPosition();
    }

    //---------- stable input hooks ----------

    /** Stable hook for vertical mouse-wheel scrolling; default is vanilla scrolling. */
    protected boolean onMouseScrolled(double mouseX, double mouseY, double verticalAmount) {
        return super.mouseScrolled(mouseX, mouseY, 0, verticalAmount);
    }

    //---------- stable narration hook ----------

    /** Stable narration hook; default narrates nothing. */
    protected void updateListNarration(NarrationElementOutput narrationElementOutput) {
    }

    //====================================================================
    // 1.21.1 wiring — everything below maps the stable hooks above onto
    // the version-specific vanilla API and is expected to change on ports.
    //====================================================================

    // 1.21.1: AbstractWidget.render is final; renderWidget is the overridable entry.
    @Override
    public final void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderContents(guiGraphics, mouseX, mouseY, partialTick);
    }

    // 1.21.1: renderList was renamed to renderListItems.
    @Override
    protected final void renderListItems(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderRows(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected final int getScrollbarPosition() {
        return scrollbarX();
    }

    // 1.21.1: mouseScrolled gained a horizontal-amount parameter.
    @Override
    public final boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return onMouseScrolled(mouseX, mouseY, scrollY);
    }

    // 1.21.1: updateNarration is final in AbstractWidget; hook is updateWidgetNarration.
    @Override
    protected final void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        updateListNarration(narrationElementOutput);
    }

    @Override
    protected final void renderListBackground(GuiGraphics guiGraphics) {
    }

    @Override
    protected final void renderListSeparators(GuiGraphics guiGraphics) {
    }
}
