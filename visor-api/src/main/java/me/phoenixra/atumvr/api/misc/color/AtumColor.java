package me.phoenixra.atumvr.api.misc.color;

import org.jetbrains.annotations.NotNull;

import java.awt.*;


/**
 * The AtumColor interface defines the common functionality for color objects
 * Colors are represented with normalized float values
 * for red, green, blue, and alpha (transparency) as well as corresponding integer
 * representations (0–255) cached for performance reasons.
 * <br><br>
 * The interface also provides several pre-defined color constants and static factory
 * methods to create either mutable or immutable color instances
 */
public interface AtumColor {

    /**Symbol used in color codes formatting*/
    String COLOR_SYMBOL = "§";

    /** Predefined white color */
    AtumColorImmutable WHITE = immutable(1.0f, 1.0f, 1.0f, 1.0f);

    /** Predefined black color */
    AtumColorImmutable BLACK = immutable(0.0f, 0.0f, 0.0f, 1.0f);

    /** Predefined red color */
    AtumColorImmutable RED = immutable(1.0f, 0.0f, 0.0f, 1.0f);

    /** Predefined green color */
    AtumColorImmutable GREEN = immutable(0.0f, 1.0f, 0.0f, 1.0f);

    /** Predefined blue color */
    AtumColorImmutable BLUE = immutable(0.0f, 0.0f, 1.0f, 1.0f);

    /** Predefined yellow color */
    AtumColorImmutable YELLOW = immutable(1.0f, 1.0f, 0.0f, 1.0f);

    /** Predefined cyan color */
    AtumColorImmutable CYAN = immutable(0.0f, 1.0f, 1.0f, 1.0f);

    /** Predefined magenta color */
    AtumColorImmutable MAGENTA = immutable(1.0f, 0.0f, 1.0f, 1.0f);

    /** Predefined gray color: (50% gray) */
    AtumColorImmutable GRAY = immutable(0.5f, 0.5f, 0.5f, 1.0f);

    /** Predefined dark gray color: (25% gray) */
    AtumColorImmutable DARK_GRAY = immutable(0.25f, 0.25f, 0.25f, 1.0f);

    /** Predefined light gray color: (75% gray) */
    AtumColorImmutable LIGHT_GRAY = immutable(0.75f, 0.75f, 0.75f, 1.0f);

    /** Predefined orange color */
    AtumColorImmutable ORANGE = immutable(1.0f, 0.5f, 0.0f, 1.0f);

    /** Predefined pink color */
    AtumColorImmutable PINK = immutable(1.0f, 0.68f, 0.68f, 1.0f);

    /** Predefined purple color */
    AtumColorImmutable PURPLE = immutable(0.5f, 0.0f, 0.5f, 1.0f);

    /** Predefined brown color */
    AtumColorImmutable BROWN = immutable(0.5f, 0.25f, 0.0f, 1.0f);

    /** Predefined lime color */
    AtumColorImmutable LIME = immutableFromHex("#39FF14");


    /**
     * Returns the normalized red component of this color.
     *
     * @return red value in the range 0.0 to 1.0
     */
    float getRed();

    /**
     * Returns the normalized green component of this color.
     *
     * @return green value in the range 0.0 to 1.0
     */
    float getGreen();

    /**
     * Returns the normalized blue component of this color.
     *
     * @return blue value in the range 0.0 to 1.0
     */
    float getBlue();

    /**
     * Returns the normalized alpha (transparency) component of this color.
     *
     * @return alpha value in the range 0.0 to 1.0
     */
    float getAlpha();

    /**
     * Returns the integer red component (0–255) of this color.
     *
     * @return red component as an integer
     */
    int getRedInt();

    /**
     * Returns the integer green component (0–255) of this color.
     *
     * @return green component as an integer
     */
    int getGreenInt();

    /**
     * Returns the integer blue component (0–255) of this color.
     *
     * @return blue component as an integer
     */
    int getBlueInt();

    /**
     * Returns the integer alpha component (0–255) of this color.
     *
     * @return alpha component as an integer
     */
    int getAlphaInt();


    /**
     * Blends this color with another color.
     * <p>
     * The blending ratio determines how much of the other color is mixed in:
     * a ratio of 0.0f returns this color, while a ratio of 1.0f returns the other color.
     * </p>
     *
     * @param other the color to blend with (must not be {@code null})
     * @param ratio the blend ratio (0.0f to 1.0f)
     * @return the blended color; in immutable implementations, a new instance is returned,
     *         while mutable implementations update this instance
     */
    @NotNull AtumColor blend(@NotNull AtumColor other, float ratio);

    /**
     * Multiplies the color’s RGB channels by the given multiplier.
     * <p>
     * For immutable colors, this returns a new instance with the modified values;
     * for mutable colors, it updates this instance.
     * </p>
     *
     * @param multiplier the factor by which to multiply the RGB components
     * @return the resulting color after multiplication
     */
    @NotNull AtumColor multiply(float multiplier);

