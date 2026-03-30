package org.vmstudio.visor.core.client.gui.overlays.builtin.keyboard;

import lombok.Getter;

public class KeyboardKey {
    @Getter
    private final String label;
    @Getter
    private final String input;
    @Getter
    private final int fallbackKey;
    @Getter
    private final int fallbackModifiers;

    public KeyboardKey(String label,
                       String input,
                       int fallbackKey,
                       int fallbackModifiers) {
        this.label = label;
        this.input = input;
        this.fallbackKey = fallbackKey;
        this.fallbackModifiers = fallbackModifiers;
    }

    public boolean hasFallback() {
        return fallbackKey != -1;
    }
}