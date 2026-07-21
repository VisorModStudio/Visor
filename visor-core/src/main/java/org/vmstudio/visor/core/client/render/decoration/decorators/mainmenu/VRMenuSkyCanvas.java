package org.vmstudio.visor.core.client.render.decoration.decorators.mainmenu;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import me.phoenixra.atumvr.api.misc.color.AtumColor;
import me.phoenixra.atumvr.api.misc.color.AtumColorImmutable;
import net.minecraft.Util;
import net.minecraft.client.renderer.GameRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.lwjgl.opengl.GL11C;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.ClientFeature;
import org.vmstudio.visor.api.client.events.input.ActionButtonVREvent;
import org.vmstudio.visor.api.client.events.render.RenderFrameStartedVREvent;
import org.vmstudio.visor.api.client.events.render.RenderPipelineStageVREvent;
import org.vmstudio.visor.api.client.gui.helpers.TexturesHelper;
import org.vmstudio.visor.api.client.input.action.framework.VRActionButton;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.client.player.pose.VRPlayerPoseClient;
import org.vmstudio.visor.api.client.render.RenderPipelineStage;
import org.vmstudio.visor.api.client.render.VRRenderPass;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.common.eventbus.listener.VREventHandler;
import org.vmstudio.visor.api.common.eventbus.listener.VREventListener;
import org.vmstudio.visor.api.common.player.VRPose;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.input.actions.ActionLeftMouse;
import org.vmstudio.visor.core.client.render.helpers.RenderHelper;
import org.vmstudio.visor.core.client.render.helpers.RenderPoseHelper;
import org.vmstudio.visor.core.client.settings.VRClientSettings;
import org.vmstudio.visor.core.client.settings.options.enums.MainMenuSceneMode;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

//@TODO IT IS PROTOTYPE! REWORK FROM SCRATCH AFTER 0.7.0
public final class VRMenuSkyCanvas implements VREventListener {

    private static VRMenuSkyCanvas INSTANCE;

    private static final float CURSOR_DASH_HALF_SIZE = 0.009f;
    private static final int CURSOR_DASH_COUNT = 8;
    private static final float CURSOR_DASH_START = 0.15f;
    private static final float CURSOR_DASH_DUTY = 0.4f;

    private static final AtumColorImmutable DRAW_COLOR = AtumColor.immutable(228, 228, 228);
    private static final AtumColorImmutable ERASE_COLOR = AtumColor.immutable(255, 118, 118);

    private static final int ERASE_RING_DOTS = 12;
    private static final float ERASE_RING_SPIN = 0.5f;
    private static final float ERASE_RING_SIN =
            (float) Math.sqrt(1.0 - VRMenuSky.USER_ERASE_CONE_COS * (double) VRMenuSky.USER_ERASE_CONE_COS);

    private static final float DRAW_DOT_SPACING = 1.5f;
    private static final float DRAW_STEP_COS =
            (float) Math.cos(DRAW_DOT_SPACING / VRMenuSky.USER_DOT_RADIUS);

    private static final long STROKE_HAPTIC_INTERVAL_MS = 45L;

    private final boolean[] hasStrokeDot = new boolean[HandType.values().length];
    private final float[] lastDotX = new float[HandType.values().length];
    private final float[] lastDotY = new float[HandType.values().length];
    private final float[] lastDotZ = new float[HandType.values().length];
    private final long[] lastHapticMs = new long[HandType.values().length];

    private VRMenuSkyCanvas() {
    }

    public static void init() {
        if (INSTANCE != null) {
            return;
        }
        INSTANCE = new VRMenuSkyCanvas();
        VisorAPI.eventBus().registerListener(
                ClientContext.coreAddon,
                INSTANCE
        );
    }


    private static boolean isSkyCanvasActive() {
        return VisorAPI.clientState().sceneType().isMainMenu()
                && VRClientSettings.getMainMenuScene() == MainMenuSceneMode.SKY
                && ClientContext.visor.isFeatureEnabled(ClientFeature.INPUT_MOUSE);
    }

    // ---- EVENTS ----

    @VREventHandler
    public void onActionMouse(ActionButtonVREvent event) {
        if (!(event.getActionButton() instanceof ActionLeftMouse leftMouse)) {
            return;
        }
        if (!isSkyCanvasActive()) {
            return;
        }
        HandType hand = leftMouse.getHandType();
        if (ClientContext.cursorHandler.isHandFocused(hand)) {
            return;
        }

        event.setCanceled(true);
        if (event.isPressEvent()) {
            updateStroke(hand);
        }
    }

