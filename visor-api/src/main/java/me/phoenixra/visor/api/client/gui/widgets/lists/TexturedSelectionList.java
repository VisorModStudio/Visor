package me.phoenixra.visor.api.client.gui.widgets.lists;

import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Getter;
import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.client.gui.helpers.GuiHelper;
import me.phoenixra.visor.api.client.gui.GuiTexture;
import me.phoenixra.visor.api.client.gui.overlays.framework.VROverlayScreen;
import me.phoenixra.visor.api.client.gui.widgets.info.WidgetInfoButtonImaged;
import me.phoenixra.visor.api.client.gui.widgets.info.WidgetInfoSelectionList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;


public class TexturedSelectionList extends AbstractSelectionList<TexturedSelectionList.TexturedRow> {

    @Getter
    private final WidgetInfoSelectionList widgetInfo;

    private final int paddingTop;
    private final int paddingLeft;
    private final int scrollBarWidth;
    private final int columns;
    private final int columnGap;

    private final Consumer<TexturedEntry> onSelected;

    /**
     * All logical entries (flat), keyed by id.
     */
    private final Map<String, TexturedEntry> entriesMap = new LinkedHashMap<>();

    /**
     * The currently selected entry (across all columns).
     */
    @Getter
    @Nullable
    private TexturedEntry selectedEntry;

    private Map<String, String> rawEntries;

    private long lastDragCall = -1;

    @Nullable
    private Tooltip tooltip;
    private int tooltipMsDelay = 0;
    private long hoverOrFocusedStartTime;
    private boolean wasHoveredOrFocused;
    @Nullable
    private String tooltipEntryIdForTimer;
    private Screen visor$attachedTo;

    /**
     * Tracked for tooltip: the entry currently hovered.
     */
    @Nullable
    private TexturedEntry hoveredEntry;

    public TexturedSelectionList(@NotNull WidgetInfoSelectionList widgetInfo,
                                 @NotNull Map<String, String> rawEntries,
                                 @NotNull Consumer<TexturedEntry> onSelected) {
        super(Minecraft.getInstance(),
                widgetInfo.getWidth(),
                widgetInfo.getHeight(),
                widgetInfo.getY(),
                widgetInfo.getY() + widgetInfo.getHeight(),
                widgetInfo.getEntryHeight()
        );

        this.widgetInfo = widgetInfo;
        this.paddingTop = widgetInfo.getPaddingTop();
        this.paddingLeft = widgetInfo.getPaddingLeft();
        this.scrollBarWidth = widgetInfo.getScrollBarWidth();
        this.columns = Math.max(1, widgetInfo.getColumns());
        this.columnGap = widgetInfo.getColumnGap();

        this.onSelected = onSelected;

        this.setLeftPos(widgetInfo.getX());
        this.setRenderTopAndBottom(false);
        this.setRenderBackground(false);
        this.setRenderSelection(false);

        resetEntries(rawEntries);
    }

    // ── Column geometry helpers ──────────────────────────────────────

    /**
     * Total width available for columns (excludes scrollbar + padding).
     */
    private int getColumnsAreaWidth() {
        return this.width - scrollBarWidth - paddingLeft * 2;
    }

    /**
     * Width of a single column cell.
     */
    private int getColumnWidth() {
        int totalGap = (columns - 1) * columnGap;
        return (getColumnsAreaWidth() - totalGap) / columns;
    }

    /**
     * Left X of a given column index (0-based).
     */
    private int getColumnLeft(int col) {
        return getRowLeft() + col * (getColumnWidth() + columnGap);
    }

    /**
     * Determine which column index a mouse X coordinate falls in, or -1.
     */
    private int getColumnAtX(double mouseX) {
        for (int c = 0; c < columns; c++) {
            int left = getColumnLeft(c);
            if (mouseX >= left && mouseX < left + getColumnWidth()) {
                return c;
            }
        }
        return -1;
    }

    // ── Row building ─────────────────────────────────────────────────

    /**
     * Pack a flat list of entries into rows of N columns.
     */
    private void rebuildRows(List<TexturedEntry> entries) {
        this.clearEntries();
        for (int i = 0; i < entries.size(); i += columns) {
            TexturedEntry[] rowEntries = new TexturedEntry[columns];
            for (int c = 0; c < columns && (i + c) < entries.size(); c++) {
                rowEntries[c] = entries.get(i + c);
            }
            this.addEntry(new TexturedRow(rowEntries));
        }
    }

