package org.vmstudio.visor.core.client.render.decoration.decorators.mainmenu;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import me.phoenixra.atumvr.api.misc.color.AtumColor;
import me.phoenixra.atumvr.api.misc.color.AtumColorImmutable;
import me.phoenixra.atumvr.api.misc.color.AtumColorMutable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11C;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Procedural sky for the VR main menu
 */
public final class VRMenuSky {
    // ---- DEBUG ----
    private static final boolean DEBUG_FAST_CYCLE = false;
    private static final float DEBUG_CYCLE_SEC = 60f;
    private static final int DEBUG_FORCE_MOON_PHASE = -1; // 0..7 in the vanilla 4x2 atlas (0 = full); -1 = disabled

    // ---- PALETTE ----
    private static final AtumColorImmutable DAY_ZENITH = AtumColor.immutable(74, 116, 176, 255);
    private static final AtumColorImmutable DAY_HORIZON = AtumColor.immutable(190, 210, 230, 255);
    private static final AtumColorImmutable NIGHT_ZENITH = AtumColor.immutable(8, 10, 28, 255);
    private static final AtumColorImmutable NIGHT_HORIZON = AtumColor.immutable(22, 26, 52, 255);
    private static final AtumColorImmutable DUSK_HORIZON = AtumColor.immutable(240, 140, 70, 255);

    private static final AtumColorImmutable DAY_CLOUD = AtumColor.immutable(245, 248, 255, 255);
    private static final AtumColorImmutable NIGHT_CLOUD = AtumColor.immutable(40, 46, 70, 255);
    private static final AtumColorImmutable WARM_CLOUD = AtumColor.immutable(255, 200, 150, 255);

    private static final AtumColorImmutable SUN_WHITE = AtumColor.immutable(255, 255, 255, 255);
    private static final AtumColorImmutable SUN_WARM = AtumColor.immutable(255, 150, 70, 255);

    private static final AtumColorImmutable UFO_BODY_HALO = new AtumColorImmutable(52,211,153,255);
    private static final AtumColorImmutable UFO_BODY_CORE = new AtumColorImmutable(190,245,220,255);
    private static final AtumColorImmutable UFO_LIGHT = new AtumColorImmutable(255,226,90,255);

    private static final AtumColorImmutable VISOR_STAR_HALO_C = AtumColor.immutable(30, 200, 245);
    private static final AtumColorImmutable VISOR_STAR_CORE_C = AtumColor.immutable(200, 250, 255);

    // ---- SKY BOX ----
    private static final float SKY_BOX = 100.0f;

    private static final float SUNRISE_TIME = 6.0f;
    private static final float SUNSET_TIME = 18.0f;

    private static final double ARC_TILT_RAD = Math.toRadians(20f); // 0 = sun passes dead overhead; ~20 arcs it through the south

    private static final long SKY_UPDATE_FREQUENCY = 200L;

    // ---- CELESTIAL BODIES ----
    private static final ResourceLocation SUN_TEXTURE = new ResourceLocation("textures/environment/sun.png");
    private static final float SUN_DISTANCE = 92.0f;
    private static final float SUN_SIZE = 13.0f;

    private static final ResourceLocation MOON_TEXTURE = new ResourceLocation("textures/environment/moon_phases.png");
    private static final float MOON_DISTANCE = 90.0f;
    private static final float MOON_SIZE = 10.0f;

    // real lunar phase: days since a known new moon, wrapped to the synodic month
    private static final double SYNODIC_MONTH_DAYS = 29.530588853;
    private static final long NEW_MOON_EPOCH_MS = 947_182_440_000L; // 2000-01-06 18:14 UTC

    // ---- STARS ----
    private static final int STAR_COUNT = 500;
    private static final float STAR_RADIUS = 96.0f;
    private static final float STAR_SIZE = 0.45f;
    private static final float STAR_BRIGHT = 0.9f;
    private static final float[][][] STAR_QUAD; // [star][corner][xyz]
    private static final float[] STAR_PHASE;    // per-star twinkle phase offset

    // ---- SHOOTING STARS ----
    private static final float SHOOTINGSTAR_FREQUENCY = 20f;
    private static final float SHOOTINGSTAR_CHANCE = 0.85f;
    private static final float SHOOTINGSTAR_DURATION_SEC = 0.65f;
    private static final float SHOOTINGSTAR_ARC = 0.38f;        // radians of sky crossed per streak
    private static final float SHOOTINGSTAR_TAIL_ARC = 0.11f;   // angular length of the fading trail
    private static final float SHOOTINGSTAR_RADIUS = 95.0f;
    private static final float SHOOTINGSTAR_WIDTH = 0.22f;      // half-width at the bright head

    // ---- MIDNIGHT UFO (00:00-01:00) ----
    //amount of columns has to be 11!!
    private static final String[] UFO_ROWS = {
            "....111....",
            "...11111...",
            ".111111111.",
            "11111111111",
            ".2..333..4.", // chasing lights
    };
    private static final float UFO_DOT_SPACING = 0.9f;
    private static final float UFO_RADIUS = 90.0f;
    private static final float UFO_DOT_HALO = 0.85f;
    private static final float UFO_DOT_CORE = 0.42f;

    private static final float UFO_FREQUENCY = 35f;
    private static final float UFO_LIFETIME = 15f;
    private static final float UFO_FADE_SEC = 0.3f;
    private static final float UFO_LIGHT_STEP_HZ = 2f;
    private static final double UFO_AZIMUTH_STEP = 2.39996;

    private static final float[] UFO_LX;
    private static final float[] UFO_LY;
    private static final int[] UFO_LIGHT_IDX;
    private static final int UFO_DOTS_AMOUNT;
    private static final int UFO_LIGHT_GROUPS;

    // ---- VISOR SIGN ----
    private static final Vector3f VISOR_DIR = new Vector3f(0f, 0.5f, -1f).normalize();
    private static final float VISOR_RADIUS = 88.0f;
    private static final float VISOR_DOTS_SPACING = 1.6f;   // spacing between dots
    private static final float VISOR_SUPPRESS_DOT = 0.95f; // hide background stars within this cone

    // skin sizes and colors
    private static final float VISOR_STAR_HALO = 1.15f;
    private static final float VISOR_STAR_CORE = 0.60f;

    private static final float VISOR_CLOUD_PUFF = 1.35f;
    private static final float VISOR_CLOUD_CORE = 0.85f;

    // skin switch + animation
    private static final float VISOR_DAY_THRESHOLD = 0.5f; // curDay at which the sign flips star<->cloud
    private static final float VISOR_GLEAM_W = 4.0f;   // half-width of the traveling highlight, in columns
    private static final float VISOR_GLEAM_AMT = 0.55f;  // extra brightness at the gleam
    private static final float VISOR_GLEAM_SPEED = 2.5f; // columns per second
    private static final float VISOR_PULSE_AMT = 0.22f; // size breathing in %
    private static final float VISOR_PULSE_SPEED = 1.1f;  // breaths per second

    // glyphs, rows top->bottom, '1' = lit
    private static final int GLYPH_W = 5, GLYPH_H = 7, GLYPH_GAP = 1;
    private static final String[][] VISOR_GLYPHS = {
            { "10001", "10001", "10001", "10001", "01010", "01010", "00100" }, // V
            { "11111", "00100", "00100", "00100", "00100", "00100", "11111" }, // I
            { "01110", "10001", "10000", "01110", "00001", "10001", "01110" }, // S
            { "01110", "10001", "10001", "10001", "10001", "10001", "01110" }, // O
            { "11110", "10001", "10001", "11110", "10100", "10010", "10001" }, // R
    };
    private static final int VISOR_TOTAL_COLS =
            VISOR_GLYPHS.length * GLYPH_W + (VISOR_GLYPHS.length - 1) * GLYPH_GAP;

