package org.vmstudio.visor.core.client.render.player.model;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import lombok.Getter;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.core.client.player.VRClientPlayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.ModelPart.Polygon;
import net.minecraft.client.model.geom.ModelPart.Vertex;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class VRPlayerModelWithArms<T extends LivingEntity> extends VRPlayerModel<T> {
    private final boolean slim;
    private final ModelPart leftShoulder;
    private final ModelPart rightShoulder;
    private final ModelPart leftShoulder_sleeve;
    private final ModelPart rightShoulder_sleeve;

    @Getter
    private final ModelPart leftHand;
    @Getter
    private final ModelPart rightHand;

    private boolean laying;

    public VRPlayerModelWithArms(ModelPart modelPart, boolean isSlim) {
        super(modelPart, isSlim);
        this.slim = isSlim;
        // use left/right arm as shoulders
        this.leftShoulder = modelPart.getChild("left_arm");
        this.rightShoulder = modelPart.getChild("right_arm");
        this.leftShoulder_sleeve = modelPart.getChild("leftShoulder_sleeve");
        this.rightShoulder_sleeve = modelPart.getChild("rightShoulder_sleeve");
        this.rightHand = modelPart.getChild("rightHand");
        this.leftHand = modelPart.getChild("leftHand");


        //finger hax
        // some mods remove the base parts
        if (!leftShoulder.cubes.isEmpty()) {
            copyUV(leftShoulder.cubes.get(0).polygons[1], leftHand.cubes.get(0).polygons[1]);
            copyUV(leftShoulder.cubes.get(0).polygons[1], leftHand.cubes.get(0).polygons[0]);
        }
        if (!rightShoulder.cubes.isEmpty()) {
            copyUV(rightShoulder.cubes.get(0).polygons[1], this.rightHand.cubes.get(0).polygons[1]);
            copyUV(rightShoulder.cubes.get(0).polygons[1], this.rightHand.cubes.get(0).polygons[0]);
        }

        if (!rightSleeve.cubes.isEmpty()) {
            copyUV(rightShoulder_sleeve.cubes.get(0).polygons[1], this.rightSleeve.cubes.get(0).polygons[1]);
            copyUV(rightShoulder_sleeve.cubes.get(0).polygons[1], this.rightSleeve.cubes.get(0).polygons[0]);
        }
        if (!leftSleeve.cubes.isEmpty()) {
            copyUV(leftShoulder_sleeve.cubes.get(0).polygons[1], leftSleeve.cubes.get(0).polygons[1]);
            copyUV(leftShoulder_sleeve.cubes.get(0).polygons[1], leftSleeve.cubes.get(0).polygons[0]);
        }
    }

    private void copyUV(Polygon source, Polygon dest) {
        for (int i = 0; i < source.vertices.length; i++) {
            Vertex newVertex = new Vertex(dest.vertices[i].pos, source.vertices[i].u, source.vertices[i].v);
            /*if (OptifineHelper.isOptifineLoaded()) {
                OptifineHelper.copyRenderPositions(dest.vertices[i], newVertex);
            }*/
            dest.vertices[i] = newVertex;
        }
    }


    public static MeshDefinition createMesh(CubeDeformation cubeDeformation,
                                            boolean slim) {
        MeshDefinition meshdefinition = VRPlayerModel.createMesh(cubeDeformation, slim);
        PartDefinition partdefinition = meshdefinition.getRoot();

        if (slim) {
            partdefinition.addOrReplaceChild(
                    "leftHand",
                    CubeListBuilder.create().texOffs(32, 55).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 5.0F, 4.0F, cubeDeformation),
                    PartPose.offset(5.0F, 2.5F, 0.0F)
            );
            partdefinition.addOrReplaceChild(
                    "left_sleeve",
                    CubeListBuilder.create().texOffs(48, 55)
                            .addBox(-1.0F, -2.0F, -2.0F, 3.0F, 5.0F, 4.0F, cubeDeformation.extend(0.25f)),
                    PartPose.offset(5.0F, 2.5F, 0.0F)
            );
            partdefinition.addOrReplaceChild(
                    "rightHand",
                    CubeListBuilder.create().texOffs(40, 23).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 5.0F, 4.0F, cubeDeformation),
                    PartPose.offset(-5.0F, 2.5F, 0.0F)
            );
            partdefinition.addOrReplaceChild(
                    "right_sleeve",
                    CubeListBuilder.create().texOffs(40, 39).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 5.0F, 4.0F, cubeDeformation.extend(0.25f)),
                    PartPose.offset(-5.0F, 2.5F, 0.0F)
            );
            partdefinition.addOrReplaceChild(
                    "left_arm",
                    CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 5.0F, 4.0F, cubeDeformation),
                    PartPose.offset(5.0F, 2.5F, 0.0F)
            );
            partdefinition.addOrReplaceChild(
                    "right_arm",
                    CubeListBuilder.create().texOffs(40, 16).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 5.0F, 4.0F, cubeDeformation),
                    PartPose.offset(-5.0F, 2.5F, 0.0F)
            );
            partdefinition.addOrReplaceChild(
                    "leftShoulder_sleeve",
                    CubeListBuilder.create().texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 5.0F, 4.0F, cubeDeformation.extend(0.25f)),
                    PartPose.offset(5.0F, 2.5F, 0.0F)
            );
            partdefinition.addOrReplaceChild(
                    "rightShoulder_sleeve",
                    CubeListBuilder.create().texOffs(40, 32).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 5.0F, 4.0F, cubeDeformation.extend(0.25f)),
                    PartPose.offset(-5.0F, 2.5F, 0.0F)
            );
        } else {
            partdefinition.addOrReplaceChild(
                    "leftHand",
                    CubeListBuilder.create().texOffs(32, 55).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 5.0F, 4.0F, cubeDeformation),
                    PartPose.offset(5.0F, 2.5F, 0.0F)
            );
            partdefinition.addOrReplaceChild(
                    "left_sleeve",
                    CubeListBuilder.create().texOffs(48, 55).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 5.0F, 4.0F, cubeDeformation.extend(0.25f)),
                    PartPose.offset(5.0F, 2.5F, 0.0F)
            );
            partdefinition.addOrReplaceChild(
                    "rightHand",
                    CubeListBuilder.create().texOffs(40, 23).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 5.0F, 4.0F, cubeDeformation),
                    PartPose.offset(-5.0F, 2.5F, 0.0F)
            );
            partdefinition.addOrReplaceChild(
                    "right_sleeve",
                    CubeListBuilder.create().texOffs(40, 39).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 5.0F, 4.0F, cubeDeformation.extend(0.25f)),
                    PartPose.offset(-5.0F, 2.5F, 0.0F)
            );
            partdefinition.addOrReplaceChild(
                    "left_arm",
                    CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 5.0F, 4.0F, cubeDeformation),
                    PartPose.offset(5.0F, 2.5F, 0.0F)
            );
            partdefinition.addOrReplaceChild(
                    "right_arm",
                    CubeListBuilder.create().texOffs(40, 16).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 5.0F, 4.0F, cubeDeformation),
                    PartPose.offset(-5.0F, 2.5F, 0.0F)
            );
            partdefinition.addOrReplaceChild(
                    "leftShoulder_sleeve",
                    CubeListBuilder.create().texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 5.0F, 4.0F, cubeDeformation.extend(0.25f)),
                    PartPose.offset(5.0F, 2.5F, 0.0F)
            );
            partdefinition.addOrReplaceChild(
                    "rightShoulder_sleeve",
                    CubeListBuilder.create().texOffs(40, 32).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 5.0F, 4.0F, cubeDeformation.extend(0.25f)),
                    PartPose.offset(-5.0F, 2.5F, 0.0F)
            );
        }
        return meshdefinition;
    }



    @Override
    public void setupAnim(T entity,
                          float limbSwing,
                          float limbSwingAmount,
                          float ageInTicks,
                          float netHeadYaw,
                          float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        var vrPlayer= VRClientPlayers.getPlayer(entity.getUUID());

        if (vrPlayer == null) {
            return;
        }
        var renderPose = vrPlayer.getPoseData(PlayerPoseType.RENDER);

        var offhandDir = renderPose.getOffhand().getDirection();
        var mainHandDir = renderPose.getMainHand().getDirection();


        double height = -1.501F * vrPlayer.getFullHeightScale();
        float offhandYaw = (float) Mth.atan2(-offhandDir.x(), -offhandDir.z());
        float offhandPitch = (float) Math.asin(offhandDir.y() / offhandDir.length());
        float mainHandYaw = (float) Mth.atan2(-mainHandDir.x(), -mainHandDir.z());
        float mainHandPitch = (float) Math.asin(mainHandDir.y() / mainHandDir.length());
        double bodyYaw = renderPose.getBodyYaw();

        this.laying = this.swimAmount > 0.0F || entity.isFallFlying() && !entity.isAutoSpinAttack();

        if (!vrPlayer.isLeftHanded()) {
            this.rightShoulder.setPos(
                    -Mth.cos(this.body.yRot) * 5.0F,
                    this.slim ? 2.5F : 2.0F,
                    Mth.sin(this.body.yRot) * 5.0F
            );
            this.leftShoulder.setPos(
                    Mth.cos(this.body.yRot) * 5.0F,
                    this.slim ? 2.5F : 2.0F,
                    -Mth.sin(this.body.yRot) * 5.0F
            );
        } else {
            this.leftShoulder.setPos(
                    -Mth.cos(this.body.yRot) * 5.0F,
                    this.slim ? 2.5F : 2.0F,
                    Mth.sin(this.body.yRot) * 5.0F
            );
            this.rightShoulder.setPos(
                    Mth.cos(this.body.yRot) * 5.0F,
                    this.slim ? 2.5F : 2.0F,
                    -Mth.sin(this.body.yRot) * 5.0F
            );
        }

        if (this.crouching) {
            this.rightShoulder.y += 3.2F;
            this.leftShoulder.y += 3.2F;
        }

        Vector3f offhandArmPos = renderPose.getOffhand().getPosition()
                .add(0.0f, (float) height, 0.0f, new Vector3f())
                .rotateY((float) (-Math.PI + bodyYaw))
                .mul(16.0F / vrPlayer.getFullHeightScale());
        this.leftHand.setPos(
                -offhandArmPos.x,
                -offhandArmPos.y,
                offhandArmPos.z
        );
        this.leftHand.xRot = (float) ((double) (-offhandPitch) + (Math.PI * 1.5D));
        this.leftHand.yRot = (float) (Math.PI - (double) offhandYaw - bodyYaw);
        this.leftHand.zRot = 0.0F;


        Vector3fc leftShoulderPos = new Vector3f(
                this.leftShoulder.x + offhandArmPos.x,
                this.leftShoulder.y + offhandArmPos.y,
                this.leftShoulder.z - offhandArmPos.z
        );
        float leftShoulderYaw = (float) Mth.atan2(leftShoulderPos.x(), leftShoulderPos.z());
        float leftShoulderPitch = (float) ((Math.PI * 1.5D) - Math.asin(leftShoulderPos.y() / leftShoulderPos.length()));
        this.leftShoulder.zRot = 0.0F;
        this.leftShoulder.xRot = leftShoulderPitch;
        this.leftShoulder.yRot = leftShoulderYaw;

        if (this.leftShoulder.yRot > 0.0F) {
            this.leftShoulder.yRot = 0.0F;
        }

        if (this.leftArmPose == ArmPose.THROW_SPEAR) {
            this.leftHand.xRot = (float) ((double) this.leftHand.xRot - (Math.PI / 2D));
        }

        Vector3f mainArm = renderPose.getMainHand().getPosition()
                .add(0.0f, (float) height, 0.0f, new Vector3f())
                .rotateY((float) (-Math.PI + bodyYaw))
                .mul(16.0F / vrPlayer.getFullHeightScale());

        this.rightHand.setPos(-mainArm.x, -((float) mainArm.y), mainArm.z);
        this.rightHand.xRot = (float) ((double) (-mainHandPitch) + (Math.PI * 1.5D));
        this.rightHand.yRot = (float) (Math.PI - (double) mainHandYaw - bodyYaw);
        this.rightHand.zRot = 0.0F;

        Vec3 rightShoulderPos = new Vec3(
                (double) this.rightShoulder.x + mainArm.x,
                (double) this.rightShoulder.y + mainArm.y,
                (double) this.rightShoulder.z - mainArm.z
        );
        float rightShoulderYaw = (float) Mth.atan2(rightShoulderPos.x, rightShoulderPos.z);
        float rightShoulderPitch = (float) ((Math.PI * 1.5D) - Math.asin(rightShoulderPos.y / rightShoulderPos.length()));
        this.rightShoulder.zRot = 0.0F;
        this.rightShoulder.xRot = rightShoulderPitch;
        this.rightShoulder.yRot = rightShoulderYaw;

        if (this.rightShoulder.yRot < 0.0F) {
            this.rightShoulder.yRot = 0.0F;
        }

        if (this.rightArmPose == ArmPose.THROW_SPEAR) {
            this.rightHand.xRot = (float) ((double) this.rightHand.xRot - (Math.PI / 2D));
        }

        if (this.laying) {
            this.rightShoulder.xRot = (float) ((double) this.rightShoulder.xRot - (Math.PI / 2D));
            this.leftShoulder.xRot = (float) ((double) this.leftShoulder.xRot - (Math.PI / 2D));
        }

        this.leftSleeve.copyFrom(this.leftHand);
        this.rightSleeve.copyFrom(this.rightHand);
        this.leftShoulder_sleeve.copyFrom(this.leftShoulder);
        this.rightShoulder_sleeve.copyFrom(this.rightShoulder);
        this.leftShoulder_sleeve.visible = this.leftSleeve.visible;
        this.rightShoulder_sleeve.visible = this.rightSleeve.visible;
    }

    @Override
    public void setAllVisible(boolean flag) {
        super.setAllVisible(flag);

        this.rightShoulder.visible = flag;
        this.leftShoulder.visible = flag;
        this.rightShoulder_sleeve.visible = flag;
        this.leftShoulder_sleeve.visible = flag;
        this.rightHand.visible = flag;
        this.leftHand.visible = flag;
    }

    @Override
    protected ModelPart getArm(HumanoidArm arm) {
        return arm == HumanoidArm.LEFT ? this.leftHand : this.rightHand;
    }

    @Override
    public void translateToHand(HumanoidArm arm, PoseStack poseStack) {
        ModelPart armModel = this.getArm(arm);

        if (this.laying) {
            poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        }

        armModel.translateAndRotate(poseStack);
        poseStack.mulPose(Axis.XP.rotation(Mth.sin( this.attackTime * Mth.PI)));
        poseStack.translate(0.0D, -0.5D, 0.0D);
    }
    @Override
    protected Iterable<ModelPart> bodyParts() {
        return ImmutableList.of(
                this.body,
                this.leftHand, this.rightHand,
                this.leftShoulder, this.rightShoulder,
                this.leftShoulder_sleeve, this.rightShoulder_sleeve,
                this.rightLeg, this.leftLeg,
                this.hat,
                this.leftPants, this.rightPants,
                this.leftSleeve, this.rightSleeve,
                this.jacket
        );
    }

}
