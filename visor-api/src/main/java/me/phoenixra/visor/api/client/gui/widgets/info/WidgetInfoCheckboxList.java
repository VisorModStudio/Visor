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
public class WidgetInfoCheckboxList extends WidgetInfo{

    private final GuiTexture textureEntry;
    private final GuiTexture textureCheckbox;
    private final GuiTexture textureCheckboxHovered;
    private final GuiTexture textureCheckboxSelected;

    @Setter @Accessors(chain = true)
    private GuiTexture textureCheckboxHoveredSelected;

    @Setter @Accessors(chain = true)
    private GuiTexture textureScrollBar = OverlayOptionTextures.SCROLL_BAR;
    @Setter @Accessors(chain = true)
    private GuiTexture textureScrollBarActive = OverlayOptionTextures.SCROLL_BAR_ACTIVE;

    @Setter
    @Accessors(chain = true)
    private AtumColor textColor = AtumColor.WHITE;

    @Setter @Accessors(chain = true)
    private int itemHeight = 15;

    @Setter @Accessors(chain = true)
    private int paddingTop = 3;

    @Setter @Accessors(chain = true)
    private int paddingLeft = 4;

    @Setter @Accessors(chain = true)
    private int paddingCheckbox = 5;

    @Setter @Accessors(chain = true)
    private int scrollBarWidth = 4;

    /**
     * [element id -> tooltip[
     */
    @Setter @Accessors(chain = true)
    private Function<String, Component> tooltip;

    public WidgetInfoCheckboxList(@NotNull GuiTexture textureEntry,
                                  @NotNull GuiTexture textureCheckbox,
                                  @NotNull GuiTexture textureCheckboxHovered,
                                  @NotNull GuiTexture textureCheckboxSelected,
                                  int x, int y, int width, int height) {
        super(x, y, width, height);
        this.textureEntry = textureEntry;
        this.textureCheckbox = textureCheckbox;
        this.textureCheckboxHovered = textureCheckboxHovered;
        this.textureCheckboxSelected = textureCheckboxSelected;
        this.textureCheckboxHoveredSelected = textureCheckboxSelected;


    }

    public WidgetInfoCheckboxList(@NotNull WidgetInfoCheckboxList copyFrom,
                                  int x, int y, int width, int height) {
        super(x, y, width, height);
        textureEntry = copyFrom.textureEntry;
        textureCheckbox = copyFrom.textureCheckbox;
        textureCheckboxHovered = copyFrom.textureCheckboxHovered;
        textureCheckboxSelected = copyFrom.textureCheckboxSelected;
        textureCheckboxHoveredSelected = copyFrom.textureCheckboxHoveredSelected;
        textureScrollBar = copyFrom.textureScrollBar;
        textureScrollBarActive = copyFrom.textureScrollBarActive;
        textColor = copyFrom.textColor;
        itemHeight = copyFrom.itemHeight;
        paddingTop = copyFrom.paddingTop;
        paddingLeft = copyFrom.paddingLeft;
        paddingCheckbox = copyFrom.paddingCheckbox;
        scrollBarWidth = copyFrom.scrollBarWidth;
        tooltip = copyFrom.tooltip;
    }
}
