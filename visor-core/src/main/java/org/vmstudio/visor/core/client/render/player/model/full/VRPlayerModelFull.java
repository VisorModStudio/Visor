package org.vmstudio.visor.core.client.render.player.model.full;

import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.vmstudio.visor.api.client.player.VRClientPlayer;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.common.player.VRPose;
import org.vmstudio.visor.core.client.player.VRClientPlayers;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;
import org.vmstudio.visor.core.client.player.body.full.VRBodyFull;
import org.vmstudio.visor.core.client.render.VRRenderState;
import org.vmstudio.visor.core.client.render.player.model.CenteredArmsPlayerMesh;
import org.vmstudio.visor.core.client.settings.VRClientSettings;
import org.vmstudio.visor.core.client.utils.ModelUtils;

public class VRPlayerModelFull<T extends LivingEntity> extends PlayerModel<T> {

    protected VRClientPlayer vrPlayer;

    protected HumanoidArm mainArm = HumanoidArm.RIGHT;

    protected float bodyYaw;
    protected boolean isMainPlayer;

    public VRPlayerModelFull(ModelPart root, boolean isSlim) {
        super(root, isSlim);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        if (!VRClientPlayers.isTracked(entity) || VRRenderState.getPhase().isVRGui()) {
            return;
        }

        var vrPlayer = VRClientPlayers.getPlayer(entity.getUUID());
        if (vrPlayer == null) {
            this.vrPlayer = null;
            return;
        }

        // Self perspective (first-person of local player): arms are hidden by
        // VRPlayerRendererFull.setModelProperties; VR hands are rendered via
        // VRHandRenderer. Skip the VR arm posing entirely — body and legs animate
        // vanilla which is what we want.
        if (VRRenderState.isSelfModelRender(entity)) {
            this.vrPlayer = vrPlayer;
            this.bodyYaw = vrPlayer.getPoseData(PlayerPoseType.RENDER).getBodyYaw();
            this.mainArm = vrPlayer.isLeftHanded() ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
            this.isMainPlayer = VRRenderState.isSelfModelPlayer(entity);
            return;
        }

        // Third-person / other-player perspective: vanilla-looking body. Arms rotate
        // by yaw/pitch from the controller; roll is applied to the held item separately
        // (see ItemInHandLayerMixin).
        animateThirdPersonVRModel(this, vrPlayer);
    }

    private static void animateThirdPersonVRModel(VRPlayerModelFull<?> model, VRClientPlayer vrPlayer) {
        var poseRender = vrPlayer.getPoseData(PlayerPoseType.RENDER);
        VRBodyFull vrBody = (VRBodyFull) poseRender.getBody();
        float bodyYaw = poseRender.getBodyYaw();

        HumanoidArm mainArm = vrPlayer.isLeftHanded() ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
        HumanoidArm offArm = mainArm.getOpposite();

        applyYawPitchToArm(model, mainArm, vrBody.getMainHand().getPose(), bodyYaw);
        applyYawPitchToArm(model, offArm,  vrBody.getOffhand().getPose(),  bodyYaw);

        // copy to sleeves
        model.leftSleeve.copyFrom(model.leftArm);
        model.rightSleeve.copyFrom(model.rightArm);

        model.vrPlayer = vrPlayer;
        model.mainArm = mainArm;
        model.bodyYaw = bodyYaw;
        model.isMainPlayer = false;
    }

    /**
     * Rotates the arm to match the controller's yaw/pitch (without roll), keeping
     * the centered shoulder pivot. The roll component is intentionally dropped here
     * and applied to the held item by ItemInHandLayerMixin instead — this keeps the
     * arm clean for video recording.
     *
     * Yaw and pitch are clamped to anatomical ranges so the arm can't pass through
     * the chest, the back of the body, or the top of the head — important when the
     * model is being filmed for content.
     */
    private static void applyYawPitchToArm(VRPlayerModelFull<?> model,
                                           HumanoidArm arm,
                                           VRPose handPose,
                                           float bodyYaw) {
        boolean left = arm == HumanoidArm.LEFT;
        ModelPart armPart = left ? model.leftArm : model.rightArm;

        armPart.x = CenteredArmsPlayerMesh.armPivotX(model.slim, left);
        armPart.y = CenteredArmsPlayerMesh.armPivotY(model.slim);
        armPart.z = 0.0F;

        float pitch = clampArmPitch(handPose.getPitch());
        float delta = clampArmYawDelta(handPose.getYaw(), bodyYaw, left);

        // arm.xRot: -PI/2 -> arm horizontal forward; -PI -> straight up; 0 -> straight down.
        // arm.yRot: signed body-relative yaw of the controller (positive = player's right).
        armPart.setRotation(-Mth.HALF_PI - pitch, delta, 0.0F);
    }

    private static float clampArmYawDelta(float handYaw, float bodyYaw, boolean leftArm) {
        float delta = wrapToPi(handYaw - bodyYaw);
        if (leftArm) {
            delta = Mth.clamp(delta, -BEHIND_LIMIT, CROSS_BODY_LIMIT);
        } else {
            delta = Mth.clamp(delta, -CROSS_BODY_LIMIT, BEHIND_LIMIT);
        }
        return delta;  // returns the clamped delta directly, not bodyYaw + delta
    }

    /**
     * Clamps the controller's yaw to a comfortable range relative to the body so the
     * arm can't sweep through the torso or wrap fully behind the player. Operates on
     * the signed delta (handYaw - bodyYaw) normalized to (-PI, PI].
     */
    private static float clampArmYaw(float handYaw, float bodyYaw, boolean leftArm) {
        float delta = wrapToPi(handYaw - bodyYaw);
        if (leftArm) {
            delta = Mth.clamp(delta, -BEHIND_LIMIT, CROSS_BODY_LIMIT);
        } else {
            delta = Mth.clamp(delta, -CROSS_BODY_LIMIT, BEHIND_LIMIT);
        }
        return bodyYaw + delta;
    }

    /**
     * Clamps the controller's pitch so the arm can't rotate past straight up
     * (which would graze/pass through the head) or past straight down.
     */
    private static float clampArmPitch(float pitch) {
        return Mth.clamp(pitch, -PITCH_DOWN_LIMIT, PITCH_UP_LIMIT);
    }

    private static float wrapToPi(float angle) {
        angle %= Mth.TWO_PI;
        if (angle > Mth.PI)  angle -= Mth.TWO_PI;
        if (angle < -Mth.PI) angle += Mth.TWO_PI;
        return angle;
    }

    // ~30° across the chest midline before clipping into the torso.
    private static final float CROSS_BODY_LIMIT = Mth.DEG_TO_RAD * 30.0F;
    // ~170° from the body forward — i.e. stop ~10° short of straight behind.
    private static final float BEHIND_LIMIT = Mth.DEG_TO_RAD * 170.0F;
    // ~85° up from horizontal — keeps the arm just below the top of the head.
    private static final float PITCH_UP_LIMIT = Mth.DEG_TO_RAD * 85.0F;
    // ~85° down from horizontal — full natural reach toward the feet.
    private static final float PITCH_DOWN_LIMIT = Mth.DEG_TO_RAD * 85.0F;

    public void hideLeftArm() {
        this.leftArm.visible = false;
        this.leftSleeve.visible = false;
    }

    public void hideRightArm() {
        this.rightArm.visible = false;
        this.rightSleeve.visible = false;
    }
}