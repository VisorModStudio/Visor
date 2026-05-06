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

        if (!VRClientPlayers.isTracked(entity) || VRRenderState.getPhase().isVRGui()) {
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

        // Step 1: each arm follows the controller's aim vector in pitch + yaw,
        // with zRot=0 (no roll). The held item rides on this same frame via
        // translateToHand, so it also follows the aim vector without rolling.
        applyYawPitchToArm(model, playerId, mainArm, vrBody.getMainHand().getPose(), bodyYaw);
        applyYawPitchToArm(model, playerId, offArm,  vrBody.getOffhand().getPose(),  bodyYaw);

        // Step 2: layer the vanilla swing animation back on top so attacks read in
        // third person. The body's own twist (set by super.setupAnim) is propagated
        // to both arms; the swinging arm additionally gets the forward arc + roll.
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
        // zRot stays 0 — no roll on the arm cube and no roll on the held item
        // (the item inherits this frame via vanilla translateToHand).
        armPart.setRotation(-Mth.HALF_PI - frame.armPitch, frame.armYawDelta, 0.0F);
    }

    /**
     * Re-applies a vanilla-shaped attack swing on top of the VR rotation. Mirrors
     * {@code HumanoidModel.setupAttackAnimation} just enough to make the swing visible
     * to viewers without disturbing the rest of the VR posing.
     */
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

        // Body twist oscillation — vanilla writes this into body.yRot already, so we
        // reuse it. Both arms inherit the twist so they don't appear "detached" from
        // the rotating torso.
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
        attackPart.yRot += bodyTwist;          // attacker gets +body.yRot * 2 total
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
}