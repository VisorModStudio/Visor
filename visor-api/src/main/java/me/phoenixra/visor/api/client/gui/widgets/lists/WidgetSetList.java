package me.phoenixra.visor.api.client.gui.widgets.lists;


import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Getter;
import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.client.gui.GuiTexture;
import me.phoenixra.visor.api.client.gui.widgets.info.WidgetInfoWidgetSetList;
import me.phoenixra.visor.api.client.gui.widgets.sets.DynamicWidgetSet;
import me.phoenixra.visor.api.client.gui.widgets.sets.WidgetSet;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;


/**
 * A scrollable list where each entry is a {@link DynamicWidgetSet}.
 * <p>
 * Entries are not selectable — each entry manages its own widgets
 * and interaction.
 */
public class WidgetSetList implements GuiEventListener, Renderable, NarratableEntry {

    @Getter
    private final WidgetInfoWidgetSetList widgetInfo;

    // Layout
    private final int x0, y0, x1, y1;
    private final int listWidth, listHeight;
    private final int paddingTop, paddingLeft;
    private final int scrollBarWidth;
    private final int entryHeight, entryGap;
    private final int columns, columnGap;

    // Entries
    private final List<Entry> entries = new ArrayList<>();
    private final Map<String, Entry> entriesById = new LinkedHashMap<>();

    // Scrolling
    private double scrollAmount = 0;
    private boolean scrolling = false;
    private long lastDragCall = -1;

    // Focus
    private boolean focused = false;

    /**
     * Called when widgets are added/removed so the parent screen
     * can re-populate its widget list.
     */
    private final Runnable onWidgetsChanged;

    public WidgetSetList(@NotNull WidgetInfoWidgetSetList widgetInfo,
                         @NotNull Runnable onWidgetsChanged) {
        this.widgetInfo = widgetInfo;
        this.onWidgetsChanged = onWidgetsChanged;

        this.x0 = widgetInfo.getX();
        this.y0 = widgetInfo.getY();
        this.listWidth = widgetInfo.getWidth();
        this.listHeight = widgetInfo.getHeight();
        this.x1 = x0 + listWidth;
        this.y1 = y0 + listHeight;

        this.paddingTop = widgetInfo.getPaddingTop();
        this.paddingLeft = widgetInfo.getPaddingLeft();
        this.scrollBarWidth = widgetInfo.getScrollBarWidth();
        this.entryHeight = widgetInfo.getEntryHeight();
        this.entryGap = widgetInfo.getEntryGap();
        this.columns = Math.max(1, widgetInfo.getColumns());
        this.columnGap = widgetInfo.getColumnGap();
    }

    public void init(@NotNull Collection<? extends Entry> entries){
        entries.forEach(it->{
            addEntry(it);
            it.initWidgets();
        });
    }

    public void onTick() {
        for (Entry entry : entries) {
            entry.onTick();
        }
    }



    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (VisorAPI.clientState().stateMode().isActive()
                && scrolling
                && lastDragCall + 200 < System.currentTimeMillis()) {
            scrolling = false;
            lastDragCall = -1;
        }

        updateEntryPositions();

        guiGraphics.enableScissor(x0, y0, x1, y1);

        for (Entry entry : entries) {
            EntryVisibility vis = getEntryVisibility(entry);
            if (vis == EntryVisibility.HIDDEN) {
                continue;
            }

            entry.onPreRender(guiGraphics, mouseX, mouseY, partialTick);

            for (var widget : entry.getWidgets()) {
                widget.render(guiGraphics, mouseX, mouseY, partialTick);
            }
        }

        guiGraphics.disableScissor();

        for (Entry entry : entries) {
            if (getEntryVisibility(entry) != EntryVisibility.HIDDEN) {
                entry.onPostRender(guiGraphics, mouseX, mouseY, partialTick);
            }
        }

