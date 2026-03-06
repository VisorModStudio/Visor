package org.vmstudio.visor.core.client.utils;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import org.joml.*;
import org.vmstudio.visor.api.client.input.HandAction;
import org.vmstudio.visor.api.client.player.VRClientPlayer;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseClient;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.common.utils.VRMathUtils;
import org.vmstudio.visor.compatibility.sodium.SodiumHelper;
import org.vmstudio.visor.core.client.ClientContext;

import javax.annotation.Nullable;
import java.lang.Math;

public class ModelUtils {
    public static HandAction handAction = null;

    public static void textureHack(ModelPart source, ModelPart target) {
        // some mods remove the base parts
        if (source.cubes.isEmpty()) return;

        copyUV(source.cubes.get(0).polygons[1], target.cubes.get(0).polygons[1]);
        copyUV(source.cubes.get(0).polygons[1], target.cubes.get(0).polygons[0]);

        // sodium has custom internal ModelPart geometry which also needs to be modified
        if (SodiumHelper.isLoaded()) {
            SodiumHelper.copyModelCuboidUV(source, target, 3, 3);
            SodiumHelper.copyModelCuboidUV(source, target, 3, 2);
        }
    }

    public static void textureHackUpper(ModelPart source, ModelPart target) {
        // some mods remove the base parts
        if (source.cubes.isEmpty()) return;

        // set bottom of target
        copyUV(source.cubes.get(0).polygons[1], target.cubes.get(0).polygons[1]);
        // set those to the top of the source
        copyUV(source.cubes.get(0).polygons[0], target.cubes.get(0).polygons[0]);
        copyUV(source.cubes.get(0).polygons[0], source.cubes.get(0).polygons[1]);

        // sodium has custom internal ModelPart geometry which also needs to be modified
        if (SodiumHelper.isLoaded()) {
            SodiumHelper.copyModelCuboidUV(source, target, 3, 3);
            SodiumHelper.copyModelCuboidUV(source, target, 2, 2);
            SodiumHelper.copyModelCuboidUV(source, source, 2, 3);
        }
    }

    private static void copyUV(ModelPart.Polygon source, ModelPart.Polygon target) {
        for (int i = 0; i < source.vertices.length; i++) {
            ModelPart.Vertex newVertex = new ModelPart.Vertex(target.vertices[i].pos, source.vertices[i].u,
                    source.vertices[i].v);

            target.vertices[i] = newVertex;
        }
    }



    public static float getBendProgress(LivingEntity entity,
                                        VRClientPlayer clientPlayer,
                                        Vector3fc headPivot) {
        // no bending when spinning
        if (entity.isAutoSpinAttack()) return 0.0F;

        // default player eye height, -0.2 neck offset
        float eyeHeight = 1.42F * clientPlayer.getPoseData(PlayerPoseType.RENDER).getWorldScale();

        float heightOffset = Mth.clamp(headPivot.y() - eyeHeight * clientPlayer.getFullHeightScale(), -eyeHeight, 0F);

        float progress = heightOffset / -eyeHeight;

        if (entity.isCrouching()) {
            progress = Math.max(progress, 0.125F);
        }
        if (entity.isPassenger()) {
            // don't go below sitting position
            progress = Math.min(progress, 0.5F);
        }
        return progress;
    }


    public static void worldToModel(
            LivingEntity player, Vector3fc position, VRClientPlayer clientPlayer, float bodyYaw,
            boolean useWorldScale, Vector3f out)
    {
        out.set(position);

        if (player.isAutoSpinAttack()) {
            out.y += 1F;
        }

        // worldscale includes entity scale
        if (useWorldScale) {
            out.div(clientPlayer.getPoseData(PlayerPoseType.RENDER).getWorldScale());
        } else {
            out.div(ScaleHelper.getEntityEyeHeightScale(player, ClientContext.visor.getPartialTicks()));
        }


        final float scale = 0.9375F * clientPlayer.getFullHeightScale();
        out.sub(0.0F, 1.501F * scale, 0.0F) // move to player center
                .rotateY(-Mth.PI + bodyYaw) // apply player rotation
                .mul(16.0F / scale)
                .mul(-1, -1, 1); // scale to player space
    }


    public static void worldToModelDirection(Vector3fc direction, float bodyYaw, Vector3f out) {
        direction.rotateY(-Mth.PI + bodyYaw, out);
        out.set(-out.x(), -out.y(), out.z());
    }

    public static void modelToWorldDirection(Vector3fc direction, float bodyYaw, Vector3f out) {
        out.set(-direction.x(), -direction.y(), direction.z())
                .rotateY(Mth.PI - bodyYaw);
    }


    public static Vector3f modelToWorld(
            LivingEntity player, Vector3fc modelPosition, VRClientPlayer clientPlayer, float bodyYaw,
            boolean applyScale, boolean useWorldScale, Vector3f out)
    {
        return modelToWorld(player, modelPosition.x(), modelPosition.y(), modelPosition.z(), clientPlayer, bodyYaw,
                applyScale, useWorldScale, out);
    }


