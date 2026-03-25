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
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.common.player.VRPose;
import org.vmstudio.visor.core.client.player.VRClientPlayers;
import org.vmstudio.visor.core.client.render.VRRenderState;
import org.vmstudio.visor.core.client.utils.ModelUtils;

public class VRPlayerModelSimple<T extends LivingEntity> extends PlayerModel<T> {

    protected VRClientPlayer vrPlayer;

    protected float bodyYaw;
    protected HumanoidArm mainArm = HumanoidArm.RIGHT;
    protected boolean isMainPlayer;

    protected HumanoidArm attackArm = null;


    public VRPlayerModelSimple(ModelPart root, boolean isSlim) {
        super(root, isSlim);

    }


    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        if (VRClientPlayers.isTracked(entity)) {
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
        var mainHandPose = poseRender.getMainHand();
        var offhandPose = poseRender.getOffhand();
        float bodyYaw = poseRender.getBodyYaw();

        ModelPart mainHand = vrPlayer.isLeftHanded() ? model.leftArm : model.rightArm;
        ModelPart offHand = vrPlayer.isLeftHanded() ? model.rightArm : model.leftArm;

        applyHandPose(vrPlayer, mainHand, mainHandPose, bodyYaw);
        applyHandPose(vrPlayer, offHand, offhandPose, bodyYaw);

        // copy to sleeves
        model.leftSleeve.copyFrom(model.leftArm);
        model.rightSleeve.copyFrom(model.rightArm);

        model.isMainPlayer = isMainPlayer;
        model.vrPlayer = vrPlayer;
        model.mainArm = mainArm;
        model.bodyYaw = bodyYaw;
    }

    private static void applyHandPose(VRClientPlayer vrPlayer,
                                      ModelPart arm, VRPose pose, float bodyYaw) {
        var pos = new Vector3f();
        ModelUtils.worldToModel(
                vrPlayer,
                pose.getRelativePosition(),
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

    protected void doAttackAnim(HumanoidArm side, PoseStack poseStack) {
        if (side == this.attackArm) {
            poseStack.translate(0.0F, 0.5F, 0.0F);
            poseStack.mulPose(Axis.XP.rotation(Mth.sin(this.attackTime * Mth.PI)));
            poseStack.translate(0.0F, -0.5F, 0.0F);
        }
    }
}
