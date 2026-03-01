package org.vmstudio.visor.api.client.gui.widgets.info;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import me.phoenixra.atumvr.api.misc.color.AtumColor;
import org.vmstudio.visor.api.client.gui.GuiTexture;
import org.vmstudio.visor.api.client.gui.overlays.options.OptionTextures;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

@Getter @Setter
public class WidgetInfoCheckboxList extends WidgetInfo {

    @Accessors(chain = true)
    private GuiTexture textureEntry;
    @Accessors(chain = true)
    private GuiTexture textureCheckbox;
    @Accessors(chain = true)
    private GuiTexture textureCheckboxHovered;
    @Accessors(chain = true)
    private GuiTexture textureCheckboxSelected;

    @Accessors(chain = true)
    private GuiTexture textureCheckboxHoveredSelected;

    @Accessors(chain = true)
    private GuiTexture textureScrollBar = OptionTextures.SCROLL_BAR;
    @Accessors(chain = true)
    private GuiTexture textureScrollBarActive = OptionTextures.SCROLL_BAR_ACTIVE;


    @Accessors(chain = true)
    private AtumColor textColor = AtumColor.WHITE;

    @Accessors(chain = true)
    private int itemHeight = 15;

    @Accessors(chain = true)
    private int paddingTop = 3;

    @Accessors(chain = true)
    private int paddingLeft = 4;

    @Accessors(chain = true)
    private int paddingCheckbox = 5;

    @Accessors(chain = true)
    private int scrollBarWidth = 4;

    /**
     * If true - checkbox will be on the left,
     * otherwise on the right
     */
    @Accessors(chain = true)
    private boolean checkboxLeftSided = true;
    /**
     * [element id -> tooltip[
     */
    @Accessors(chain = true)
    private Function<String, Component> tooltip;


    public WidgetInfoCheckboxList(@NotNull WidgetInfoCheckboxList copyFrom) {
        super(copyFrom);
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
        checkboxLeftSided = copyFrom.checkboxLeftSided;
        tooltip = copyFrom.tooltip;
    }

    public WidgetInfoCheckboxList() {

    }

    public WidgetInfoCheckboxList textures(GuiTexture textureEntry,
                                           GuiTexture textureCheckbox,
                                           GuiTexture textureCheckboxHovered,
                                           GuiTexture textureCheckboxSelected,
                                           GuiTexture textureCheckboxHoveredSelected){
        return setTextureEntry(textureEntry)
                .setTextureCheckbox(textureCheckbox)
                .setTextureCheckboxHovered(textureCheckboxHovered)
                .setTextureCheckboxSelected(textureCheckboxSelected)
                .setTextureCheckboxHoveredSelected(textureCheckboxHoveredSelected);
    }
    public WidgetInfoCheckboxList scrollBarTextures(GuiTexture textureScrollBar,
                                                     GuiTexture textureScrollBarActive){
        return setTextureScrollBar(textureScrollBar)
                .setTextureScrollBarActive(textureScrollBarActive);
    }

    @Override
    public WidgetInfoCheckboxList pos(int x, int y) {
        return (WidgetInfoCheckboxList) super.pos(x, y);
    }

    @Override
    public WidgetInfoCheckboxList size(int width, int height) {
        return (WidgetInfoCheckboxList) super.size(width, height);
    }
}