    @VREventHandler
    public void onFrameStarted(RenderFrameStartedVREvent event) {
        if (!isSkyCanvasActive()) {
            hasStrokeDot[HandType.MAIN.ordinal()] = false;
            hasStrokeDot[HandType.OFFHAND.ordinal()] = false;
            return;
        }
        for (HandType hand : HandType.values()) {
            updateStroke(hand);
        }
    }

    @VREventHandler
    public void onAfterWorldRender(RenderPipelineStageVREvent event) {
        if (event.getStage() != RenderPipelineStage.AFTER_WORLD) {
            return;
        }
        if (event.getRenderPhase().isVanilla()) {
            return;
        }
        if (!isSkyCanvasActive()) {
            return;
        }
        renderAimPreviews(event.getRenderPass(), event.getPoseStack());
    }

    // ---- HOLD-TO-PAINT ----

    private void updateStroke(@NotNull HandType hand) {
        int i = hand.ordinal();

        VRActionButton trigger = ClientContext.inputManager.getActionLeftMouse(hand);
        boolean held = trigger != null && trigger.isPressed();

        if (!held
                || ClientContext.cursorHandler.isHandFocused(hand)
                || !ClientContext.rawPoseHandler.getControllerData(hand).isTracking()) {
            hasStrokeDot[i] = false;
            return;
        }

        Aim aim = computeAim(hand);
        if (aim == null || aim.skyDir.y < VRMenuSky.USER_DOT_MIN_Y) {
            hasStrokeDot[i] = false;
            return;
        }

        if (hand == HandType.MAIN) {
            paintDot(i, aim.skyDir);
        } else {
            eraseDots(i, aim.skyDir);
        }
    }

    private void paintDot(int i, @NotNull Vector3f dir) {
        if (hasStrokeDot[i]) {
            float alignment = dir.x * lastDotX[i] + dir.y * lastDotY[i] + dir.z * lastDotZ[i];
            if (alignment >= DRAW_STEP_COS) {
                return;
            }
        }
        if (!VRMenuSky.addUserDot(dir.x, dir.y, dir.z)) {
            return;
        }
        lastDotX[i] = dir.x;
        lastDotY[i] = dir.y;
        lastDotZ[i] = dir.z;
        hasStrokeDot[i] = true;
        tickHaptic(i, HandType.MAIN);
    }

    private void eraseDots(int i, @NotNull Vector3f dir) {
        if (VRMenuSky.eraseUserDotsAt(dir.x, dir.y, dir.z)) {
            tickHaptic(i, HandType.OFFHAND);
        }
    }

    private void tickHaptic(int i, @NotNull HandType hand) {
        long now = Util.getMillis();
        if (now - lastHapticMs[i] < STROKE_HAPTIC_INTERVAL_MS) {
            return;
        }
        lastHapticMs[i] = now;
        ClientContext.inputManager.triggerHapticPulseClick(hand);
    }


    // ---- AIM RENDERING ----

    public static void renderAimPreviews(@NotNull VRRenderPass renderPass, @NotNull PoseStack poseStack) {
        for (HandType hand : HandType.values()) {
            if (ClientContext.cursorHandler.isHandFocused(hand)) {
                continue;
            }
            if (!ClientContext.rawPoseHandler.getControllerData(hand).isTracking()) {
                continue;
            }
            renderAimPreview(hand, renderPass, poseStack);
        }
    }

    private static void renderAimPreview(@NotNull HandType hand,
                                         @NotNull VRRenderPass renderPass,
                                         @NotNull PoseStack poseStack) {
        Aim aim = computeAim(hand);
        if (aim == null) {
            return;
        }
        if (aim.skyDir.y < VRMenuSky.USER_DOT_MIN_Y) {
            return;
        }
        boolean erase = hand != HandType.MAIN;
        AtumColor color = erase ? ERASE_COLOR : DRAW_COLOR;

        poseStack.pushPose();
        poseStack.setIdentity();
        RenderPoseHelper.applyCameraOrientation(renderPass, poseStack);
        RenderPoseHelper.applyHandPose(hand, poseStack);
        Matrix4f poseMatrix = poseStack.last().pose();

        // --- GL setup ---
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GL11C.GL_ALWAYS);
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        if (MC.getOverlay() == null) {
            var whiteTex = TexturesHelper.getWhiteTexture();
            MC.getTextureManager().bindForSetup(whiteTex);
            RenderSystem.setShaderTexture(0, whiteTex);
        }

        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        float dashSpan = aim.distance - CURSOR_DASH_START;
        for (int i = 0; i < CURSOR_DASH_COUNT; i++) {
            float nearFrac = i / (float) CURSOR_DASH_COUNT;
            float farFrac = (i + CURSOR_DASH_DUTY) / (float) CURSOR_DASH_COUNT;
            float nearDist = CURSOR_DASH_START + dashSpan * nearFrac * nearFrac;
            float farDist = CURSOR_DASH_START + dashSpan * farFrac * farFrac;
            RenderHelper.renderCuboid(
                    builder, poseMatrix,
                    new Vector3f(0, 0, -nearDist),
                    new Vector3f(0, 0, -farDist),
                    -CURSOR_DASH_HALF_SIZE, CURSOR_DASH_HALF_SIZE,
                    -CURSOR_DASH_HALF_SIZE, CURSOR_DASH_HALF_SIZE,
                    color
            );
        }

