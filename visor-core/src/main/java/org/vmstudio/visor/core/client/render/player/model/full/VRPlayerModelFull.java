package org.vmstudio.visor.core.client.render.player.model.full;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import org.vmstudio.visor.api.client.player.VRClientPlayer;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.common.player.VRPose;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.player.VRClientPlayers;
import org.vmstudio.visor.core.client.player.body.full.VRBodyFull;
import org.vmstudio.visor.core.client.render.VRRenderState;
import org.vmstudio.visor.core.client.render.player.model.ArmPoseClamp;
import org.vmstudio.visor.core.client.render.player.model.CenteredArmsPlayerMesh;

import java.util.UUID;

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

        if (VRRenderState.getPhase().isVRGui()) {
            if (entity.isFallFlying() || entity.isVisuallySwimming()) {
                this.head.xRot = headPitch * Mth.DEG_TO_RAD;
                this.hat.copyFrom(this.head);
            }
            return;
        }
        if (!VRClientPlayers.isTracked(entity)) {
            return;
        }

        var vrPlayer = VRClientPlayers.getPlayer(entity.getUUID());
        if (vrPlayer == null) {
            this.vrPlayer = null;
            return;
        }

        if (VRRenderState.isSelfModelRender(entity)) {
            this.vrPlayer = vrPlayer;
            this.bodyYaw = vrPlayer.getPoseData(PlayerPoseType.RENDER).getBodyYaw();
            this.mainArm = vrPlayer.isLeftHanded() ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
            this.isMainPlayer = VRRenderState.isSelfModelPlayer(entity);
            return;
        }

        animateThirdPersonVRModel(this, entity, vrPlayer);
    }

    private static void animateThirdPersonVRModel(VRPlayerModelFull<?> model,
                                                  LivingEntity entity,
                                                  VRClientPlayer vrPlayer) {
        var poseRender = vrPlayer.getPoseData(PlayerPoseType.RENDER);
        VRBodyFull vrBody = (VRBodyFull) poseRender.getBody();
        float bodyYaw = poseRender.getBodyYaw();

        HumanoidArm mainArm = vrPlayer.isLeftHanded() ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
        HumanoidArm offArm = mainArm.getOpposite();
        UUID playerId = vrPlayer.getMcPlayer().getUUID();

        applyYawPitchToArm(model, playerId, mainArm, vrBody.getMainHand().getPose(), bodyYaw);
        applyYawPitchToArm(model, playerId, offArm,  vrBody.getOffhand().getPose(),  bodyYaw);
        applyHmdHead(model, poseRender.getHmd(), bodyYaw);
        if (entity instanceof AbstractClientPlayer player) {
            float partialTicks = ClientContext.visor != null
                    ? ClientContext.visor.getPartialTicks()
                    : 1.0F;
            applyVanillaSwingPose(model, player, partialTicks);
        }

        model.leftSleeve.copyFrom(model.leftArm);
        model.rightSleeve.copyFrom(model.rightArm);

        model.vrPlayer = vrPlayer;
        model.mainArm = mainArm;
        model.bodyYaw = bodyYaw;
        model.isMainPlayer = false;
    }

    private static void applyYawPitchToArm(VRPlayerModelFull<?> model,
                                           UUID playerId,
                                           HumanoidArm arm,
                                           VRPose handPose,
                                           float bodyYaw) {
        boolean left = arm == HumanoidArm.LEFT;
        ModelPart armPart = left ? model.leftArm : model.rightArm;

        armPart.x = CenteredArmsPlayerMesh.armPivotX(model.slim, left);
        armPart.y = CenteredArmsPlayerMesh.armPivotY(model.slim);
        armPart.z = 0.0F;

        ArmPoseClamp.ArmFrame frame = ArmPoseClamp.solveArmFrame(playerId, handPose, bodyYaw, left);
        armPart.setRotation(-Mth.HALF_PI - frame.armPitch, frame.armYawDelta, 0.0F);
    }

    private static void applyVanillaSwingPose(VRPlayerModelFull<?> model,
                                              AbstractClientPlayer player,
                                              float partialTicks) {
        InteractionHand swinging = player.swingingArm;
        if (swinging == null) {
            return;
        }
        float attackTime = player.getAttackAnim(partialTicks);
        if (attackTime <= 0.0F) {
            return;
        }

        HumanoidArm attackArm = (swinging == InteractionHand.MAIN_HAND)
                ? player.getMainArm()
                : player.getMainArm().getOpposite();

        float bodyTwist = model.body.yRot;
        model.leftArm.yRot  += bodyTwist;
        model.rightArm.yRot += bodyTwist;

        // Swing arc on the attacking arm only.
        ModelPart attackPart = (attackArm == HumanoidArm.LEFT) ? model.leftArm : model.rightArm;

        float f = 1.0F - attackTime;
        f *= f;
        f *= f;
        f = 1.0F - f;
        float forward = Mth.sin(f * Mth.PI);
        float roll    = Mth.sin(attackTime * Mth.PI);

        attackPart.xRot -= forward * 1.2F;
        attackPart.yRot += bodyTwist;
        attackPart.zRot -= roll * 0.4F;
    }

    public void hideLeftArm() {
        this.leftArm.visible = false;
        this.leftSleeve.visible = false;
    }

    public void hideRightArm() {
        this.rightArm.visible = false;
        this.rightSleeve.visible = false;
    }
    private static void applyHmdHead(VRPlayerModelFull<?> model,   // <-- Simple uses VRPlayerModelSimple<?>
                                     VRPose hmd,
                                     float bodyYaw) {
        model.head.xRot = -hmd.getPitch();
        model.head.yRot = hmd.getYaw() - bodyYaw;
        model.head.zRot = 0.0F;
        model.hat.copyFrom(model.head);
    }
}