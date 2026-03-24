package org.vmstudio.visor.compatibility.sodium;

import net.minecraft.client.model.geom.ModelPart;
import org.joml.Vector2f;
import org.vmstudio.visor.api.ModLoader;
import org.vmstudio.visor.compatibility.sodium.extensions.ModelCuboidExtension;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.utils.ClassUtils;

import java.lang.reflect.Field;

public class SodiumHelper {

    private SodiumHelper() {
        throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
    }

    private static boolean INITIALIZED = false;

    private static boolean HAS_MODELCUBOID_QUADS;
    private static boolean HAS_MODELCUBOID_FLOATS;
    private static boolean HAS_MODELCUBOID_CUBES;
    private static boolean HAS_MODELCUBOID_LONGS;
    private static Field ModelPart_sodium$cuboids;
    private static Field ModelCuboid_quads;

    private static Field Cube_sodium$cuboid;

    // quad uvs
    private static Field ModelCuboid_u0;
    private static Field ModelCuboid_u1;
    private static Field ModelCuboid_u2;
    private static Field ModelCuboid_u3;
    private static Field ModelCuboid_u4;
    private static Field ModelCuboid_u5;
    private static Field ModelCuboid_v0;
    private static Field ModelCuboid_v1;
    private static Field ModelCuboid_v2;

    private static Field ModelCuboid_textures;

    private static Field ModelCuboid$Quad_textures;


    public static boolean isLoaded() {
        return ModLoader.get().isModLoaded("sodium")
                || ModLoader.get().isModLoaded("rubidium")
                || ModLoader.get().isModLoaded("embeddium");
    }


    public static void copyModelCuboidUV(ModelPart source, ModelPart dest, int sourcePoly, int destPoly) {
        if (init()) {
            try {
                if (HAS_MODELCUBOID_QUADS) {
                    // sodium 0.4.9-0.5.3
                    Object sourceQuad = ((Object[]) ModelCuboid_quads.get(
                            ((Object[]) ModelPart_sodium$cuboids.get(source))[0])
                    )[sourcePoly];
                    Object destQuad = ((Object[]) ModelCuboid_quads.get(
                            ((Object[]) ModelPart_sodium$cuboids.get(dest))[0])
                    )[destPoly];

                    Vector2f[] sourceTextures = (Vector2f[]) ModelCuboid$Quad_textures.get(sourceQuad);
                    Vector2f[] destTextures = (Vector2f[]) ModelCuboid$Quad_textures.get(destQuad);

                    for (int i = 0; i < sourceTextures.length; i++) {
                        destTextures[i].x = sourceTextures[i].x;
                        destTextures[i].y = sourceTextures[i].y;
                    }
                } else if (HAS_MODELCUBOID_FLOATS || HAS_MODELCUBOID_LONGS) {
                    // sodium 0.5.4+
                    Object sourceCuboid = HAS_MODELCUBOID_CUBES ? Cube_sodium$cuboid.get(source.cubes.get(0)) :
                            ((Object[]) ModelPart_sodium$cuboids.get(source))[0];
                    Object destCuboid = HAS_MODELCUBOID_CUBES ? Cube_sodium$cuboid.get(dest.cubes.get(0)) :
                            ((Object[]) ModelPart_sodium$cuboids.get(dest))[0];

                    if (HAS_MODELCUBOID_FLOATS) {
                        // sodium 0.5.4-0.6.13
                        float[][] UVs = new float[][]{{
                                (float) ModelCuboid_u0.get(sourceCuboid),
                                (float) ModelCuboid_u1.get(sourceCuboid),
                                (float) ModelCuboid_u2.get(sourceCuboid),
                                (float) ModelCuboid_u3.get(sourceCuboid),
                                (float) ModelCuboid_u4.get(sourceCuboid),
                                (float) ModelCuboid_u5.get(sourceCuboid)
                        }, {
                                (float) ModelCuboid_v0.get(sourceCuboid),
                                (float) ModelCuboid_v1.get(sourceCuboid),
                                (float) ModelCuboid_v2.get(sourceCuboid)
                        }};
                        ((ModelCuboidExtension) destCuboid).visor$addOverrides(
                                mapDirection(destPoly),
                                mapDirection(sourcePoly),
                                UVs
                        );
                    } else {
                        // sodium 0.7+
                        long[] sourceUVs = (long[]) ModelCuboid_textures.get(sourceCuboid);
                        long[] destUVs = (long[]) ModelCuboid_textures.get(destCuboid);
                        destUVs[mapDirection(destPoly) * 4] = sourceUVs[mapDirection(sourcePoly) * 4];
                        destUVs[mapDirection(destPoly) * 4 + 1] = sourceUVs[mapDirection(sourcePoly) * 4 + 1];
                        destUVs[mapDirection(destPoly) * 4 + 2] = sourceUVs[mapDirection(sourcePoly) * 4 + 2];
                        destUVs[mapDirection(destPoly) * 4 + 3] = sourceUVs[mapDirection(sourcePoly) * 4 + 3];
                    }
                }
            } catch (IllegalAccessException | ClassCastException e) {
                ClientContext.visor.getLogger().error(
                        "Visor: sodium version has ModelCuboids, but fields are an unexpected type.",
                        e
                );
                HAS_MODELCUBOID_FLOATS = false;
                HAS_MODELCUBOID_QUADS = false;
            }
        }
    }