        // eraser ring marker
        var glowSprite = VRMenuSky.glowSprite();
        if (erase && glowSprite != null) {
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            RenderSystem.setShaderTexture(0, glowSprite);

            int[] colorInt = color.asIntArray(false);

            float hitDistance = aim.distance;
            float seconds = (float) ((Util.getMillis() % 100_000L) / 1000.0);
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            float ringRadius = hitDistance * ERASE_RING_SIN;
            float spinAngle = seconds * ERASE_RING_SPIN;
            for (int i = 0; i < ERASE_RING_DOTS; i++) {
                float dotAngle = spinAngle + i * (float) (Math.PI * 2.0 / ERASE_RING_DOTS);
                float dotX = ringRadius * (float) Math.cos(dotAngle);
                float dotY = ringRadius * (float) Math.sin(dotAngle);
                markerQuad(builder, poseMatrix, dotX, dotY, -hitDistance, 0.5f, colorInt, 150);
            }
            markerQuad(builder, poseMatrix, 0, 0, -hitDistance, 0.35f, colorInt, 120);

            BufferUploader.drawWithShader(builder.end());
        }

        // --- restore GL ---
        RenderSystem.enableCull();
        RenderSystem.depthFunc(GL11C.GL_LEQUAL);
        RenderSystem.depthMask(true);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        poseStack.popPose();
    }

    private static void markerQuad(BufferBuilder builder, Matrix4f poseMatrix, float cx, float cy, float z,
                                   float halfSize, int[] color, int a) {
        builder.vertex(poseMatrix, cx - halfSize, cy - halfSize, z).uv(0f, 0f).color(color[0], color[1], color[2], a).endVertex();
        builder.vertex(poseMatrix, cx + halfSize, cy - halfSize, z).uv(1f, 0f).color(color[0], color[1], color[2], a).endVertex();
        builder.vertex(poseMatrix, cx + halfSize, cy + halfSize, z).uv(1f, 1f).color(color[0], color[1], color[2], a).endVertex();
        builder.vertex(poseMatrix, cx - halfSize, cy + halfSize, z).uv(0f, 1f).color(color[0], color[1], color[2], a).endVertex();
    }

    // ---- AIM MATH ----

    private record Aim(Vector3f skyDir, float distance) {
    }

    @Nullable
    private static Aim computeAim(@NotNull HandType hand) {
        VRPlayerPoseClient renderPose = ClientContext.localPlayer.getPoseData(PlayerPoseType.RENDER);
        VRPose handPose = renderPose.getHand(hand);

        Vector3f aimDir = new Vector3f(handPose.getDirection());
        if (aimDir.lengthSquared() < 1.0e-6f) {
            return null;
        }
        aimDir.normalize();

        Vector3fc origin = renderPose.getOrigin();
        float relX = handPose.getPosition().x() - origin.x();
        float relY = handPose.getPosition().y() - origin.y();
        float relZ = handPose.getPosition().z() - origin.z();
        float radius = VRMenuSky.USER_DOT_RADIUS;
        float proj = relX * aimDir.x + relY * aimDir.y + relZ * aimDir.z;
        float centerDistSq = relX * relX + relY * relY + relZ * relZ;
        float disc = proj * proj - (centerDistSq - radius * radius);
        if (disc <= 0f) {
            return null;
        }
        float hitDistance = -proj + (float) Math.sqrt(disc);
        Vector3f hitDir = new Vector3f(
                relX + aimDir.x * hitDistance,
                relY + aimDir.y * hitDistance,
                relZ + aimDir.z * hitDistance
        ).normalize();

        Quaternionf sceneRot = Axis.YN.rotation(-renderPose.getRotationY());
        return new Aim(sceneRot.transformInverse(hitDir), hitDistance);
    }
}
