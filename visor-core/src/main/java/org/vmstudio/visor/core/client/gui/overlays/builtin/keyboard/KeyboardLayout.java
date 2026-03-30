package org.vmstudio.visor.core.client.gui.overlays.builtin.keyboard;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class KeyboardLayout {
    @Getter
    private final KeyboardLayoutId id;
    @Getter
    private final String switchLabel;
    private final KeyboardKey[][] normalLayer;
    private final KeyboardKey[][] shiftLayer;
    private final int maxColumns;

    public KeyboardLayout(@NotNull KeyboardLayoutId id,
                          @NotNull String switchLabel,
                          @NotNull KeyboardKey[][] normalLayer,
                          @NotNull KeyboardKey[][] shiftLayer) {
        this.id = id;
        this.switchLabel = switchLabel;
        this.normalLayer = normalLayer;
        this.shiftLayer = shiftLayer;

        int columns = 0;
        for (KeyboardKey[] row : normalLayer) {
            columns = Math.max(columns, row.length);
        }
        this.maxColumns = columns;
    }

    public @NotNull KeyboardKey[][] getLayer(boolean shifted) {
        return shifted ? shiftLayer : normalLayer;
    }

    public int getMaxColumns() {
        return maxColumns;
    }
}