    private static int mapDirection(int old) {
        return switch (old) {
            case 1 -> 2;
            case 2 -> 0;
            case 3 -> 1;
            case 4 -> 3;
            case 5 -> 5;
            default -> 4; // 0 case
        };
    }

    private static boolean init() {
        if (INITIALIZED) {
            return true;
        }

        try {
            // model
            Class<?> ModelCuboid = ClassUtils.getClassWithAlternative(
                    "me.jellysquid.mods.sodium.client.render.immediate.model.ModelCuboid",
                    "net.caffeinemc.mods.sodium.client.render.immediate.model.ModelCuboid"
            );

            try {
                // sodium 0.4.9-0.5.11
                ModelPart_sodium$cuboids = ModelPart.class.getDeclaredField("sodium$cuboids");
                ModelPart_sodium$cuboids.setAccessible(true);
            } catch (NoSuchFieldException ignored) {
                // sodium 0.6+
                Cube_sodium$cuboid = ModelPart.Cube.class.getDeclaredField("sodium$cuboid");
                Cube_sodium$cuboid.setAccessible(true);
                HAS_MODELCUBOID_CUBES = true;
            }
            try {
                Class<?> ModelCuboid$Quad = ClassUtils.getClassWithAlternative(
                        "me.jellysquid.mods.sodium.client.render.immediate.model.ModelCuboid$Quad",
                        "net.caffeinemc.mods.sodium.client.render.immediate.model.ModelCuboid$Quad"
                );
                // sodium 0.4.9-0.5.3
                ModelCuboid_quads = ModelCuboid.getDeclaredField("quads");
                ModelCuboid$Quad_textures = ModelCuboid$Quad.getDeclaredField("textures");
                HAS_MODELCUBOID_QUADS = true;
            } catch (ClassNotFoundException noQuads) {
                try {
                    // sodium 0.5.4-0.6.13
                    ModelCuboid_u0 = ModelCuboid.getDeclaredField("u0");
                    ModelCuboid_u1 = ModelCuboid.getDeclaredField("u1");
                    ModelCuboid_u2 = ModelCuboid.getDeclaredField("u2");
                    ModelCuboid_u3 = ModelCuboid.getDeclaredField("u3");
                    ModelCuboid_u4 = ModelCuboid.getDeclaredField("u4");
                    ModelCuboid_u5 = ModelCuboid.getDeclaredField("u5");
                    ModelCuboid_v0 = ModelCuboid.getDeclaredField("v0");
                    ModelCuboid_v1 = ModelCuboid.getDeclaredField("v1");
                    ModelCuboid_v2 = ModelCuboid.getDeclaredField("v2");
                    HAS_MODELCUBOID_FLOATS = true;
                } catch (NoSuchFieldException array) {
                    // sodium 0.7+
                    ModelCuboid_textures = ModelCuboid.getDeclaredField("textures");
                    HAS_MODELCUBOID_LONGS = true;
                }
            }
        } catch (ClassNotFoundException ignored) {
            // ignore, old versions
        } catch (NoSuchFieldException e) {
            ClientContext.visor.getLogger().error(
                    "Visor: sodium version has ModelCuboids, but some fields are not found.",
                    e
            );
        }
        INITIALIZED = true;
        return true;
    }

}
