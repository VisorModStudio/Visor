package org.vmstudio.visor.core.client.render.decoration.decorators.mainmenu;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import me.phoenixra.atumvr.api.misc.color.AtumColor;
import me.phoenixra.atumvr.api.misc.color.AtumColorImmutable;
import me.phoenixra.atumvr.api.misc.color.AtumColorMutable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11C;

import java.time.LocalTime;
import java.util.Arrays;

/**
 * Procedural sky for the VR main menu
 */
public final class VRMenuSky {
    // ---- DEBUG ----
    private static final boolean DEBUG_FAST_CYCLE = true;
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

    // ---- VISOR SIGN ----
    private static final Vector3f VISOR_DIR = new Vector3f(0f, 0.5f, -1f).normalize();
    private static final float VISOR_RADIUS = 88.0f; // inside the star shell so it draws in front
    private static final float VISOR_PITCH = 1.6f;   // spacing between pixels (world units at VISOR_RADIUS)
    private static final float VISOR_SUPPRESS_DOT = 0.95f; // hide background stars within this cone (~18 deg)


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
        float[][][] quads = new float[STAR_COUNT][][];
        float[] phases = new float[STAR_COUNT];
        int kept = 0;
        for (int star = 0; star < STAR_COUNT; star++) {
            float phi = hash01(star, 0, 20) * (float) (2 * Math.PI);
            float cosT = hash01(star, 0, 21) * 2f - 1f;
            float sinT = (float) Math.sqrt(Math.max(0f, 1f - cosT * cosT));
            float dx = sinT * (float) Math.cos(phi);
            float dy = cosT;
            float dz = sinT * (float) Math.sin(phi);

            if (dy < -0.05f) {
                continue;
            }
            //Keep a clean area around VISOR sign
            if (dx * VISOR_DIR.x + dy * VISOR_DIR.y + dz * VISOR_DIR.z >= VISOR_SUPPRESS_DOT) {
                continue;
            }

            Vector3f[] basis = billboardBasis(new Vector3f(dx, dy, dz));
            Vector3f right = basis[0], up = basis[1];
            float bx = dx * STAR_RADIUS, by = dy * STAR_RADIUS, bz = dz * STAR_RADIUS;
            float sz = STAR_SIZE * 0.5f * (0.6f + hash01(star, 0, 22) * 1.1f);
            float[][] q = new float[4][3];
            setCorner(q[0], bx, by, bz, right, up, -sz, -sz);
            setCorner(q[1], bx, by, bz, right, up,  sz, -sz);
            setCorner(q[2], bx, by, bz, right, up,  sz,  sz);
            setCorner(q[3], bx, by, bz, right, up, -sz,  sz);
            quads[kept] = q;
            phases[kept] = star * 1.37f;
            kept++;
        }
        STAR_QUAD = Arrays.copyOf(quads, kept);
        STAR_PHASE = Arrays.copyOf(phases, kept);
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

        // --- Restore ---
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
    }

    public static void renderLast(PoseStack poseStack) {

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
        float tw = 1f - Math.min(1f, Math.abs(elevation) / 0.28f);
        currentTwilight = tw * tw * (3f - 2f * tw);

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
            float[][] q = STAR_QUAD[star];
            starVertex(builder, pose, q[0], alpha);
            starVertex(builder, pose, q[1], alpha);
            starVertex(builder, pose, q[2], alpha);
            starVertex(builder, pose, q[3], alpha);
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

    // ====== HELPERS ======

    // --- SKY BOX ---
    private static void zenith(BufferBuilder builder, Matrix4f pose, float x, float y, float z) {
        builder.vertex(pose, x, y, z)
                .color(currentZenith.getRedInt(), currentZenith.getGreenInt(), currentZenith.getBlueInt(), 255)
                .endVertex();
    }

    private static void horizon(BufferBuilder builder, Matrix4f pose, float x, float y, float z) {
        float inverseLen = 1f / (float) Math.sqrt(x * x + z * z);
        float w = 0.5f + 0.5f * (x * inverseLen * sunAzimuthX + z * inverseLen * sunAzimuthZ);
        float duskAmount = currentTwilight * (0.25f + 0.75f * w);
        int[] rgb = currentHorizonBase.blend(DUSK_HORIZON, duskAmount, new int[3]);
        builder.vertex(pose, x, y, z).color(rgb[0], rgb[1], rgb[2], 255).endVertex();
    }


    private static float smoothstep(float edge0, float edge1, float x) {
        float t = Math.min(1f, Math.max(0f, (x - edge0) / (edge1 - edge0)));
        return t * t * (3f - 2f * t);
    }


    // --- CELESTIAL BODIES ---
    private static void billboardBasis(Vector3f dir,
                                       Vector3f outRight,
                                       Vector3f outUp) {
        float hx = 0f, hy = 1f, hz = 0f;
        if (Math.abs(dir.y) > 0.99f) {
            hx = 1f;
            hy = 0f;
        }

        outRight.set(
                hy * dir.z - hz * dir.y,
                hz * dir.x - hx * dir.z,
                hx * dir.y - hy * dir.x
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
                                        Vector3f c, Vector3f right, Vector3f up,
                                        float a, float b,
                                        float u, float v) {
        float x = c.x + right.x * a + up.x * b;
        float y = c.y + right.y * a + up.y * b;
        float z = c.z + right.z * a + up.z * b;
        builder.vertex(pose, x, y, z).uv(u, v).endVertex();
    }


    // --- STARS ---
    private static void setCorner(float[] out, float bx, float by, float bz, Vector3f right, Vector3f up, float a, float b) {
        out[0] = bx + right.x * a + up.x * b;
        out[1] = by + right.y * a + up.y * b;
        out[2] = bz + right.z * a + up.z * b;
    }

    private static void starVertex(BufferBuilder builder,
                                   Matrix4f pose,
                                   float[] p, int alpha) {
        builder.vertex(pose, p[0], p[1], p[2]).color(255, 255, 255, alpha).endVertex();
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
}
