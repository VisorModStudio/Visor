package org.vmstudio.visor.core.client.render.decoration.effects;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.api.client.render.VRRenderPass;
import org.vmstudio.visor.api.client.render.decoration.VRDecorator;
import org.vmstudio.visor.api.client.render.decoration.annotations.RegisterVRGameEffect;
import org.vmstudio.visor.api.client.render.decoration.effects.VRGameEffect;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.core.client.render.decoration.decorators.winscreen.VREndFloor;
import org.vmstudio.visor.core.client.render.decoration.decorators.winscreen.VREndVoid;
import org.vmstudio.visor.core.client.render.helpers.RenderEffectsHelper;
import org.vmstudio.visor.core.client.render.helpers.RenderPoseHelper;
import org.vmstudio.visor.core.client.render.helpers.RenderStateHelper;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;


@RegisterVRGameEffect
public class GameEffectEndCredits extends VRGameEffect {
    public static final String ID = "end_credits";

    private static final float TICKS_PER_SECOND = 20.0f;

    private static final float FADE_SECONDS = 1.2f;
    private static final float SCENE_START = 0.5f;

    private static final float DRIFT_DEG_PER_SECOND = 1.0f;

    private static final float PORTAL_TICKS_PER_SECOND = 60.0f;

    private float fade;
    private float driftDegree;
    private float portalTicks;

    public GameEffectEndCredits(@NotNull VisorAddon owner) {
        super(owner);
    }

    public void reset() {
        fade = 0.0f;
        driftDegree = 0.0f;
        portalTicks = 0.0f;
    }

    @Override
    public void render(@NotNull VRRenderPass renderPass,
                       @NotNull PoseStack poseStack,
                       float partialTicks) {
        if (renderPass == VRRenderPass.worldUpdater()) {
            advance();
        }

        if (fade >= SCENE_START) {
            poseStack.pushPose();
            poseStack.setIdentity();
            RenderPoseHelper.applyCameraOrientation(renderPass, poseStack);

            VREndVoid.render(poseStack, driftDegree * Mth.DEG_TO_RAD, portalTicks);
            VREndFloor.render(poseStack);

            poseStack.popPose();
        }

        RenderEffectsHelper.renderInBlockEffect(veilAlpha());
        RenderStateHelper.restoreAfterExternalRender();
    }

    private float veilAlpha() {
        return fade < SCENE_START
                ? fade / SCENE_START
                : (1.0f - fade) / (1.0f - SCENE_START);
    }

    private void advance() {
        float seconds = MC.getDeltaFrameTime() / TICKS_PER_SECOND;
        fade = Math.min(1.0f, fade + seconds / FADE_SECONDS);
        driftDegree += seconds * DRIFT_DEG_PER_SECOND;
        portalTicks += seconds * PORTAL_TICKS_PER_SECOND;
    }

    @Override
    public boolean isVisible(@NotNull VRDecorator currentDecorator) {
        return true;
    }

    @Override
    public @NotNull String getId() {
        return ID;
    }
}
