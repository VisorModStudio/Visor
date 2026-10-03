package org.vmstudio.visor.api.compatibility.mcversion.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.narration.NarrationElementOutput;
//? if >=1.21.9 {
import net.minecraft.client.input.MouseButtonEvent;
//?}

/**
 * Cross-mc-version adapter for AbstractSelectionList
 */
@Environment(EnvType.CLIENT)
public abstract class McSelectionList<E extends McSelectionList.Entry<E>> extends AbstractSelectionList<E> {


    @Environment(EnvType.CLIENT)
    public abstract static class Entry<E extends Entry<E>> extends AbstractSelectionList.Entry<E>
            implements McGuiEventListener {

        //? if >=1.21.9 {
        // the row index vanilla no longer passes, filled in by McSelectionList.renderItem
        int rowIndex;
        //?}


        // ------- STABLE API -------

        protected void renderRowBack(GuiGraphicsExtractor guiGraphics, int index,
                                     int top, int left, int rowWidth, int rowHeight,
                                     int mouseX, int mouseY, boolean hovering, float partialTick) {
        }

        protected abstract void renderRow(GuiGraphicsExtractor guiGraphics, int index,
                                          int top, int left, int rowWidth, int rowHeight,
                                          int mouseX, int mouseY, boolean hovering, float partialTick);


        // ------- MC-VERSION SPECIFIC IMPLEMENTATION -------

        //? if >=1.21.9 {
        @Override
        //? if >=26.1 {
        public final void extractContent(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY,
                                         boolean hovering, float partialTick) {
        //?} else {
        /*public final void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY,
                                        boolean hovering, float partialTick) {
        *///?}
            renderRowBack(guiGraphics, rowIndex, getY(), getX(), getWidth(), getHeight(),
                    mouseX, mouseY, hovering, partialTick);
            renderRow(guiGraphics, rowIndex, getY(), getX(), getWidth(), getHeight(),
                    mouseX, mouseY, hovering, partialTick);
        }
        //?} else {
        /*@Override
        public final void renderBack(GuiGraphics guiGraphics, int index,
                                     int top, int left, int rowWidth, int rowHeight,
                                     int mouseX, int mouseY, boolean hovering, float partialTick) {
            renderRowBack(guiGraphics, index, top, left, rowWidth, rowHeight,
                    mouseX, mouseY, hovering, partialTick);
        }

        @Override
        public final void render(GuiGraphics guiGraphics, int index,
                                 int top, int left, int rowWidth, int rowHeight,
                                 int mouseX, int mouseY, boolean hovering, float partialTick) {
            renderRow(guiGraphics, index, top, left, rowWidth, rowHeight,
                    mouseX, mouseY, hovering, partialTick);
        }
        *///?}
    }

    protected McSelectionList(Minecraft minecraft,
                              int width, int height,
                              int x, int y,
                              int itemHeight) {
        //? if >=1.20.3 {
        super(Minecraft.getInstance(),
                width,
                height,
                y,
                itemHeight
        );
        this.setX(x);
        //?} else {
        /*super(Minecraft.getInstance(),
                width,
                height,
                y,
                y + height,
                itemHeight
        );
        this.setLeftPos(x);
        *///?}
    }


    // ------- STABLE API -------

    protected abstract void renderContents(GuiGraphicsExtractor guiGraphics,
                                           int mouseX, int mouseY,
                                           float partialTick);

    protected void renderRows(GuiGraphicsExtractor guiGraphics,
                              int mouseX, int mouseY,
                              float partialTick) {
        //? if >=26.1 {
        super.extractListItems(guiGraphics, mouseX, mouseY, partialTick);
        //?} elif >=1.20.5 {
        /*super.renderListItems(guiGraphics, mouseX, mouseY, partialTick);
        *///?} else {
        /*super.renderList(guiGraphics, mouseX, mouseY, partialTick);
        *///?}
    }


    protected int scrollbarX() {
        //? if >=1.21.4 {
        return super.scrollBarX();
        //?} else {
        /*return super.getScrollbarPosition();
        *///?}
    }

    protected final int entryHeight() {
        //? if >=1.21.9 {
        return this.defaultEntryHeight;
        //?} else {
        /*return this.itemHeight;
        *///?}
    }

    // 1.21.9 dropped the header band
    protected final int listHeaderHeight() {
        //? if >=1.21.9 {
        return 0;
        //?} else {
        /*return this.headerHeight;
        *///?}
    }

    protected final void renderRow(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick,
                                   int index, int left, int top, int rowWidth, int rowHeight) {
        //? if >=1.21.9 {
        E entry = this.children().get(index);
        entry.setX(left);
        entry.setY(top);
        entry.setWidth(rowWidth);
        entry.setHeight(rowHeight);
        //? if >=26.1 {
        extractItem(guiGraphics, mouseX, mouseY, partialTick, entry);
        //?} else {
        /*renderItem(guiGraphics, mouseX, mouseY, partialTick, entry);
        *///?}
        //?} else {
        /*renderItem(guiGraphics, mouseX, mouseY, partialTick, index, left, top, rowWidth, rowHeight);
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

    protected final double scrollValue() {
        //? if >=1.21.4 {
        return scrollAmount();
        //?} else {
        /*return getScrollAmount();
        *///?}
    }

