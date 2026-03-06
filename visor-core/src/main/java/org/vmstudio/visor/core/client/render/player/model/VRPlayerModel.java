package org.vmstudio.visor.core.client.render.player.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.vmstudio.visor.api.client.VRStateMode;
import org.vmstudio.visor.api.client.player.VRClientPlayer;
import org.vmstudio.visor.api.client.player.VRRemotePlayer;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseClient;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.core.client.player.VRClientPlayers;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.vmstudio.visor.core.client.render.VRRenderState;
import org.vmstudio.visor.core.client.settings.VRClientSettings;
import org.vmstudio.visor.core.client.utils.ModelUtils;

public class VRPlayerModel<T extends LivingEntity> extends PlayerModel<T> {
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

    public VRPlayerModel(ModelPart root, boolean isSlim) {
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



    public static void animateVRModel(
            PlayerModel<LivingEntity> model,
            LivingEntity player,
            float limbSwing, float limbSwingAmount,
            Vector3f tempV,
            Vector3f tempV2,
            Matrix3f tempM
    ) {
        var vrPlayer = VRClientPlayers.getPlayer(player.getUUID());


        if (vrPlayer == null) {
            // not a vr player
            if (model instanceof VRPlayerModel<LivingEntity> vrModel) {
                vrModel.vrPlayer = null;
            }
            return;
        }

        float partialTick = ClientContext.visor.getPartialTicks();
        boolean isMainPlayer = VRRenderState.isVRBodyLocalPlayer(player);

        HumanoidArm mainArm = vrPlayer.isLeftHanded()
                ? HumanoidArm.LEFT
                : HumanoidArm.RIGHT;
        HumanoidArm attackArm = null;

        if (model.attackTime > 0F) {
            // we ignore the vanilla main arm setting
            attackArm = player.swingingArm == InteractionHand.MAIN_HAND ?
                    HumanoidArm.RIGHT : HumanoidArm.LEFT;
            if (vrPlayer.isLeftHanded()) {
                attackArm = attackArm.getOpposite();
            }
        }

        var pose =  vrPlayer.getPoseData(PlayerPoseType.TICK);


        float bodyYaw = pose.getBodyYaw();

        boolean laying = model.swimAmount > 0.0F || player.isFallFlying();
        float layAmount = player.isFallFlying() ? 1F : model.swimAmount;

        boolean swimming = (laying && player.isInWater()) || player.isFallFlying();
        boolean noLowerBodyAnimation = true;

        float bodyScale = 1F;
        float armScale = 1F;
        float legScale = 1F;

        // this check is similar to VREffectsHelper#isFirstPersonEntityPass,
        // but does different stuff for shaders shadow pass
        if (isMainPlayer && VRClientSettings.getVrBodyType().isFullBody())
        {
            bodyScale = VRClientSettings.getPlayerModelBodyScale();
            armScale = VRClientSettings.getPlayerModelArmsScale();
            legScale = VRClientSettings.getPlayerModelLegScale();
        }

        // scale the offset with the body and arm scale, to keep them attached
        float sideOffset = 4F * bodyScale + armScale;

        float xRot;

        if (swimming) {
            // in water also rotate around the view vector
            xRot = layAmount * (-Mth.HALF_PI - Mth.DEG_TO_RAD * player.getViewXRot(partialTick));
        } else {
            xRot = layAmount * -Mth.HALF_PI;
        }

        // head pivot
        if (!swimming) {
            pose.getHmd().getRotation().transformDirection(
                    0F, -0.2F, 0.1F, tempV2
            );
            tempV2.mul(vrPlayer.getFullHeightScale() * pose.getWorldScale());
        } else {
            // no pivot offset when swimming
            tempV2.zero();
        }
        tempV2.add(pose.getHmd().getPosition());

        float progress = ModelUtils.getBendProgress(player, vrPlayer, tempV2);
        float heightOffset = 22F * progress;

        // rotate head
        tempM.set(pose.getHmd().getRotation())
                .rotateLocalY(bodyYaw + Mth.PI)
                .rotateLocalX(-xRot);
        ModelUtils.setRotation(model.head, tempM, tempV);
        ModelUtils.worldToModel(player, tempV2, vrPlayer, bodyYaw, true, tempV);

        if (swimming) {
            // move the head in front of the body when swimming
            tempV.z += 3F;
        }

        // move head and body with bend
        model.head.setPos(tempV.x, tempV.y, tempV.z);
        model.body.setPos(model.head.x, model.head.y, model.head.z);

        // rotate body
        if (model.riding) {
            // when riding, rotate body to sitting position
            ModelUtils.pointModelAtModelForward(model.body, 0F, 14F, 2F + heightOffset, tempV, tempV2, tempM);
            tempM.rotateLocalX(-xRot);
            ModelUtils.setRotation(model.body, tempM, tempV);
        } else {
            // with only arms simply rotate the body in place
            model.body.setRotation(
                    Mth.PI * Math.max(0F, model.body.y / 22F) * (model instanceof VRPlayerModelWithArmsLegs ? 0.5F : 1F),
                    0F, 0F);
            if (laying) {
                float bodyXRot;
                if (swimming) {
                    bodyXRot = -xRot;
                } else {
                    float aboveGround = (heightOffset - 11F) / 11F;
                    bodyXRot = progress * (Mth.PI - Mth.HALF_PI * (1F + 0.3F * (1F - aboveGround)));
                }
                // lerp body rotation when swimming, to keep the model connected
                model.body.xRot = Mth.lerp(layAmount, model.body.xRot, bodyXRot);
                model.head.y -= 2F * layAmount;
                model.body.y -= 2F * layAmount;
            }
        }

        float cosBodyRot = Mth.cos(model.body.xRot);

        // offset arms with body rotation
        model.leftArm.x = model.body.x + sideOffset;
        model.rightArm.x = model.body.x - sideOffset;
        model.leftArm.y = 2F * cosBodyRot + model.body.y;
        model.leftArm.z = model.body.z;

        model.rightArm.y = model.leftArm.y;
        model.rightArm.z = model.leftArm.z;

        model.leftLeg.x = 1.9F;
        model.rightLeg.x = -1.9F;

        if (model.riding) {
            model.leftLeg.z = heightOffset;
            model.rightLeg.z = model.leftLeg.z;
        } else if (laying) {
            // adjust legs
            if (swimming) {
                tempV.set(0, 12, 0);
                tempV.rotateX(-xRot);
                model.leftLeg.y = model.body.y + tempV.y;
                model.leftLeg.z = model.body.z + tempV.z;
            } else {
                // move legs with bend
                float cosBodyRot2 = cosBodyRot * cosBodyRot;
                model.leftLeg.y += 10.25F - 2F * cosBodyRot2;
                model.leftLeg.z = model.body.z + 13F - cosBodyRot2 * 8F;
            }
            model.leftLeg.x += model.body.x;
            model.rightLeg.x += model.body.x;

            model.rightLeg.y = model.leftLeg.y;
            model.rightLeg.z = model.leftLeg.z;
        } else {
            model.leftLeg.x += model.body.x;
            model.rightLeg.x += model.body.x;
        }

        // regular positioning
        if (!model.riding && layAmount < 1.0F) {
            // move legs back with bend
            float newLegY = 12F + Math.min(model.body.y, 0F);
            float newLegZ = model.body.z + 10F * Mth.sin(model.body.xRot);
            if (model instanceof VRPlayerModelWithArmsLegs) {
                newLegY += 10F * Mth.sin(model.body.xRot);
            }

            model.leftLeg.y = Mth.lerp(layAmount, newLegY, model.leftLeg.y);
            model.leftLeg.z = Mth.lerp(layAmount, newLegZ, model.leftLeg.z);

            model.rightLeg.y = model.leftLeg.y;
            model.rightLeg.z = model.leftLeg.z;
        }

        var offhandPose =  pose.getOffhand();
        var mainHandPose =  pose.getMainHand();
        // arms/legs only when standing
        // arms only when not a split arms model
        if (!(model instanceof VRPlayerModelWithArms)
                && offhandPose.getPosition().distanceSquared(mainHandPose.getPosition()) > 0.0F)
        {
            ModelPart offHand = vrPlayer.isLeftHanded() ? model.rightArm : model.leftArm;
            ModelPart mainHand = vrPlayer.isLeftHanded() ? model.leftArm : model.rightArm;

            // rotation offset, since the rotation point isn't in the center.
            // this rotates the arm 0.5 or 1 pixels at full arm distance, so that the hand matches up with the center
            float offset = (vrPlayer.isLeftHanded() ? -1F : 1f) * (model.slim ? 0.016F : 0.032F) * Mth.PI * armScale;

            // main hand
            ModelUtils.worldToModel(player, mainHandPose.getPosition(), vrPlayer, bodyYaw, isMainPlayer, tempV);
            tempV.sub(mainHand.x, mainHand.y, mainHand.z);
            // move shoulders up when having the arms up, since the rotation point is slightly offset
            mainHand.y -= 2F * Math.max(0F, -tempV.y / tempV.length());

            ModelUtils.pointAtModelWithLocal(mainHandPose.getRotation().getNormalizedRotation(new Quaternionf()), bodyYaw, tempV, tempV2, tempM);

            float controllerDist = tempV.length();

            if (!VRClientSettings.isPlayerLimbsLimit() && controllerDist > 10F) {
                tempV.normalize().mul(controllerDist - 10F);
                mainHand.x += tempV.x;
                mainHand.y += tempV.y;
                mainHand.z += tempV.z;
                tempM.rotateZ(-offset);
            } else {
                // reduce correction angle with distance
                tempM.rotateZ(-offset * Math.min(10F / controllerDist, 1F));
            }

            if (VRClientSettings.isPlayerArmAnim() && attackArm == mainArm) {
                ModelUtils.swingAnimation(attackArm, model.attackTime, isMainPlayer, tempM,
                        tempV);
                mainHand.x -= tempV.x;
                mainHand.y -= tempV.y;
                mainHand.z += tempV.z;
            }
            tempM.rotateLocalX(-xRot);
            ModelUtils.setRotation(mainHand, tempM, tempV);

            // offhand
            ModelUtils.worldToModel(player, offhandPose.getPosition(), vrPlayer, bodyYaw,
                    isMainPlayer, tempV);
            tempV.sub(offHand.x, offHand.y, offHand.z);
            // move shoulders up when having the arms up, since the rotation point is slightly offset
            offHand.y -= 2F * Math.max(0F, -tempV.y / tempV.length());

            ModelUtils.pointAtModelWithLocal(offhandPose.getRotation().getNormalizedRotation(new Quaternionf()), bodyYaw, tempV, tempV2, tempM);

            controllerDist = tempV.length();

            if (!VRClientSettings.isPlayerLimbsLimit() && controllerDist > 10F) {
                tempV.normalize().mul(controllerDist - 10F);
                offHand.x += tempV.x;
                offHand.y += tempV.y;
                offHand.z += tempV.z;
                tempM.rotateZ(offset);
            } else {
                // reduce correction angle with distance
                tempM.rotateZ(offset * Math.min(10F / controllerDist, 1F));
            }

            if (VRClientSettings.isPlayerArmAnim() && attackArm != mainArm) {
                ModelUtils.swingAnimation(attackArm, model.attackTime, isMainPlayer, tempM,
                        tempV);
                offHand.x -= tempV.x;
                offHand.y -= tempV.y;
                offHand.z += tempV.z;
            }

            tempM.rotateLocalX(-xRot);
            ModelUtils.setRotation(offHand, tempM, tempV);
        }

        // legs only when not sitting

        if (layAmount > 0F) {
            // with a waist tracker the rotation is already done before
            model.body.xRot += xRot;

            if (model instanceof VRPlayerModelWithArmsLegs) {
                ModelUtils.applySwimRotationOffset(player, xRot, tempV, tempV2,
                        model.head, model.body);
            } else if (model instanceof VRPlayerModelWithArms) {
                ModelUtils.applySwimRotationOffset(player, xRot, tempV, tempV2,
                        model.head, model.body,
                        model.leftLeg, model.rightLeg);
            } else {
                ModelUtils.applySwimRotationOffset(player, xRot, tempV, tempV2,
                        model.head, model.body,
                        model.leftArm, model.rightArm,
                        model.leftLeg, model.rightLeg);
            }
        }

        model.leftArm.xScale = model.leftArm.zScale = model.rightArm.xScale = model.rightArm.zScale = armScale;
        model.body.xScale = model.body.zScale = bodyScale;
        model.leftLeg.xScale = model.leftLeg.zScale = model.rightLeg.xScale = model.rightLeg.zScale = legScale;

        // spin attack moves the model one block up
        if (player.isAutoSpinAttack()) {
            spinOffset(model.head, model.body);
            if (!(model instanceof VRPlayerModelWithArms)) {
                spinOffset(model.leftArm, model.rightArm);
            }
            if (!(model instanceof VRPlayerModelWithArmsLegs)) {
                spinOffset(model.leftLeg, model.rightLeg);
            }
        }

        model.leftSleeve.copyFrom(model.leftArm);
        model.rightSleeve.copyFrom(model.rightArm);
        model.leftPants.copyFrom(model.leftLeg);
        model.rightPants.copyFrom(model.rightLeg);
        model.hat.copyFrom(model.head);
        model.jacket.copyFrom(model.body);

        if (model instanceof VRPlayerModel vrModel) {
            vrModel.isMainPlayer = isMainPlayer;
            vrModel.vrPlayer = vrPlayer;
            vrModel.mainArm = mainArm;
            vrModel.attackArm = attackArm;
            vrModel.bodyYaw = bodyYaw;
            vrModel.laying = laying;
            vrModel.layAmount = layAmount;
            vrModel.bodyScale = bodyScale;
            vrModel.armScale = armScale;
            vrModel.legScale = legScale;
            vrModel.xRot = xRot;
        }
    }

    public void hideLeftArm(boolean completeArm) {
        this.leftArm.visible = false;
        this.leftSleeve.visible = false;
    }

    public void hideRightArm(boolean onlyHand) {
        this.rightArm.visible = false;
        this.rightSleeve.visible = false;
    }

    protected static void spinOffset(ModelPart... parts) {
        for (ModelPart part : parts) {
            part.y += 24F;
        }
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
