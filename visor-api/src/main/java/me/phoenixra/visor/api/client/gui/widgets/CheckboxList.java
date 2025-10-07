package me.phoenixra.visor.api.client.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Getter;
import me.phoenixra.visor.api.client.gui.helpers.GuiHelper;
import me.phoenixra.visor.api.client.gui.GuiTexture;
import me.phoenixra.visor.api.client.gui.widgets.info.WidgetInfoCheckboxList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.narration.NarrationElementOutput;
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

public class CheckboxList extends AbstractSelectionList<CheckboxList.CheckboxEntry> {


    private final int paddingTop;
    private final int paddingLeft;
    private final int paddingCheckbox;
    private final int scrollBarWidth;

    @Getter
    private final WidgetInfoCheckboxList widgetInfo;

    private final Consumer<CheckboxEntry> onChanged;

    private final Map<String, CheckboxEntry> entriesMap = new HashMap<>();

    private Map<String, String> rawEntries;
    private Map<String, Boolean> rawEntriesState;


    private long lastDragCall = -1;

    public CheckboxList(@NotNull WidgetInfoCheckboxList widgetInfo,
                        @NotNull Map<String, String> rawEntries,
                        @NotNull List<String> selectedEntries,
                        @Nullable Consumer<CheckboxEntry> onChanged) {
        super(Minecraft.getInstance(),
                widgetInfo.getWidth(),
                widgetInfo.getHeight(),
                widgetInfo.getY(),
                widgetInfo.getY()+widgetInfo.getHeight(),
                widgetInfo.getItemHeight()
        );

        this.widgetInfo = widgetInfo;

        this.paddingTop = widgetInfo.getPaddingTop();
        this.paddingLeft = widgetInfo.getPaddingLeft();
        this.paddingCheckbox = widgetInfo.getPaddingCheckbox();
        this.scrollBarWidth = widgetInfo.getScrollBarWidth();

        this.onChanged = onChanged;

        this.setLeftPos(widgetInfo.getX());
        this.setRenderTopAndBottom(false);
        this.setRenderBackground(false);
        this.setRenderSelection(false);

        resetEntries(rawEntries, selectedEntries);
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
            var checkboxEntry = new CheckboxEntry(
                    entry.getKey(),
                    Component.literal(entry.getValue()),
                    rawEntriesState.get(entry.getKey())
            );
            this.addEntry(
                    checkboxEntry
            );
            entriesMap.put(checkboxEntry.id, checkboxEntry);
        }
    }

    public void resetEntries(@NotNull Map<String, String> rawEntries,
                             @NotNull List<String> selectedEntries){

        clearEntries();
        entriesMap.clear();
        setScrollAmount(0);
        for(var entry : rawEntries.entrySet()){
            var checkboxEntry = new CheckboxEntry(
                    entry.getKey(),
                    Component.literal(entry.getValue()),
                    selectedEntries.contains(entry.getKey())
            );
            this.addEntry(
                    checkboxEntry
            );
            entriesMap.put(checkboxEntry.id, checkboxEntry);
        }
        this.rawEntries = rawEntries;
        this.rawEntriesState = new HashMap<>();
        for(var id : rawEntries.keySet()){
            rawEntriesState.put(
                    id,
                    selectedEntries.contains(id)
            );
        }
    }

    public void changeSelectedAll(boolean flag){
        for(var entry : children()){
            entry.setSelected(flag);
        }
    }



    public boolean isAllSelected(){
        for(var entry : rawEntriesState.entrySet()){
            if(!entry.getValue()) {
                return false;
            }
        }
        return true;
    }
    public boolean isAllNotSelected(){
        for(var entry : rawEntriesState.entrySet()){
            if(entry.getValue()) {
                return false;
            }
        }
        return true;
    }

    public @Nullable CheckboxEntry getEntry(@NotNull String id){
        return entriesMap.get(id);
    }

    public List<CheckboxList.CheckboxEntry> getSelectedEntries(){
        return children().stream()
                .filter(it->it.selected)
                .toList();
    }

    public List<String> getSelectedEntriesId(){
        return children().stream()
                .filter(it->it.selected)
                .map(it->it.id)
                .toList();
    }

    public List<String> getEntriesId(){
        return children().stream()
                .map(it->it.id)
                .toList();
    }

    public void scrollTo(@NotNull CheckboxEntry entry) {
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
    protected @Nullable CheckboxEntry getEntryAtPosition(double mouseX, double mouseY) {
        int i = this.getRowWidth() / 2;
        int j = this.x0 + this.width / 2;
        int k = j - i;
        int l = j + i;
        int m = Mth.floor(mouseY - (double)this.y0) - this.headerHeight + (int)this.getScrollAmount() - 4;
        int n = m / this.itemHeight;
        var entry = mouseX < (double)this.getScrollbarPosition()
                && mouseX >= (double)k
                && mouseX <= (double)l && n >= 0
                && m >= 0
                && n < this.getItemCount()
                ? this.children().get(n)
                : null;
        if(entry == null){
            return null;
        }
        if(!entry.isCheckboxHovered(mouseX, mouseY)){
            return null;
        }
        return entry;
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
                - (scrollBarWidth + 2)  - paddingCheckbox;
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
    public void setSelected(@Nullable CheckboxList.CheckboxEntry entry) {
        if(entry == null){
            return;
        }
        entry.setSelected(!entry.selected);
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
    public class CheckboxEntry extends Entry<CheckboxEntry> {
        @Getter
        private final String id;
        private final Component label;

        @Getter
        private boolean selected;

        private GuiTexture checkboxTex = widgetInfo.getTextureCheckbox();

        public CheckboxEntry(String id, Component label, boolean selected) {
            this.id = id;
            this.label = label;
            this.selected = selected;
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

            if(selected){
                checkboxTex = widgetInfo.getTextureCheckboxHoveredSelected();
                if(!hovering || checkboxTex == null){
                    checkboxTex = widgetInfo.getTextureCheckboxSelected();
                }
            } else if (hovering) {
                checkboxTex =  widgetInfo.getTextureCheckboxHovered();
            }else{
                checkboxTex = widgetInfo.getTextureCheckbox();
            }
            int iconX = getRowLeft() + paddingCheckbox;
            int iconY = top + (rowHeight - checkboxTex.getHeight()) / 2;

            checkboxTex.blit(
                    guiGraphics,
                    iconX, iconY,
                    checkboxTex.getWidth(), checkboxTex.getHeight()
            );


            Font font = CheckboxList.this.minecraft.font;
            String text = label.getString();
            int textX = iconX + checkboxTex.getWidth()
                    + paddingCheckbox;
            int textY = top + (rowHeight - font.lineHeight) / 2;
            int textMaxHeight = rowHeight - (rowHeight - font.lineHeight) / 2;
            int textMaxWidth = rowWidth - (textX - getRowLeft())
                    - paddingCheckbox;
            int color  = widgetInfo.getTextColor().toInt();

            widgetInfo.getTextureEntry().blit(
                    guiGraphics,
                    textX, top,
                    textMaxWidth, rowHeight
            );

            GuiHelper.renderScalableText(
                    guiGraphics,
                    font, text,
                    color,
                    textX+4, textY,
                    textMaxWidth-8, textMaxHeight,
                    true
            );
        }


        public void setSelected(boolean flag){
            if(selected == flag){
                return;
            }
            selected = flag;
            CheckboxList.this.rawEntriesState.put(
                    id, selected
            );

            if(onChanged != null) {
                onChanged.accept(this);
            }
        }

        public boolean isCheckboxHovered(double mouseX, double mouseY) {
            int idx = CheckboxList.this.children().indexOf(this);
            if (idx < 0) return false;

            int rowTop = CheckboxList.this.getRowTop(idx);

            int iconX = getRowLeft() + paddingCheckbox;
            int iconY = rowTop + (itemHeight - checkboxTex.getHeight()) / 2;
            int iconW = checkboxTex.getWidth();
            int iconH = checkboxTex.getHeight();

            return mouseX >= iconX && mouseX <= iconX + iconW
                    && mouseY >= iconY && mouseY <= iconY + iconH;
        }


        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if(button == 0){
                CheckboxList.this.playSelectedSound(Minecraft.getInstance().getSoundManager());
                return true;
            }
            return false;
        }




    }
}
