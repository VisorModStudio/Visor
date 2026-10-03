package org.vmstudio.visor.core.client.gui.overlays.builtin.keyboard;

import com.mojang.blaze3d.platform.InputConstants;
import org.jetbrains.annotations.NotNull;


public final class KeyboardRow {

    private static final int[] NUMBERS = {
            InputConstants.KEY_GRAVE,
            InputConstants.KEY_1,
            InputConstants.KEY_2,
            InputConstants.KEY_3,
            InputConstants.KEY_4,
            InputConstants.KEY_5,
            InputConstants.KEY_6,
            InputConstants.KEY_7,
            InputConstants.KEY_8,
            InputConstants.KEY_9,
            InputConstants.KEY_0,
            InputConstants.KEY_MINUS,
            InputConstants.KEY_EQUALS
    };

    private static final int[] TOP = {
            InputConstants.KEY_Q,
            InputConstants.KEY_W,
            InputConstants.KEY_E,
            InputConstants.KEY_R,
            InputConstants.KEY_T,
            InputConstants.KEY_Y,
            InputConstants.KEY_U,
            InputConstants.KEY_I,
            InputConstants.KEY_O,
            InputConstants.KEY_P,
            InputConstants.KEY_LBRACKET,
            InputConstants.KEY_RBRACKET,
            InputConstants.KEY_BACKSLASH
    };

    private static final int[] HOME = {
            InputConstants.KEY_A,
            InputConstants.KEY_S,
            InputConstants.KEY_D,
            InputConstants.KEY_F,
            InputConstants.KEY_G,
            InputConstants.KEY_H,
            InputConstants.KEY_J,
            InputConstants.KEY_K,
            InputConstants.KEY_L,
            InputConstants.KEY_SEMICOLON,
            InputConstants.KEY_APOSTROPHE
    };

    private static final int[] BOTTOM = {
            InputConstants.KEY_Z,
            InputConstants.KEY_X,
            InputConstants.KEY_C,
            InputConstants.KEY_V,
            InputConstants.KEY_B,
            InputConstants.KEY_N,
            InputConstants.KEY_M,
            InputConstants.KEY_COMMA,
            InputConstants.KEY_PERIOD,
            InputConstants.KEY_SLASH
    };

    private final int[] keyCodes;
    private final String[] normalSymbols;
    private final String[] shiftSymbols;

    private KeyboardRow(int[] keyCodes, String normal, String shift) {
        this.keyCodes = keyCodes;
        this.normalSymbols = split(normal);
        this.shiftSymbols = split(shift);

        if (this.normalSymbols.length != this.shiftSymbols.length) {
            throw new IllegalArgumentException(
                    "Both layers of a row must type the same number of keys: \""
                            + normal + "\" vs \"" + shift + "\""
            );
        }
        if (this.normalSymbols.length < keyCodes.length) {
            throw new IllegalArgumentException(
                    "Row \"" + normal + "\" covers " + this.normalSymbols.length
                            + " keys but sits on " + keyCodes.length
            );
        }
    }

    public static @NotNull KeyboardRow numbers(@NotNull String normal, @NotNull String shift) {
        return new KeyboardRow(NUMBERS, normal, shift);
    }

    public static @NotNull KeyboardRow top(@NotNull String normal, @NotNull String shift) {
        return new KeyboardRow(TOP, normal, shift);
    }

    public static @NotNull KeyboardRow home(@NotNull String normal, @NotNull String shift) {
        return new KeyboardRow(HOME, normal, shift);
    }

    public static @NotNull KeyboardRow bottom(@NotNull String normal, @NotNull String shift) {
        return new KeyboardRow(BOTTOM, normal, shift);
    }

    public int size() {
        return normalSymbols.length;
    }

    public @NotNull String symbol(int index, boolean shifted) {
        return shifted ? shiftSymbols[index] : normalSymbols[index];
    }

    public int keyCodeAt(int index) {
        return index < keyCodes.length ? keyCodes[index] : -1;
    }

    private static String[] split(String row) {
        int[] codePoints = row.codePoints().toArray();
        String[] symbols = new String[codePoints.length];
        for (int i = 0; i < codePoints.length; i++) {
            symbols[i] = new String(Character.toChars(codePoints[i]));
        }
        return symbols;
    }
}
