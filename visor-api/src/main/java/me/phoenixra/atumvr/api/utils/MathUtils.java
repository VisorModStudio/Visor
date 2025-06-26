package me.phoenixra.atumvr.api.utils;




import me.phoenixra.atumconfig.api.tuples.PairRecord;
import org.joml.Matrix4f;

import java.util.concurrent.ThreadLocalRandom;

/**
 * MathUtils
 * <br><br>
 * Contains fast trigonometry methods
 * and other
 */
public class MathUtils {

    private MathUtils() {
        throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
    }

    /**
     * Sin lookup table.
     */
    private static final double[] SIN_LOOKUP = new double[65536];

    private static final double[] ASIN_TAB = new double[257];
    private static final double[] COS_TAB = new double[257];

    private static final double FRAC_BIAS = Double.longBitsToDouble(4805340802404319232L);

    static public final double degreesToRadians = Math.PI / 180;

    static {

        for (int i = 0; i < 65536; ++i) {
            SIN_LOOKUP[i] = Math.sin((double) i * 3.141592653589793D * 2.0D / 65536.0D);
        }
        for(int i = 0; i < 257; ++i) {
            double d = (double)i / 256.0;
            double e = Math.asin(d);
            COS_TAB[i] = Math.cos(e);
            ASIN_TAB[i] = e;
        }
    }


    /**
     * Get sin from lookup table
     * it is significantly faster that Math#sin()
     *
     * @param radians the radians
     * @return result
     */
    public static double fastSin(final double radians) {
        float f = (float) radians;
        return SIN_LOOKUP[(int) (f * 10430.378F) & '\uffff'];
    }

    /**
     * Get cos from lookup table
     * it is significantly faster that Math#cos()
     *
     * @param radians the radians
     * @return result
     */
    public static double fastCos(final double radians) {
        float f = (float) radians;
        return SIN_LOOKUP[(int) (f * 10430.378F + 16384.0F) & '\uffff'];
    }

    /**
     * Get tan from lookup table
     * it is significantly faster that Math#tan()
     *
     * @param radians the radians
     * @return result
     */
    public static double fastTan(final double radians) {
        float f = (float) radians;
        return SIN_LOOKUP[(int) (f * 10430.378F) & '\uffff'] /
                SIN_LOOKUP[(int) (f * 10430.378F + 16384.0F) & '\uffff'];
    }


    public static double fastAtan2(double d, double e) {
        double f = e * e + d * d;
        if (Double.isNaN(f)) {
            return Double.NaN;
        } else {
            boolean bl = d < 0.0;
            if (bl) {
                d = -d;
            }

            boolean bl2 = e < 0.0;
            if (bl2) {
                e = -e;
            }

            boolean bl3 = d > e;
            double g;
            if (bl3) {
                g = e;
                e = d;
                d = g;
            }

            g = fastInvSqrt(f);
            e *= g;
            d *= g;
            double h = FRAC_BIAS + d;
            int i = (int)Double.doubleToRawLongBits(h);
            double j = ASIN_TAB[i];
            double k = COS_TAB[i];
            double l = h - FRAC_BIAS;
            double m = d * k - e * l;
            double n = (6.0 + m * m) * m * 0.16666666666666666;
            double o = j + n;
            if (bl3) {
                o = 1.5707963267948966 - o;
            }

            if (bl2) {
                o = Math.PI - o;
            }

            if (bl) {
                o = -o;
            }

            return o;
        }
    }


    public static double fastInvSqrt(double d) {
        double e = 0.5 * d;
        long l = Double.doubleToRawLongBits(d);
        l = 6910469410427058090L - (l >> 1);
        d = Double.longBitsToDouble(l);
        d *= 1.5 - e * d * d;
        return d;
    }

    public static float fastInvCubeRoot(float f) {
        int i = Float.floatToIntBits(f);
        i = 1419967116 - i / 3;
        float g = Float.intBitsToFloat(i);
        g = 0.6666667F * g + 1.0F / (3.0F * g * g * f);
        g = 0.6666667F * g + 1.0F / (3.0F * g * g * f);
        return g;
    }

    /**
     * Bias the input value according to a curve.
     *
     * @param input The input value.
     * @param bias  The bias between -1 and 1, where higher values bias input values to lower output values.
     * @return The biased output.
     */
    public static double bias(final double input,
                              final double bias) {
        double k = Math.pow(1 - bias, 3);

        return (input * k) / (input * k - input + 1);
    }

    /**
     * Get random int inside bounds.
     * Uses ThreadLocalRandom
     *
     * @param min The min bound
     * @param max The max bound
     * @return result
     */
    public static int randInt(final int min,
                              final int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
    /**
     * Get random int inside bounds.
     * Uses ThreadLocalRandom
     *
     * @param min The min bound
     * @param max The max bound
     * @return result
     */
    public static double randDouble(final double min,
                                   final double max) {
        return ThreadLocalRandom.current().nextDouble(min, max);
    }

    /**
     * Calculates an aspect ratio
     * @param a first
     * @param b second
     * @return aspect ratio
     */
    public static PairRecord<Integer,Integer> getAspectRatio(int a, int b) {
        int gcd = getGCD(a, b);
        int aspectRatioWidth = a / gcd;
        int aspectRatioHeight = b / gcd;
        return  new PairRecord<>(
                aspectRatioWidth,
                aspectRatioHeight
        );
    }

    //greatest common divisor
    private static int getGCD(int a, int b) {
        while (b != 0) {
            int temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }

    public static Matrix4f extractRotationFromPose(Matrix4f pose){
        Matrix4f rotation = new Matrix4f();
        rotation.set(0,0, pose.get(0,0));
        rotation.set(0,1, pose.get(0,1));
        rotation.set(0,2, pose.get(0,2));
        rotation.set(0,3, 0f);
        rotation.set(1,0, pose.get(1,0));
        rotation.set(1,1, pose.get(1,1));
        rotation.set(1,2, pose.get(1,2));
        rotation.set(1,3, 0f);
        rotation.set(2,0, pose.get(2,0));;
        rotation.set(2,1, pose.get(2,1));;
        rotation.set(2,2, pose.get(2,2));;
        rotation.set(2,3, 0f);
        rotation.set(3,0, 0f);
        rotation.set(3,1, 0f);
        rotation.set(3,2, 0f);
        rotation.set(3,3, 1f);
        return rotation;
    }

}