    // ── Rendering ────────────────────────────────────────────────────

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Determine hovered entry across columns
        this.hoveredEntry = null;
        if (this.isMouseOver(mouseX, mouseY)) {
            TexturedRow row = this.getEntryAtPosition(mouseX, mouseY);
            if (row != null) {
                int col = getColumnAtX(mouseX);
                if (col >= 0) {
                    this.hoveredEntry = row.getEntry(col);
                }
            }
        }

        // Reset the vanilla `hovered` field for the row
        this.hovered = this.isMouseOver(mouseX, mouseY) ? this.getEntryAtPosition(mouseX, mouseY) : null;

        if (VisorAPI.clientState().stateMode().isActive()
                && scrolling
                && lastDragCall + 200 < System.currentTimeMillis()) {
            scrolling = false;
            lastDragCall = -1;
        }

        this.enableScissor(guiGraphics);
        this.renderList(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.disableScissor();

        int scrollX = this.getScrollbarPosition();
        int maxScroll = this.getMaxScroll();
        if (maxScroll > 0) {
            int trackTop = this.y0 + this.paddingTop;
            int trackBottom = this.y1 - this.paddingTop;
            int viewH = trackBottom - trackTop;

            int thumbH = (int) (viewH * (float) viewH / ((float) viewH + maxScroll));
            thumbH = Mth.clamp(thumbH, 32, viewH - 8);

            int thumbY = trackTop
                    + (int) (this.getScrollAmount() * (viewH - thumbH) / (float) maxScroll);

            var scrollBarTex = scrolling
                    ? widgetInfo.getTextureScrollBarActive()
                    : widgetInfo.getTextureScrollBar();
            scrollBarTex.blit(
                    guiGraphics,
                    scrollX, thumbY,
                    scrollBarWidth, thumbH
            );
        }
        updateTooltip();
        RenderSystem.disableBlend();
    }

    @Override
    protected void renderList(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int i = this.getRowLeft();
        int j = this.getRowWidth();
        int k = this.itemHeight - paddingTop;
        int l = this.getItemCount();

        for (int m = 0; m < l; ++m) {
            int n = this.getRowTop(m);
            int o = this.getRowBottom(m);
            if (o >= this.y0 && n <= this.y1) {
                this.renderItem(guiGraphics, mouseX, mouseY, partialTick, m, i, n, j, k);
            }
        }
    }

    private void updateTooltip() {
        Function<String, Component> factory = widgetInfo.getTooltip();
        if (factory == null) return;

        TexturedEntry entryForTooltip = this.hoveredEntry;

        boolean hasTarget = entryForTooltip != null;
        String newId = hasTarget ? entryForTooltip.getId() : null;

        boolean stateChanged = (hasTarget != this.wasHoveredOrFocused) ||
                !Objects.equals(this.tooltipEntryIdForTimer, newId);

        if (stateChanged) {
            if (hasTarget) {
                this.hoverOrFocusedStartTime = Util.getMillis();
            }
            this.wasHoveredOrFocused = hasTarget;
            this.tooltipEntryIdForTimer = newId;
        }

        if (!hasTarget) return;
        if (Util.getMillis() - this.hoverOrFocusedStartTime <= (long) this.tooltipMsDelay) return;

        Component tipText = factory.apply(newId);
        if (tipText == null) return;

        this.tooltip = Tooltip.create(tipText);

        Screen screen = getAttachedTo();
        if (screen != null) {
            screen.setTooltipForNextRenderPass(this.tooltip, DefaultTooltipPositioner.INSTANCE, false);
        }
    }

    private Screen getAttachedTo() {
        if (visor$attachedTo == null) {
            if (VisorAPI.clientState().stateMode().isNotActive()) {
                visor$attachedTo = Minecraft.getInstance().screen;
                return visor$attachedTo;
            }
            VROverlayScreen overlay = VisorAPI.client().getGuiManager()
                    .getCursorHandler()
                    .getFocusedOverlayScreen();

            if (overlay != null) {
                visor$attachedTo = overlay;
            } else {
                visor$attachedTo = Minecraft.getInstance().screen;
            }
            return visor$attachedTo;
        }
        return visor$attachedTo;
    }

