package org.vmstudio.visor.core.client.render.player.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.vmstudio.visor.api.common.utils.VRMathUtils;
import org.vmstudio.visor.core.client.render.player.model.models.FeetModel;
import org.vmstudio.visor.core.client.settings.VRClientSettings;
import org.vmstudio.visor.core.client.utils.ModelUtils;

public class VRPlayerModelWithArmsLegs<T extends LivingEntity> extends VRPlayerModelWithArms<T> implements FeetModel {
    public static final int LOWER_EXTENSION = 2;
    public static final int UPPER_EXTENSION = 2;

    // thighs use the vanilla leg parts
    public ModelPart leftFoot;
    public ModelPart rightFoot;
    public ModelPart leftFootPants;
    public ModelPart rightFootPants;

    private final Vector3f footDir = new Vector3f();
    private final Vector3f footOffset = new Vector3f();
    private final Vector3f kneeOffset = new Vector3f();

    private final Vector3f footPos = new Vector3f();
    private final Vector3f kneePosTemp = new Vector3f();
    private final Quaternionf footQuat = new Quaternionf();

    public VRPlayerModelWithArmsLegs(ModelPart root, boolean isSlim) {
        super(root, isSlim);
        this.leftFoot = root.getChild("left_foot");
        this.rightFoot = root.getChild("right_foot");
        this.leftFootPants = root.getChild("left_foot_pants");
        this.rightFootPants = root.getChild("right_foot_pants");

        // copy textures
        ModelUtils.textureHackUpper(this.leftLeg, this.leftFoot);
        ModelUtils.textureHackUpper(this.rightLeg, this.rightFoot);
        ModelUtils.textureHackUpper(this.leftPants, this.rightFootPants);
        ModelUtils.textureHackUpper(this.rightPants, this.leftFootPants);
    }

