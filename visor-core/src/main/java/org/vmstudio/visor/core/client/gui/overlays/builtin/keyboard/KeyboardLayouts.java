package org.vmstudio.visor.core.client.gui.overlays.builtin.keyboard;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;


public final class KeyboardLayouts {

    public static final int[][] ROW_KEY_CODES = new int[][]{
            {
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
            },
            {
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
            },
            {
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
            },
            {
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
            }
    };

    private static final Map<KeyboardLayout, KeyboardLayoutKeys> LAYOUT_KEYS = new EnumMap<>(
            KeyboardLayout.class
    );

    static {
        for(var layout : KeyboardLayout.values()){
            register(build(layout));
        }
    }

    private KeyboardLayouts() {
    }

    public static @NotNull KeyboardLayoutKeys get(@NotNull KeyboardLayout layout) {
        KeyboardLayoutKeys layoutKeys = LAYOUT_KEYS.get(layout);
        if (layoutKeys == null) {
            throw new IllegalArgumentException("Unknown keyboard layout: " + layout);
        }
        return layoutKeys;
    }

    public static @NotNull KeyboardLayoutKeys getDefault() {
        return get(KeyboardLayout.ENGLISH);
    }

    public static @NotNull List<KeyboardLayout> getSelectableLayouts() {
        return List.of(KeyboardLayout.values());
    }

    public static @NotNull List<KeyboardLayout> deserialize(
            @Nullable String rawValue
    ) {
        LinkedHashSet<KeyboardLayout> result = new LinkedHashSet<>();
        if (rawValue != null && !rawValue.isBlank()) {
            for (String part : rawValue.split(",")) {
                String trimmed = part.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }
                try {
                    KeyboardLayout layout = KeyboardLayout.valueOf(trimmed.toUpperCase());
                    result.add(layout);
                } catch (IllegalArgumentException e) {
                    //empty
                }
            }
        }

        return List.copyOf(result);
    }

    public static @NotNull String serialize(
            @NotNull Iterable<KeyboardLayout> layouts
    ) {
        LinkedHashSet<KeyboardLayout> normalized = new LinkedHashSet<>();
        for (KeyboardLayout layout : layouts) {
            if (layout != null) {
                normalized.add(layout);
            }
        }

        StringJoiner joiner = new StringJoiner(",");
        for (KeyboardLayout layout : normalized) {
            joiner.add(layout.name());
        }
        return joiner.toString();
    }

    private static void register(@NotNull KeyboardLayoutKeys layoutKeys) {
        LAYOUT_KEYS.put(layoutKeys.getLayout(), layoutKeys);
    }

    private static @NotNull KeyboardLayoutKeys build(@NotNull KeyboardLayout layout) {

        var normalRows = layout.getNormalRows();
        var shiftRows = layout.getShiftRows();

        KeyboardKey[][] normalLayer = new KeyboardKey[normalRows.length][];
        KeyboardKey[][] shiftLayer = new KeyboardKey[shiftRows.length][];

        for (int row = 0; row < normalRows.length; row++) {
            normalLayer[row] = buildRow(layout, row, normalRows[row], 0);
            shiftLayer[row] = buildRow(layout, row, shiftRows[row], GLFW.GLFW_MOD_SHIFT);
        }

        return new KeyboardLayoutKeys(layout, normalLayer, shiftLayer);
    }

    private static @NotNull KeyboardKey[] buildRow(@NotNull KeyboardLayout layout,
                                                   int rowIndex,
                                                   @NotNull String rowContent,
                                                   int fallbackModifiers) {
        int[] rowKeyCodes = ROW_KEY_CODES[rowIndex];
        String[] symbols = splitSymbols(rowContent);
        if (symbols.length != rowKeyCodes.length) {
            throw new IllegalArgumentException(
                    "Keyboard layout " + layout
                            + " row " + rowIndex
                            + " expected " + rowKeyCodes.length
                            + " symbols but got " + symbols.length
            );
        }

        KeyboardKey[] result = new KeyboardKey[symbols.length];
        for (int i = 0; i < symbols.length; i++) {
            result[i] = new KeyboardKey(
                    symbols[i],
                    symbols[i],
                    rowKeyCodes[i],
                    fallbackModifiers
            );
        }
        return result;
    }

    private static @NotNull String[] splitSymbols(@NotNull String rowContent) {
        int[] codePoints = rowContent.codePoints().toArray();
        String[] symbols = new String[codePoints.length];
        for (int i = 0; i < codePoints.length; i++) {
            symbols[i] = new String(Character.toChars(codePoints[i]));
        }
        return symbols;
    }
}