    // shared billboard basis of the sign plane
    private static final Vector3f VISOR_RIGHT;
    private static final Vector3f VISOR_UP;

    private static final DotsSign VISOR_SIGN;

    private static ResourceLocation GLOW_SPRITE = null;

    // ---- CLOUDS ----
    private static final float CLOUD_Y = -14.0f;
    private static final float SHADE_UP = 1.00f, SHADE_NS = 0.85f, SHADE_WE = 0.72f;
    private static final float CLOUD_THICK = 2.0f;
    private static final float CLOUD_TILE = 5.0f;
    private static final float CLOUD_CELL = 25.0f;
    private static final float CLOUD_JITTER = 10.0f;
    private static final float CLOUD_JITTER_Y = 4.0f;
    private static final float CLOUD_RANGE = 100.0f;
    private static final float CLOUD_FILL = 0.80f;
    private static final float CLOUD_DRIFT_X = 0.30f;
    private static final float CLOUD_DRIFT_Z = 0.12f;

    private static final float AERIAL_START = 38.0f;
    private static final float AERIAL_MAX = 0.50f;
    private static final float ALPHA_FADE_START = 60.0f;
    private static final long FADE_IN_MS = 800L;

    private static final int[][][] CLOUD_SHAPES = {
            // 2x2
            {{0, 0}, {1, 0}, {0, 1}, {1, 1}},
            // 3x2
            {{-1, 0}, {0, 0}, {1, 0}, {-1, 1}, {0, 1}, {1, 1}},
            // 3x3
            {{-1, -1}, {0, -1}, {1, -1}, {-1, 0}, {0, 0}, {1, 0}, {-1, 1}, {0, 1}, {1, 1}},
            // 4x2
            {{-1, 0}, {0, 0}, {1, 0}, {2, 0}, {-1, 1}, {0, 1}, {1, 1}, {2, 1}},
            // 4x3
            {{-1, -1}, {0, -1}, {1, -1}, {2, -1}, {-1, 0}, {0, 0}, {1, 0}, {2, 0}, {-1, 1}, {0, 1}, {1, 1}, {2, 1}},
            // 4x4
            {{0, 0}, {1, 0}, {2, 0}, {3, 0}, {0, 1}, {1, 1}, {2, 1}, {3, 1}, {0, 2}, {1, 2}, {2, 2}, {3, 2}, {0, 3}, {1, 3}, {2, 3}, {3, 3}},
            // 4x3, one corner notched
            {{-1, -1}, {0, -1}, {1, -1}, {-1, 0}, {0, 0}, {1, 0}, {2, 0}, {-1, 1}, {0, 1}, {1, 1}, {2, 1}},
            // 3x2 with a single-tile bump on top edge
            {{-1, 0}, {0, 0}, {1, 0}, {-1, 1}, {0, 1}, {1, 1}, {0, 2}},
            // 4x2, one corner notched
            {{-1, 0}, {0, 0}, {1, 0}, {2, 0}, {0, 1}, {1, 1}, {2, 1}},
    };

    private static final int FACE_XN = 1, FACE_XP = 2, FACE_ZN = 4, FACE_ZP = 8;

    private static final CloudVariant[] CLOUD_VARIANTS = new CloudVariant[CLOUD_SHAPES.length * 4];

    // ---- USER DOTS----
    private static final int USER_DOT_MAX = 1000;
    static final float USER_DOT_RADIUS = 93.0f;
    static final float USER_DOT_MIN_Y = -0.05f;
    static final float USER_ERASE_CONE_COS = 0.998f;

    private static final float[] userDotX = new float[USER_DOT_MAX];
    private static final float[] userDotY = new float[USER_DOT_MAX];
    private static final float[] userDotZ = new float[USER_DOT_MAX];
    private static final float[] userDotPhase = new float[USER_DOT_MAX];
    private static int userDotCount = 0;
    private static int userDotsAdded = 0;

    // ---- FRAME STATE ----
    private static final AtumColorMutable currentZenith = DAY_ZENITH.asMutable();
    private static final AtumColorMutable currentHorizon = DAY_HORIZON.asMutable(); // with twilight warmth
    private static final AtumColorMutable currentHorizonBase = DAY_HORIZON.asMutable();  // without twilight warmth
    private static final AtumColorMutable currentCloud = DAY_CLOUD.asMutable();
    private static final AtumColorMutable sunTint = AtumColor.mutable(0,0,0, 255);
    private static final AtumColorMutable moonTint = AtumColor.mutable(205, 215, 255, 255);

    private static final Vector3f currentSunDir = new Vector3f(0, 1, 0);
    private static final Vector3f currentMoonDir = new Vector3f(0, -1, 0);
    private static float currentDay = 1f;        // 0 = full night, 1 = full day
    private static float currentTwilight = 0f;   // 0 = no twilight, 1 = full twilight
    private static float sunAzimuthX = 1f, sunAzimuthZ = 0f; // horizontal sun direction for the dusk gradient

    private static long lastSkyUpdate = -1;
    private static final int[] blendScratch = new int[3];

    private static final Vector3f scratchDir = new Vector3f();
    private static final Vector3f scratchRight = new Vector3f();
    private static final Vector3f scratchUp = new Vector3f();
    private static final Vector3f scratchCenter = new Vector3f();

    private static long startTime = Util.getMillis();
    private static long currentTime = Util.getMillis();
    private static double currentTimeSec = Util.getMillis() / 1000f;
    private static float currentSceneTime = Util.getMillis();
    private static float currentScenePhase = 0;

