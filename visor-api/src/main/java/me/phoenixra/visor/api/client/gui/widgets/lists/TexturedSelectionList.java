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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;


public class TexturedSelectionList extends AbstractSelectionList<TexturedSelectionList.TexturedEntry> {

    @Getter
    private final WidgetInfoSelectionList widgetInfo;

    private final int paddingTop;
    private final int paddingLeft;
    private final int scrollBarWidth;

    private final Consumer<TexturedEntry> onSelected;


    private final Map<String, TexturedEntry> entriesMap = new HashMap<>();

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

    public TexturedSelectionList(@NotNull WidgetInfoSelectionList widgetInfo,
                                 @NotNull Map<String, String> rawEntries,
                                 @NotNull Consumer<TexturedEntry> onSelected) {
        super(Minecraft.getInstance(),
                widgetInfo.getWidth(),
                widgetInfo.getHeight(),
                widgetInfo.getY(),
                widgetInfo.getY()+widgetInfo.getHeight(),
                widgetInfo.getEntryHeight()
        );

        this.widgetInfo = widgetInfo;
        this.paddingTop = widgetInfo.getPaddingTop();
        this.paddingLeft = widgetInfo.getPaddingLeft();
        this.scrollBarWidth = widgetInfo.getScrollBarWidth();

        this.onSelected = onSelected;

        this.setLeftPos(widgetInfo.getX());
        this.setRenderTopAndBottom(false);
        this.setRenderBackground(false);
        this.setRenderSelection(false);

        resetEntries(rawEntries);
    }



    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.hovered = this.isMouseOver(mouseX, mouseY) ? this.getEntryAtPosition(mouseX, mouseY) : null;
        if(scrolling
                && lastDragCall + 200 < System.currentTimeMillis()){
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

            int thumbH = (int)(viewH * (float)viewH / ((float)viewH + maxScroll));
            thumbH = Mth.clamp(thumbH, 32, viewH - 8);

            int thumbY = trackTop
                    + (int)(this.getScrollAmount() * (viewH - thumbH) / (float)maxScroll);

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

        for(int m = 0; m < l; ++m) {
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

        TexturedEntry entryForTooltip = this.hovered;

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

    private Screen getAttachedTo(){
        if(visor$attachedTo == null){
            if(VisorAPI.clientState().stateMode().isNotActive()){
                visor$attachedTo = Minecraft.getInstance().screen;
                return visor$attachedTo;
            }
            VROverlayScreen overlay = VisorAPI.client().getGuiManager()
                    .getCursorHandler()
                    .getFocusedOverlayScreen();


            if(overlay != null){
                visor$attachedTo = overlay;
            }else{
                visor$attachedTo = Minecraft.getInstance().screen;
            }
            return visor$attachedTo;
        }

        return visor$attachedTo;
    }


    public void filterEntries(
            @NotNull Function<Map.Entry<String, String>, Boolean> filter
    ){
        this.clearEntries();
        entriesMap.clear();
        setScrollAmount(0);
        for(var entry : rawEntries.entrySet()){
            //filtering
            if(!filter.apply(entry)){
                continue;
            }
            //passed
            var texturedEntry = new TexturedEntry(
                    entry.getKey(),
                    Component.literal(entry.getValue())
            );
            this.addEntry(
                    texturedEntry
            );
            entriesMap.put(texturedEntry.id, texturedEntry);
        }
    }

    public void resetEntries(@NotNull Map<String, String> rawEntries){
        clearEntries();
        entriesMap.clear();
        setScrollAmount(0);
        for(var entry : rawEntries.entrySet()){
            var texturedEntry = new TexturedEntry(
                    entry.getKey(),
                    Component.literal(entry.getValue())
            );
            this.addEntry(
                    texturedEntry
            );
            entriesMap.put(texturedEntry.id, texturedEntry);
        }
        this.rawEntries = rawEntries;
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


    public @Nullable TexturedEntry getEntry(@NotNull String id){
        return entriesMap.get(id);
    }

    public void scrollTo(@NotNull TexturedEntry entry) {
        int maxScroll = this.getMaxScroll();
        if (maxScroll <= 0) {
            return;
        }
        int idx = this.children().indexOf(entry);
        if (idx < 0) {
            return;
        }
        double desired = (double)idx * this.itemHeight;
        this.setScrollAmount(desired);
    }

    @Override
    protected void updateScrollingState(double mouseX, double mouseY, int button) {
        super.updateScrollingState(mouseX, mouseY, button);
        if(scrolling){
            lastDragCall = System.currentTimeMillis();
        }
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if(scrolling) {
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



    @Override
    protected int getScrollbarPosition() {
        return this.x0 + this.width
                - (scrollBarWidth + 2);
    }

    @Override
    public int getRowWidth() {
        return this.width
                - (scrollBarWidth)
                - paddingLeft * 2;
    }

    @Override
    public int getRowLeft() {
        return this.x0 + paddingLeft;
    }

    @Override
    protected int getRowTop(int index) {
        return this.y0 + paddingTop - (int)this.getScrollAmount() + index * this.itemHeight + this.headerHeight;
    }

    @Override
    protected int getRowBottom(int index) {
        return super.getRowBottom(index) - paddingTop;
    }


    @Override
    public void updateNarration(@NotNull NarrationElementOutput narrationElementOutput) {

    }

    @Override
    public void setSelected(@Nullable TexturedSelectionList.TexturedEntry selected) {
        if(selected != getSelected()) {
            onSelected.accept(selected);
        }else if(widgetInfo.isSupportDeselection() && selected != null){
            this.playSelectedSound(Minecraft.getInstance().getSoundManager());
            onSelected.accept(null);
            super.setSelected(null);
            return;
        }
        super.setSelected(selected);
    }
    public void setSelected(@NotNull String id) {
        var entry = getEntry(id);
        if(entry == null){
            return;
        }
        setSelected(entry);
    }

    public void playSelectedSound(SoundManager handler) {
        handler.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    @Environment(EnvType.CLIENT)
    public class TexturedEntry extends Entry<TexturedEntry> {
        @Getter
        private final String id;
        private Component label;

        public TexturedEntry(String id, Component label) {
            this.id = id;
            this.label = label;
        }

        @Override
        public void renderBack(@NotNull GuiGraphics guiGraphics,
                               int index,
                               int top, int left,
                               int rowWidth, int rowHeight,
                               int mouseX, int mouseY,
                               boolean hovering,
                               float fractionalTick
        ) {

            WidgetInfoButtonImaged entryInfo = widgetInfo.getEntryButton();
            entryInfo.pos(getRowLeft(), top).size(getRowWidth(), rowHeight);

            GuiTexture texture;
            boolean selected = Objects.equals(getSelected(), this);
            if (selected) {
                texture = entryInfo.getTextureHoveredSelected();
                if (!hovering || texture == null) {
                    texture = entryInfo.getTextureSelected();
                }
            } else if (hovering) {
                texture = entryInfo.getTextureHovered();
            } else {
                texture = entryInfo.getTexture();
            }
            if(texture == null){
                texture = entryInfo.getTexture();
            }

            texture.blit(
                    guiGraphics,
                    getRowLeft(), top,
                    getRowWidth(), rowHeight
            );

            entryInfo.drawHighlight(guiGraphics, true, hovering, selected);

        }

        @Override
        public void render(@NotNull GuiGraphics guiGraphics,
                           int index,
                           int top, int left,
                           int rowWidth, int rowHeight,
                           int mouseX, int mouseY,
                           boolean hovering,
                           float fractionalTick
        ) {

            Font font = TexturedSelectionList.this.minecraft.font;
            String text = label.getString();

            int startX = getRowLeft() + 4;
            int textWidth = getRowWidth() - 8;
            int color = widgetInfo.getTextColor().asInt();

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

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if(button == 0){
                if(this != getSelected()) {
                    TexturedSelectionList.this.playSelectedSound(Minecraft.getInstance().getSoundManager());
                }
                return true;
            }
            return false;
        }


    }
}
