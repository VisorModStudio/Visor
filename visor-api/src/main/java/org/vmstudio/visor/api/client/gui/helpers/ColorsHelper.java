package org.vmstudio.visor.api.client.gui.helpers;

import me.phoenixra.atumvr.api.misc.color.AtumColor;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//@TODO Maybe get rid of it and replace with AtumColor in atumVR...
public class ColorsHelper {
    private ColorsHelper() {
        throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
    }


    private static final int CHECKER_CELL = 4;

    private static final int CHECKER_LIGHT = 0xFF9A9A9A;
    private static final int CHECKER_DARK = 0xFF6A6A6A;


    public static int argb(int alpha, int red, int green, int blue) {
        return (clamp255(alpha) << 24)
                | (clamp255(red) << 16)
                | (clamp255(green) << 8)
                | clamp255(blue);
    }

    public static int hsvToArgb(float hue, float saturation, float value, int alpha) {
        float hueClamped = wrap01(hue) * 6f;
        float saturationClamped = clamp01(saturation);
        float valueClamped = clamp01(value);

        int sector = (int) hueClamped % 6;
        float offset = hueClamped - (int) hueClamped;

        int max = to255(valueClamped);
        int min = to255(valueClamped * (1f - saturationClamped));
        int falling = to255(valueClamped * (1f - offset * saturationClamped));
        int rising = to255(valueClamped * (1f - (1f - offset) * saturationClamped));

        return switch (sector) {
            case 0 -> argb(alpha, max, rising, min);
            case 1 -> argb(alpha, falling, max, min);
            case 2 -> argb(alpha, min, max, rising);
            case 3 -> argb(alpha, min, falling, max);
            case 4 -> argb(alpha, rising, min, max);
            default -> argb(alpha, max, min, falling);
        };
    }

    public static float[] rgbToHsv(int red, int green, int blue) {
        float r = clamp255(red) / 255f;
        float g = clamp255(green) / 255f;
        float b = clamp255(blue) / 255f;

        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float delta = max - min;

        float hue = 0f;
        if (delta > 1.0e-5f) {
            if (max == r) {
                hue = ((g - b) / delta) / 6f;
            } else if (max == g) {
                hue = (2f + (b - r) / delta) / 6f;
            } else {
                hue = (4f + (r - g) / delta) / 6f;
            }
            hue = wrap01(hue);
        }

        float saturation = max <= 1.0e-5f ? 0f : delta / max;

        return new float[]{hue, saturation, max};
    }


    public static float[] toHsv(@NotNull AtumColor color) {
        return rgbToHsv(color.getRedInt(), color.getGreenInt(), color.getBlueInt());
    }


    public static @Nullable AtumColor parseHex(@Nullable String text, int alpha) {
        if (text == null) return null;

        String hex = text.trim();
        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        }
        if (hex.length() != 3 && hex.length() != 6 && hex.length() != 8) {
            return null;
        }
        if (!hex.chars().allMatch(it -> Character.digit(it, 16) >= 0)) {
            return null;
        }

        try {
            if (hex.length() == 3) {
                int r = Integer.parseInt(hex.substring(0, 1), 16);
                int g = Integer.parseInt(hex.substring(1, 2), 16);
                int b = Integer.parseInt(hex.substring(2, 3), 16);
                return AtumColor.immutable(
                        r * 17, g * 17, b * 17,
                        clamp255(alpha)
                );
            }
            int r = Integer.parseInt(hex.substring(0, 2), 16);
            int g = Integer.parseInt(hex.substring(2, 4), 16);
            int b = Integer.parseInt(hex.substring(4, 6), 16);
            int a = hex.length() == 8
                    ? Integer.parseInt(hex.substring(6, 8), 16)
                    : clamp255(alpha);
            return AtumColor.immutable(r, g, b, a);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static void drawTransparencyChecker(@NotNull GuiGraphics guiGraphics,
                                               int posX, int posY,
                                               int width, int height) {
        if (width <= 0 || height <= 0) return;

        for (int y = 0; y < height; y += CHECKER_CELL) {
            for (int x = 0; x < width; x += CHECKER_CELL) {
                boolean light = ((x / CHECKER_CELL) + (y / CHECKER_CELL)) % 2 == 0;
                guiGraphics.fill(
                        posX + x,
                        posY + y,
                        posX + Math.min(x + CHECKER_CELL, width),
                        posY + Math.min(y + CHECKER_CELL, height),
                        light ? CHECKER_LIGHT : CHECKER_DARK
                );
            }
        }
    }

    public static void drawBorder(@NotNull GuiGraphics guiGraphics,
                                  int posX, int posY,
                                  int width, int height,
                                  int color) {
        guiGraphics.fill(posX - 1, posY - 1, posX + width + 1, posY, color);
        guiGraphics.fill(posX - 1, posY + height, posX + width + 1, posY + height + 1, color);
        guiGraphics.fill(posX - 1, posY, posX, posY + height, color);
        guiGraphics.fill(posX + width, posY, posX + width + 1, posY + height, color);
    }


    private static float wrap01(float value) {
        float wrapped = value % 1f;
        return wrapped < 0f ? wrapped + 1f : wrapped;
    }

    private static float clamp01(float value) {
        return Math.max(0f, Math.min(1f, value));
    }

    private static int to255(float normalized) {
        return Math.round(clamp01(normalized) * 255f);
    }

    private static int clamp255(int value) {
        return Math.max(0, Math.min(255, value));
    }
}
