package org.vmstudio.visor.api.client.gui.widgets.info;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.vmstudio.visor.api.client.gui.GuiTexture;
import org.jetbrains.annotations.NotNull;

public class WidgetInfoImage extends WidgetInfo {
    @Getter @Setter @Accessors(chain = true)
    private GuiTexture texture;


    public WidgetInfoImage(@NotNull WidgetInfoImage copyFrom) {
        super(copyFrom);
        this.texture = copyFrom.texture;
    }

    public WidgetInfoImage() {

    }

    @Override
    public WidgetInfoImage pos(int x, int y) {
        return (WidgetInfoImage) super.pos(x, y);
    }

    @Override
    public WidgetInfoImage size(int width, int height) {
        return (WidgetInfoImage) super.size(width, height);
    }
}