    static {
        // ---- STARS ----
        float[][][] starQuads = new float[STAR_COUNT][][];
        float[] starPhases = new float[STAR_COUNT];
        int keptStars = 0;
        for (int star = 0; star < STAR_COUNT; star++) {
            float azimuth = hash01(star, 0, 20) * (float) (2 * Math.PI);
            float cosTheta = hash01(star, 0, 21) * 2f - 1f;
            float sinTheta = (float) Math.sqrt(Math.max(0f, 1f - cosTheta * cosTheta));
            float dirX = sinTheta * (float) Math.cos(azimuth);
            float dirY = cosTheta;
            float dirZ = sinTheta * (float) Math.sin(azimuth);

            if (dirY < -0.05f) {
                continue;
            }
            //Keep a clean area around VISOR sign
            if (dirX * VISOR_DIR.x + dirY * VISOR_DIR.y + dirZ * VISOR_DIR.z >= VISOR_SUPPRESS_DOT) {
                continue;
            }

            Vector3f[] basis = billboardBasis(new Vector3f(dirX, dirY, dirZ));
            Vector3f right = basis[0], up = basis[1];
            float baseX = dirX * STAR_RADIUS, baseY = dirY * STAR_RADIUS, baseZ = dirZ * STAR_RADIUS;
            float halfSize = STAR_SIZE * 0.5f * (0.6f + hash01(star, 0, 22) * 1.1f);
            float[][] corners = new float[4][3];
            setCorner(corners[0], baseX, baseY, baseZ, right, up, -halfSize, -halfSize);
            setCorner(corners[1], baseX, baseY, baseZ, right, up,  halfSize, -halfSize);
            setCorner(corners[2], baseX, baseY, baseZ, right, up,  halfSize,  halfSize);
            setCorner(corners[3], baseX, baseY, baseZ, right, up, -halfSize,  halfSize);
            starQuads[keptStars] = corners;
            starPhases[keptStars] = star * 1.37f;
            keptStars++;
        }
        STAR_QUAD = Arrays.copyOf(starQuads, keptStars);
        STAR_PHASE = Arrays.copyOf(starPhases, keptStars);


        // ---- UFO ----
        int rows = UFO_ROWS.length, cols = UFO_ROWS[0].length();
        float centerCol = (cols - 1) / 2f;
        float centerRow = (rows - 1) / 2f;
        int dotCount = 0;
        int maxLightGroup = -1;
        for (String row : UFO_ROWS) {
            for (int column = 0; column < cols; column++) {
                char cell = row.charAt(column);
                if (cell == '1') {
                    dotCount++;
                } else if (cell >= '2' && cell <= '9') {
                    dotCount++;
                    maxLightGroup = Math.max(maxLightGroup, cell - '2');
                }
            }
        }
        UFO_DOTS_AMOUNT = dotCount;
        UFO_LIGHT_GROUPS = Math.max(1, maxLightGroup + 1);

        UFO_LX = new float[dotCount];
        UFO_LY = new float[dotCount];
        UFO_LIGHT_IDX = new int[dotCount];
        int dotIndex = 0;
        for (int row = 0; row < rows; row++) {
            String line = UFO_ROWS[row];
            for (int column = 0; column < cols; column++) {
                char cell = line.charAt(column);
                boolean light = cell >= '2' && cell <= '9';
                if (cell != '1' && !light) {
                    continue;
                }
                UFO_LX[dotIndex] = (centerCol - column) * UFO_DOT_SPACING;
                UFO_LY[dotIndex] = (centerRow - row) * UFO_DOT_SPACING;
                UFO_LIGHT_IDX[dotIndex] = light ? cell - '2' : -1;
                dotIndex++;
            }
        }

        // ---- VISOR SIGN ----
        Vector3f[] basis = billboardBasis(VISOR_DIR);
        VISOR_RIGHT = basis[0];
        VISOR_UP = basis[1];
        VISOR_SIGN = createDotsSign(VISOR_GLYPHS);

        // ---- CLOUDS ----
        for (int shapeIndex = 0; shapeIndex < CLOUD_SHAPES.length; shapeIndex++) {
            int[][] shape = CLOUD_SHAPES[shapeIndex];
            int tileCount = shape.length;
            for (int rotation = 0; rotation < 4; rotation++) {
                int[] tileXs = new int[tileCount];
                int[] tileZs = new int[tileCount];
                int sumX = 0, sumZ = 0;
                for (int tile = 0; tile < tileCount; tile++) {
                    int tileX = shape[tile][0], tileZ = shape[tile][1];
                    for (int turn = 0; turn < rotation; turn++) {
                        int rotatedX = tileZ;
                        tileZ = -tileX;
                        tileX = rotatedX;
                    }
                    tileXs[tile] = tileX;
                    tileZs[tile] = tileZ;
                    sumX += tileX;
                    sumZ += tileZ;
                }
                byte[] faces = new byte[tileCount];
                for (int tile = 0; tile < tileCount; tile++) {
                    int faceFlags = 0;
                    if (!hasTile(tileXs, tileZs, tileCount, tileXs[tile] - 1, tileZs[tile])) {
                        faceFlags |= FACE_XN;
                    }
                    if (!hasTile(tileXs, tileZs, tileCount, tileXs[tile] + 1, tileZs[tile])) {
                        faceFlags |= FACE_XP;
                    }
                    if (!hasTile(tileXs, tileZs, tileCount, tileXs[tile], tileZs[tile] - 1)) {
                        faceFlags |= FACE_ZN;
                    }
                    if (!hasTile(tileXs, tileZs, tileCount, tileXs[tile], tileZs[tile] + 1)) {
                        faceFlags |= FACE_ZP;
                    }
                    faces[tile] = (byte) faceFlags;
                }
                CLOUD_VARIANTS[shapeIndex * 4 + rotation] = new CloudVariant(tileXs, tileZs, faces,
                        (sumX / (float) tileCount) * CLOUD_TILE, (sumZ / (float) tileCount) * CLOUD_TILE);
            }
        }
    }

    private VRMenuSky() {
    }


    // ====== ENTRY POINTS ======

    public static void reset() {
        startTime = Util.getMillis();
    }

