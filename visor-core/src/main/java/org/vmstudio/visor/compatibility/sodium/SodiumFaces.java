package org.vmstudio.visor.compatibility.sodium;

public class SodiumFaces {
    private static final int[] FROM_VANILLA_POLYGON = {4, 2, 0, 1, 3, 5};
    public static final int COUNT = 6;

    private SodiumFaces() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static int fromVanilla(int vanillaPolygon) {
        if (vanillaPolygon < 0 || vanillaPolygon >= FROM_VANILLA_POLYGON.length) {
            throw new IllegalArgumentException("not a cuboid polygon index: " + vanillaPolygon);
        }
        return FROM_VANILLA_POLYGON[vanillaPolygon];
    }
}
