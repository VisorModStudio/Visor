package org.vmstudio.visor.core.client.render.player.model.simple;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
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
import org.vmstudio.visor.core.client.render.player.model.ControllerSpaceItemAnchorModel;
import org.vmstudio.visor.core.client.utils.ModelUtils;

public class VRPlayerModelSimple<T extends LivingEntity> extends PlayerModel<T> implements ControllerSpaceItemAnchorModel {

    protected VRClientPlayer vrPlayer;

    protected float bodyYaw;
    protected HumanoidArm mainArm = HumanoidArm.RIGHT;
    protected boolean isMainPlayer;

    protected HumanoidArm attackArm = null;


    public final ModelPart leftHand;
    public final ModelPart rightHand;
    public final ModelPart leftHandSleeve;
    public final ModelPart rightHandSleeve;


    public VRPlayerModelSimple(ModelPart root, boolean isSlim) {
        super(root, isSlim);
        this.leftHand = root.getChild("left_hand");
        this.rightHand = root.getChild("right_hand");
        this.leftHandSleeve = root.getChild("left_hand_sleeve");
        this.rightHandSleeve = root.getChild("right_hand_sleeve");
        ModelUtils.copyTextures(this.leftArm, this.leftHand);
        ModelUtils.copyTextures(this.rightArm, this.rightHand);
        ModelUtils.copyTextures(this.leftSleeve, this.leftHandSleeve);
        ModelUtils.copyTextures(this.rightSleeve, this.rightHandSleeve);
    }


    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        if (VRClientPlayers.isTracked(entity) && !VRRenderState.getPhase().isVRGui()) {
            animateVRModel(this, entity, limbSwing, limbSwingAmount);

        }
    }

    private void animateVRModel(
            VRPlayerModelSimple<?> model,
            LivingEntity player,
            float limbSwing, float limbSwingAmount
    ) {
        var vrPlayer = VRClientPlayers.getPlayer(player.getUUID());

        if (vrPlayer == null) {
            model.vrPlayer = null;
            return;
        }

        boolean isMainPlayer = VRRenderState.isSelfModelPlayer(player);

        HumanoidArm mainArm = vrPlayer.isLeftHanded()
                ? HumanoidArm.LEFT
                : HumanoidArm.RIGHT;

        var poseRender = vrPlayer.getPoseData(PlayerPoseType.RENDER);

        VRBody vrBody = poseRender.getBody();

        var mainHandPose = vrBody.getMainHand().getPose();
        var offhandPose = vrBody.getOffhand().getPose();
        float bodyYaw = poseRender.getBodyYaw();

        ModelPart mainHand = vrPlayer.isLeftHanded() ? model.leftArm : model.rightArm;
        ModelPart offHand = vrPlayer.isLeftHanded() ? model.rightArm : model.leftArm;

        var modelOrigin = ModelUtils.getModelOrigin(player);
        applyHandPose(vrPlayer, modelOrigin, mainHand, mainHandPose, bodyYaw);
        applyHandPose(vrPlayer, modelOrigin, offHand, offhandPose, bodyYaw);

        // copy to sleeves
        model.leftSleeve.copyFrom(model.leftArm);
        model.rightSleeve.copyFrom(model.rightArm);

        model.isMainPlayer = isMainPlayer;
        model.vrPlayer = vrPlayer;
        model.mainArm = mainArm;
        model.bodyYaw = bodyYaw;
    }

    private static void applyHandPose(VRClientPlayer vrPlayer,
                                      Vector3f modelOrigin,
                                      ModelPart arm, VRPose pose, float bodyYaw) {
        var pos = new Vector3f();
        Vector3f relativePos = pose.getPosition().sub(modelOrigin, new Vector3f());

        ModelUtils.worldToModel(
                vrPlayer,
                relativePos,
                bodyYaw,
                true,
                pos
        );
        arm.x = pos.x();
        arm.y = pos.y();
        arm.z = pos.z();

        Matrix3f tempM = new Matrix3f();
        Vector3f tempV = new Vector3f();
        Quaternionf rot = pose.getRotation().getNormalizedRotation(new Quaternionf());
        ModelUtils.toModelDir(bodyYaw, rot, tempM);
        ModelUtils.setRotation(arm, tempM, tempV);
    }



    @Override
    public void translateToHand(HumanoidArm side, PoseStack poseStack) {
        // can't call super, because, the vanilla slim offset doesn't work with rotations
        this.getArm(side).translateAndRotate(poseStack);

        if (this.slim) {
            poseStack.translate(side == HumanoidArm.LEFT ? -0.0625F : 0.0625F, 0.0F, 0.0F);
        }

        doAttackAnim(side, poseStack);
    }

    @Override
    public void applyLocalHandItemAnchor(HumanoidArm side, PoseStack poseStack) {
        if (this.slim) {
            poseStack.translate(side == HumanoidArm.LEFT ? -0.0625F : 0.0625F, 0.0F, 0.0F);
        }
    }

    protected void doAttackAnim(HumanoidArm side, PoseStack poseStack) {
        if (side == this.attackArm) {
            poseStack.translate(0.0F, 0.5F, 0.0F);
            poseStack.mulPose(Axis.XP.rotation(Mth.sin(this.attackTime * Mth.PI)));
            poseStack.translate(0.0F, -0.5F, 0.0F);
        }
    }
}
