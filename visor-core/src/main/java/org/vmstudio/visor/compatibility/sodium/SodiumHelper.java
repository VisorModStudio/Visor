package org.vmstudio.visor.compatibility.sodium;

import net.minecraft.client.model.geom.ModelPart;
import org.joml.Vector2f;
import org.vmstudio.visor.api.ModLoader;
import org.vmstudio.visor.api.common.utils.LoggerUtils;
import org.vmstudio.visor.compatibility.CompatClasses;
import org.vmstudio.visor.compatibility.sodium.extensions.ModelCuboidExtension;

import java.lang.reflect.Field;

//SODIUM COMPATIBILITY
public final class SodiumHelper {
    private static final String CUBOID_JELLYSQUID =
            "me.jellysquid.mods.sodium.client.render.immediate.model.ModelCuboid";
    private static final String CUBOID_CAFFEINEMC =
            "net.caffeinemc.mods.sodium.client.render.immediate.model.ModelCuboid";

    enum Layout {
        UNKNOWN,
        QUAD_VECTORS,
        UV_SCALARS,
        PACKED_LONGS
    }

    private static Layout layout;

    private static Field cuboidsOnPart;
    private static Field cuboidOnCube;

    private static Field quadsOnCuboid;
    private static Field cornersOnQuad;
    private static Field packedTexturesOnCuboid;

    private static final Field[] U_CUTS = new Field[6];
    private static final Field[] V_CUTS = new Field[3];

    private SodiumHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static boolean isLoaded() {
        var modLoader = ModLoader.get();
        return modLoader.isModLoaded("sodium")
                || modLoader.isModLoaded("rubidium")
                || modLoader.isModLoaded("embeddium");
    }

    public static void copyFaceUv(ModelPart source, ModelPart dest, int sourcePolygon, int destPolygon) {
        switch (resolveLayout()) {
            case QUAD_VECTORS -> copyQuadCorners(source, dest, sourcePolygon, destPolygon);
            case UV_SCALARS -> overrideFromScalars(source, dest, sourcePolygon, destPolygon);
            case PACKED_LONGS -> copyPackedFace(source, dest, sourcePolygon, destPolygon);
            case UNKNOWN -> {}
        }
    }

    private static void copyQuadCorners(ModelPart source, ModelPart dest, int sourcePolygon, int destPolygon) {
        try {
            Vector2f[] from = (Vector2f[]) cornersOnQuad.get(quadAt(source, sourcePolygon));
            Vector2f[] to = (Vector2f[]) cornersOnQuad.get(quadAt(dest, destPolygon));
            for (int i = 0; i < from.length && i < to.length; i++) {
                to[i].set(from[i]);
            }
        } catch (IllegalAccessException | ClassCastException e) {
            logGiveUp(e);
        }
    }

    private static void overrideFromScalars(ModelPart source, ModelPart dest, int sourcePolygon, int destPolygon) {
        try {
            CuboidUv sourceUv = readScalars(cuboidOf(source));
            FaceUv face = sourceUv.face(SodiumFaces.fromVanilla(sourcePolygon));
            ((ModelCuboidExtension) cuboidOf(dest)).visor$overrideFace(SodiumFaces.fromVanilla(destPolygon), face);
        } catch (IllegalAccessException | ClassCastException e) {
            logGiveUp(e);
        }
    }

    private static void copyPackedFace(ModelPart source, ModelPart dest, int sourcePolygon, int destPolygon) {
        try {
            long[] from = (long[]) packedTexturesOnCuboid.get(cuboidOf(source));
            long[] to = (long[]) packedTexturesOnCuboid.get(cuboidOf(dest));
            int fromBase = SodiumFaces.fromVanilla(sourcePolygon) * 4;
            int toBase = SodiumFaces.fromVanilla(destPolygon) * 4;
            System.arraycopy(from, fromBase, to, toBase, 4);
        } catch (IllegalAccessException | ClassCastException e) {
            logGiveUp(e);
        }
    }

    private static CuboidUv readScalars(Object cuboid) throws IllegalAccessException {
        float[] u = new float[U_CUTS.length];
        for (int i = 0; i < U_CUTS.length; i++) {
            u[i] = (float) U_CUTS[i].get(cuboid);
        }
        float[] v = new float[V_CUTS.length];
        for (int i = 0; i < V_CUTS.length; i++) {
            v[i] = (float) V_CUTS[i].get(cuboid);
        }
        return new CuboidUv(u[0], u[1], u[2], u[3], u[4], u[5], v[0], v[1], v[2]);
    }

    private static Object cuboidOf(ModelPart part) throws IllegalAccessException {
        if (cuboidOnCube != null) {
            return cuboidOnCube.get(part.cubes.get(0));
        }
        return ((Object[]) cuboidsOnPart.get(part))[0];
    }

    private static Object quadAt(ModelPart part, int polygon) throws IllegalAccessException {
        return ((Object[]) quadsOnCuboid.get(cuboidOf(part)))[polygon];
    }

    private static void logGiveUp(Throwable t) {
        layout = Layout.UNKNOWN;
        LoggerUtils.getLogger().error("sodium's cuboid fields are not shape we resolved them as", t);
    }

    private static Layout resolveLayout() {
        Layout known = layout;
        if (known != null) {
            return known;
        }
        synchronized (SodiumHelper.class) {
            if (layout == null) {
                layout = detectLayout();
            }
            return layout;
        }
    }

    private static Layout detectLayout() {
        Class<?> cuboid = CompatClasses.find(CUBOID_JELLYSQUID, CUBOID_CAFFEINEMC);
        if (cuboid == null) {
            return Layout.UNKNOWN;
        }
        if (!linkCuboidToModelPart()) {
            return Layout.UNKNOWN;
        }

        Class<?> quad = CompatClasses.find(CUBOID_JELLYSQUID + "$Quad", CUBOID_CAFFEINEMC + "$Quad");
        if (quad != null) {
            quadsOnCuboid = openField(cuboid, "quads");
            cornersOnQuad = openField(quad, "textures");
            return quadsOnCuboid != null && cornersOnQuad != null ? Layout.QUAD_VECTORS : Layout.UNKNOWN;
        }

        if (linkUvScalarrs(cuboid)) {
            return Layout.UV_SCALARS;
        }

        packedTexturesOnCuboid = openField(cuboid, "textures");
        if (packedTexturesOnCuboid != null) {
            return Layout.PACKED_LONGS;
        }

        LoggerUtils.getLogger().warn("Sodium is installed, but its ModelCuboid doesn't have texture layout that we checked");
        return Layout.UNKNOWN;
    }

    private static boolean linkCuboidToModelPart() {
        cuboidsOnPart = openField(ModelPart.class, "sodium$cuboids");
        if (cuboidsOnPart != null) {
            return true;
        }
        cuboidOnCube = openField(ModelPart.Cube.class, "sodium$cuboid");
        return cuboidOnCube != null;
    }

    private static boolean linkUvScalarrs(Class<?> cuboid) {
        for (int i = 0; i < U_CUTS.length; i++) {
            U_CUTS[i] = openField(cuboid, "u" + i);
            if (U_CUTS[i] == null) {
                return false;
            }
        }
        for (int i = 0; i < V_CUTS.length; i++) {
            V_CUTS[i] = openField(cuboid, "v" + i);
            if (V_CUTS[i] == null) {
                return false;
            }
        }
        return true;
    }

    private static Field openField(Class<?> owner, String name) {
        try {
            Field field = owner.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException | RuntimeException e) {
            return null;
        }
    }
}
