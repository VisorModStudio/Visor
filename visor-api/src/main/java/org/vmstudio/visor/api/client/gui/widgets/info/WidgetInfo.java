package org.vmstudio.visor.api.client.gui.widgets.info;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
public class WidgetInfo {

    private int x = 0, y = 0, width = 10, height = 10;

    public WidgetInfo(@NotNull WidgetInfo copyFrom) {
        this.x = copyFrom.x;
        this.y = copyFrom.y;
        this.width = copyFrom.width;
        this.height = copyFrom.height;
    }

    public WidgetInfo() {

    }

    public WidgetInfo pos(int x, int y){
        this.x = x;
        this.y = y;
        return this;
    }
    public WidgetInfo size(int width, int height){
        this.width = width;
        this.height = height;
        return this;
    }

}
