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

    //---------- stable scroll state (moved to AbstractScrollArea in 1.21.2) ----------

    /** Current vertical scroll offset, in pixels. */
    protected final double scrollOffset() {
        return this.scrollAmount();
    }

    /** Sets the vertical scroll offset; vanilla clamps it to the content. */
    protected final void scrollOffset(double offset) {
        this.setScrollAmount(offset);
    }

    /** Largest scroll offset the content allows; 0 when everything fits. */
    protected final int maxScrollOffset() {
        return this.maxScrollAmount();
    }

    /** Whether the scrollbar thumb is currently being dragged. */
    protected final boolean isScrolling() {
        return this.scrolling;
    }

    /** Forces the scrollbar drag state; used to cancel a drag that went stale. */
    protected final void setScrolling(boolean scrolling) {
        this.scrolling = scrolling;
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
        return super.scrollBarX();
    }

    //---------- stable row geometry hooks ----------

    /** Stable hook for the top edge of a row; default is vanilla placement. */
    protected int rowTop(int index) {
        return super.getRowTop(index);
    }

    /** Stable hook for the bottom edge of a row; default is vanilla placement. */
    protected int rowBottom(int index) {
        return super.getRowBottom(index);
    }

    //---------- stable input hooks ----------

    /** Stable hook for vertical mouse-wheel scrolling; default is vanilla scrolling. */
    protected boolean onMouseScrolled(double mouseX, double mouseY, double verticalAmount) {
        return super.mouseScrolled(mouseX, mouseY, 0, verticalAmount);
    }

    /** Stable hook fired right after vanilla refreshes the scrollbar drag state on a click. */
    protected void onUpdateScrolling(double mouseX, double mouseY, int button) {
    }

    //---------- stable narration hook ----------

    /** Stable narration hook; default narrates nothing. */
    protected void updateListNarration(NarrationElementOutput narrationElementOutput) {
    }

    //====================================================================
    // 1.21.4 wiring — everything below maps the stable hooks above onto
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
    protected final int scrollBarX() {
        return scrollbarX();
    }

    @Override
    public final int getRowTop(int index) {
        return rowTop(index);
    }

    @Override
    public final int getRowBottom(int index) {
        return rowBottom(index);
    }

    // 1.21.1: mouseScrolled gained a horizontal-amount parameter.
    @Override
    public final boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return onMouseScrolled(mouseX, mouseY, scrollY);
    }


    @Override
    public final boolean updateScrolling(double mouseX, double mouseY, int button) {
        boolean grabbed = super.updateScrolling(mouseX, mouseY, button);
        onUpdateScrolling(mouseX, mouseY, button);
        return grabbed;
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