    /**
     * Inverts the RGB channels of this color.
     * <p>
     * Each channel is replaced by its complement (1.0f - value).
     * </p>
     *
     * @return the inverted color; for immutable implementations, a new instance is returned,
     *         while mutable implementations update this instance
     */
    @NotNull AtumColor invert();

    /**
     * Converts this color to its grayscale equivalent using a luminance formula.
     * <p>
     * The formula typically uses weighted values for red, green, and blue.
     * </p>
     *
     * @return the grayscale color; in immutable implementations, a new instance is returned,
     *         while mutable implementations update this instance
     */
    @NotNull AtumColor toGrayscale();

    /**
     * Calculates the contrast ratio between this color and another.
     * <p>
     * The contrast ratio is calculated using the standard relative luminance formula,
     * which is useful for accessibility and design.
     * </p>
     *
     * @param other the other color to compare with (must not be {@code null})
     * @return the contrast ratio as a double value
     */
    double getContrastRatio(@NotNull AtumColor other);

    /**
     * Returns a color with the specified alpha (transparency) value.
     * <p>
     * In immutable implementations, a new color instance is returned with the updated alpha;
     * in mutable implementations, this instance is updated.
     * </p>
     *
     * @param newAlpha the new alpha value (from 0.0 to 1.0)
     * @return the color with the updated alpha value
     */
    @NotNull AtumColor withAlpha(float newAlpha);


    // ======= UTILITY METHODS =======

    /**
     * Lightens the color by blending it with white.
     * <p>
     * The factor determines how much white is added:
     * a factor of 0.0 returns the original color, while a factor of 1.0 returns pure white.
     * </p>
     *
     * @param factor the amount to lighten the color (0.0 to 1.0)
     * @return the lightened color
     */
    default @NotNull AtumColor lighten(float factor){
        return blend(WHITE, factor);
    }

    /**
     * Darkens the color by blending it with black.
     * <p>
     * The factor determines how much black is added:
     * a factor of 0.0 returns the original color, while a factor of 1.0 returns pure black.
     * </p>
     *
     * @param factor the amount to darken the color (0.0 to 1.0)
     * @return the darkened color
     */
    default @NotNull AtumColor darken(float factor){
        return blend(BLACK, factor);
    }

    /**
     * Linearly interpolates (lerps) between this color and another color.
     * <p>
     * The interpolation factor {@code t} determines the mix:
     * a value of 0.0 returns this color and a value of 1.0 returns the other color.
     * </p>
     *
     * @param other the target color for interpolation (must not be {@code null})
     * @param t     the interpolation factor between 0.0 and 1.0
     * @return the interpolated color
     */
    default @NotNull AtumColor lerp(@NotNull AtumColor other, float t){
        return blend(other, t);
    }

    /**
     * Converts this color to the HSB (Hue, Saturation, Brightness) color space.
     * <p>
     * The returned array contains three values:
     * hue, saturation, and brightness (each in the range 0.0 to 1.0).
     * </p>
     *
     * @return a float array with HSB values
     */
    default float[] toHSB() {
        return Color.RGBtoHSB(getRedInt(), getGreenInt(), getBlueInt(), null);
    }

    /**
     * Converts this color to an integer representation.
     * <p>
     * The integer is formed by shifting and combining the RGBA components:
     * red (shifted 16 bits), green (shifted 8 bits), blue, and alpha (shifted 24 bits).
     * </p>
     *
     * @return the integer representation of this color
     */
    default int toInt() {
        return (getRedInt() << 16)
                | (getGreenInt() << 8)
                | getBlueInt()
                | (getAlphaInt() << 24);
    }

    /**
     * Converts this color to a hexadecimal string.
     * <p>
     * If {@code withAlpha} is {@code true}, the string includes the alpha component.
     * Otherwise, only the RGB components are included.
     * </p>
     *
     * @param withAlpha {@code true} to include alpha; {@code false} otherwise
     * @return the hexadecimal string representation of this color
     */
    default @NotNull String toHex(boolean withAlpha) {
        if (withAlpha) {
            return String.format(
                    "#%02x%02x%02x%02x",
                    getRedInt(), getGreenInt(), getBlueInt(),
                    getAlphaInt()
            );
        }
        return String.format(
                "#%02x%02x%02x",
                getRedInt(),
                getGreenInt(),
                getBlueInt()
        );
    }

    /**
     * Returns an AWT {@link Color} object corresponding to this color.
     *
     * @return a new {@link Color} instance with this color's RGBA values
     */
    default @NotNull Color asAwtColor(){
        return new Color(getRed(),getGreen(),getBlue(), getAlpha());
    }

