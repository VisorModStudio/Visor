package org.vmstudio.visor.core.client.gui.overlays.builtin.keyboard;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.EnumMap;
import java.util.Map;

public final class KeyboardLayouts {

    private static final int[][] ROW_KEY_CODES = new int[][]{
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

    private static final Map<KeyboardLayoutId, KeyboardLayout> LAYOUTS = new EnumMap<>(
            KeyboardLayoutId.class
    );

    static {
        register(build(
                KeyboardLayoutId.EN_US,
                new String[]{
                        "`1234567890-=",
                        "qwertyuiop[]\\",
                        "asdfghjkl;'",
                        "zxcvbnm,./"
                },
                new String[]{
                        "~!@#$%^&*()_+",
                        "QWERTYUIOP{}|",
                        "ASDFGHJKL:\"",
                        "ZXCVBNM<>?"
                }
        ));
        register(build(
                KeyboardLayoutId.RU,
                new String[]{
                        "ё1234567890-=",
                        "йцукенгшщзхъ\\",
                        "фывапролджэ",
                        "ячсмитьбю."
                },
                new String[]{
                        "Ё!\"№;%:?*()_+",
                        "ЙЦУКЕНГШЩЗХЪ/",
                        "ФЫВАПРОЛДЖЭ",
                        "ЯЧСМИТЬБЮ,"
                }
        ));
        register(build(
                KeyboardLayoutId.UA,
                new String[]{
                        "'1234567890-=",
                        "йцукенгшщзхїґ",
                        "фівапролджє",
                        "ячсмитьбю."
                },
                new String[]{
                        "₴!\"№;%:?*()_+",
                        "ЙЦУКЕНГШЩЗХЇҐ",
                        "ФІВАПРОЛДЖЄ",
                        "ЯЧСМИТЬБЮ,"
                }
        ));
        register(build(
                KeyboardLayoutId.DE,
                new String[]{
                        "^1234567890ß´",
                        "qwertzuiopü+#",
                        "asdfghjklöä",
                        "yxcvbnm,.-"
                },
                new String[]{
                        "°!\"§$%&/()=?`",
                        "QWERTZUIOPÜ*'",
                        "ASDFGHJKLÖÄ",
                        "YXCVBNM;:_"
                }
        ));
        register(build(
                KeyboardLayoutId.FR,
                new String[]{
                        "²&é\"'(-è_çà)=",
                        "azertyuiop^$*",
                        "qsdfghjklmù",
                        "wxcvbn,;:!"
                },
                new String[]{
                        "³1234567890°+",
                        "AZERTYUIOP¨£µ",
                        "QSDFGHJKLM%",
                        "WXCVBN?./§"
                }
        ));
        register(build(
                KeyboardLayoutId.ES_ES,
                new String[]{
                        "º1234567890'¡",
                        "qwertyuiop`+ç",
                        "asdfghjklñ´",
                        "zxcvbnm,.-"
                },
                new String[]{
                        "ª!\"·$%&/()=?¿",
                        "QWERTYUIOP^*Ç",
                        "ASDFGHJKLÑ¨",
                        "ZXCVBNM;:_"
                }
        ));
        register(build(
                KeyboardLayoutId.IT,
                new String[]{
                        "\\1234567890'ì",
                        "qwertyuiopè+ù",
                        "asdfghjklòà",
                        "zxcvbnm,.-"
                },
                new String[]{
                        "|!\"£$%&/()=?^",
                        "QWERTYUIOPé*§",
                        "ASDFGHJKLç°",
                        "ZXCVBNM;:_"
                }
        ));
        register(build(
                KeyboardLayoutId.PT_PT,
                new String[]{
                        "\\1234567890'«",
                        "qwertyuiop+´~",
                        "asdfghjklçº",
                        "zxcvbnm,.-"
                },
                new String[]{
                        "|!\"#$%&/()=?»",
                        "QWERTYUIOP*`^",
                        "ASDFGHJKLÇª",
                        "ZXCVBNM;:_"
                }
        ));
    }

    private KeyboardLayouts() {
    }

    public static @NotNull KeyboardLayout get(@NotNull KeyboardLayoutId id) {
        KeyboardLayout layout = LAYOUTS.get(id);
        if (layout == null) {
            throw new IllegalArgumentException("Unknown keyboard layout: " + id);
        }
        return layout;
    }

    public static @NotNull KeyboardLayout getDefault() {
        return get(KeyboardLayoutId.EN_US);
    }

    private static void register(@NotNull KeyboardLayout layout) {
        LAYOUTS.put(layout.getId(), layout);
    }

    private static @NotNull KeyboardLayout build(@NotNull KeyboardLayoutId id,
                                                 @NotNull String[] normalRows,
                                                 @NotNull String[] shiftRows) {
        if (normalRows.length != ROW_KEY_CODES.length
                || shiftRows.length != ROW_KEY_CODES.length) {
            throw new IllegalArgumentException(
                    "Keyboard layout " + id + " must define " + ROW_KEY_CODES.length + " rows"
            );
        }

        KeyboardKey[][] normalLayer = new KeyboardKey[normalRows.length][];
        KeyboardKey[][] shiftLayer = new KeyboardKey[shiftRows.length][];

        for (int row = 0; row < normalRows.length; row++) {
            normalLayer[row] = buildRow(id, row, normalRows[row], 0);
            shiftLayer[row] = buildRow(id, row, shiftRows[row], GLFW.GLFW_MOD_SHIFT);
        }

        return new KeyboardLayout(id, id.getButtonLabel(), normalLayer, shiftLayer);
    }

    private static @NotNull KeyboardKey[] buildRow(@NotNull KeyboardLayoutId id,
                                                   int rowIndex,
                                                   @NotNull String rowContent,
                                                   int fallbackModifiers) {
        int[] rowKeyCodes = ROW_KEY_CODES[rowIndex];
        String[] symbols = splitSymbols(rowContent);
        if (symbols.length != rowKeyCodes.length) {
            throw new IllegalArgumentException(
                    "Keyboard layout " + id
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
