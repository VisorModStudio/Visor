package org.vmstudio.visor.core.client.gui.overlays.builtin.keyboard;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class KeyboardLayoutKeys {
    @Getter
    private final KeyboardLayout layout;
    private final KeyboardKey[][] normalLayer;
    private final KeyboardKey[][] shiftLayer;
    @Getter
    private final int maxColumns;

    public KeyboardLayoutKeys(@NotNull KeyboardLayout layout,
                              @NotNull KeyboardKey[][] normalLayer,
                              @NotNull KeyboardKey[][] shiftLayer) {
        this.layout = layout;
        this.normalLayer = normalLayer;
        this.shiftLayer = shiftLayer;

        int columns = 0;
        for (KeyboardKey[] row : normalLayer) {
            columns = Math.max(columns, row.length);
        }
        this.maxColumns = columns;
    }

    public @NotNull KeyboardKey[][] getKeys(boolean shifted) {
        return shifted ? shiftLayer : normalLayer;
    }

}