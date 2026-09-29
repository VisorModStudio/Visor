package org.vmstudio.visor.core.client.render.player.model.full;

import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import org.vmstudio.visor.api.client.player.VRClientPlayer;
import org.vmstudio.visor.api.client.player.body.VRBodyType;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.common.player.VRPose;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.player.VRClientPlayers;
import org.vmstudio.visor.core.client.player.body.full.VRBodyFull;
import org.vmstudio.visor.core.client.render.VRRenderState;
import org.vmstudio.visor.core.client.render.player.model.ArmPoseClamp;
import org.vmstudio.visor.core.client.render.player.model.CenteredArmsPlayerMesh;
//? if >=1.21.2 {
//? if >=1.21.9 {
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
//?} else {
/*import net.minecraft.client.renderer.entity.state.PlayerRenderState;
*///?}
import org.vmstudio.visor.core.client.render.player.VRPlayerRenderState;
//?}

import java.util.UUID;

//? if >=1.21.2 {
public class VRPlayerModelFull extends PlayerModel {
//?} else {
/*public class VRPlayerModelFull extends PlayerModel<AbstractClientPlayer> {
*///?}

    protected VRClientPlayer vrPlayer;
    protected HumanoidArm mainArm = HumanoidArm.RIGHT;
    protected float bodyYaw;
    protected boolean isMainPlayer;

    public VRPlayerModelFull(ModelPart root, boolean isSlim) {
        super(root, isSlim);
    }

    //? if >=1.21.2 {
    @Override
    //? if >=1.21.9 {
    public void setupAnim(AvatarRenderState state) {
    //?} else {
    /*public void setupAnim(PlayerRenderState state) {
    *///?}
        super.setupAnim(state);
        AbstractClientPlayer player = VRPlayerRenderState.playerOf(state);
        if (player == null) {
            return;
        }
        // setModelProperties is gone, part visibility is reset by setupAnim now
        applyVisibility(player);
        animate(player, state.isFallFlying, state.isVisuallySwimming, state.xRot);
    }
    //?} else {
    /*@Override
    public void setupAnim(AbstractClientPlayer entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        animate(entity, entity.isFallFlying(), entity.isVisuallySwimming(), headPitch);
    }
    *///?}

    private void animate(AbstractClientPlayer entity, boolean fallFlying, boolean visuallySwimming, float headPitch) {
        if (VRRenderState.getPhase().isVRGui()) {
            if (fallFlying || visuallySwimming) {
                this.head.xRot = headPitch * Mth.DEG_TO_RAD;
                //? if <1.21.2 {
                /*this.hat.copyFrom(this.head);
                *///?}
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

    public void applyVisibility(AbstractClientPlayer player) {
        if (VRRenderState.isSelfModelRender(player)) {
            this.head.visible = false;
            this.hat.visible = false;

            VRBodyType.ModelSelfVisibility visibility =
                    ClientContext.localPlayer.getBodyType().getSelfModelVisibility();
            if (visibility == VRBodyType.ModelSelfVisibility.WITHOUT_HANDS) {
                hideLeftArm();
                hideRightArm();
            }
        } else if (VRRenderState.isSpectatedVRView(player)) {
            this.head.visible = false;
            this.hat.visible = false;
            hideLeftArm();
            hideRightArm();
        }
    }

    private static void animateThirdPersonVRModel(VRPlayerModelFull model,
                                                  AbstractClientPlayer player,
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
        float partialTicks = ClientContext.visor != null
                ? ClientContext.visor.getPartialTicks()
                : 1.0F;
        applyVanillaSwingPose(model, player, partialTicks);

        //? if <1.21.2 {
        /*model.leftSleeve.copyFrom(model.leftArm);
        model.rightSleeve.copyFrom(model.rightArm);
        *///?}

        model.vrPlayer = vrPlayer;
        model.mainArm = mainArm;
        model.bodyYaw = bodyYaw;
        model.isMainPlayer = false;
    }

    private static void applyYawPitchToArm(VRPlayerModelFull model,
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

    private static void applyVanillaSwingPose(VRPlayerModelFull model,
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
    private static void applyHmdHead(VRPlayerModelFull model,
                                     VRPose hmd,
                                     float bodyYaw) {
        model.head.xRot = -hmd.getPitch();
        model.head.yRot = hmd.getYaw() - bodyYaw;
        model.head.zRot = 0.0F;
        //? if <1.21.2 {
        /*model.hat.copyFrom(model.head);
        *///?}
    }
}