    /**
     * Converts a normalized float color value to an integer value (0–255).
     *
     * @param value the normalized float value (0.0 to 1.0)
     * @return the corresponding integer value (0 to 255)
     */
    default int floatToInt(float value) {
        if (value <= 0) return 0;
        if (value >= 1) return 255;
        return (int)(value * 255);
    }


    // ======= STATIC FACTORY METHODS =======

    /**
     * Creates a mutable color instance from normalized float components.
     *
     * @param red   the red component (0.0 to 1.0)
     * @param green the green component (0.0 to 1.0)
     * @param blue  the blue component (0.0 to 1.0)
     * @param alpha the alpha component (0.0 to 1.0)
     * @return a new {@link AtumColorMutable} instance
     */
    static @NotNull AtumColorMutable mutable(float red, float green, float blue, float alpha) {
        return new AtumColorMutable(red, green, blue, alpha);
    }

    /**
     * Creates a mutable color instance from integer components.
     *
     * @param red   the red component (0 to 255)
     * @param green the green component (0 to 255)
     * @param blue  the blue component (0 to 255)
     * @param alpha the alpha component (0 to 255)
     * @return a new {@link AtumColorMutable} instance
     */
    static @NotNull AtumColorMutable mutable(int red, int green, int blue, int alpha) {
        return new AtumColorMutable(red, green, blue, alpha);
    }

    /**
     * Creates a mutable color instance from a hexadecimal string.
     *
     * @param hex a hexadecimal string representing the color (e.g. "#RRGGBB" or "#RRGGBBAA")
     * @return a new mutable {@link AtumColorMutable} instance
     * @throws IllegalArgumentException if the hex string is not in a valid format
     */
    static @NotNull AtumColorMutable mutableFromHex(String hex) {
        float[] values = valuesFromHex(hex);
        return new AtumColorMutable(
                values[0],
                values[1],
                values[2],
                values[3]
        );
    }


    /**
     * Creates an immutable color instance from normalized float components.
     *
     * @param red   the red component (0.0 to 1.0)
     * @param green the green component (0.0 to 1.0)
     * @param blue  the blue component (0.0 to 1.0)
     * @param alpha the alpha component (0.0 to 1.0)
     * @return a new immutable {@link AtumColorImmutable} instance
     */
    static @NotNull AtumColorImmutable immutable(float red, float green, float blue, float alpha) {
        return new AtumColorImmutable(red, green, blue, alpha);
    }

    /**
     * Creates an immutable color instance from integer components.
     *
     * @param red   the red component (0 to 255)
     * @param green the green component (0 to 255)
     * @param blue  the blue component (0 to 255)
     * @param alpha the alpha component (0 to 255)
     * @return a new immutable {@link AtumColorImmutable} instance
     */
    static @NotNull AtumColorImmutable immutable(int red, int green, int blue, int alpha) {
        return new AtumColorImmutable(red, green, blue, alpha);
    }

    /**
     * Creates an immutable color instance from a hexadecimal string.
     *
     * @param hex a hexadecimal string representing the color (e.g. "#RRGGBB" or "#RRGGBBAA")
     * @return a new immutable {@link AtumColorImmutable} instance
     * @throws IllegalArgumentException if the hex string is not in a valid format
     */
    static @NotNull AtumColorImmutable immutableFromHex(String hex) {
        float[] values = valuesFromHex(hex);
        return new AtumColorImmutable(
                values[0],
                values[1],
                values[2],
                values[3]
        );
    }


    /**
     * Parses a hexadecimal color string and returns the corresponding normalized float values.
     * <p>
     * The returned array contains values in the order: [red, green, blue, alpha],
     * where each value is in the range 0.0 to 255.0.
     * </p>
     *
     * @param hex the hexadecimal string (e.g. "#RRGGBB" or "#RRGGBBAA")
     * @return an array of floats representing the color components
     * @throws IllegalArgumentException if the hex string is not in a valid format
     */
    static float[] valuesFromHex(String hex){
        if(hex.startsWith("#")) {
            hex = hex.substring(1);
        }
        if (hex.length() == 6) {
            int r = Integer.parseInt(hex.substring(0, 2), 16);
            int g = Integer.parseInt(hex.substring(2, 4), 16);
            int b = Integer.parseInt(hex.substring(4, 6), 16);
            return new float[]{r, g, b, 255};
        } else if (hex.length() == 8) {
            int r = Integer.parseInt(hex.substring(0, 2), 16);
            int g = Integer.parseInt(hex.substring(2, 4), 16);
            int b = Integer.parseInt(hex.substring(4, 6), 16);
            int a = Integer.parseInt(hex.substring(6, 8), 16);
            return new float[]{r, g, b, a};
        }
        throw new IllegalArgumentException("Invalid hex format");
    }
}