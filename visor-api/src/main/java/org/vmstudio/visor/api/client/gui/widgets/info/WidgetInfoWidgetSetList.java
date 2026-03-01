package org.vmstudio.visor.api.client.gui.widgets.info;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.vmstudio.visor.api.client.gui.GuiTexture;
import org.vmstudio.visor.api.client.gui.overlays.options.OptionTextures;
import org.jetbrains.annotations.NotNull;

@Getter @Setter
public class WidgetInfoWidgetSetList extends WidgetInfo {

    @Accessors(chain = true)
    private GuiTexture textureScrollBar = OptionTextures.SCROLL_BAR;

    @Accessors(chain = true)
    private GuiTexture textureScrollBarActive = OptionTextures.SCROLL_BAR_ACTIVE;

    @Accessors(chain = true)
    private int scrollBarWidth = 4;

    /**
     * Height of each entry row (the area each WidgetSet occupies).
     */
    @Accessors(chain = true)
    private int entryHeight = 30;

    /**
     * Vertical gap between entries.
     */
    @Accessors(chain = true)
    private int entryGap = 2;

    @Accessors(chain = true)
    private int paddingTop = 3;

    @Accessors(chain = true)
    private int paddingLeft = 4;

    /**
     * Number of columns. Defaults to 1.
     */
    @Accessors(chain = true)
    private int columns = 1;

    /**
     * Horizontal gap between columns.
     * Only relevant when {@link #columns} > 1.
     */
    @Accessors(chain = true)
    private int columnGap = 2;


    public WidgetInfoWidgetSetList(@NotNull WidgetInfoWidgetSetList copyFrom) {
        super(copyFrom);
        textureScrollBar = copyFrom.textureScrollBar;
        textureScrollBarActive = copyFrom.textureScrollBarActive;
        scrollBarWidth = copyFrom.scrollBarWidth;
        entryHeight = copyFrom.entryHeight;
        entryGap = copyFrom.entryGap;
        paddingTop = copyFrom.paddingTop;
        paddingLeft = copyFrom.paddingLeft;
        columns = copyFrom.columns;
        columnGap = copyFrom.columnGap;
    }

    public WidgetInfoWidgetSetList() {
    }

    public WidgetInfoWidgetSetList scrollBarTextures(GuiTexture textureScrollBar,
                                                     GuiTexture textureScrollBarActive) {
        return setTextureScrollBar(textureScrollBar)
                .setTextureScrollBarActive(textureScrollBarActive);
    }

    @Override
    public WidgetInfoWidgetSetList size(int width, int height) {
        return (WidgetInfoWidgetSetList) super.size(width, height);
    }

    @Override
    public WidgetInfoWidgetSetList pos(int x, int y) {
        return (WidgetInfoWidgetSetList) super.pos(x, y);
    }
}