    public static void renderFirst(PoseStack poseStack) {

        // --- Prepare variables ---
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        Matrix4f pose = poseStack.last().pose();
        prepareSkyBox();
        currentTime = Util.getMillis();
        currentTimeSec = currentTime / 1000d;
        currentSceneTime = currentSceneTime();
        currentScenePhase = sceneTimeToPhase(currentSceneTime);

        // --- Setup ---
        RenderSystem.clear(GL11C.GL_COLOR_BUFFER_BIT | GL11C.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.depthMask(false);
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // --- Render ---
        renderSkyBox(builder, pose);

        renderStars(builder, pose);

        renderSun(builder, pose);
        renderMoon(builder, pose);

        renderVisorSign(builder, pose);
        renderUfo(builder, pose);

        renderUserDots(builder, pose);

        // --- Restore ---
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
    }

    public static void renderLast(PoseStack poseStack) {
        // --- Prepare variables ---
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        Matrix4f pose = poseStack.last().pose();

        // --- Setup ---
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        // blend now ON: edge clouds dissolve via alpha rather than being recolored solids
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();

        // --- Render ---
        renderClouds(builder, pose);
    }

    // ====== TIME ======

    private static float currentSceneTime() {
        if (DEBUG_FAST_CYCLE) {
            float period = DEBUG_CYCLE_SEC * 1000f;
            return (currentTime % (long) period) / period * 24f;
        }
        LocalTime now = LocalTime.now();
        return now.getHour() + now.getMinute() / 60f + now.getSecond() / 3600f;
    }

    private static float sceneTimeToPhase(float time) {
        // 0 = sunrise | 0.25 = noon | 0.5 = sunset | 0.75 = midnight
        if (time >= SUNRISE_TIME && time < SUNSET_TIME) {
            return 0.5f * (time - SUNRISE_TIME) / (SUNSET_TIME - SUNRISE_TIME);
        }
        float nightTime = (time < SUNRISE_TIME)
                ? time + 24f
                : time;
        return 0.5f + 0.5f * (nightTime - SUNSET_TIME) / (SUNRISE_TIME + 24f - SUNSET_TIME);
    }

    private static int currentMoonPhase() {
        if (DEBUG_FORCE_MOON_PHASE >= 0) {
            return DEBUG_FORCE_MOON_PHASE & 7;
        }
        double age = ((System.currentTimeMillis() - NEW_MOON_EPOCH_MS) / 86_400_000.0) % SYNODIC_MONTH_DAYS;
        return (int) ((Math.round(age / SYNODIC_MONTH_DAYS * 8.0) + 4) & 7);
    }


    // ====== SKY BOX ======

    private static void prepareSkyBox() {
        if (!DEBUG_FAST_CYCLE && currentTime - lastSkyUpdate < SKY_UPDATE_FREQUENCY) {
            return;
        }
        lastSkyUpdate = currentTime;

        float theta = currentScenePhase * (float) (Math.PI * 2.0);

        float sin = (float) Math.sin(theta), cos = (float) Math.cos(theta);
        currentSunDir.set(
                cos,
                (float) Math.cos(ARC_TILT_RAD) * sin,
                (float) Math.sin(ARC_TILT_RAD) * sin
        ).normalize();
        currentMoonDir.set(currentSunDir).mul(-1f);

        float sunDirX = currentSunDir.x;
        float elevation = currentSunDir.y;
        float sunDirZ = currentSunDir.z;

        float azimuthLen = (float) Math.sqrt(sunDirX * sunDirX + sunDirZ * sunDirZ);
        if (azimuthLen > 1e-4f) {
            sunAzimuthX = sunDirX / azimuthLen;
            sunAzimuthZ = sunDirZ / azimuthLen;
        }

        currentDay = smoothstep(-0.12f, 0.18f, elevation);
        float twilight = 1f - Math.min(1f, Math.abs(elevation) / 0.28f);
        currentTwilight = twilight * twilight * (3f - 2f * twilight);

        currentZenith.set(
                NIGHT_ZENITH.blend(DAY_ZENITH, currentDay, blendScratch)
        );

        currentHorizonBase.set(
                NIGHT_HORIZON.blend(DAY_HORIZON, currentDay, blendScratch)
        );

        currentHorizon.set(
                currentHorizonBase.blend(DUSK_HORIZON, currentTwilight, blendScratch)
        );

        currentCloud.set(
                NIGHT_CLOUD.blend(DAY_CLOUD, currentDay, blendScratch)
        ).set(
                currentCloud.blend(WARM_CLOUD, currentTwilight * 0.8f, blendScratch)
        );
    }

    private static void renderSkyBox(BufferBuilder builder,
                                     Matrix4f pose){
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        // -Z wall
        horizon(builder, pose, -SKY_BOX, -SKY_BOX, -SKY_BOX);
        horizon(builder, pose, SKY_BOX, -SKY_BOX, -SKY_BOX);
        zenith(builder, pose, SKY_BOX, SKY_BOX, -SKY_BOX);
        zenith(builder, pose, -SKY_BOX, SKY_BOX, -SKY_BOX);
        // +Z wall
        horizon(builder, pose, SKY_BOX, -SKY_BOX, SKY_BOX);
        horizon(builder, pose, -SKY_BOX, -SKY_BOX, SKY_BOX);
        zenith(builder, pose, -SKY_BOX, SKY_BOX, SKY_BOX);
        zenith(builder, pose, SKY_BOX, SKY_BOX, SKY_BOX);
        // -X wall
        horizon(builder, pose, -SKY_BOX, -SKY_BOX, SKY_BOX);
        horizon(builder, pose, -SKY_BOX, -SKY_BOX, -SKY_BOX);
        zenith(builder, pose, -SKY_BOX, SKY_BOX, -SKY_BOX);
        zenith(builder, pose, -SKY_BOX, SKY_BOX, SKY_BOX);
        // +X wall
        horizon(builder, pose, SKY_BOX, -SKY_BOX, -SKY_BOX);
        horizon(builder, pose, SKY_BOX, -SKY_BOX, SKY_BOX);
        zenith(builder, pose, SKY_BOX, SKY_BOX, SKY_BOX);
        zenith(builder, pose, SKY_BOX, SKY_BOX, -SKY_BOX);
        // TOP
        zenith(builder, pose, -SKY_BOX, SKY_BOX, -SKY_BOX);
        zenith(builder, pose, SKY_BOX, SKY_BOX, -SKY_BOX);
        zenith(builder, pose, SKY_BOX, SKY_BOX, SKY_BOX);
        zenith(builder, pose, -SKY_BOX, SKY_BOX, SKY_BOX);
        // BOTTOM
        horizon(builder, pose, -SKY_BOX, -SKY_BOX, SKY_BOX);
        horizon(builder, pose, SKY_BOX, -SKY_BOX, SKY_BOX);
        horizon(builder, pose, SKY_BOX, -SKY_BOX, -SKY_BOX);
        horizon(builder, pose, -SKY_BOX, -SKY_BOX, -SKY_BOX);

        BufferUploader.drawWithShader(builder.end());
    }

    // ====== CELESTIAL BODIES ======

    private static void renderSun(BufferBuilder builder,
                                  Matrix4f pose) {
        float elevation = currentSunDir.y;
        float visible = smoothstep(-0.06f, 0.04f, elevation);
        if (visible <= 0f) {
            return;
        }
        sunTint.set(
                SUN_WARM.blend(
                        SUN_WHITE,
                        smoothstep(0.04f, 0.30f, elevation),
                        blendScratch
                )
        );
        float size = SUN_SIZE * (1f + 0.45f * currentTwilight);
        renderCelestial(
                builder, pose,
                currentSunDir, visible,
                SUN_TEXTURE, SUN_DISTANCE, size,
                sunTint,
                0f, 0f, 1f, 1f
        );
    }

    private static void renderMoon(BufferBuilder builder,
                                   Matrix4f pose) {
        float visible = smoothstep(-0.06f, 0.06f, currentMoonDir.y);
        if (visible <= 0f) {
            return;
        }
        int phase = currentMoonPhase();
        float u0 = (phase % 4) / 4f;
        float v0 = ((int)(phase / 4f)) / 2f;
        renderCelestial(
                builder, pose,
                currentMoonDir, visible,
                MOON_TEXTURE, MOON_DISTANCE, MOON_SIZE,
                moonTint,
                u0, v0, u0 + 0.25f, v0 + 0.5f
        );
    }

    private static void renderCelestial(BufferBuilder builder, Matrix4f pose,
                                        Vector3f dir, float visible,
                                        ResourceLocation texture, float distance, float size,
                                        AtumColor color,
                                        float u0, float v0, float u1, float v1) {
        scratchCenter.set(dir).mul(distance);
        billboardBasis(dir, scratchRight, scratchUp);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShaderColor(color.getRed(), color.getGreen(), color.getBlue(), visible);
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);

        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        billboardVertex(builder, pose, scratchCenter, scratchRight, scratchUp, -size, -size, u0, v0);
        billboardVertex(builder, pose, scratchCenter, scratchRight, scratchUp,  size, -size, u1, v0);
        billboardVertex(builder, pose, scratchCenter, scratchRight, scratchUp,  size,  size, u1, v1);
        billboardVertex(builder, pose, scratchCenter, scratchRight, scratchUp, -size,  size, u0, v1);
        BufferUploader.drawWithShader(builder.end());

        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    // ====== STARS ======

    private static void renderStars(BufferBuilder builder, Matrix4f pose) {
        float night = 1f - currentDay;
        if (night <= 0.05f) {
            return;
        }

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);

        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        for (int star = 0; star < STAR_QUAD.length; star++) {
            float twinkle = 0.65f + 0.35f * (float) Math.sin(currentTimeSec * 1.6f + STAR_PHASE[star]);
            int alpha = (int) (255f * night * twinkle * STAR_BRIGHT);
            float[][] quad = STAR_QUAD[star];
            starVertex(builder, pose, quad[0], alpha);
            starVertex(builder, pose, quad[1], alpha);
            starVertex(builder, pose, quad[2], alpha);
            starVertex(builder, pose, quad[3], alpha);
        }

        emitShootingStar(builder, pose, night);

        BufferUploader.drawWithShader(builder.end());

        RenderSystem.defaultBlendFunc();
    }

