package org.vmstudio.visor.core.client.render.player.model.simple;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import org.vmstudio.visor.api.client.player.VRClientPlayer;
import org.vmstudio.visor.api.client.player.body.VRBody;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.common.player.VRPose;
import org.vmstudio.visor.core.client.render.VRRenderState;
import org.vmstudio.visor.core.client.render.player.model.ArmPoseClamp;
import org.vmstudio.visor.core.client.render.player.model.CenteredArmsPlayerMesh;
import org.vmstudio.visor.extensions.client.entity.EntityRenderStateExtension;

import java.util.UUID;

// 1.21.2: PlayerModel is no longer generic - it is fixed to PlayerRenderState
public class VRPlayerModelSimple extends PlayerModel {

    protected VRClientPlayer vrPlayer;
    protected float bodyYaw;
    protected HumanoidArm mainArm = HumanoidArm.RIGHT;
    protected boolean isMainPlayer;

    public VRPlayerModelSimple(ModelPart root, boolean isSlim) {
        super(root, isSlim);
    }

    // 1.21.2: the model is handed a render-state snapshot instead of the entity
    @Override
    public void setupAnim(PlayerRenderState renderState) {
        // no crouch hip movement when roomscale crawling.
        // PlayerModel.crouching is gone; the flag lives on the render state now.
        renderState.isCrouching &= !renderState.isVisuallySwimming;

        super.setupAnim(renderState);

        if (VRRenderState.getPhase().isVRGui()) {
            if (renderState.isFallFlying || renderState.isVisuallySwimming) {
                this.head.xRot = renderState.xRot * Mth.DEG_TO_RAD;
                this.hat.copyFrom(this.head);
            }
            return;
        }

        // entity-derived VR data is resolved during extractRenderState and parked on the state
        EntityRenderStateExtension ext = (EntityRenderStateExtension) renderState;
        var vrPlayer = ext.visor$getVRPlayer();

        // 1.21.2 removed PlayerRenderer#setModelProperties, so the part visibility it used
        // to apply belongs here - super.setupAnim resets these every frame.
        if (vrPlayer != null && VRRenderState.isSpectatedVRView(vrPlayer.getMcPlayer())) {
            this.head.visible = false;
            this.hat.visible = false;
            this.body.visible = false;
            this.jacket.visible = false;
            this.leftArm.visible = false;
            this.rightArm.visible = false;
            this.leftSleeve.visible = false;
            this.rightSleeve.visible = false;
            this.leftLeg.visible = false;
            this.rightLeg.visible = false;
            this.leftPants.visible = false;
            this.rightPants.visible = false;
        }

        if (vrPlayer == null) {
            this.vrPlayer = null;
            return;
        }

        animateThirdPersonVRModel(this, renderState, vrPlayer);
    }

    private static void animateThirdPersonVRModel(VRPlayerModelSimple model,
                                                  PlayerRenderState renderState,
                                                  VRClientPlayer vrPlayer) {
        var poseRender = vrPlayer.getPoseData(PlayerPoseType.RENDER);
        VRBody vrBody = poseRender.getBody();
        float bodyYaw = poseRender.getBodyYaw();

        HumanoidArm mainArm = vrPlayer.isLeftHanded() ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
        HumanoidArm offArm = mainArm.getOpposite();
        UUID playerId = vrPlayer.getMcPlayer().getUUID();

        applyYawPitchToArm(model, playerId, mainArm, vrBody.getMainHand().getPose(), bodyYaw);
        applyYawPitchToArm(model, playerId, offArm,  vrBody.getOffhand().getPose(),  bodyYaw);
        applyHmdHead(model, poseRender.getHmd(), bodyYaw);
        applyVanillaSwingPose(model, renderState);

        model.leftSleeve.copyFrom(model.leftArm);
        model.rightSleeve.copyFrom(model.rightArm);

        model.vrPlayer = vrPlayer;
        model.mainArm = mainArm;
        model.bodyYaw = bodyYaw;
        model.isMainPlayer = false;
    }

    private static void applyYawPitchToArm(VRPlayerModelSimple model,
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

    // 1.21.2: swingingArm/getAttackAnim/getMainArm are pre-resolved into the render state
    private static void applyVanillaSwingPose(VRPlayerModelSimple model,
                                              PlayerRenderState renderState) {
        float attackTime = renderState.attackTime;
        if (attackTime <= 0.0F) {
            return;
        }
        HumanoidArm attackArm = renderState.attackArm;
        if (attackArm == null) {
            return;
        }

        float bodyTwist = model.body.yRot;
        model.leftArm.yRot  += bodyTwist;
        model.rightArm.yRot += bodyTwist;

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

    @Override
    public void translateToHand(HumanoidArm side, PoseStack poseStack) {
        this.getArm(side).translateAndRotate(poseStack);
        if (this.slim) {
            poseStack.translate(side == HumanoidArm.LEFT ? -0.0625F : 0.0625F, 0.0F, 0.0F);
        }
    }

    private static void applyHmdHead(VRPlayerModelSimple model,
                                     VRPose hmd,
                                     float bodyYaw) {
        model.head.xRot = -hmd.getPitch();
        model.head.yRot = hmd.getYaw() - bodyYaw;
        model.head.zRot = 0.0F;
        model.hat.copyFrom(model.head);
    }
}
