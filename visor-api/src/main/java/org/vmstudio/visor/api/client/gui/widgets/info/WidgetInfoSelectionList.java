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
public class WidgetInfoSelectionList extends WidgetInfo {


    /**
     * Info for entry buttons, only textures and highlight options used
     */
    @Accessors(chain = true)
    private WidgetInfoButtonImaged entryButton;

    @Accessors(chain = true)
    private AtumColor textColor = AtumColor.WHITE;


    @Accessors(chain = true)
    private GuiTexture textureScrollBar = OptionTextures.SCROLL_BAR;

    @Accessors(chain = true)
    private GuiTexture textureScrollBarActive = OptionTextures.SCROLL_BAR_ACTIVE;

    @Accessors(chain = true)
    private int scrollBarWidth = 4;


    @Accessors(chain = true)
    private int entryHeight = 15;

    @Accessors(chain = true)
    private int paddingTop = 3;

    @Accessors(chain = true)
    private int paddingLeft = 4;


    /**
     * Number of columns to display entries in.
     */
    @Accessors(chain = true)
    private int columns = 1;

    /**
     * Horizontal gap (in pixels) between columns.
     * Only relevant when {@link #columns} > 1.
     */
    @Accessors(chain = true)
    private int columnGap = 4;

    /**
     * If supported, can deselect entry
     * and run callback with null value
     */
    @Accessors(chain = true)
    private boolean supportDeselection = false;

    /**
     * [element id -> tooltip[
     */
    @Accessors(chain = true)
    private Function<String, Component> tooltip;


    public WidgetInfoSelectionList(@NotNull WidgetInfoSelectionList copyFrom) {
        super(copyFrom);
        entryButton = copyFrom.entryButton;
        textureScrollBar = copyFrom.textureScrollBar;
        textureScrollBarActive = copyFrom.textureScrollBarActive;
        textColor = copyFrom.textColor;
        entryHeight = copyFrom.entryHeight;
        paddingTop = copyFrom.paddingTop;
        paddingLeft = copyFrom.paddingLeft;
        scrollBarWidth = copyFrom.scrollBarWidth;
        supportDeselection = copyFrom.supportDeselection;
        tooltip = copyFrom.tooltip;
    }
    public WidgetInfoSelectionList() {

    }

    public WidgetInfoSelectionList scrollBarTextures(GuiTexture textureScrollBar,
                                                     GuiTexture textureScrollBarActive){
        return setTextureScrollBar(textureScrollBar)
                .setTextureScrollBarActive(textureScrollBarActive);
    }

    @Override
    public WidgetInfoSelectionList size(int width, int height) {
        return (WidgetInfoSelectionList) super.size(width, height);
    }

    @Override
    public WidgetInfoSelectionList pos(int x, int y) {
        return (WidgetInfoSelectionList) super.pos(x, y);
    }
}
