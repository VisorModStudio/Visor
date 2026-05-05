package org.vmstudio.visor.core.client.render.player.model.simple;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.vmstudio.visor.api.client.player.VRClientPlayer;
import org.vmstudio.visor.api.client.player.body.VRBody;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.common.player.VRPose;
import org.vmstudio.visor.core.client.player.VRClientPlayers;
import org.vmstudio.visor.core.client.render.VRRenderState;
import org.vmstudio.visor.core.client.render.player.model.CenteredArmsPlayerMesh;
import org.vmstudio.visor.core.client.utils.ModelUtils;

public class VRPlayerModelSimple<T extends LivingEntity> extends PlayerModel<T> {

    protected VRClientPlayer vrPlayer;

    protected float bodyYaw;
    protected HumanoidArm mainArm = HumanoidArm.RIGHT;
    protected boolean isMainPlayer;



    public VRPlayerModelSimple(ModelPart root, boolean isSlim) {
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

        // For HandsOnly, the local player's first-person rendering doesn't draw the body
        // model at all (NO_MODEL visibility -> isDetached=false in first-person). So this
        // path is for third-person view of the local player and for any pass of remote
        // players. We want a vanilla-looking body where only the hand orientation tracks
        // the controller (yaw/pitch). Roll is applied to the held item by ItemInHandLayerMixin.
        animateThirdPersonVRModel(this, vrPlayer);
    }

    private static void animateThirdPersonVRModel(VRPlayerModelSimple<?> model, VRClientPlayer vrPlayer) {
        var poseRender = vrPlayer.getPoseData(PlayerPoseType.RENDER);
        VRBody vrBody = poseRender.getBody();
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
        model.isMainPlayer = VRRenderState.isSelfModelPlayer(vrPlayer.getMcPlayer());
    }

    /**
     * Rotates the arm to match the controller's yaw/pitch (without roll), keeping the
     * centered shoulder pivot. The roll component is intentionally dropped here and
     * applied to the held item by ItemInHandLayerMixin instead — this keeps the arm
     * visually clean (no twisting) for third-person/video recording.
     *
     * Yaw and pitch are clamped to anatomical ranges so the arm can't pass through the
     * chest, the back of the body, or the top of the head.
     */
    private static void applyYawPitchToArm(VRPlayerModelSimple<?> model,
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

    private static float clampArmYaw(float handYaw, float bodyYaw, boolean leftArm) {
        float delta = wrapToPi(handYaw - bodyYaw);
        if (leftArm) {
            delta = Mth.clamp(delta, -BEHIND_LIMIT, CROSS_BODY_LIMIT);
        } else {
            delta = Mth.clamp(delta, -CROSS_BODY_LIMIT, BEHIND_LIMIT);
        }
        return bodyYaw + delta;
    }

    private static float clampArmPitch(float pitch) {
        return Mth.clamp(pitch, -PITCH_DOWN_LIMIT, PITCH_UP_LIMIT);
    }

    private static float wrapToPi(float angle) {
        angle %= Mth.TWO_PI;
        if (angle > Mth.PI)  angle -= Mth.TWO_PI;
        if (angle < -Mth.PI) angle += Mth.TWO_PI;
        return angle;
    }

    private static final float CROSS_BODY_LIMIT = Mth.DEG_TO_RAD * 30.0F;
    private static final float BEHIND_LIMIT = Mth.DEG_TO_RAD * 170.0F;
    private static final float PITCH_UP_LIMIT = Mth.DEG_TO_RAD * 85.0F;
    private static final float PITCH_DOWN_LIMIT = Mth.DEG_TO_RAD * 85.0F;


    @Override
    public void translateToHand(HumanoidArm side, PoseStack poseStack) {
        // can't call super, because, the vanilla slim offset doesn't work with rotations
        this.getArm(side).translateAndRotate(poseStack);

        if (this.slim) {
            poseStack.translate(side == HumanoidArm.LEFT ? -0.0625F : 0.0625F, 0.0F, 0.0F);
        }


    }

}