    public static Vector3f modelToWorld(
            LivingEntity player, float x, float y, float z, VRClientPlayer clientPlayer, float bodyYaw,
            boolean applyScale, boolean useWorldScale, Vector3f out)
    {
        final float scale = 0.9375F * clientPlayer.getFullHeightScale();
        out.set(-x, -y, z)
                .mul(scale / 16.0F)
                .rotateY(Mth.PI - bodyYaw)
                .add(0.0F, 1.501F * scale, 0.0F);


        if (applyScale) {
            // worldscale includes entity scale
            if (useWorldScale) {
                out.mul(clientPlayer.getPoseData(PlayerPoseType.RENDER).getWorldScale());
            } else {
                out.mul(ScaleHelper.getEntityEyeHeightScale(player, ClientContext.visor.getPartialTicks()));
            }
        }

        return out;
    }


    public static void pointModelAtLocal(
            LivingEntity player, ModelPart part, Vector3fc target, Quaternionfc targetRot, VRClientPlayer clientPlayer,
            float bodyYaw, boolean useWorldScale, Vector3f tempVDir, Vector3f tempVUp, Matrix3f tempM)
    {
        // convert target to model
        worldToModel(player, target, clientPlayer, bodyYaw, useWorldScale, tempVDir);

        // calculate direction
        tempVDir.sub(part.x, part.y, part.z);

        // get the up vector the ModelPart should face
        targetRot.transform(VRMathUtils.RIGHT_VECTOR, tempVUp);
        worldToModelDirection(tempVUp, bodyYaw, tempVUp);

        tempVDir.cross(tempVUp, tempVUp);

        // rotate model
        pointAtModel(tempVDir, tempVUp, tempM);
    }


    public static void pointAtModelWithLocal(
            Quaternionfc targetRot, float bodyYaw, Vector3fc dir, Vector3f tempVUp, Matrix3f tempM)
    {

        // get the up vector the ModelPart should face
        targetRot.transform(VRMathUtils.RIGHT_VECTOR, tempVUp);
        worldToModelDirection(tempVUp, bodyYaw, tempVUp);

        dir.cross(tempVUp, tempVUp);

        // rotate model
        pointAtModel(dir, tempVUp, tempM);
    }


    public static void pointModelAtModelForward(
            ModelPart part, float targetX, float targetY, float targetZ, Vector3f tempVDir,
            Vector3f tempVUp, Matrix3f tempM)
    {
        // calculate direction
        tempVDir.set(targetX - part.x, targetY - part.y, targetZ - part.z);

        // get the up vector the ModelPart should face
        tempVDir.cross(VRMathUtils.LEFT_VECTOR, tempVUp);

        // rotate model
        pointAtModel(tempVDir, tempVUp, tempM);
    }

    public static void pointModelAtModelWithUp(
            ModelPart part, float targetX, float targetY, float targetZ, Vector3fc up, Vector3f tempVDir, Matrix3f tempM)
    {
        // calculate direction
        tempVDir.set(targetX - part.x, targetY - part.y, targetZ - part.z);

        // rotate model
        pointAtModel(tempVDir, up, tempM);
    }


    public static void pointAtModel(Vector3fc dir, Vector3fc upDir, Matrix3f tempM) {
        tempM.setLookAlong(
                -dir.x(), -dir.y(), dir.z(),
                -upDir.x(), -upDir.y(), upDir.z()).transpose();
        // ModelParts are rotated 90°
        tempM.rotateX(Mth.HALF_PI);
    }


    public static void toModelDir(float bodyYaw, Quaternionfc direction, Matrix3f tempM) {
        tempM.set(direction);
        // undo body yaw
        tempM.rotateLocalY(bodyYaw + Mth.PI);
        // ModelParts are rotated 90°
        tempM.rotateX(Mth.HALF_PI);
    }


    public static void setRotation(ModelPart part, Matrix3fc rotation, Vector3f tempV) {
        rotation.getEulerAnglesZYX(tempV);
        // ModelPart x and y axes are flipped
        // this can be nan when it is perfectly aligned with pointing left. 0 isn't right here, but beter than nan
        part.setRotation(-tempV.x, Float.isNaN(tempV.y) ? 0F : -tempV.y, tempV.z);
    }

