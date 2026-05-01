package org.vmstudio.visor.core.client.gui.overlays.builtin.keyboard;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


@Getter
public enum KeyboardLayout {
    EN_US("EN", "English (US)",
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
    RU("RU", "Russian",
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
    UA("UA", "Ukrainian",
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
    DE("DE", "German",
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
    FR("FR", "French",
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
    ES_ES("ES", "Spanish",
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
    IT("IT", "Italian",
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
    PT_PT("PT", "Portuguese",
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

    public static @Nullable KeyboardLayout byName(@NotNull String rawValue) {
        for (KeyboardLayout value : values()) {
            if (value.name().equalsIgnoreCase(rawValue)) {
                return value;
            }
        }
        return null;
    }
}