    protected final int maxScroll() {
        //? if >=1.21.4 {
        return maxScrollAmount();
        //?} else {
        /*return getMaxScroll();
        *///?}
    }

    protected void onScrollStateUpdated(double mouseX, double mouseY, int button) {
    }

    protected boolean onMouseScrolled(double mouseX, double mouseY, double verticalAmount) {
        //? if >=1.20.2 {
        return super.mouseScrolled(mouseX, mouseY, 0, verticalAmount);
        //?} else {
        /*return super.mouseScrolled(mouseX, mouseY, verticalAmount);
        *///?}
    }

    public void setRenderTopAndBottom(boolean render) {
        //? if <1.20.2 {
        /*super.setRenderTopAndBottom(render);
        *///?}
    }

    public void setRenderSelection(boolean render) {
        //? if <1.20.2 {
        /*super.setRenderSelection(render);
        *///?}
    }

    // 1.20.5 dropped the flag, the background moved to renderListBackground
    public void setRenderBackground(boolean render) {
        //? if <1.20.5 {
        /*super.setRenderBackground(render);
        *///?}
    }

    protected void updateListNarration(NarrationElementOutput narrationElementOutput) {
    }



    protected final int listLeft() {
        //? if >=1.20.3 {
        return getX();
        //?} else {
        /*return x0;
        *///?}
    }
    protected final int listRight() {
        //? if >=1.20.3 {
        return getRight();
        //?} else {
        /*return x1;
        *///?}
    }

    protected final int listTop() {
        //? if >=1.20.3 {
        return getY();
        //?} else {
        /*return y0;
        *///?}
    }
    protected final int listBottom() {
        //? if >=1.20.3 {
        return getBottom();
        //?} else {
        /*return y1;
        *///?}
    }



    // ------- MC-VERSION SPECIFIC IMPLEMENTATION -------

    //? if >=26.1 {
    @Override
    public final void extractWidgetRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderContents(guiGraphics, mouseX, mouseY, partialTick);
    }
    //?} elif >=1.20.3 {
    /*@Override
    public final void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderContents(guiGraphics, mouseX, mouseY, partialTick);
    }
    *///?} else {
    /*@Override
    public final void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderContents(guiGraphics, mouseX, mouseY, partialTick);
    }
    *///?}

    //? if >=26.1 {
    @Override
    protected final void extractListItems(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderRows(guiGraphics, mouseX, mouseY, partialTick);
    }
    //?} elif >=1.20.5 {
    /*@Override
    protected final void renderListItems(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderRows(guiGraphics, mouseX, mouseY, partialTick);
    }
    *///?} else {
    /*@Override
    protected final void renderList(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderRows(guiGraphics, mouseX, mouseY, partialTick);
    }
    *///?}

    //? if <1.20.2 {
    /*@Override
    protected void renderBackground(GuiGraphics guiGraphics) {
    }
    *///?}

    //? if >=1.20.3 {
    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        updateListNarration(narrationElementOutput);
    }
    //?} else {
    /*@Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {
        updateListNarration(narrationElementOutput);
    }
    *///?}


    //? if >=1.21.4 {
    @Override
    protected final int scrollBarX() {
        return scrollbarX();
    }
    //?} else {
    /*@Override
    protected final int getScrollbarPosition() {
        return scrollbarX();
    }
    *///?}

    //? if >=1.21.9 {
    @Override
    public final boolean updateScrolling(MouseButtonEvent event) {
        boolean scrollingNow = super.updateScrolling(event);
        onScrollStateUpdated(event.x(), event.y(), event.button());
        return scrollingNow;
    }

    //? if >=26.1 {
    @Override
    protected void extractItem(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick, E entry) {
        entry.rowIndex = this.children().indexOf(entry);
        super.extractItem(guiGraphics, mouseX, mouseY, partialTick, entry);
    }
    //?} else {
    /*@Override
    protected void renderItem(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, E entry) {
        entry.rowIndex = this.children().indexOf(entry);
        super.renderItem(guiGraphics, mouseX, mouseY, partialTick, entry);
    }
    *///?}

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
    //?} elif >=1.21.4 {
    /*@Override
    public final boolean updateScrolling(double mouseX, double mouseY, int button) {
        boolean scrollingNow = super.updateScrolling(mouseX, mouseY, button);
        onScrollStateUpdated(mouseX, mouseY, button);
        return scrollingNow;
    }
    *///?} else {
    /*@Override
    protected final void updateScrollingState(double mouseX, double mouseY, int button) {
        super.updateScrollingState(mouseX, mouseY, button);
        onScrollStateUpdated(mouseX, mouseY, button);
    }
    *///?}

    //? if >=1.20.2 {
    @Override
    public final boolean mouseScrolled(double mouseX, double mouseY,
                                       double scrollX, double scrollY) {
        return onMouseScrolled(mouseX, mouseY, scrollY);
    }
    //?} else {
    /*@Override
    public final boolean mouseScrolled(double mouseX, double mouseY, double scrollY) {
        return onMouseScrolled(mouseX, mouseY, scrollY);
    }
    *///?}


}
