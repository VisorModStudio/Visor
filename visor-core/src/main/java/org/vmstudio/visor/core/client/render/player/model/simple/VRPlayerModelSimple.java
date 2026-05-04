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


    private static void applyYawPitchToArm(VRPlayerModelSimple<?> model,
                                           HumanoidArm arm,
                                           VRPose handPose,
                                           float bodyYaw) {
        boolean left = arm == HumanoidArm.LEFT;
        ModelPart armPart = left ? model.leftArm : model.rightArm;

        armPart.x = left ? 5.0F : -5.0F;
        armPart.y = 2.0F;
        armPart.z = 0.0F;

        Quaternionf noRoll = new Quaternionf()
                .rotateY(Mth.PI - handPose.getYaw())
                .rotateX(handPose.getPitch());

        Matrix3f matrix = new Matrix3f();
        ModelUtils.toModelDir(bodyYaw, noRoll, matrix);
        ModelUtils.setRotation(armPart, matrix, new Vector3f());
    }


    @Override
    public void translateToHand(HumanoidArm side, PoseStack poseStack) {
        // can't call super, because, the vanilla slim offset doesn't work with rotations
        this.getArm(side).translateAndRotate(poseStack);

        if (this.slim) {
            poseStack.translate(side == HumanoidArm.LEFT ? -0.0625F : 0.0625F, 0.0F, 0.0F);
        }


    }

}