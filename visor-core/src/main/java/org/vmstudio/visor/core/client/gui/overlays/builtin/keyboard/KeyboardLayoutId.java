package org.vmstudio.visor.core.client.gui.overlays.builtin.keyboard;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum KeyboardLayoutId {
    EN_US("EN", "English (US)"),
    RU("RU", "Russian"),
    UA("UA", "Ukrainian"),
    DE("DE", "German"),
    FR("FR", "French"),
    ES_ES("ES", "Spanish"),
    IT("IT", "Italian"),
    PT_PT("PT", "Portuguese");

    @Getter
    private final String buttonLabel;
    @Getter
    private final String displayName;

    KeyboardLayoutId(@NotNull String buttonLabel,
                     @NotNull String displayName) {
        this.buttonLabel = buttonLabel;
        this.displayName = displayName;
    }

    public @NotNull KeyboardLayoutId next() {
        KeyboardLayoutId[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    public static @Nullable KeyboardLayoutId byName(@NotNull String rawValue) {
        for (KeyboardLayoutId value : values()) {
            if (value.name().equalsIgnoreCase(rawValue)) {
                return value;
            }
        }
        return null;
    }
}