    public static MeshDefinition createMesh(CubeDeformation cubeDeformation, boolean slim) {
        MeshDefinition meshDefinition = VRPlayerModelWithArms.createMesh(cubeDeformation, slim);
        PartDefinition partDefinition = meshDefinition.getRoot();

        int upperExtension = UPPER_EXTENSION;
        int lowerExtension = LOWER_EXTENSION;
        float lowerShrinkage = -0.05F;

        // feet
        partDefinition.addOrReplaceChild("left_foot", CubeListBuilder.create()
                .texOffs(16, 55 - lowerExtension)
                .addBox(-2.0F, -5.0F - lowerExtension, -2.0F, 4.0F, 5.0F + lowerExtension, 4.0F,
                    cubeDeformation.extend(lowerShrinkage)),
            PartPose.offset(1.9F, 24.0F, 0.0F));
        partDefinition.addOrReplaceChild("left_foot_pants", CubeListBuilder.create()
                .texOffs(0, 55 - lowerExtension)
                .addBox(-2.0F, -5.0F - lowerExtension, -2.0F, 4.0F, 5.0F + lowerExtension, 4.0F,
                    cubeDeformation.extend(0.25F + lowerShrinkage)),
            PartPose.offset(1.9F, 24.0F, 0.0F));
        partDefinition.addOrReplaceChild("right_foot", CubeListBuilder.create()
                .texOffs(0, 23 - lowerExtension)
                .addBox(-2.0F, -5.0F - lowerExtension, -2.0F, 4.0F, 5.0F + lowerExtension, 4.0F,
                    cubeDeformation.extend(lowerShrinkage)),
            PartPose.offset(-1.9F, 24.0F, 0.0F));
        partDefinition.addOrReplaceChild("right_foot_pants", CubeListBuilder.create()
                .texOffs(0, 39 - lowerExtension)
                .addBox(-2.0F, -5.0F - lowerExtension, -2.0F, 4.0F, 5.0F + lowerExtension, 4.0F,
                    cubeDeformation.extend(0.25F + lowerShrinkage)),
            PartPose.offset(-1.9F, 24.0F, 0.0F));

        // thighs
        partDefinition.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(16, 48)
                .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 5.0F + upperExtension, 4.0F, cubeDeformation),
            PartPose.offset(1.9F, 12.0F, 0.0F));
        partDefinition.addOrReplaceChild("left_pants", CubeListBuilder.create()
                .texOffs(0, 48)
                .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 5.0F + upperExtension, 4.0F, cubeDeformation.extend(0.25f)),
            PartPose.offset(1.9F, 12.0F, 0.0F));
        partDefinition.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(0, 16)
                .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 5.0F + upperExtension, 4.0F, cubeDeformation),
            PartPose.offset(-1.9F, 12.0F, 0.0F));
        partDefinition.addOrReplaceChild("right_pants", CubeListBuilder.create()
                .texOffs(0, 32)
                .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 5.0F + upperExtension, 4.0F, cubeDeformation.extend(0.25f)),
            PartPose.offset(-1.9F, 12.0F, 0.0F));
        return meshDefinition;
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return Iterables.concat(super.bodyParts(),
            ImmutableList.of(this.leftFoot, this.rightFoot, this.leftFootPants, this.rightFootPants));
    }

    @Override
    public void setupAnim(
        T player, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
    {
        super.setupAnim(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        if (this.vrPlayer == null) {
            return;
        }
        boolean noLegs = this.riding || this.laying || player.isFallFlying();
        if (!noLegs) {
            if (VRClientSettings.isPlayerWalkAnim()) {
                // vanilla walking animation on top
                // limbSwingAmount = 1;
                float limbRotation = Mth.cos(limbSwing * 0.6662F) * limbSwingAmount;
                this.footOffset
                    .set(0, -0.5F, 0)
                    .rotateX(limbRotation)
                    .sub(0, -0.5F, 0)
                    .mul(1F, 0.75F, 1F)
                    .rotateY(-this.bodyYaw);
                this.kneeOffset
                    .set(0, -0.5F, 0)
                    .rotateX(-Math.abs(limbRotation))
                    .sub(0, -0.5F, 0)
                    .rotateY(-this.bodyYaw);
            } else {
                this.footOffset.zero();
                this.kneeOffset.zero();
            }

            // left leg
            Vector3fc kneePos;
            this.footPos.set(this.leftLeg.x, 24 + Math.min(this.body.y, 0F), this.leftLeg.z);
            ModelUtils.modelToWorld(player, this.footPos, this.vrPlayer, this.bodyYaw, true, true,
                    this.footPos);
            this.footQuat.identity().rotateY(Mth.PI - this.bodyYaw);
            if (player.isAutoSpinAttack()) {
                // player is offset 1 block during the spin
                this.footPos.y -= 1F;
            }
            kneePos = null;

            this.footPos.add(this.footOffset);
            positionConnectedLimb(player, this.leftLeg, this.leftFoot, this.footPos, this.footQuat, 0F,
                    kneePos, false, null, true);

            // right leg
            this.footPos.set(this.rightLeg.x, 24 + Math.min(this.body.y, 0F), this.rightLeg.z);
            ModelUtils.modelToWorld(player, this.footPos, this.vrPlayer, this.bodyYaw, true, true,
                    this.footPos);
            if (player.isAutoSpinAttack()) {
                // player is offset 1 block during the spin
                this.footPos.y -= 1F;
            }

            kneePos = null;

            this.footPos.add(-this.footOffset.x, this.footOffset.y, -this.footOffset.z);
            positionConnectedLimb(player, this.rightLeg, this.rightFoot, this.footPos, this.footQuat, 0F,
                    kneePos, false, null, true);
        }

        if (this.layAmount > 0F) {
            ModelUtils.applySwimRotationOffset(player, this.xRot, this.tempV, this.tempV2,
                this.leftLeg, this.rightLeg,
                this.leftFoot, this.rightFoot);
        }

        if (noLegs) {
            // align the feet with the legs
            this.footQuat.rotationZYX(this.leftLeg.zRot, this.leftLeg.yRot, this.leftLeg.xRot);
            this.footQuat.transform(0, 12, 0, this.tempV);
            this.leftFoot.setPos(
                this.leftLeg.x + this.tempV.x,
                this.leftLeg.y + this.tempV.y,
                this.leftLeg.z + this.tempV.z);
            this.rightFoot.setPos(
                this.rightLeg.x - this.tempV.x,
                this.rightLeg.y + this.tempV.y,
                this.rightLeg.z + (this.riding ? this.tempV.z : -this.tempV.z));
            this.leftFoot.setRotation(this.leftLeg.xRot, this.leftLeg.yRot, this.leftLeg.zRot);
            this.rightFoot.setRotation(this.rightLeg.xRot, this.rightLeg.yRot, this.rightLeg.zRot);
        }

        this.leftFoot.xScale = this.leftFoot.zScale = this.rightFoot.xScale = this.rightFoot.zScale = this.legScale;

        if (player.isAutoSpinAttack()) {
            spinOffset(this.leftLeg, this.rightLeg, this.leftFoot, this.rightFoot);
        }

        this.leftPants.copyFrom(this.leftLeg);
        this.rightPants.copyFrom(this.rightLeg);
        this.leftFootPants.copyFrom(this.leftFoot);
        this.rightFootPants.copyFrom(this.rightFoot);
        this.leftFootPants.visible = this.leftPants.visible;
        this.rightFootPants.visible = this.rightPants.visible;
    }

    @Override
    public ModelPart getLeftFoot() {
        return this.leftFoot;
    }

    @Override
    public ModelPart getRightFoot() {
        return this.rightFoot;
    }

    @Override
    public void copyPropertiesTo(HumanoidModel<T> model) {
        super.copyPropertiesTo(model);
        if (model instanceof FeetModel feetModel) {
            feetModel.getLeftFoot().copyFrom(this.leftFoot);
            feetModel.getRightFoot().copyFrom(this.rightFoot);
        }
    }

    @Override
    public void setAllVisible(boolean visible) {
        super.setAllVisible(visible);

        this.leftFoot.visible = visible;
        this.rightFoot.visible = visible;
        this.leftFootPants.visible = visible;
        this.rightFootPants.visible = visible;
    }
}
