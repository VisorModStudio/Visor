package me.phoenixra.visor.api.client.gui.widgets.info;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import me.phoenixra.atumvr.api.misc.color.AtumColor;
import me.phoenixra.visor.api.client.gui.GuiTexture;
import me.phoenixra.visor.api.client.gui.overlay.options.OverlayOptionTextures;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

@Getter
public class WidgetInfoSelectionList extends WidgetInfo{


    private final GuiTexture textureEntry;
    private final GuiTexture textureEntryHovered;
    private final GuiTexture textureEntrySelected;

    @Setter @Accessors(chain = true)
    private GuiTexture textureEntryHoveredSelected;

    @Setter @Accessors(chain = true)
    private GuiTexture textureScrollBar = OverlayOptionTextures.SCROLL_BAR;
    @Setter @Accessors(chain = true)
    private GuiTexture textureScrollBarActive = OverlayOptionTextures.SCROLL_BAR_ACTIVE;


    @Setter @Accessors(chain = true)
    private AtumColor textColor = AtumColor.WHITE;

    @Setter @Accessors(chain = true)
    private int itemHeight = 15;

    @Setter @Accessors(chain = true)
    private int paddingTop = 3;

    @Setter @Accessors(chain = true)
    private int paddingLeft = 4;

    @Setter @Accessors(chain = true)
    private int scrollBarWidth = 4;

    /**
     * If supported, can deselect entry
     * and run callback with null value
     */
    @Setter @Accessors(chain = true)
    private boolean supportDeselection = false;

    /**
     * [element id -> tooltip[
     */
    @Setter @Accessors(chain = true)
    private Function<String, Component> tooltip;

    public WidgetInfoSelectionList(@NotNull GuiTexture textureEntry,
                                   @NotNull GuiTexture textureEntryHovered,
                                   @NotNull GuiTexture textureEntrySelected,
                                   int x, int y, int width, int height) {
        super(x, y, width, height);


        this.textureEntry = textureEntry;
        this.textureEntryHovered = textureEntryHovered;
        this.textureEntrySelected = textureEntrySelected;
        this.textureEntryHoveredSelected = textureEntrySelected;
        this.textureScrollBarActive = textureScrollBar;
    }
    public WidgetInfoSelectionList(@NotNull WidgetInfoSelectionList copyFrom,
                                  int x, int y, int width, int height) {
        super(x, y, width, height);
        textureEntry = copyFrom.textureEntry;
        textureEntryHovered = copyFrom.textureEntryHovered;
        textureEntrySelected = copyFrom.textureEntrySelected;
        textureEntryHoveredSelected = copyFrom.textureEntryHoveredSelected;
        textureScrollBar = copyFrom.textureScrollBar;
        textureScrollBarActive = copyFrom.textureScrollBarActive;
        textColor = copyFrom.textColor;
        itemHeight = copyFrom.itemHeight;
        paddingTop = copyFrom.paddingTop;
        paddingLeft = copyFrom.paddingLeft;
        scrollBarWidth = copyFrom.scrollBarWidth;
        supportDeselection = copyFrom.supportDeselection;
        tooltip = copyFrom.tooltip;
    }
}