    // ── Entry management (public API unchanged) ──────────────────────

    public void filterEntries(
            @NotNull Function<Map.Entry<String, String>, Boolean> filter
    ) {
        entriesMap.clear();
        setScrollAmount(0);
        List<TexturedEntry> filtered = new ArrayList<>();
        for (var entry : rawEntries.entrySet()) {
            if (!filter.apply(entry)) {
                continue;
            }
            var texturedEntry = new TexturedEntry(
                    entry.getKey(),
                    Component.literal(entry.getValue())
            );
            filtered.add(texturedEntry);
            entriesMap.put(texturedEntry.id, texturedEntry);
        }
        rebuildRows(filtered);
    }

    public void resetEntries(@NotNull Map<String, String> rawEntries) {
        entriesMap.clear();
        setScrollAmount(0);
        List<TexturedEntry> all = new ArrayList<>();
        for (var entry : rawEntries.entrySet()) {
            var texturedEntry = new TexturedEntry(
                    entry.getKey(),
                    Component.literal(entry.getValue())
            );
            all.add(texturedEntry);
            entriesMap.put(texturedEntry.id, texturedEntry);
        }
        this.rawEntries = rawEntries;
        rebuildRows(all);
    }

    public void renameEntry(String id, Component newLabel) {
        if (this.rawEntries != null && this.rawEntries.containsKey(id)) {
            this.rawEntries.put(id, newLabel.getString());
        }
        TexturedEntry entry = this.entriesMap.get(id);
        if (entry == null) {
            return;
        }
        entry.label = newLabel;
    }

    public @Nullable TexturedEntry getEntry(@NotNull String id) {
        return entriesMap.get(id);
    }

    public void scrollTo(@NotNull TexturedEntry entry) {
        int maxScroll = this.getMaxScroll();
        if (maxScroll <= 0) return;

        // Find which row contains this entry
        for (int i = 0; i < this.getItemCount(); i++) {
            TexturedRow row = this.children().get(i);
            if (row.contains(entry)) {
                double desired = (double) i * this.itemHeight;
                this.setScrollAmount(desired);
                return;
            }
        }
    }

    // ── Selection ────────────────────────────────────────────────────

    /**
     * Select a logical entry by reference.
     */
    public void setSelectedEntry(@Nullable TexturedEntry entry) {
        if (entry != selectedEntry) {
            this.selectedEntry = entry;
            onSelected.accept(entry);
        } else if (widgetInfo.isSupportDeselection() && entry != null) {
            this.playSelectedSound(Minecraft.getInstance().getSoundManager());
            this.selectedEntry = null;
            onSelected.accept(null);
        }
    }

    /**
     * Select a logical entry by id.
     */
    public void setSelectedEntry(@NotNull String id) {
        var entry = getEntry(id);
        if (entry == null) return;
        setSelectedEntry(entry);
    }

    /**
     * @deprecated Use {@link #setSelectedEntry(TexturedEntry)} instead.
     * Kept for minimal API breakage; delegates to the new method.
     */
    @Deprecated
    @Override
    public void setSelected(@Nullable TexturedRow row) {
        // no-op: selection is tracked at the entry level
    }

    /**
     * @deprecated Use {@link #setSelectedEntry(String)} instead.
     */
    @Deprecated
    public void setSelected(@NotNull String id) {
        setSelectedEntry(id);
    }

