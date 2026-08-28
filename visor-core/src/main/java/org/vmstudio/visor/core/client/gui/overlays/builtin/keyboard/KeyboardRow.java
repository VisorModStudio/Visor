package org.vmstudio.visor.core.client.gui.overlays.builtin.keyboard;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;


public final class KeyboardRow {

    private static final int[] NUMBERS = {
            GLFW.GLFW_KEY_GRAVE_ACCENT,
            GLFW.GLFW_KEY_1,
            GLFW.GLFW_KEY_2,
            GLFW.GLFW_KEY_3,
            GLFW.GLFW_KEY_4,
            GLFW.GLFW_KEY_5,
            GLFW.GLFW_KEY_6,
            GLFW.GLFW_KEY_7,
            GLFW.GLFW_KEY_8,
            GLFW.GLFW_KEY_9,
            GLFW.GLFW_KEY_0,
            GLFW.GLFW_KEY_MINUS,
            GLFW.GLFW_KEY_EQUAL
    };

    private static final int[] TOP = {
            GLFW.GLFW_KEY_Q,
            GLFW.GLFW_KEY_W,
            GLFW.GLFW_KEY_E,
            GLFW.GLFW_KEY_R,
            GLFW.GLFW_KEY_T,
            GLFW.GLFW_KEY_Y,
            GLFW.GLFW_KEY_U,
            GLFW.GLFW_KEY_I,
            GLFW.GLFW_KEY_O,
            GLFW.GLFW_KEY_P,
            GLFW.GLFW_KEY_LEFT_BRACKET,
            GLFW.GLFW_KEY_RIGHT_BRACKET,
            GLFW.GLFW_KEY_BACKSLASH
    };

    private static final int[] HOME = {
            GLFW.GLFW_KEY_A,
            GLFW.GLFW_KEY_S,
            GLFW.GLFW_KEY_D,
            GLFW.GLFW_KEY_F,
            GLFW.GLFW_KEY_G,
            GLFW.GLFW_KEY_H,
            GLFW.GLFW_KEY_J,
            GLFW.GLFW_KEY_K,
            GLFW.GLFW_KEY_L,
            GLFW.GLFW_KEY_SEMICOLON,
            GLFW.GLFW_KEY_APOSTROPHE
    };

    private static final int[] BOTTOM = {
            GLFW.GLFW_KEY_Z,
            GLFW.GLFW_KEY_X,
            GLFW.GLFW_KEY_C,
            GLFW.GLFW_KEY_V,
            GLFW.GLFW_KEY_B,
            GLFW.GLFW_KEY_N,
            GLFW.GLFW_KEY_M,
            GLFW.GLFW_KEY_COMMA,
            GLFW.GLFW_KEY_PERIOD,
            GLFW.GLFW_KEY_SLASH
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
