package org.vmstudio.visor.core.client.gui.overlays.builtin.keyboard;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public enum KeyboardLayoutId {
    EN_US("EN"),
    RU("RU"),
    UA("UA"),
    DE("DE"),
    FR("FR"),
    ES_ES("ES"),
    IT("IT"),
    PT_PT("PT");

    @Getter
    private final String buttonLabel;

    KeyboardLayoutId(@NotNull String buttonLabel) {
        this.buttonLabel = buttonLabel;
    }

    public @NotNull KeyboardLayoutId next() {
        KeyboardLayoutId[] values = values();
        return values[(ordinal() + 1) % values.length];
    }
}