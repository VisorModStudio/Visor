package me.phoenixra.visor.api.client.gui.widgets.info;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import me.phoenixra.visor.api.client.gui.GuiTexture;
import org.jetbrains.annotations.NotNull;

public class WidgetInfoImage extends WidgetInfo {
    @Getter @Setter @Accessors(chain = true)
    private GuiTexture texture;
    public WidgetInfoImage(@NotNull GuiTexture texture,
                           int x, int y, int width, int height) {
        super(x, y, width, height);
        this.texture = texture;
    }
    public WidgetInfoImage(@NotNull WidgetInfoImage copyFrom,
                           int x, int y, int width, int height) {
        super(x, y, width, height);
        this.texture = copyFrom.texture;
    }

}
