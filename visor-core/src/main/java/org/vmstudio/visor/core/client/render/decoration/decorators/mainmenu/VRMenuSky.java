package org.vmstudio.visor.core.client.render.decoration.decorators.mainmenu;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import me.phoenixra.atumvr.api.misc.color.AtumColor;
import me.phoenixra.atumvr.api.misc.color.AtumColorImmutable;
import me.phoenixra.atumvr.api.misc.color.AtumColorMutable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11C;

import java.time.LocalTime;

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

    // ---- SKY BOX ----
    private static final float SKY_BOX = 100.0f;

    private static final float SUNRISE_TIME = 6.0f;
    private static final float SUNSET_TIME = 18.0f;

    private static final double ARC_TILT_RAD = Math.toRadians(20f); // 0 = sun passes dead overhead; ~20 arcs it through the south

    private static final long SKY_UPDATE_FREQUENCY = 200L;

    // ---- FRAME STATE ----
    private static final AtumColorMutable currentZenith = DAY_ZENITH.asMutable();
    private static final AtumColorMutable currentHorizon = DAY_HORIZON.asMutable(); // with twilight warmth
    private static final AtumColorMutable currentHorizonBase = DAY_HORIZON.asMutable();  // without twilight warmth
    private static final AtumColorMutable currentCloud = DAY_CLOUD.asMutable();

    private static final Vector3f currentSunDir = new Vector3f(0, 1, 0);
    private static final Vector3f currentMoonDir = new Vector3f(0, -1, 0);
    private static float currentDay = 1f;        // 0 = full night, 1 = full day
    private static float currentTwilight = 0f;   // 0 = no twilight, 1 = full twilight
    private static float sunAzimuthX = 1f, sunAzimuthZ = 0f; // horizontal sun direction for the dusk gradient

    private static long lastSkyUpdate = -1;
    private static final int[] blendScratch = new int[3];


    private static long startTime = Util.getMillis();

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

        // --- Restore ---
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
    }

    public static void renderLast(PoseStack poseStack) {

    }

    // ====== TIME ======

    private static float currentTime() {
        if (DEBUG_FAST_CYCLE) {
            float period = DEBUG_CYCLE_SEC * 1000f;
            return (Util.getMillis() % (long) period) / period * 24f;
        }
        LocalTime now = LocalTime.now();
        return now.getHour() + now.getMinute() / 60f + now.getSecond() / 3600f;
    }


    private static float timeToPhase(float time) {
        // 0 = sunrise | 0.25 = noon | 0.5 = sunset | 0.75 = midnight
        if (time >= SUNRISE_TIME && time < SUNSET_TIME) {
            return 0.5f * (time - SUNRISE_TIME) / (SUNSET_TIME - SUNRISE_TIME);
        }
        float nightTime = (time < SUNRISE_TIME)
                ? time + 24f
                : time;
        return 0.5f + 0.5f * (nightTime - SUNSET_TIME) / (SUNRISE_TIME + 24f - SUNSET_TIME);
    }


    // ====== SKY BOX ======

    private static void prepareSkyBox() {
        long now = Util.getMillis();
        if (!DEBUG_FAST_CYCLE && now - lastSkyUpdate < SKY_UPDATE_FREQUENCY) {
            return;
        }
        lastSkyUpdate = now;

        float phase = timeToPhase(currentTime());
        float theta = phase * (float) (Math.PI * 2.0);

        float sin = (float) Math.sin(theta), cos = (float) Math.cos(theta);
        currentSunDir.set(
                cos,
                (float) Math.cos(ARC_TILT_RAD) * sin,
                (float) Math.sin(ARC_TILT_RAD) * sin
        ).normalize();
        currentMoonDir.set(currentSunDir).mul(-1f);

        float sunDirX = currentSunDir.x;
        float sunDirY = currentSunDir.y;
        float sunDirZ = currentSunDir.z;

        float azimuthLen = (float) Math.sqrt(sunDirX * sunDirX + sunDirZ * sunDirZ);
        if (azimuthLen > 1e-4f) {
            sunAzimuthX = sunDirX / azimuthLen;
            sunAzimuthZ = sunDirZ / azimuthLen;
        }

        currentDay = smoothstep(-0.12f, 0.18f, sunDirY);
        float tw = 1f - Math.min(1f, Math.abs(sunDirY) / 0.28f);
        currentTwilight = tw * tw * (3f - 2f * tw);

        currentZenith.setRGBA(
                NIGHT_ZENITH.blend(DAY_ZENITH, currentDay, blendScratch)
        );

        currentHorizonBase.setRGBA(
                NIGHT_HORIZON.blend(DAY_HORIZON, currentDay, blendScratch)
        );

        currentHorizon.setRGBA(
                currentHorizonBase.blend(DUSK_HORIZON, currentTwilight, blendScratch)
        );

        currentCloud.setRGBA(
                NIGHT_CLOUD.blend(DAY_CLOUD, currentDay, blendScratch)
        ).setRGBA(
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


    // ====== HELPERS ======

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
}