    public void playSelectedSound(SoundManager handler) {
        handler.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    // ── Scrolling ────────────────────────────────────────────────────

    @Override
    protected void updateScrollingState(double mouseX, double mouseY, int button) {
        super.updateScrollingState(mouseX, mouseY, button);
        if (scrolling) {
            lastDragCall = System.currentTimeMillis();
        }
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (scrolling) {
            lastDragCall = System.currentTimeMillis();
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            this.scrolling = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    // ── Layout overrides ─────────────────────────────────────────────

    @Override
    protected int getScrollbarPosition() {
        return this.x0 + this.width - (scrollBarWidth + 2);
    }

    @Override
    public int getRowWidth() {
        return this.width - scrollBarWidth - paddingLeft * 2;
    }

    @Override
    public int getRowLeft() {
        return this.x0 + paddingLeft;
    }

    @Override
    protected int getRowTop(int index) {
        return this.y0 + paddingTop - (int) this.getScrollAmount() + index * this.itemHeight + this.headerHeight;
    }

    @Override
    protected int getRowBottom(int index) {
        return super.getRowBottom(index) - paddingTop;
    }

    @Override
    public void updateNarration(@NotNull NarrationElementOutput narrationElementOutput) {
    }

    // ══════════════════════════════════════════════════════════════════
    //  TexturedRow — one row in the AbstractSelectionList, holds N entries
    // ══════════════════════════════════════════════════════════════════

    @Environment(EnvType.CLIENT)
    public class TexturedRow extends Entry<TexturedRow> {

        private final TexturedEntry[] entries;

        public TexturedRow(TexturedEntry[] entries) {
            this.entries = entries;
        }

        /**
         * @return the entry at the given column, or null if column is empty.
         */
        @Nullable
        public TexturedEntry getEntry(int col) {
            if (col < 0 || col >= entries.length) return null;
            return entries[col];
        }

        public boolean contains(TexturedEntry entry) {
            for (TexturedEntry e : entries) {
                if (e == entry) return true;
            }
            return false;
        }

        @Override
        public void renderBack(@NotNull GuiGraphics guiGraphics,
                               int index,
                               int top, int left,
                               int rowWidth, int rowHeight,
                               int mouseX, int mouseY,
                               boolean hovering,
                               float fractionalTick) {

            int colWidth = getColumnWidth();
            WidgetInfoButtonImaged entryInfo = widgetInfo.getEntryButton();

            for (int c = 0; c < columns; c++) {
                TexturedEntry entry = getEntry(c);
                if (entry == null) continue;

                int colLeft = getColumnLeft(c);
                boolean colHovered = hovering
                        && mouseX >= colLeft && mouseX < colLeft + colWidth;
                boolean selected = entry == selectedEntry;

                entryInfo.pos(colLeft, top).size(colWidth, rowHeight);

                GuiTexture texture;
                if (selected) {
                    texture = entryInfo.getTextureHoveredSelected();
                    if (!colHovered || texture == null) {
                        texture = entryInfo.getTextureSelected();
                    }
                } else if (colHovered) {
                    texture = entryInfo.getTextureHovered();
                } else {
                    texture = entryInfo.getTexture();
                }
                if (texture == null) {
                    texture = entryInfo.getTexture();
                }

                texture.blit(guiGraphics, colLeft, top, colWidth, rowHeight);
                entryInfo.drawHighlight(guiGraphics, true, colHovered, selected);
            }
        }

        @Override
        public void render(@NotNull GuiGraphics guiGraphics,
                           int index,
                           int top, int left,
                           int rowWidth, int rowHeight,
                           int mouseX, int mouseY,
                           boolean hovering,
                           float fractionalTick) {

            Font font = TexturedSelectionList.this.minecraft.font;
            int colWidth = getColumnWidth();
            int color = widgetInfo.getTextColor().asInt();

            for (int c = 0; c < columns; c++) {
                TexturedEntry entry = getEntry(c);
                if (entry == null) continue;

                int colLeft = getColumnLeft(c);
                String text = entry.label.getString();

                int startX = colLeft + 4;
                int textWidth = colWidth - 8;

                GuiHelper.renderScalableText(
                        guiGraphics,
                        font,
                        text,
                        color,
                        startX, top,
                        textWidth, rowHeight,
                        true
                );
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0) {
                int col = getColumnAtX(mouseX);
                TexturedEntry entry = getEntry(col);
                if (entry != null) {
                    if (entry != selectedEntry) {
                        TexturedSelectionList.this.playSelectedSound(
                                Minecraft.getInstance().getSoundManager()
                        );
                    }
                    setSelectedEntry(entry);
                    return true;
                }
            }
            return false;
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  TexturedEntry — a single logical entry (id + label)
    // ══════════════════════════════════════════════════════════════════

    @Environment(EnvType.CLIENT)
    public static class TexturedEntry {
        @Getter
        private final String id;
        private Component label;

        public TexturedEntry(String id, Component label) {
            this.id = id;
            this.label = label;
        }

        public Component getLabel() {
            return label;
        }
    }
}