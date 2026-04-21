package org.vmstudio.visor.core.client.render.player.model.full;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.HumanoidArm;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.vmstudio.visor.api.client.player.VRClientPlayer;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.common.player.VRPose;
import org.vmstudio.visor.core.client.player.VRClientPlayers;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.vmstudio.visor.core.client.player.body.full.VRBodyFull;
import org.vmstudio.visor.core.client.render.VRRenderState;
import org.vmstudio.visor.core.client.render.player.model.HandModel;
import org.vmstudio.visor.core.client.render.player.model.ControllerSpaceItemAnchorModel;
import org.vmstudio.visor.core.client.utils.ModelUtils;

public class VRPlayerModelFull<T extends LivingEntity> extends PlayerModel<T> implements ControllerSpaceItemAnchorModel {
    public static final int LOWER_EXTENSION = 2;
    public static final int UPPER_EXTENSION = 3;

    protected VRClientPlayer vrPlayer;

    protected HumanoidArm attackArm = null;
    protected HumanoidArm mainArm = HumanoidArm.RIGHT;

    protected float bodyYaw;
    protected boolean isMainPlayer;

    public ModelPart leftHand;
    public ModelPart rightHand;
    public ModelPart leftHandSleeve;
    public ModelPart rightHandSleeve;

    public VRPlayerModelFull(ModelPart root, boolean isSlim) {
        super(root, isSlim);
        this.leftHandSleeve = root.getChild("left_hand_sleeve");
        this.rightHandSleeve = root.getChild("right_hand_sleeve");
        this.leftHand = root.getChild("left_hand");
        this.rightHand = root.getChild("right_hand");

        // copy textures
        ModelUtils.copyTextures(this.leftArm, this.leftHand);
        ModelUtils.copyTextures(this.rightArm, this.rightHand);
        ModelUtils.copyTextures(this.leftSleeve, this.leftHandSleeve);
        ModelUtils.copyTextures(this.rightSleeve, this.rightHandSleeve);
    }