    public static void estimateJointDir(
            ModelPart upper, ModelPart lower, Quaternionfc lowerRot, float bodyYaw, boolean jointDown,
            @Nullable Vector3fc jointPos, LivingEntity player, VRClientPlayer clientPlayer, boolean useWorldScale,
            Vector3f tempV, Vector3f tempV2)
    {
        if (jointPos != null) {
            // use mid arm point to joint direction
            tempV.set(upper.x + lower.x, upper.y + lower.y, upper.z + lower.z)
                    .mul(0.5F);
            ModelUtils.worldToModel(player, jointPos, clientPlayer, bodyYaw, useWorldScale, tempV2);
            tempV2.sub(tempV, tempV);
        } else {
            // point the elbow away from the hand direction
            // hand direction, up forward/down back
            lowerRot.transform(0F, jointDown ? -1F : 1F, jointDown ? 1F : -1F, tempV);
            ModelUtils.worldToModelDirection(tempV, bodyYaw, tempV);
        }
        // arm dir
        tempV2.set(lower.x - upper.x, lower.y - upper.y, lower.z - upper.z);

        // calculate the vector perpendicular to the arm dir
        float dot = tempV2.dot(tempV) / tempV2.dot(tempV2);
        tempV2.mul(dot);
        tempV.sub(tempV2).normalize();
    }

    public static void estimateJoint(
            float startX, float startY, float startZ, float endX, float endY, float endZ, Vector3fc preferredDirection,
            float limbLength, Vector3f tempV)
    {
        tempV.set(startX, startY, startZ);
        float distance = tempV.distance(endX, endY, endZ);
        tempV.add(endX, endY, endZ).mul(0.5F);
        if (distance < limbLength) {
            // move the mid point outwards so that the limb length is reached
            float offsetDistance = (float) Math.sqrt((limbLength * limbLength - distance * distance) * 0.25F);
            tempV.add(preferredDirection.x() * offsetDistance,
                    preferredDirection.y() * offsetDistance,
                    preferredDirection.z() * offsetDistance);
        }
    }


    public static void swingAnimation(
            HumanoidArm arm, float attackTime, boolean isMainPlayer,
            Matrix3f tempM, Vector3f tempV)
    {
        if(handAction == null) {
            return;
        }
        // zero it always, since it's supposed to have the offset at the end
        tempV.zero();
        if (attackTime > 0.0F) {
            if (!isMainPlayer || handAction == HandAction.ATTACK) {
                // arm swing animation
                float rotation;
                if (attackTime > 0.5F) {
                    rotation = Mth.sin(attackTime * Mth.PI + Mth.PI);
                } else {
                    rotation = Mth.sin((attackTime * 3.0F) * Mth.PI);
                }

                tempM.rotateX(rotation * 30.0F * Mth.DEG_TO_RAD);
            } else {
                switch (handAction) {
                    case USE -> {
                        // hand forward animation
                        float movement;
                        if (attackTime > 0.25F) {
                            movement = Mth.sin(attackTime * Mth.HALF_PI + Mth.PI);
                        } else {
                            movement = Mth.sin(attackTime * Mth.TWO_PI);
                        }
                        tempM.transform(VRMathUtils.DOWN_VECTOR, tempV).mul((1F + movement) * 1.6F);
                    }
                    case INTERACT -> {
                        // arm rotation animation
                        float rotation;
                        if (attackTime > 0.5F) {
                            rotation = Mth.sin(attackTime * Mth.PI + Mth.PI);
                        } else {
                            rotation = Mth.sin(attackTime * 3.0F * Mth.PI);
                        }

                        tempM.rotateY((arm == HumanoidArm.RIGHT ? -40.0F : 40.0F) * rotation * Mth.DEG_TO_RAD);
                    }
                }
            }
        }
    }


    public static void swingAnimation(
            ModelPart part, HumanoidArm arm, float offset, float attackTime, boolean isMainPlayer, Matrix3f tempM,
            Vector3f tempV, Vector3f tempV2)
    {
        if (attackTime > 0.0F) {
            // need to get the pre and post rotation point, to offset the modelPart correctly
            tempM.transform(0, offset, 0, tempV2);

            swingAnimation(arm, attackTime, isMainPlayer, tempM, tempV);
            // apply offset from the animation
            part.x -= tempV.x;
            part.y -= tempV.y;
            part.z += tempV.z;

            tempM.transform(0, offset, 0, tempV);

            // apply the offset from the rotation point
            part.x += tempV2.x - tempV.x;
            part.y += tempV2.y - tempV.y;
            part.z -= tempV2.z - tempV.z;
        }
    }


    public static void applySwimRotationOffset(
            LivingEntity player, float xRot, Vector3f tempV, Vector3f tempV2, ModelPart... parts)
    {
        // calculate rotation offset, since the player model is offset while swimming
        if (player.isVisuallySwimming() && !player.isAutoSpinAttack() && !player.isFallFlying()) {
            tempV2.set(0.0F, 17.06125F, 5.125F);
            tempV2.rotateX(-xRot);
            tempV2.y += 2;
        } else {
            // make sure this one is empty
            tempV2.set(0, 0, 0);
        }

        for (ModelPart part : parts) {
            tempV.set(part.x, part.y, part.z);

            tempV.sub(tempV2);

            // apply swimming rotation to the offset
            tempV.y -= 24F;
            tempV.rotateX(xRot);
            tempV.y += 24F;
            part.setPos(tempV.x, tempV.y, tempV.z);
        }
    }

}
