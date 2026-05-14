package org.vmstudio.visor.core.client.gui.overlays.builtin.keyboard;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


@Getter
public enum KeyboardLayout {
    ENGLISH("EN", "English",
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
            }),
    RUSSIAN("RU", "Russian",
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
            }),
    UKRAINIAN("UA", "Ukrainian",
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
            }),
    GERMAN("DE", "German",
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
            }),
    FRENCH("FR", "French",
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
            }),
    SPANISH("ES", "Spanish",
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
            }),
    ITALIAN("IT", "Italian",
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
            }),
    PORTUGUESE("PT", "Portuguese",
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
            });

    private final String label;
    private final String displayName;

    private final String[] normalRows;
    private final String[] shiftRows;

    KeyboardLayout(@NotNull String label,
                   @NotNull String displayName,
                   @NotNull String[] normalRows,
                   @NotNull String[] shiftRows) {
        this.label = label;
        this.displayName = displayName;
        this.normalRows = normalRows;
        this.shiftRows  = shiftRows;

    }

    public @NotNull KeyboardLayout next() {
        KeyboardLayout[] values = values();
        return values[(ordinal() + 1) % values.length];
    }


    /**
     * Maps a language code (e.g. "ru_ru", "fr_fr")
     * to a keyboard layout.
     */
    public static @Nullable KeyboardLayout fromLangCode(@NotNull String langCode) {
        String lower = langCode.toLowerCase();
        if (lower.startsWith("en")) return ENGLISH;
        if (lower.startsWith("ru")) return RUSSIAN;
        if (lower.startsWith("uk")) return UKRAINIAN;
        if (lower.startsWith("de")) return GERMAN;
        if (lower.startsWith("fr")) return FRENCH;
        if (lower.startsWith("es")) return SPANISH;
        if (lower.startsWith("it")) return ITALIAN;
        if (lower.startsWith("pt")) return PORTUGUESE;
        return null;
    }
}