    public static MeshDefinition createMesh(CubeDeformation cubeDeformation, boolean slim) {
        MeshDefinition meshDefinition = PlayerModel.createMesh(cubeDeformation, slim);
        PartDefinition root = meshDefinition.getRoot();
        int upperExtension = UPPER_EXTENSION;
        int lowerExtension = LOWER_EXTENSION;
        float lowerShrinkage = -0.05F;

        if (slim) {
            root.addOrReplaceChild("left_hand", CubeListBuilder.create()
                            .texOffs(32, 55 - lowerExtension)
                            .addBox(-1.5F, -5.0F - lowerExtension, -2.0F, 3.0F, 5.0F + lowerExtension, 4.0F,
                                    cubeDeformation.extend(lowerShrinkage)),
                    PartPose.offset(5.5F, 12.0F, 0.0F));
            root.addOrReplaceChild("left_hand_sleeve", CubeListBuilder.create()
                            .texOffs(48, 55 - lowerExtension)
                            .addBox(-1.5F, -5.0F - lowerExtension, -2.0F, 3.0F, 5.0F + lowerExtension, 4.0F,
                                    cubeDeformation.extend(0.25f + lowerShrinkage)),
                    PartPose.offset(5.5F, 12.0F, 0.0F));
            root.addOrReplaceChild("right_hand", CubeListBuilder.create()
                            .texOffs(40, 23 - lowerExtension)
                            .addBox(-1.5F, -5.0F - lowerExtension, -2.0F, 3.0F, 5.0F + lowerExtension, 4.0F,
                                    cubeDeformation.extend(lowerShrinkage)),
                    PartPose.offset(-5.5F, 12.0F, 0.0F));
            root.addOrReplaceChild("right_hand_sleeve", CubeListBuilder.create()
                            .texOffs(40, 39 - lowerExtension)
                            .addBox(-1.5F, -5.0F - lowerExtension, -2.0F, 3.0F, 5.0F + lowerExtension, 4.0F,
                                    cubeDeformation.extend(0.25f + lowerShrinkage)),
                    PartPose.offset(-5.5F, 12.0F, 0.0F));
            root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                            .texOffs(32, 48)
                            .addBox(-1.0F, -2.0F, -2.0F, 3.0F, 5.0F + upperExtension, 4.0F, cubeDeformation),
                    PartPose.offset(5.0F, 2.0F, 0.0F));
            root.addOrReplaceChild("left_sleeve", CubeListBuilder.create()
                            .texOffs(48, 48)
                            .addBox(-1.0F, -2.0F, -2.0F, 3.0F, 5.0F + upperExtension, 4.0F, cubeDeformation.extend(0.25f)),
                    PartPose.offset(5.0F, 2.0F, 0.0F));
            root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                            .texOffs(40, 16)
                            .addBox(-2.0F, -2.0F, -2.0F, 3.0F, 5.0F + upperExtension, 4.0F, cubeDeformation),
                    PartPose.offset(-5.0F, 2.0F, 0.0F));
            root.addOrReplaceChild("right_sleeve", CubeListBuilder.create()
                            .texOffs(40, 32)
                            .addBox(-2.0F, -2.0F, -2.0F, 3.0F, 5.0F + upperExtension, 4.0F, cubeDeformation.extend(0.25f)),
                    PartPose.offset(-5.0F, 2.0F, 0.0F));
        } else {
            root.addOrReplaceChild("left_hand", CubeListBuilder.create()
                            .texOffs(32, 55 - lowerExtension)
                            .addBox(-2.0F, -5.0F - lowerExtension, -2.0F, 4.0F, 5.0F + lowerExtension, 4.0F,
                                    cubeDeformation.extend(lowerShrinkage)),
                    PartPose.offset(5.0F, 2.5F, 0.0F));
            root.addOrReplaceChild("left_hand_sleeve", CubeListBuilder.create()
                            .texOffs(48, 55 - lowerExtension)
                            .addBox(-2.0F, -5.0F - lowerExtension, -2.0F, 4.0F, 5.0F + lowerExtension, 4.0F,
                                    cubeDeformation.extend(0.25f + lowerShrinkage)),
                    PartPose.offset(5.0F, 2.5F, 0.0F));
            root.addOrReplaceChild("right_hand", CubeListBuilder.create()
                            .texOffs(40, 23 - lowerExtension)
                            .addBox(-2.0F, -5.0F - lowerExtension, -2.0F, 4.0F, 5.0F + lowerExtension, 4.0F,
                                    cubeDeformation.extend(lowerShrinkage)),
                    PartPose.offset(-5.0F, 2.5F, 0.0F));
            root.addOrReplaceChild("right_hand_sleeve", CubeListBuilder.create()
                            .texOffs(40, 39 - lowerExtension)
                            .addBox(-2.0F, -5.0F - lowerExtension, -2.0F, 4.0F, 5.0F + lowerExtension, 4.0F,
                                    cubeDeformation.extend(0.25f + lowerShrinkage)),
                    PartPose.offset(-5.0F, 2.5F, 0.0F));
            root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                            .texOffs(32, 48)
                            .addBox(-1.0F, -2.0F, -2.0F, 4.0F, 5.0F + upperExtension, 4.0F, cubeDeformation),
                    PartPose.offset(5.0F, 2.5F, 0.0F));
            root.addOrReplaceChild("left_sleeve", CubeListBuilder.create()
                            .texOffs(48, 48)
                            .addBox(-1.0F, -2.0F, -2.0F, 4.0F, 5.0F + upperExtension, 4.0F, cubeDeformation.extend(0.25f)),
                    PartPose.offset(5.0F, 2.5F, 0.0F));
            root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                            .texOffs(40, 16)
                            .addBox(-3.0F, -2.0F, -2.0F, 4.0F, 5.0F + upperExtension, 4.0F, cubeDeformation),
                    PartPose.offset(-5.0F, 2.5F, 0.0F));
            root.addOrReplaceChild("right_sleeve", CubeListBuilder.create()
                            .texOffs(40, 32)
                            .addBox(-3.0F, -2.0F, -2.0F, 4.0F, 5.0F + upperExtension, 4.0F, cubeDeformation.extend(0.25f)),
                    PartPose.offset(-5.0F, 2.5F, 0.0F));
        }
        return meshDefinition;
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        if (VRClientPlayers.isTracked(entity) && !VRRenderState.getPhase().isVRGui()) {
            animateVRModel(this, entity, limbSwing, limbSwingAmount);

        }
    }

    private void animateVRModel(
            VRPlayerModelFull<?> model,
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

        VRBodyFull vrBody = (VRBodyFull) poseRender.getBody();

        var mainHandPose = vrBody.getMainHand().getPose();
        var offhandPose = vrBody.getOffhand().getPose();
        float bodyYaw = poseRender.getBodyYaw();

        ModelPart mainUpperArm = vrPlayer.isLeftHanded() ? model.leftArm : model.rightArm;
        ModelPart mainLowerArm = vrPlayer.isLeftHanded() ? model.leftHand : model.rightHand;
        ModelPart offUpperArm  = vrPlayer.isLeftHanded() ? model.rightArm : model.leftArm;
        ModelPart offLowerArm  = vrPlayer.isLeftHanded() ? model.rightHand : model.leftHand;


        var modelOrigin = ModelUtils.getModelOrigin(player);
        applyArm(vrPlayer, modelOrigin, mainUpperArm, mainLowerArm, mainHandPose, bodyYaw);
        applyArm(vrPlayer, modelOrigin, offUpperArm, offLowerArm, offhandPose, bodyYaw);

        // copy to sleeves
        model.leftSleeve.copyFrom(model.leftArm);
        model.rightSleeve.copyFrom(model.rightArm);
        this.leftHandSleeve.copyFrom(this.leftHand);
        this.rightHandSleeve.copyFrom(this.rightHand);
        this.leftHandSleeve.visible &= this.leftSleeve.visible;
        this.rightHandSleeve.visible &= this.rightSleeve.visible;

        model.isMainPlayer = isMainPlayer;
        model.vrPlayer = vrPlayer;
        model.mainArm = mainArm;
        model.bodyYaw = bodyYaw;
    }

    private static void applyArm(
            VRClientPlayer vrPlayer,
            Vector3f modelOrigin,
            ModelPart upperArm, ModelPart lowerArm,
            VRPose handPose, float bodyYaw
    ) {
        Vector3f temp  = new Vector3f();
        Matrix3f tempM = new Matrix3f();

        Vector3f handPos = new Vector3f();
        Vector3f relativePos = handPose.getPosition().sub(modelOrigin, new Vector3f());

        ModelUtils.worldToModel(
                vrPlayer,
                relativePos,
                bodyYaw,
                true,
                handPos
        );
        lowerArm.x = handPos.x();
        lowerArm.y = handPos.y();
        lowerArm.z = handPos.z();

        Quaternionf handRot = handPose.getRotation().getNormalizedRotation(new Quaternionf());
        ModelUtils.toModelDir(bodyYaw, handRot, tempM);
        ModelUtils.setRotation(lowerArm, tempM, temp);

        ModelUtils.pointModelAtModelForward(
                upperArm,
                lowerArm.x, lowerArm.y, lowerArm.z,
                temp, new Vector3f(), tempM
        );
        ModelUtils.setRotation(upperArm, tempM, temp);
    }

    public void hideLeftArm() {
        this.leftHand.visible = false;
        this.leftHandSleeve.visible = false;
        this.leftArm.visible = false;
        this.leftSleeve.visible = false;
    }

    public void hideRightArm() {
        this.rightHand.visible = false;
        this.rightHandSleeve.visible = false;
        this.rightArm.visible = false;
        this.rightSleeve.visible = false;
    }




    @Override
    protected Iterable<ModelPart> bodyParts() {
        return Iterables.concat(super.bodyParts(),
                ImmutableList.of(this.leftHand, this.rightHand, this.leftHandSleeve, this.rightHandSleeve));
    }
    @Override
    public void copyPropertiesTo(HumanoidModel<T> model) {
        super.copyPropertiesTo(model);
        if (model instanceof HandModel handModel) {
            handModel.getLeftHand().copyFrom(this.leftHand);
            handModel.getRightHand().copyFrom(this.rightHand);
        }
    }

    @Override
    public void setAllVisible(boolean visible) {
        super.setAllVisible(visible);

        this.leftHand.visible = visible;
        this.rightHand.visible = visible;
        this.leftHandSleeve.visible = visible;
        this.rightHandSleeve.visible = visible;
    }



    @Override
    protected ModelPart getArm(HumanoidArm side) {
        return side == HumanoidArm.RIGHT ? this.rightHand : this.leftHand;
    }

    @Override
    public void translateToHand(HumanoidArm side, PoseStack poseStack) {
        this.getArm(side).translateAndRotate(poseStack);

        poseStack.translate(side == HumanoidArm.LEFT ? -0.0625F : 0.0625F, -0.65F, 0.0F);

        doAttackAnim(side, poseStack);
    }

    @Override
    public void applyLocalHandItemAnchor(HumanoidArm side, PoseStack poseStack) {
        poseStack.translate(side == HumanoidArm.LEFT ? -0.0625F : 0.0625F, -0.65F, 0.0F);
    }

    protected void doAttackAnim(HumanoidArm side, PoseStack poseStack) {
        if (side == this.attackArm) {
            poseStack.translate(0.0F, 0.5F, 0.0F);
            poseStack.mulPose(Axis.XP.rotation(Mth.sin(this.attackTime * Mth.PI)));
            poseStack.translate(0.0F, -0.5F, 0.0F);
        }
    }
}