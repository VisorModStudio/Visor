package org.vmstudio.visor.core.client.render.player.modeltest;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.vmstudio.visor.api.client.player.VRClientPlayer;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.common.player.VRPose;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.player.VRClientPlayers;
import org.vmstudio.visor.core.client.render.VRRenderState;
import org.vmstudio.visor.core.client.settings.VRClientSettings;
import org.vmstudio.visor.core.client.utils.ModelUtils;

public class VRPlayerModelTest<T extends LivingEntity> extends PlayerModel<T> {
    public ModelPart vrHMD;

    protected VRClientPlayer vrPlayer;

    protected float bodyYaw;
    protected boolean laying;
    protected float xRot;
    protected float layAmount;
    protected HumanoidArm attackArm = null;
    protected HumanoidArm mainArm = HumanoidArm.RIGHT;
    protected boolean isMainPlayer;
    protected float bodyScale;
    protected float armScale;
    protected float legScale;

    // temp vec for most math
    protected final Vector3f tempV = new Vector3f();
    protected final Vector3f tempV2 = new Vector3f();
    // temp mat3 for rotations
    protected final Matrix3f tempM = new Matrix3f();


    public VRPlayerModelTest(ModelPart root, boolean isSlim) {
        super(root, isSlim);
        this.vrHMD = root.getChild("vrHMD");

    }

    public static MeshDefinition createMesh(CubeDeformation cubeDeformation, boolean slim) {
        MeshDefinition meshDefinition = PlayerModel.createMesh(cubeDeformation, slim);
        PartDefinition root = meshDefinition.getRoot();
        root.addOrReplaceChild("vrHMD", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-3.5F, -6.0F, -7.5F,
                                7.0F, 4.0F, 5.0F, cubeDeformation),
                PartPose.ZERO);

        return meshDefinition;
    }


    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        if (VRClientPlayers.isTracked(entity)) {
            VRPlayerModelTest.animateVRModel(this, entity, limbSwing, limbSwingAmount);

        }
    }

    public static void animateVRModel(
            VRPlayerModelTest<?> model,
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
                bodyYaw, true, pos
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
            poseStack.translate(side == HumanoidArm.RIGHT ? 0.03125F : -0.03125F, 0.0F, 0.0F);
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