        renderScrollbar(guiGraphics);
        RenderSystem.disableBlend();
    }

    private void renderScrollbar(@NotNull GuiGraphics guiGraphics) {
        int maxScroll = getMaxScroll();
        if (maxScroll <= 0) return;

        int trackTop = y0 + paddingTop;
        int trackBottom = y1 - paddingTop;
        int viewH = trackBottom - trackTop;

        int thumbH = (int) (viewH * (float) viewH / ((float) viewH + maxScroll));
        thumbH = Mth.clamp(thumbH, 32, viewH - 8);

        int thumbY = trackTop
                + (int) (scrollAmount * (viewH - thumbH) / (float) maxScroll);

        GuiTexture scrollBarTex = scrolling
                ? widgetInfo.getTextureScrollBarActive()
                : widgetInfo.getTextureScrollBar();

        scrollBarTex.blit(
                guiGraphics,
                getScrollbarX(), thumbY,
                scrollBarWidth, thumbH
        );
    }


    private void updateEntryPositions() {
        int colWidth = getColumnWidth();

        for (int i = 0; i < entries.size(); i++) {
            int row = i / columns;
            int col = i % columns;

            int entryX = getColumnLeft(col);
            int entryY = getRowTop(row);

            Entry entry = entries.get(i);
            entry.setLocation(entryX, entryY, colWidth, entryHeight);
        }
    }

    // Column geometry

    private int getContentAreaWidth() {
        return listWidth - scrollBarWidth - paddingLeft * 2;
    }

    private int getColumnWidth() {
        int totalGap = (columns - 1) * columnGap;
        return (getContentAreaWidth() - totalGap) / columns;
    }

    private int getColumnLeft(int col) {
        return x0 + paddingLeft + col * (getColumnWidth() + columnGap);
    }

    // Row geometry

    private int getRowCount() {
        return (int) Math.ceil((double) entries.size() / columns);
    }

    private int getRowHeight() {
        return entryHeight + entryGap;
    }

    private int getTotalContentHeight() {
        int rows = getRowCount();
        if (rows == 0) return 0;
        return rows * getRowHeight() - entryGap;
    }

    private int getMaxScroll() {
        return Math.max(0, getTotalContentHeight() + paddingTop * 2 - listHeight);
    }

    private int getRowTop(int row) {
        return y0 + paddingTop - (int) scrollAmount + row * getRowHeight();
    }

    // Visibility

    private enum EntryVisibility {
        FULLY_VISIBLE,
        PARTIALLY_VISIBLE,
        HIDDEN
    }

    private EntryVisibility getEntryVisibility(Entry entry) {
        int entryTop = entry.getY();
        int entryBottom = entryTop + entry.getHeight();

        if (entryBottom < y0 || entryTop > y1) {
            return EntryVisibility.HIDDEN;
        }
        if (entryTop > y0 && entryBottom < y1) {
            return EntryVisibility.FULLY_VISIBLE;
        }
        return EntryVisibility.PARTIALLY_VISIBLE;
    }


    public void addEntry(@NotNull Entry entry) {
        entries.add(entry);
        entriesById.put(entry.id, entry);
    }

    public void removeEntry(@NotNull String id) {
        Entry entry = entriesById.remove(id);
        if (entry != null) {
            entries.remove(entry);
            onWidgetsChanged.run();
        }
    }

    public void clearEntries() {
        entries.clear();
        entriesById.clear();
        scrollAmount = 0;
        onWidgetsChanged.run();
    }

    @Nullable
    public Entry getEntry(@NotNull String id) {
        return entriesById.get(id);
    }

    public List<Entry> getEntries() {
        return Collections.unmodifiableList(entries);
    }


    public void setScrollAmount(double amount) {
        this.scrollAmount = Mth.clamp(amount, 0.0, getMaxScroll());
    }

    public double getScrollAmount() {
        return scrollAmount;
    }

    private int getScrollbarX() {
        return x1 - scrollBarWidth - 2;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (!isMouseOver(mouseX, mouseY)) return false;
        setScrollAmount(scrollAmount - delta * (getRowHeight()));
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isMouseOver(mouseX, mouseY)) return false;

        if (button == 0 && getMaxScroll() > 0) {
            int sbX = getScrollbarX();
            if (mouseX >= sbX && mouseX < sbX + scrollBarWidth + 2) {
                scrolling = true;
                lastDragCall = System.currentTimeMillis();
                return true;
            }
        }

        for (Entry entry : entries) {
            if (getEntryVisibility(entry) == EntryVisibility.HIDDEN) {
                continue;
            }
            for (var widget : entry.getWidgets()) {
                if (widget.mouseClicked(mouseX, mouseY, button)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            scrolling = false;
        }
        for (Entry entry : entries) {
            if (getEntryVisibility(entry) == EntryVisibility.HIDDEN) {
                continue;
            }
            for (var widget : entry.getWidgets()) {
                widget.mouseReleased(mouseX, mouseY, button);
            }
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY,
                                int button, double dragX, double dragY) {
        if (scrolling && button == 0) {
            lastDragCall = System.currentTimeMillis();
            int maxScroll = getMaxScroll();
            if (maxScroll <= 0) return true;

            int trackTop = y0 + paddingTop;
            int trackBottom = y1 - paddingTop;
            int trackHeight = trackBottom - trackTop;

            double scale = (double) maxScroll / trackHeight;
            setScrollAmount(scrollAmount + dragY * scale);
            return true;
        }
        return false;
    }





    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= x0 && mouseX < x1
                && mouseY >= y0 && mouseY < y1;
    }

    @Override
    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    @Override
    public boolean isFocused() {
        return focused;
    }

    @Override
    public @NotNull NarrationPriority narrationPriority() {
        return NarrationPriority.NONE;
    }

    @Override
    public void updateNarration(@NotNull NarrationElementOutput output) {
    }


    @Getter
    public static abstract class Entry extends DynamicWidgetSet {

        private final String id;

        private int x, y, width, height;

        public Entry(@NotNull String id,
                     @NotNull Runnable onWidgetsChanged) {
            super(onWidgetsChanged);
            this.id = id;
        }

        public void setLocation(int x, int y,
                                int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }
}