    private static void emitShootingStar(BufferBuilder builder, Matrix4f pose,
                                         float night) {

        int windowIndex = (int) (currentTimeSec / SHOOTINGSTAR_FREQUENCY);
        if (hash01(windowIndex, 0, 30) > SHOOTINGSTAR_CHANCE) {
            return;
        }

        float meteorStartOffsetSec = 1f + hash01(windowIndex, 0, 31)
                * (SHOOTINGSTAR_FREQUENCY - SHOOTINGSTAR_DURATION_SEC - 2f);

        float meteorAgeSec = (float) (currentTimeSec - (double) windowIndex * SHOOTINGSTAR_FREQUENCY)
                - meteorStartOffsetSec;

        if (meteorAgeSec < 0f || meteorAgeSec > SHOOTINGSTAR_DURATION_SEC) {
            return;
        }

        float meteorProgress = meteorAgeSec / SHOOTINGSTAR_DURATION_SEC;
        float fadeInOut = (float) Math.sin(Math.PI * meteorProgress);
        int meteorAlpha = (int) (235f * night * fadeInOut);

        if (meteorAlpha <= 0) {
            return;
        }

        // Spawn direction: mid-elevation, any azimuth, away from the sign
        float spawnAzimuth = hash01(windowIndex, 1, 32) * (float) (Math.PI * 2.0);
        float spawnDirY = 0.35f + 0.45f * hash01(windowIndex, 1, 33);
        float spawnHorizontalRadius = (float) Math.sqrt(1f - spawnDirY * spawnDirY);
        float spawnDirX = spawnHorizontalRadius * (float) Math.cos(spawnAzimuth);
        float spawnDirZ = spawnHorizontalRadius * (float) Math.sin(spawnAzimuth);

        if (spawnDirX * VISOR_DIR.x + spawnDirY * VISOR_DIR.y + spawnDirZ * VISOR_DIR.z > 0.9f) {
            return;
        }

        // Travel tangent: horizontal direction rolled by a random angle around the spawn direction
        float inverseHorizontalRadius = 1f / Math.max(1e-4f, spawnHorizontalRadius);

        float horizontalTangentX = -spawnDirZ * inverseHorizontalRadius;
        float horizontalTangentZ = spawnDirX * inverseHorizontalRadius;

        float rolledBasisX = spawnDirY * horizontalTangentZ;
        float rolledBasisY = spawnDirZ * horizontalTangentX - spawnDirX * horizontalTangentZ;
        float rolledBasisZ = -spawnDirY * horizontalTangentX;

        float tangentRoll = hash01(windowIndex, 2, 34) * (float) (Math.PI * 2.0);
        float rollCos = (float) Math.cos(tangentRoll);
        float rollSin = (float) Math.sin(tangentRoll);

        float travelDirX = horizontalTangentX * rollCos + rolledBasisX * rollSin;
        float travelDirY = rolledBasisY * rollSin;
        float travelDirZ = horizontalTangentZ * rollCos + rolledBasisZ * rollSin;

        // Head/tail on the great circle through the spawn direction along the travel tangent
        float headAngle = meteorProgress * SHOOTINGSTAR_ARC;
        float tailAngle = headAngle - SHOOTINGSTAR_TAIL_ARC;

        float headCos = (float) Math.cos(headAngle);
        float headSin = (float) Math.sin(headAngle);
        float tailCos = (float) Math.cos(tailAngle);
        float tailSin = (float) Math.sin(tailAngle);

        float headDirX = spawnDirX * headCos + travelDirX * headSin;
        float headDirY = spawnDirY * headCos + travelDirY * headSin;
        float headDirZ = spawnDirZ * headCos + travelDirZ * headSin;

        float tailDirX = spawnDirX * tailCos + travelDirX * tailSin;
        float tailDirY = spawnDirY * tailCos + travelDirY * tailSin;
        float tailDirZ = spawnDirZ * tailCos + travelDirZ * tailSin;

        // Side vector perpendicular to view direction and travel direction
        float sideDirX = headDirY * travelDirZ - headDirZ * travelDirY;
        float sideDirY = headDirZ * travelDirX - headDirX * travelDirZ;
        float sideDirZ = headDirX * travelDirY - headDirY * travelDirX;

        float inverseSideLength = 1f / (float) Math.sqrt(Math.max(
                1e-6f,
                sideDirX * sideDirX + sideDirY * sideDirY + sideDirZ * sideDirZ
        ));

        sideDirX *= inverseSideLength;
        sideDirY *= inverseSideLength;
        sideDirZ *= inverseSideLength;

        float meteorRadius = SHOOTINGSTAR_RADIUS;
        float headHalfWidth = SHOOTINGSTAR_WIDTH;
        float tailHalfWidth = SHOOTINGSTAR_WIDTH * 0.15f;

        builder.vertex(
                pose,
                tailDirX * meteorRadius - sideDirX * tailHalfWidth,
                tailDirY * meteorRadius - sideDirY * tailHalfWidth,
                tailDirZ * meteorRadius - sideDirZ * tailHalfWidth
        ).color(255, 248, 230, 0).endVertex();

        builder.vertex(
                pose,
                tailDirX * meteorRadius + sideDirX * tailHalfWidth,
                tailDirY * meteorRadius + sideDirY * tailHalfWidth,
                tailDirZ * meteorRadius + sideDirZ * tailHalfWidth
        ).color(255, 248, 230, 0).endVertex();

        builder.vertex(
                pose,
                headDirX * meteorRadius + sideDirX * headHalfWidth,
                headDirY * meteorRadius + sideDirY * headHalfWidth,
                headDirZ * meteorRadius + sideDirZ * headHalfWidth
        ).color(255, 252, 245, meteorAlpha).endVertex();

        builder.vertex(
                pose,
                headDirX * meteorRadius - sideDirX * headHalfWidth,
                headDirY * meteorRadius - sideDirY * headHalfWidth,
                headDirZ * meteorRadius - sideDirZ * headHalfWidth
        ).color(255, 252, 245, meteorAlpha).endVertex();
    }

    // ====== UFO ======

