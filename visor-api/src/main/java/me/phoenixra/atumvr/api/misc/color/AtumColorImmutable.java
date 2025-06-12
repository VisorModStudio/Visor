package me.phoenixra.atumvr.api.misc.color;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;


@Getter
public class AtumColorImmutable implements AtumColor {
    private final float red;
    private final float green;
    private final float blue;
    private final float alpha;


    private final int redInt;
    private final int greenInt;
    private final int blueInt;
    private final int alphaInt;


    public AtumColorImmutable(float red, float green, float blue, float alpha) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
        this.redInt = floatToInt(red);
        this.greenInt = floatToInt(green);
        this.blueInt = floatToInt(blue);
        this.alphaInt = floatToInt(alpha);
    }

    public AtumColorImmutable(int red, int green, int blue, int alpha) {
        this.red = red / 255f;
        this.green = green / 255f;
        this.blue = blue / 255f;
        this.alpha = alpha / 255f;
        this.redInt = floatToInt(this.red);
        this.greenInt = floatToInt(this.green);
        this.blueInt = floatToInt(this.blue);
        this.alphaInt = floatToInt(this.alpha);
    }


    @Override
    public @NotNull AtumColorImmutable blend(@NotNull AtumColor other, float ratio) {
        float newRed   = red   * (1 - ratio) + other.getRed()   * ratio;
        float newGreen = green * (1 - ratio) + other.getGreen() * ratio;
        float newBlue  = blue  * (1 - ratio) + other.getBlue()  * ratio;
        float newAlpha = alpha * (1 - ratio) + other.getAlpha() * ratio;
        return new AtumColorImmutable(newRed, newGreen, newBlue, newAlpha);
    }

    @Override
    public @NotNull AtumColorImmutable multiply(float multiplier) {
        return new AtumColorImmutable(red * multiplier, green * multiplier, blue * multiplier, alpha);
    }


    @Override
    public @NotNull AtumColorImmutable invert() {
        return new AtumColorImmutable(1f - red, 1f - green, 1f - blue, alpha);
    }

    @Override
    public @NotNull AtumColorImmutable toGrayscale() {
        float luminance = 0.3f * red + 0.59f * green + 0.11f * blue;
        return new AtumColorImmutable(luminance, luminance, luminance, alpha);
    }

    @Override
    public @NotNull AtumColorImmutable withAlpha(float newAlpha) {
        return new AtumColorImmutable(red, green, blue, newAlpha);
    }

    @Override
    public double getContrastRatio(@NotNull AtumColor other) {
        double l1 = relativeLuminance(this);
        double l2 = relativeLuminance(other);
        double lighter = Math.max(l1, l2);
        double darker  = Math.min(l1, l2);
        return (lighter + 0.05) / (darker + 0.05);
    }

    private double relativeLuminance(AtumColor color) {
        double r = adjust(color.getRed());
        double g = adjust(color.getGreen());
        double b = adjust(color.getBlue());
        return 0.2126 * r + 0.7152 * g + 0.0722 * b;
    }

    private double adjust(double channel) {
        return (channel <= 0.03928) ? channel / 12.92 : Math.pow((channel + 0.055) / 1.055, 2.4);
    }

    public AtumColorMutable asMutable(){
        return new AtumColorMutable(red, green, blue, alpha);
    }

    @Override
    public String toString() {
        return "AtumColorImmutable{" +
                "red=" + red +
                ", green=" + green +
                ", blue=" + blue +
                ", alpha=" + alpha +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AtumColor)) return false;
        AtumColor that = (AtumColor) o;
        return Float.compare(that.getRed(), red) == 0 &&
                Float.compare(that.getGreen(), green) == 0 &&
                Float.compare(that.getBlue(), blue) == 0 &&
                Float.compare(that.getAlpha(), alpha) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(red, green, blue, alpha);
    }
}
