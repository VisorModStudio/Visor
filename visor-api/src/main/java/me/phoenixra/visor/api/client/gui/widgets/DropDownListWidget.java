package me.phoenixra.visor.api.client.gui.widgets;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;


public class DropDownListWidget extends AbstractButton {

    private final int ITEM_HEIGHT = 12;

    @Setter
    private int visibleItems = 5;


    @Getter
    private final List<Component> items;
    @Getter
    private boolean expanded = false;
    @Getter
    private int selectedIndex = -1;
    private int scrollOffset = 0;



    //dragging
    @Getter
    private boolean draggingScrollbar = false;
    private int dragStartY = 0;
    private int initialScrollOffset = 0;


    //text scrolling
    private long startTextScrolling = -1;
    private long elementScrollingText = -1;
    private final Component defaultMessage;

    private final Consumer<Integer> responder;


    public DropDownListWidget(int x, int y,
                              int width, int height,
                              Component message,
                              List<Component> items,
                              int startIndex,
                              Consumer<Integer> responder) {
        super(x, y, width, height, message);
        this.defaultMessage = message;
        this.items = items;
        this.responder = responder;
        setSelectedIndex(startIndex, false);
    }
    public DropDownListWidget(int x, int y,
                              int width, int height,
                              Component message, List<Component> items) {
        this(x, y, width, height, message,items,-1,null);
    }

    /**
     * Called when the main button is pressed. Here we simply toggle the expanded state.
     */
    @Override
    public void onPress() {
        expanded = !expanded;
    }