    private static void renderUfo(BufferBuilder builder, Matrix4f pose) {
        if (currentSceneTime >= 1f) {
            return; //only between 00:00AM and 01:00AM
        }

        int cycle = (int) (currentTimeSec / UFO_FREQUENCY);
        float ageSec = (float) (currentTimeSec - (double) cycle * UFO_FREQUENCY);
        if (ageSec >= UFO_LIFETIME) {
            return; // departed for the rest of this cycle
        }

        ensureGlowSprite();

        ufoParkingDir(cycle, scratchDir);
        billboardBasis(scratchDir, scratchRight, scratchUp);
        float ufoX = scratchDir.x * UFO_RADIUS;
        float ufoY = scratchDir.y * UFO_RADIUS;
        float ufoZ = scratchDir.z * UFO_RADIUS;

        // blink in/out at both ends instead of popping
        float fade = clamp01(Math.min(ageSec, UFO_LIFETIME - ageSec) / UFO_FADE_SEC);
        int chaseStep = (int) (ageSec * UFO_LIGHT_STEP_HZ) % UFO_LIGHT_GROUPS;

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, GLOW_SPRITE);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);

        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        for (int i = 0; i < UFO_DOTS_AMOUNT; i++) {
            float dotX = ufoX + scratchRight.x * UFO_LX[i] + scratchUp.x * UFO_LY[i];
            float dotY = ufoY + scratchRight.y * UFO_LX[i] + scratchUp.y * UFO_LY[i];
            float dotZ = ufoZ + scratchRight.z * UFO_LX[i] + scratchUp.z * UFO_LY[i];

            int group = UFO_LIGHT_IDX[i];
            if (group >= 0) { // belly light: bright on its chase step, dim otherwise
                boolean lit = group == chaseStep;
                int haloAlpha = (int) ((lit ? 160 : 40) * fade);
                int coreAlpha = (int) ((lit ? 255 : 70) * fade);
                dotQuad(builder, pose, scratchRight, scratchUp, dotX, dotY, dotZ,
                        UFO_DOT_HALO * 0.8f, UFO_LIGHT, haloAlpha);
                dotQuad(builder, pose, scratchRight, scratchUp, dotX, dotY, dotZ,
                        UFO_DOT_CORE * 0.9f, UFO_LIGHT, coreAlpha);
            } else { // hull dot
                dotQuad(builder, pose, scratchRight, scratchUp, dotX, dotY, dotZ,
                        UFO_DOT_HALO, UFO_BODY_HALO, (int) (150 * fade));
                dotQuad(builder, pose, scratchRight, scratchUp, dotX, dotY, dotZ,
                        UFO_DOT_CORE, UFO_BODY_CORE, (int) (235 * fade));
            }
        }
        BufferUploader.drawWithShader(builder.end());

        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    private static void ufoParkingDir(int cycle, Vector3f out) {
        float azimuth = (float) ((cycle * UFO_AZIMUTH_STEP) % (Math.PI * 2.0));
        float elevation = 0.175f + 0.075f * (float) Math.sin(cycle * 1.7);
        float horiz = (float) Math.cos(elevation);
        out.set(
                horiz * (float) Math.cos(azimuth),
                (float) Math.sin(elevation),
                horiz * (float) Math.sin(azimuth)
        );
        if (out.dot(VISOR_DIR) > 0.93f) {
            azimuth += 1.1f;
            out.x = horiz * (float) Math.cos(azimuth);
            out.z = horiz * (float) Math.sin(azimuth);
        }
    }

    // ====== VISOR SIGN ======

    private static void renderVisorSign(BufferBuilder builder,
                                        Matrix4f pose) {
        ensureGlowSprite();

        float gleamPos = (float) (currentTimeSec * VISOR_GLEAM_SPEED) % (VISOR_TOTAL_COLS + VISOR_GLEAM_W * 2f) - VISOR_GLEAM_W;
        boolean asCloudDots = currentDay >= VISOR_DAY_THRESHOLD;

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, GLOW_SPRITE);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        if (asCloudDots) {
            RenderSystem.defaultBlendFunc();
        } else {
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE); // additive
        }

        int[] cloudTint = {0, 0, 0};
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        for (int i = 0; i < VISOR_SIGN.n; i++) {
            float gleam = 1f + VISOR_GLEAM_AMT * Math.max(0f, 1f - Math.abs(VISOR_SIGN.col[i] - gleamPos) / VISOR_GLEAM_W);
            float cx = VISOR_SIGN.px[i], cy = VISOR_SIGN.py[i], cz = VISOR_SIGN.pz[i]; // anchored
            if (asCloudDots) {
                // calmer, slower breathing for the cloud skin
                float pulse = 1f + VISOR_PULSE_AMT * 0.6f * (float) Math.sin(currentTimeSec * VISOR_PULSE_SPEED * 0.7f + i * 1.3f);
                cloudTint[0] = (int) Math.min(255f, currentCloud.getRedInt() * gleam); // tinted live + brightened by the gleam
                cloudTint[1] = (int) Math.min(255f, currentCloud.getGreenInt() * gleam);
                cloudTint[2] = (int) Math.min(255f, currentCloud.getBlueInt() * gleam);
                spriteQuad(builder, pose, cx, cy, cz, VISOR_CLOUD_PUFF * pulse, cloudTint, 200);
                spriteQuad(builder, pose, cx, cy, cz, VISOR_CLOUD_CORE * pulse, cloudTint, 235);
            } else {
                float pulse = 1f + VISOR_PULSE_AMT * (float) Math.sin(currentTimeSec * VISOR_PULSE_SPEED + i * 1.7f);
                int haloAlpha = (int) Math.min(255f, 170f * gleam);
                int coreAlpha = (int) Math.min(255f, 255f * gleam);
                int[] color = VISOR_STAR_HALO_C.asIntArray(false);
                spriteQuad(builder, pose, cx, cy, cz, VISOR_STAR_HALO * pulse, color, haloAlpha);
                spriteQuad(builder, pose, cx, cy, cz, VISOR_STAR_CORE * pulse, color, coreAlpha);
            }
        }
        BufferUploader.drawWithShader(builder.end());

        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    // ====== CLOUDS ======

    private static void renderClouds(BufferBuilder builder,
                                     Matrix4f pose) {

        // how far the whole cloud field has drifted since launch, in world units
        double driftX = CLOUD_DRIFT_X * currentTimeSec;
        double driftZ = CLOUD_DRIFT_Z * currentTimeSec;

        // grid-cell index bounds covering the visible range, with a wider margin
        // so edge clouds whose tiles reach into range are still emitted
        int minCellX = (int) Math.floor((driftX - CLOUD_RANGE) / CLOUD_CELL) - 2;
        int maxCellX = (int) Math.ceil((driftX + CLOUD_RANGE) / CLOUD_CELL) + 2;
        int minCellZ = (int) Math.floor((driftZ - CLOUD_RANGE) / CLOUD_CELL) - 2;
        int maxCellZ = (int) Math.ceil((driftZ + CLOUD_RANGE) / CLOUD_CELL) + 2;

        // only cull once the WHOLE cloud is past the edge; per-vertex alpha hides the boundary
        float cullDistance = CLOUD_RANGE + 24f;

        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        for (int cellX = minCellX; cellX <= maxCellX; cellX++) {
            for (int cellZ = minCellZ; cellZ <= maxCellZ; cellZ++) {
                if (hash01(cellX, cellZ, 0) > CLOUD_FILL) {
                    continue; // empty cell
                }

                // scatter the cloud inside its cell so the underlying grid isn't visible
                float jitterX = (hash01(cellX, cellZ, 1) * 2f - 1f) * CLOUD_JITTER;
                float jitterZ = (hash01(cellX, cellZ, 2) * 2f - 1f) * CLOUD_JITTER;

                // cloud center relative to the viewer; the field scrolls past as the drift grows
                float cloudCenterX = (float) (cellX * (double) CLOUD_CELL + jitterX - driftX);
                float cloudCenterZ = (float) (cellZ * (double) CLOUD_CELL + jitterZ - driftZ);

                float distanceToCloud = (float) Math.sqrt(
                        cloudCenterX * cloudCenterX + cloudCenterZ * cloudCenterZ
                );
                if (distanceToCloud > cullDistance) {
                    continue;
                }
                emitCloud(builder, pose, cloudCenterX, cloudCenterZ, cellX, cellZ);
            }
        }
        BufferUploader.drawWithShader(builder.end());
    }

    private static void emitCloud(BufferBuilder builder, Matrix4f pose,
                                  float cloudCenterX, float cloudCenterZ,
                                  int cellX, int cellZ) {
        // deterministic shape + rotation pick for this cell, from the pre-baked variants
        int shapeIndex = (int) (hash01(cellX, cellZ, 10) * CLOUD_SHAPES.length);
        int rotation = (int) (hash01(cellX, cellZ, 11) * 4f) & 3;
        CloudVariant variant = CLOUD_VARIANTS[shapeIndex * 4 + rotation];
        int tileCount = variant.tx.length;

        float halfTile = CLOUD_TILE * 0.5f;
        float cloudCenterY = CLOUD_Y + (hash01(cellX, cellZ, 3) * 2f - 1f) * CLOUD_JITTER_Y;
        float bottomY = cloudCenterY - CLOUD_THICK;
        float topY = cloudCenterY + CLOUD_THICK;

        for (int tile = 0; tile < tileCount; tile++) {
            // tile center; the baked offsets keep the shape's centroid on the cloud center
            float tileCenterX = cloudCenterX + variant.tx[tile] * CLOUD_TILE - variant.offX;
            float tileCenterZ = cloudCenterZ + variant.tz[tile] * CLOUD_TILE - variant.offZ;

            float minX = tileCenterX - halfTile, maxX = tileCenterX + halfTile;
            float minZ = tileCenterZ - halfTile, maxZ = tileCenterZ + halfTile;

            // top face is always exposed
            cloudFace(builder, pose, SHADE_UP,
                    minX, topY, maxZ,
                    maxX, topY, maxZ,
                    maxX, topY, minZ,
                    minX, topY, minZ);

            // side faces only where no neighbor tile covers them (flags baked at class load)
            int exposedFaces = variant.faces[tile];
            if ((exposedFaces & FACE_XN) != 0) {
                // -X wall
                cloudFace(builder, pose, SHADE_WE,
                        minX, bottomY, minZ,
                        minX, bottomY, maxZ,
                        minX, topY, maxZ,
                        minX, topY, minZ);
            }
            if ((exposedFaces & FACE_XP) != 0) {
                // +X wall
                cloudFace(builder, pose, SHADE_WE,
                        maxX, bottomY, minZ,
                        maxX, topY, minZ,
                        maxX, topY, maxZ,
                        maxX, bottomY, maxZ);
            }
            if ((exposedFaces & FACE_ZN) != 0) {
                // -Z wall
                cloudFace(builder, pose, SHADE_NS,
                        minX, bottomY, minZ,
                        minX, topY, minZ,
                        maxX, topY, minZ,
                        maxX, bottomY, minZ);
            }
            if ((exposedFaces & FACE_ZP) != 0) {
                // +Z wall
                cloudFace(builder, pose, SHADE_NS,
                        minX, bottomY, maxZ,
                        maxX, bottomY, maxZ,
                        maxX, topY, maxZ,
                        minX, topY, maxZ);
            }
        }
    }

    private static void cloudFace(BufferBuilder builder, Matrix4f pose,
                                  float shade,
                                  float corner1X, float corner1Y, float corner1Z,
                                  float corner2X, float corner2Y, float corner2Z,
                                  float corner3X, float corner3Y, float corner3Z,
                                  float corner4X, float corner4Y, float corner4Z) {
        int[] shadedColor = new int[3];
        currentCloud.multiply(shade, shadedColor);
        cloudVertex(builder, pose, corner1X, corner1Y, corner1Z, shadedColor);
        cloudVertex(builder, pose, corner2X, corner2Y, corner2Z, shadedColor);
        cloudVertex(builder, pose, corner3X, corner3Y, corner3Z, shadedColor);
        cloudVertex(builder, pose, corner4X, corner4Y, corner4Z, shadedColor);
    }

    private static void cloudVertex(BufferBuilder builder, Matrix4f pose,
                                    float x, float y, float z,
                                    int[] shadedColor) {
        float dist = (float) Math.sqrt(x * x + z * z);

        // blending cloud with horizon
        float aerial = AERIAL_MAX * smoothstep(AERIAL_START, CLOUD_RANGE, dist);
        int r = (int) (shadedColor[0] + (currentHorizon.getRedInt() - shadedColor[0]) * aerial);
        int g = (int) (shadedColor[1] + (currentHorizon.getGreenInt() - shadedColor[1]) * aerial);
        int b = (int) (shadedColor[2] + (currentHorizon.getBlueInt() - shadedColor[2]) * aerial);

        // alpha: dissolve cloud when out of visible area
        float dissolve = 1f - smoothstep(ALPHA_FADE_START, CLOUD_RANGE, dist);
        int a = (int) (255f * dissolve);

        builder.vertex(pose, x, y, z).color(r, g, b, a).endVertex();
    }

    // ---- USER SKY DOTS ----

    public static boolean addUserDot(float dirX, float dirY, float dirZ) {
        if (dirY < USER_DOT_MIN_Y) {
            return false;
        }
        if (userDotCount == USER_DOT_MAX) {
            System.arraycopy(userDotX, 1, userDotX, 0, USER_DOT_MAX - 1);
            System.arraycopy(userDotY, 1, userDotY, 0, USER_DOT_MAX - 1);
            System.arraycopy(userDotZ, 1, userDotZ, 0, USER_DOT_MAX - 1);
            System.arraycopy(userDotPhase, 1, userDotPhase, 0, USER_DOT_MAX - 1);
            userDotCount--;
        }
        userDotX[userDotCount] = dirX;
        userDotY[userDotCount] = dirY;
        userDotZ[userDotCount] = dirZ;
        userDotPhase[userDotCount] = userDotsAdded * 1.7f;
        userDotsAdded++;
        userDotCount++;
        return true;
    }


    public static boolean eraseUserDotsAt(float dirX, float dirY, float dirZ) {
        int writeIndex = 0;
        for (int i = 0; i < userDotCount; i++) {
            float alignment = userDotX[i] * dirX + userDotY[i] * dirY + userDotZ[i] * dirZ;
            if (alignment >= USER_ERASE_CONE_COS) {
                continue; // erased
            }
            if (writeIndex != i) {
                userDotX[writeIndex] = userDotX[i];
                userDotY[writeIndex] = userDotY[i];
                userDotZ[writeIndex] = userDotZ[i];
                userDotPhase[writeIndex] = userDotPhase[i];
            }
            writeIndex++;
        }
        boolean removedAny = writeIndex != userDotCount;
        userDotCount = writeIndex;
        return removedAny;
    }

    private static void renderUserDots(BufferBuilder builder, Matrix4f pose) {
        if (userDotCount == 0) {
            return;
        }
        ensureGlowSprite();
        if (GLOW_SPRITE == null) {
            return;
        }

        boolean showClouds = currentDay >= VISOR_DAY_THRESHOLD;

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, GLOW_SPRITE);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        if (showClouds) {
            RenderSystem.defaultBlendFunc();
        } else {
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE); // additive
        }


        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        for (int i = 0; i < userDotCount; i++) {
            scratchDir.set(userDotX[i], userDotY[i], userDotZ[i]);
            billboardBasis(scratchDir, scratchRight, scratchUp);
            float cx = userDotX[i] * USER_DOT_RADIUS;
            float cy = userDotY[i] * USER_DOT_RADIUS;
            float cz = userDotZ[i] * USER_DOT_RADIUS;
            if (showClouds) {
                int[] color = currentCloud.asIntArray(false);

                dotQuad(builder, pose, scratchRight, scratchUp, cx, cy, cz, VISOR_CLOUD_PUFF, color, 200);
                dotQuad(builder, pose, scratchRight, scratchUp, cx, cy, cz, VISOR_CLOUD_CORE, color, 235);
            } else {
                int[] colorHalo = VISOR_STAR_HALO_C.asIntArray(false);
                int[] colorCore = VISOR_STAR_CORE_C.asIntArray(false);

                dotQuad(builder, pose, scratchRight, scratchUp, cx, cy, cz, VISOR_STAR_HALO, colorHalo, 165);
                dotQuad(builder, pose, scratchRight, scratchUp, cx, cy, cz, VISOR_STAR_CORE, colorCore, 255);
            }
        }
        BufferUploader.drawWithShader(builder.end());

        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }
    static ResourceLocation glowSprite() {
        ensureGlowSprite();
        return GLOW_SPRITE;
    }

    // ====== HELPERS ======

    // ---- COMMON
    private static float smoothstep(float edge0, float edge1, float x) {
        float t = Math.min(1f, Math.max(0f, (x - edge0) / (edge1 - edge0)));
        return t * t * (3f - 2f * t);
    }

    private static float hash01(int i, int j, int salt) {
        long h = i * 0x9E3779B97F4A7C15L + j * 0xC2B2AE3D27D4EB4FL + salt * 0x165667B19E3779F9L;
        h ^= (h >>> 30);
        h *= 0xBF58476D1CE4E5B9L;
        h ^= (h >>> 27);
        h *= 0x94D049BB133111EBL;
        h ^= (h >>> 31);
        return (h >>> 40) * (1.0f / (1 << 24));
    }


    // --- SKY BOX
    private static void zenith(BufferBuilder builder, Matrix4f pose, float x, float y, float z) {
        builder.vertex(pose, x, y, z)
                .color(currentZenith.getRedInt(), currentZenith.getGreenInt(), currentZenith.getBlueInt(), 255)
                .endVertex();
    }

    private static void horizon(BufferBuilder builder, Matrix4f pose, float x, float y, float z) {
        float inverseLen = 1f / (float) Math.sqrt(x * x + z * z);
        float sunWeight = 0.5f + 0.5f * (x * inverseLen * sunAzimuthX + z * inverseLen * sunAzimuthZ);
        float duskAmount = currentTwilight * (0.25f + 0.75f * sunWeight);
        int[] rgb = currentHorizonBase.blend(DUSK_HORIZON, duskAmount, new int[3]);
        builder.vertex(pose, x, y, z).color(rgb[0], rgb[1], rgb[2], 255).endVertex();
    }



    // --- CELESTIAL BODIES
    private static void billboardBasis(Vector3f dir,
                                       Vector3f outRight,
                                       Vector3f outUp) {
        // reference up; swap toward X near the poles so the cross product stays stable
        float refX = 0f, refY = 1f, refZ = 0f;
        if (Math.abs(dir.y) > 0.99f) {
            refX = 1f;
            refY = 0f;
        }

        outRight.set(
                refY * dir.z - refZ * dir.y,
                refZ * dir.x - refX * dir.z,
                refX * dir.y - refY * dir.x
        ).normalize();

        outUp.set(dir).cross(outRight).normalize();
    }

    // for the class-load bakes only
    private static Vector3f[] billboardBasis(Vector3f dir) {
        Vector3f right = new Vector3f(), up = new Vector3f();
        billboardBasis(dir, right, up);
        return new Vector3f[]{right, up};
    }

    private static void billboardVertex(BufferBuilder builder,
                                        Matrix4f pose,
                                        Vector3f center, Vector3f right, Vector3f up,
                                        float rightOffset, float upOffset,
                                        float u, float v) {
        float x = center.x + right.x * rightOffset + up.x * upOffset;
        float y = center.y + right.y * rightOffset + up.y * upOffset;
        float z = center.z + right.z * rightOffset + up.z * upOffset;
        builder.vertex(pose, x, y, z).uv(u, v).endVertex();
    }


    // --- STARS
    private static void setCorner(float[] out, float baseX, float baseY, float baseZ, Vector3f right, Vector3f up, float rightOffset, float upOffset) {
        out[0] = baseX + right.x * rightOffset + up.x * upOffset;
        out[1] = baseY + right.y * rightOffset + up.y * upOffset;
        out[2] = baseZ + right.z * rightOffset + up.z * upOffset;
    }

    private static void starVertex(BufferBuilder builder,
                                   Matrix4f pose,
                                   float[] p, int alpha) {
        builder.vertex(pose, p[0], p[1], p[2]).color(255, 255, 255, alpha).endVertex();
    }

    // ---- GLOWING DOTS
    private static void spriteQuad(BufferBuilder builder,
                                   Matrix4f pose,
                                   float cx, float cy, float cz,
                                   float hs,
                                   int[] color,
                                   int a) {
        dotQuad(builder, pose, VISOR_RIGHT, VISOR_UP, cx, cy, cz, hs, color, a);
    }

    private static float clamp01(float v) {
        return Math.max(0f, Math.min(1f, v));
    }

    private static void dotQuad(BufferBuilder builder, Matrix4f pose, Vector3f right, Vector3f up,
                                float cx, float cy, float cz, float hs,
                                AtumColor color, int a) {
        dotQuad(builder, pose, right, up, cx, cy, cz, hs, color.asIntArray(false), a);
    }

    // glow-sprite quad around a center, in an arbitrary billboard basis
    private static void dotQuad(BufferBuilder builder, Matrix4f pose,
                                Vector3f right, Vector3f up,
                                float cx, float cy, float cz, float hs,
                                int[] color, int a) {
        dotVertex(builder, pose, right, up, cx, cy, cz, -hs, -hs, 0f, 0f, color[0], color[1], color[2], a);
        dotVertex(builder, pose, right, up, cx, cy, cz,  hs, -hs, 1f, 0f, color[0], color[1], color[2], a);
        dotVertex(builder, pose, right, up, cx, cy, cz,  hs,  hs, 1f, 1f, color[0], color[1], color[2], a);
        dotVertex(builder, pose, right, up, cx, cy, cz, -hs,  hs, 0f, 1f, color[0], color[1], color[2], a);
    }

    private static void dotVertex(BufferBuilder builder, Matrix4f pose,
                                  Vector3f right, Vector3f up,
                                  float cx, float cy, float cz,
                                  float offsetX, float offsetY, float u, float v, int r, int g, int b, int a) {
        float x = cx + right.x * offsetX + up.x * offsetY;
        float y = cy + right.y * offsetX + up.y * offsetY;
        float z = cz + right.z * offsetX + up.z * offsetY;
        builder.vertex(pose, x, y, z).uv(u, v).color(r, g, b, a).endVertex();
    }

    private static void ensureGlowSprite() {
        if (GLOW_SPRITE != null) {
            return;
        }
        int size = 64;
        NativeImage img = new NativeImage(NativeImage.Format.RGBA, size, size, false);
        float center = (size - 1) / 2f;
        float radius = size / 2f;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float offsetX = (x - center) / radius;
                float offsetY = (y - center) / radius;
                float dist = (float) Math.sqrt(offsetX * offsetX + offsetY * offsetY);
                float alpha = smoothstep(1.0f, 0.0f, dist);
                alpha = alpha * alpha; // tighter core, softer halo
                int alphaByte = (int) (alpha * 255f);

                img.setPixelRGBA(x, y, (alphaByte << 24) | 0x00FFFFFF);
            }
        }
        DynamicTexture tex = new DynamicTexture(img);
        GLOW_SPRITE = Minecraft.getInstance().getTextureManager().register("visor_glow", tex);
    }

    // ---- VISOR SIGN
    private static DotsSign createDotsSign(String[][] glyphs) {
        Vector3f center = new Vector3f(VISOR_DIR).mul(VISOR_RADIUS);
        float centerCol = (VISOR_TOTAL_COLS - 1) / 2f;
        float centerRow = (GLYPH_H - 1) / 2f;

        List<float[]> points = new ArrayList<>();
        List<Integer> columns = new ArrayList<>();
        for (int glyphIndex = 0; glyphIndex < glyphs.length; glyphIndex++) {
            String[] glyph = glyphs[glyphIndex];
            for (int row = 0; row < GLYPH_H; row++) {
                String line = glyph[row];
                for (int glyphCol = 0; glyphCol < GLYPH_W; glyphCol++) {
                    if (line.charAt(glyphCol) != '1') {
                        continue;
                    }
                    int globalCol = glyphIndex * (GLYPH_W + GLYPH_GAP) + glyphCol;
                    float localX = (centerCol - globalCol) * VISOR_DOTS_SPACING; // un-mirrored
                    float localY = (centerRow - row) * VISOR_DOTS_SPACING;  // +up (row 0 = top)
                    points.add(new float[]{
                            center.x + VISOR_RIGHT.x * localX + VISOR_UP.x * localY,
                            center.y + VISOR_RIGHT.y * localX + VISOR_UP.y * localY,
                            center.z + VISOR_RIGHT.z * localX + VISOR_UP.z * localY
                    });
                    columns.add(globalCol);
                }
            }
        }
        int count = points.size();
        float[] px = new float[count];
        float[] py = new float[count];
        float[] pz = new float[count];
        int[] col = new int[count];
        for (int i = 0; i < count; i++) {
            float[] point = points.get(i);
            px[i] = point[0];
            py[i] = point[1];
            pz[i] = point[2];
            col[i] = columns.get(i);
        }
        return new DotsSign(px, py, pz, col);
    }

    // ---- CLOUDS
    private static float menuFadeIn() {
        return smoothstep(0f, 1f, (Util.getMillis() - startTime) / (float) FADE_IN_MS);
    }

    private static boolean hasTile(int[] tileXs, int[] tileZs, int tileCount, int queryX, int queryZ) {
        for (int i = 0; i < tileCount; i++) {
            if (tileXs[i] == queryX && tileZs[i] == queryZ) {
                return true;
            }
        }
        return false;
    }

    private static final class DotsSign {
        final float[] px, py, pz;
        final int[] col;
        final int n;

        DotsSign(float[] px, float[] py, float[] pz, int[] col) {
            this.px = px;
            this.py = py;
            this.pz = pz;
            this.col = col;
            this.n = px.length;
        }
    }

    private static final class CloudVariant {
        final int[] tx, tz;
        final byte[] faces;
        final float offX, offZ;

        CloudVariant(int[] tx, int[] tz, byte[] faces, float offX, float offZ) {
            this.tx = tx;
            this.tz = tz;
            this.faces = faces;
            this.offX = offX;
            this.offZ = offZ;
        }
    }
}