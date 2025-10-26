package me.phoenixra.visor.api.client.gui.widgets.sets;

import lombok.Setter;
import lombok.experimental.Accessors;
import me.phoenixra.visor.api.client.gui.GuiTexture;
import me.phoenixra.visor.api.client.gui.overlays.options.OptionTextures;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class WidgetsList extends DynamicWidgetSet {

    private final int x;
    private final int y;
    private final int width;
    private final int height;

    private final int scrollBarWidth;
    private final int columns;
    private final int columnGap;
    private final int rowGap;
    private final int entryHeight;

    private final GuiTexture textureScrollBar;
    private final GuiTexture textureScrollBarActive;

    private final List<AbstractWidget> widgets;

    private Scrollbar scrollbar;

    private double scrollAmount = 0;
    private int maxScroll = 0;

    public WidgetsList(@NotNull Builder builder) {
        super(builder.onWidgetsChanged);

        this.x = builder.x;
        this.y = builder.y;
        this.width = builder.width;
        this.height = builder.height;

        this.scrollBarWidth = builder.scrollBarWidth;
        this.columns = Math.max(1, builder.columns);
        this.columnGap = Math.max(0, builder.columnGap);
        this.rowGap = Math.max(0, builder.rowGap);
        this.entryHeight = Math.max(1, builder.entryHeight);

        this.textureScrollBar = builder.textureScrollBar;
        this.textureScrollBarActive = builder.textureScrollBarActive;

        this.widgets = new ArrayList<>(builder.widgets);
    }

    @Override
    public <T extends GuiEventListener & Renderable & NarratableEntry> List<T> initWidgets() {
        recalcMaxScroll();
        layoutChildren();

        scrollbar = new Scrollbar();

        List<T> out = new ArrayList<>(widgets.size() + 1);
        for (AbstractWidget w : widgets) {
            out.add((T) w);
        }
        out.add((T) scrollbar);
        return out;
    }

    @Override
    public <T extends GuiEventListener & Renderable & NarratableEntry> List<T> getWidgets() {
        layoutChildren();
        scrollbar.updateBounds();

        List<T> out = new ArrayList<>(widgets.size() + 1);
        for (AbstractWidget w : widgets) {
            out.add((T) w);
        }
        out.add((T) scrollbar);
        return out;
    }


    @Override
    public void onTick() {
        for (var widget : widgets) {
            if (widget instanceof EditBox editBox) {
                editBox.tick();
            }
        }
    }

    public void onPreRender(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        guiGraphics.enableScissor(contentLeft(), contentTop(), this.x + this.width, this.y + this.height);
    }

    public void onPostRender(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        guiGraphics.disableScissor();
    }

    // ---------------------------
    // Layout / scrolling
    // ---------------------------

    private int contentLeft() {
        return x;
    }

    private int contentTop() {
        return y;
    }

    private int rowWidth() {
        return Math.max(0, width - (scrollBarWidth + 4));
    }

    private int viewTop() {
        return y;
    }

    private int viewBottom() {
        return y + height;
    }

    private void recalcMaxScroll() {
        int rows = (int) Math.ceil(widgets.size() / (double) columns);
        int totalRowGaps = Math.max(0, rows - 1) * rowGap;
        int contentHeight =  rows * entryHeight + totalRowGaps;
        maxScroll = Math.max(0, contentHeight - height);
        scrollAmount = Mth.clamp(scrollAmount, 0, maxScroll);
    }

    private void layoutChildren() {
        if (widgets.isEmpty()) return;

        int totalGaps = Math.max(0, columns - 1) * columnGap;
        int avail = Math.max(0, rowWidth() - totalGaps);
        int baseCell = columns > 0 ? Math.max(0, avail / columns) : 0;
        int remainder = columns > 0 ? Math.max(0, avail - baseCell * columns) : 0;

        int[] colWidths = new int[columns];
        for (int c = 0; c < columns; c++) {
            colWidths[c] = baseCell + ((c == columns - 1) ? remainder : 0);
        }

        int[] colX = new int[columns];
        int curX = contentLeft();
        for (int c = 0; c < columns; c++) {
            colX[c] = curX;
            curX += colWidths[c];
            if (c < columns - 1) curX += columnGap;
        }

        int viewportTop = viewTop();
        int viewportBottom = viewBottom();

        for (int i = 0; i < widgets.size(); i++) {
            AbstractWidget w = widgets.get(i);
            int row = i / columns;
            int col = i % columns;

            int cellX = colX[col];
            int cellY = contentTop() + row * (entryHeight + rowGap) - (int) scrollAmount;

            int cellW = colWidths[col];
            int cellH = entryHeight;

            if (maxScroll == 0) {
                int minY = contentTop();
                int maxY = contentTop() + Math.max(0, height - cellH);
                cellY = Mth.clamp(cellY, minY, maxY);
            }

            w.setX(cellX);
            w.setY(cellY);
            w.setWidth(cellW);
            w.height = cellH;

            boolean intersects = (cellY + cellH) > viewportTop && cellY < viewportBottom;
            w.visible = intersects;
            w.active = intersects;

            if (!intersects) {
                w.setFocused(false);
            }
        }
    }

    private void applyScroll(double newAmount) {
        double clamped = Mth.clamp(newAmount, 0, maxScroll);
        if (clamped != scrollAmount) {
            scrollAmount = clamped;
            layoutChildren();
            scrollbar.updateBounds();
        }
    }

    // ---------- mouse scroll integration ----------
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (scrollbar.mouseScrolled(mouseX, mouseY, delta)) return true;

        // if over viewport, scroll content
        if (mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height) {
            double step = (entryHeight + rowGap) * 0.75;
            applyScroll(scrollAmount - delta * step);
            return true;
        }
        return false;
    }


    private int getScrollbarX() {
        return x + width - (scrollBarWidth + 2);
    }
    private int getScrollbarY() {
        return y;
    }
    private int getScrollbarWidth() {
        return scrollBarWidth;
    }
    private int getScrollbarHeight() {
        return height;
    }


    private class Scrollbar extends AbstractWidget {

        private boolean dragging = false;

        public Scrollbar() {
            super(0, 0, 0, 0, Component.empty());
            updateBounds();
        }

        void updateBounds() {

            this.setX(getScrollbarX());
            this.setY(getScrollbarY());
            this.setWidth(getScrollbarWidth());
            this.height = getScrollbarHeight();

            this.visible = maxScroll > 0;
            this.active = this.visible;
        }

        private int viewH() {
            int trackTop = y;
            int trackBottom = y + height;
            return Math.max(0, trackBottom - trackTop);
        }

        private int thumbHeight() {
            int vh = viewH();
            if (vh <= 0 || maxScroll <= 0) return 0;
            int th = (int) (vh * (float) vh / ((float) vh + maxScroll));
            return Mth.clamp(th, 32, Math.max(0, vh - 8));
        }

        private int thumbY() {
            int trackTop = y;
            int vh = viewH();
            int th = thumbHeight();
            if (vh <= 0 || maxScroll <= 0 || th <= 0) return trackTop;
            int available = Math.max(0, vh - th);
            return trackTop + (int) (scrollAmount * (available / (double) maxScroll));
        }

        @Override
        protected void renderWidget(GuiGraphics gg, int mx, int my, float pt) {
            if (!this.visible) return;
            int ty = thumbY();
            int th = thumbHeight();
            GuiTexture bar = dragging ? textureScrollBarActive : textureScrollBar;
            if (bar != null && th > 0) {
                bar.blit(gg, this.getX(), ty, this.getWidth(), th);
            }
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            if (maxScroll <= 0) return;
            int ty = thumbY();
            int th = thumbHeight();
            if (mouseY >= ty && mouseY < ty + th) {
                dragging = true;
            } else {
                int rowStep = entryHeight + rowGap;
                double page = Math.max(rowStep, height - rowStep);
                if (mouseY < ty) applyScroll(scrollAmount - page);
                else applyScroll(scrollAmount + page);
            }
        }

        @Override
        public void onRelease(double mouseX, double mouseY) {
            dragging = false;
        }

        @Override
        protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
            if (!dragging || maxScroll <= 0) return;
            int vh = viewH();
            int th = thumbHeight();
            if (vh <= 0 || th <= 0) return;
            int trackTop = y;
            int available = Math.max(0, vh - th);
            int offsetFromTop = (int) Mth.clamp(mouseY - trackTop - th / 2.0, 0, available);
            double ratio = available == 0 ? 0 : (offsetFromTop / (double) available);
            applyScroll(ratio * maxScroll);
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
            if (!this.visible) return false;
            if (mouseX >= this.getX() && mouseX < this.getX() + this.getWidth()
                    && mouseY >= this.getY() && mouseY < this.getY() + this.height) {
                if (maxScroll > 0) {
                    double step = (entryHeight + rowGap) * 0.75;
                    applyScroll(scrollAmount - delta * step);
                    return true;
                }
            }
            return false;
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput out) {
            if (this.visible && maxScroll > 0) {
                out.add(net.minecraft.client.gui.narration.NarratedElementType.TITLE, Component.translatable("narration.scrollbar"));
                out.add(net.minecraft.client.gui.narration.NarratedElementType.POSITION,
                        Component.translatable("narrator.position.list",
                                (int) scrollAmount, maxScroll));
            }
        }
    }

    // ---------- Builder ----------
    public static class Builder {

        private final Collection<AbstractWidget> widgets;
        @NotNull Runnable onWidgetsChanged;

        private int x, y, width, height;

        @Setter @Accessors(chain = true)
        private GuiTexture textureScrollBar = OptionTextures.SCROLL_BAR;
        @Setter @Accessors(chain = true)
        private GuiTexture textureScrollBarActive = OptionTextures.SCROLL_BAR_ACTIVE;

        @Setter @Accessors(chain = true)
        private int scrollBarWidth = 4;

        @Setter @Accessors(chain = true)
        private int entryHeight = 15;

        @Setter @Accessors(chain = true)
        private int columns = 1;

        @Setter @Accessors(chain = true)
        private int columnGap = 4;

        @Setter @Accessors(chain = true)
        private int rowGap = 4;

        public Builder(@NotNull Runnable onWidgetsChanged,
                       @NotNull Collection<AbstractWidget> widgets) {
            this.onWidgetsChanged = onWidgetsChanged;
            this.widgets = widgets;
        }

        public Builder pos(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public Builder size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public WidgetsList build() {
            if (width <= 0 || height <= 0) {
                throw new IllegalStateException("WidgetsList: size(width,height) must be set and > 0");
            }
            return new WidgetsList(this);
        }
    }
}