    /**
     * Renders the base button and, if expanded, the dropdown list along with the interactive scrollbar.
     */
    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Render the base button (background, border, and label)
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);

        if (expanded) {
            int dropdownX = this.getX();
            int dropdownY = this.getY() + this.getHeight();
            int dropdownHeight = visibleItems * ITEM_HEIGHT;
            // Draw the dropdown background
            guiGraphics.fill(dropdownX, dropdownY, dropdownX + this.getWidth(), dropdownY + dropdownHeight, 0xFF000000);

            // Render each visible item in the dropdown.
            Minecraft mc = Minecraft.getInstance();
            var font = mc.font;
            // Padding inside each item
            int padding = 2;
            // Adjust available width for text if scrollbar is present.
            int availableWidth = (items.size() > visibleItems) ? (this.getWidth() - 12) : (this.getWidth() - 4);
            for (int i = 0; i < visibleItems; i++) {
                int itemIndex = i + scrollOffset;
                if (itemIndex >= items.size()) break;
                int itemY = dropdownY + i * ITEM_HEIGHT;
                Component itemText = items.get(itemIndex);
                String text = itemText.getString();



                // Check if the mouse is hovering this item
                boolean itemHovered = (mouseX >= dropdownX && mouseX < dropdownX + this.getWidth()
                        && mouseY >= itemY && mouseY < itemY + ITEM_HEIGHT);

                // If the item is selected, draw its selected background; otherwise, if hovered, draw hover highlight.
                if (itemIndex == selectedIndex) {
                    int selectedBackgroundColor = 0x803399FF; // semi-transparent blue
                    guiGraphics.fill(dropdownX, itemY, dropdownX + this.getWidth(), itemY + ITEM_HEIGHT, selectedBackgroundColor);
                } else if (itemHovered) {
                    int hoveredBackgroundColor = 0xC0333333; // semi-transparent dark grey; adjust as needed
                    guiGraphics.fill(dropdownX, itemY, dropdownX + this.getWidth(), itemY + ITEM_HEIGHT, hoveredBackgroundColor);
                }


                int textColor = (itemIndex == selectedIndex) ? 0xFFFFFFFF : 0xFFCCCCCC;
                int textWidth = font.width(text);

                // If text is too wide, animate scrolling on hover
                if (textWidth > availableWidth) {
                    if (itemHovered) {
                        if(startTextScrolling == -1 || elementScrollingText != i){
                            elementScrollingText = i;
                            startTextScrolling = System.currentTimeMillis();
                        }
                        // Animation parameters
                        int msPerPixel = 50;    // 50 ms per pixel
                        int holdTimeStart = 1000; // 1 second hold at the start
                        int holdTimeEnd = 1000;   // 1 second hold at the end
                        int extra = textWidth - availableWidth;
                        int scrollTime = extra * msPerPixel;
                        int cycleTime = holdTimeStart + scrollTime + holdTimeEnd;
                        long timePassed = System.currentTimeMillis() - startTextScrolling;
                        long t = timePassed % cycleTime;
                        int offset;
                        if (t < holdTimeStart) {
                            // Hold at the beginning
                            offset = 0;
                        } else if (t < holdTimeStart + scrollTime) {
                            // Scroll smoothly
                            offset = (int)((t - holdTimeStart) / (float)msPerPixel);
                        } else {
                            // Hold at the end
                            offset = extra;
                        }

                        guiGraphics.enableScissor(dropdownX + padding, itemY, dropdownX + padding + availableWidth, itemY + ITEM_HEIGHT);
                        guiGraphics.drawString(font, text, dropdownX + padding - offset, itemY + (ITEM_HEIGHT - 8) / 2, textColor);
                        guiGraphics.disableScissor();
                    } else {
                        if(elementScrollingText == i) {
                            startTextScrolling = -1;
                        }
                        // Not hovered: draw truncated text
                        String truncatedText = font.plainSubstrByWidth(text, availableWidth);
                        guiGraphics.drawString(font, truncatedText, dropdownX + padding, itemY + (ITEM_HEIGHT - 8) / 2, textColor);
                    }
                } else {
                    // If text fits, center it.
                    guiGraphics.drawCenteredString(font, text, dropdownX + this.getWidth() / 2, itemY + (ITEM_HEIGHT - 8) / 2, textColor);
                }
            }

            // Draw the scrollbar if necessary
            if (items.size() > visibleItems) {
                int scrollbarWidth = 8;
                int scrollbarX = dropdownX + this.getWidth() - scrollbarWidth;
                int scrollbarHeight = dropdownHeight;
                // Draw scrollbar background
                guiGraphics.fill(scrollbarX, dropdownY, scrollbarX + scrollbarWidth, dropdownY + scrollbarHeight, 0xFF444444);
                int thumbHeight = Math.max(10, scrollbarHeight * visibleItems / items.size());
                int maxScroll = items.size() - visibleItems;
                int thumbY = dropdownY + (maxScroll == 0 ? 0 : (scrollOffset * (scrollbarHeight - thumbHeight) / maxScroll));
                // Change the thumb color if dragging
                int thumbColor = draggingScrollbar ? 0xFFCCCCCC : 0xFF888888;
                // Draw the scrollbar thumb
                guiGraphics.fill(scrollbarX, thumbY, scrollbarX + scrollbarWidth, thumbY + thumbHeight, thumbColor);
            }
        }
    }

    /**
     * When dragging the scrollbar thumb, update the scroll offset based on the drag delta.
     */
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (draggingScrollbar) {

            int scrollbarHeight = visibleItems * ITEM_HEIGHT;
            int thumbHeight = Math.max(10, scrollbarHeight * visibleItems / items.size());
            int maxScroll = items.size() - visibleItems;
            int availableTrack = scrollbarHeight - thumbHeight;
            int deltaY = (int) (mouseY - dragStartY);

            // Compute the initial thumb position based on the initial scroll offset.
            int initialThumbPos = (int) (initialScrollOffset * availableTrack / (float) maxScroll);
            int newThumbPos = initialThumbPos + deltaY;

            // Update scroll offset proportionally.
            int newScrollOffset = (int) (newThumbPos * maxScroll / (float) availableTrack);
            scrollOffset = Mth.clamp(newScrollOffset, 0, maxScroll);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    /**
     * When the mouse button is released, stop dragging the scrollbar.
     */
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (draggingScrollbar) {
            draggingScrollbar = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }
    /**
     * Handle mouse clicks. In addition to toggling the dropdown or selecting an item,
     * we check if the user clicked on the scrollbar thumb to initiate dragging.
     */
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.active || !this.visible) {
            return false;
        }

        int dropdownX = this.getX();
        int dropdownY = this.getY() + this.getHeight();
        int dropdownWidth = this.getWidth();
        int dropdownHeight = visibleItems * ITEM_HEIGHT;

        // If expanded and a scrollbar is visible, check if the click is on the thumb.
        if (expanded && items.size() > visibleItems) {
            int scrollbarWidth = 8;
            int scrollbarX = dropdownX + this.getWidth() - scrollbarWidth - 2;
            int thumbHeight = Math.max(10, dropdownHeight * visibleItems / items.size());
            int maxScroll = items.size() - visibleItems;
            int thumbY = dropdownY + (maxScroll == 0 ? 0 : (scrollOffset * (dropdownHeight - thumbHeight) / maxScroll));
            if (mouseX >= scrollbarX && mouseX <= scrollbarX + scrollbarWidth &&
                    mouseY >= thumbY && mouseY <= thumbY + thumbHeight) {
                draggingScrollbar = true;
                dragStartY = (int) mouseY;
                initialScrollOffset = scrollOffset;
                return true;
            }
        }

        boolean buttonClicked = this.clicked(mouseX, mouseY);
        boolean dropdownClicked = expanded &&
                (mouseX >= dropdownX && mouseX <= dropdownX + dropdownWidth &&
                        mouseY >= dropdownY && mouseY <= dropdownY + dropdownHeight);

        if (buttonClicked) {
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            expanded = !expanded;
            return true;
        } else if (dropdownClicked) {
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            int clickedIndex = (int) ((mouseY - dropdownY) / ITEM_HEIGHT) + scrollOffset;
            if (clickedIndex < items.size()) {
                setSelectedIndex(clickedIndex, true);
            }
            expanded = false;
            return true;
        } else if (expanded) {
            // Clicked outside while expanded: collapse the dropdown.
            expanded = false;
        }
        return false;
    }


    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollDelta) {
        if (expanded && items.size() > visibleItems) {
            scrollOffset = (int) Mth.clamp(scrollOffset - scrollDelta, 0, items.size() - visibleItems);
            return true;
        }
        return false;
    }


    public void setSelectedIndex(int selectedIndex, boolean useResponder) {

        this.selectedIndex = selectedIndex;
        setMessage(getSelectedItem());

        if(selectedIndex >= 0){
            setTooltip(Tooltip.create(defaultMessage));
            if(this.responder != null && useResponder) {
                this.responder.accept(selectedIndex);
            }
        }else{
            setTooltip(null);
        }
    }

    /**
     * Returns the currently selected item (or null if none).
     */
    public Component getSelectedItem() {
        return (selectedIndex >= 0 && selectedIndex < items.size()) ? items.get(selectedIndex) : defaultMessage;
    }

    /**
     * Provides narration text for accessibility. Customize as needed.
     */
    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, Component.translatable("narration.dropdown.title", this.getMessage()));
    }


    public static Builder builder(@NotNull List<Component> items){
        return new Builder(items);
    }
    public static class Builder{
        private int x = 0;
        private int y = 0;
        private int width = 50;
        private int height = 15;

        private List<Component> items;
        @Setter @Accessors(chain = true)
        private int visibleItems = 5;
        @Setter @Accessors(chain = true)
        private int startIndex = -1;
        @Setter @Accessors(chain = true)
        private Component message = Component.literal("Select");
        @Setter @Accessors(chain = true)
        private Consumer<Integer> responder = null;

        public Builder(@NotNull List<Component> items){
            this.items = items;
        }
        public Builder pos(int x, int y){
            this.x = x;
            this.y = y;
            return this;
        }
        public Builder size(int width, int height){
            this.width = width;
            this.height = height;
            return this;
        }

        public DropDownListWidget build(){
            DropDownListWidget out =  new DropDownListWidget(
                    x,y,
                    width,height,
                    message,
                    items,
                    startIndex,
                    responder
            );
            out.setVisibleItems(visibleItems);
            return out;
        }

    }
}
