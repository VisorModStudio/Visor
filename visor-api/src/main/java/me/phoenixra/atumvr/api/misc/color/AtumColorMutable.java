package me.phoenixra.atumvr.api.misc.color;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Getter
public class AtumColorMutable implements AtumColor {
    private float red;
    private float green;
    private float blue;
    private float alpha;

    private int redInt;
    private int greenInt;
    private int blueInt;
    private int alphaInt;


    public AtumColorMutable(float red, float green, float blue, float alpha) {
        setRGBA(red, green, blue, alpha);
    }

    public AtumColorMutable(int red, int green, int blue, int alpha) {
        setRGBA(red, green, blue, alpha);
    }


    public void setRGBA(int red, int green, int blue, int alpha){
        this.red = red / 255f;
        this.green = green / 255f;
        this.blue = blue / 255f;
        this.alpha = alpha / 255f;
        updateIntValues();
    }
    public void setRGBA(float red, float green, float blue, float alpha){
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
        updateIntValues();
    }

    private void updateIntValues() {
        this.redInt = floatToInt(red);
        this.greenInt = floatToInt(green);
        this.blueInt = floatToInt(blue);
        this.alphaInt = floatToInt(alpha);
    }

    @Override
    public @NotNull AtumColorMutable blend(@NotNull AtumColor other, float ratio) {
        red = red * (1 - ratio) + other.getRed() * ratio;
        green = green * (1 - ratio) + other.getGreen() * ratio;
        blue = blue * (1 - ratio) + other.getBlue() * ratio;
        alpha = alpha * (1 - ratio) + other.getAlpha() * ratio;
        updateIntValues();
        return this;
    }

    @Override
    public @NotNull AtumColorMutable multiply(float multiplier) {
        red *= multiplier;
        green *= multiplier;
        blue *= multiplier;
        updateIntValues();
        return this;
    }

    @Override
    public @NotNull AtumColorMutable invert() {
        red   = 1f - red;
        green = 1f - green;
        blue  = 1f - blue;
        updateIntValues();
        return this;
    }

    @Override
    public @NotNull AtumColorMutable toGrayscale() {
        float luminance = 0.3f * red + 0.59f * green + 0.11f * blue;
        red = green = blue = luminance;
        updateIntValues();
        return this;
    }


    @Override
    public @NotNull AtumColorMutable withAlpha(float newAlpha) {
        alpha = newAlpha;
        updateIntValues();
        return this;
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


    public AtumColorImmutable asImmutable(){
        return new AtumColorImmutable(red, green, blue, alpha);
    }


    @Override
    public String toString() {
        return "AtumColorMutable{" +
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
