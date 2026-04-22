package org.vmstudio.visor.core.client.utils;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.joml.*;
import org.vmstudio.visor.api.client.input.HandAction;
import org.vmstudio.visor.api.client.player.VRClientPlayer;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.common.utils.VRMathUtils;
import org.vmstudio.visor.compatibility.sodium.SodiumHelper;
import org.vmstudio.visor.core.client.ClientContext;

public class ModelUtils {

    public static void copyTextures(ModelPart source, ModelPart target) {
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

    public static void copyTexturesUpper(ModelPart source, ModelPart target) {
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



    public static Vector3f getModelOrigin(@NotNull LivingEntity entity){
        float partialTicks = ClientContext.visor.getPartialTicks();
        return new Vector3f(
                (float) Mth.lerp(partialTicks, entity.xo, entity.getX()),
                (float) Mth.lerp(partialTicks, entity.yo, entity.getY()),
                (float) Mth.lerp(partialTicks, entity.zo, entity.getZ())
        );
    }


    public static void worldToModel(
            VRClientPlayer vrPlayer,
            Vector3fc position, float bodyYaw,
            boolean useWorldScale, Vector3f out)
    {
        out.set(position);

        if (vrPlayer.getMcPlayer().isAutoSpinAttack()) {
            out.y += 1F;
        }

        if (useWorldScale) {
            out.div(vrPlayer.getPoseData(PlayerPoseType.RENDER).getWorldScale());
        } else {
            out.div(ScaleHelper.getEntityEyeHeightScale(vrPlayer.getMcPlayer(), ClientContext.visor.getPartialTicks()));
        }


        final float scale = 0.9375F * vrPlayer.getFullHeightScale();
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
            if (useWorldScale) {
                out.mul(clientPlayer.getPoseData(PlayerPoseType.RENDER).getWorldScale());
            } else {
                out.mul(ScaleHelper.getEntityEyeHeightScale(player, ClientContext.visor.getPartialTicks()));
            }
        }

        return out;
    }




    public static void pointModelAtModelForward(
            ModelPart part, float targetX, float targetY, float targetZ, Vector3f tempVDir,
            Vector3f tempVUp, Matrix3f tempM)
    {
        tempVDir.set(targetX - part.x, targetY - part.y, targetZ - part.z);

        tempVDir.cross(VRMathUtils.LEFT_VECTOR, tempVUp);

        pointAtModel(tempVDir, tempVUp, tempM);
    }

    public static void pointModelAtModelWithUp(
            ModelPart part, float targetX, float targetY, float targetZ, Vector3fc up, Vector3f tempVDir, Matrix3f tempM)
    {
        tempVDir.set(targetX - part.x, targetY - part.y, targetZ - part.z);

        pointAtModel(tempVDir, up, tempM);
    }


    public static void pointAtModel(Vector3fc dir, Vector3fc upDir, Matrix3f tempM) {
        tempM.setLookAlong(
                -dir.x(), -dir.y(), dir.z(),
                -upDir.x(), -upDir.y(), upDir.z()).transpose();
        tempM.rotateX(Mth.HALF_PI);
    }


    public static void toModelDir(float bodyYaw, Quaternionfc direction, Matrix3f tempM) {
        tempM.set(direction);
        tempM.rotateLocalY(bodyYaw + Mth.PI);
        tempM.rotateX(Mth.HALF_PI);
    }


    public static void setRotation(ModelPart part, Matrix3fc rotation, Vector3f tempV) {
        rotation.getEulerAnglesZYX(tempV);
       part.setRotation(-tempV.x, Float.isNaN(tempV.y) ? 0F : -tempV.y, tempV.z);
    }



    public static void swingAnimation(
            HumanoidArm arm, float attackTime, boolean isMainPlayer,
            Matrix3f matrix, Vector3f pos)
    {
        var handAction = ClientContext.handRenderer.getSwingType();
        pos.zero();
        if (attackTime > 0.0F) {
            if (!isMainPlayer || handAction == HandAction.ATTACK) {
                float rotation;
                if (attackTime > 0.5F) {
                    rotation = Mth.sin(attackTime * Mth.PI + Mth.PI);
                } else {
                    rotation = Mth.sin((attackTime * 3.0F) * Mth.PI);
                }

                matrix.rotateX(rotation * 30.0F * Mth.DEG_TO_RAD);
            } else {
                switch (handAction) {
                    case USE -> {
                        float movement;
                        if (attackTime > 0.25F) {
                            movement = Mth.sin(attackTime * Mth.HALF_PI + Mth.PI);
                        } else {
                            movement = Mth.sin(attackTime * Mth.TWO_PI);
                        }

                        float distance = (1F + movement) * 1.6F;

                        matrix.transform(VRMathUtils.DOWN_VECTOR, pos).mul(distance);
                    }
                    case INTERACT -> {
                        float rotation;
                        if (attackTime > 0.5F) {
                            rotation = Mth.sin(attackTime * Mth.PI + Mth.PI);
                        } else {
                            rotation = Mth.sin(attackTime * 3.0F * Mth.PI);
                        }

                        matrix.rotateY((arm == HumanoidArm.RIGHT ? -40.0F : 40.0F) * rotation * Mth.DEG_TO_RAD);
                    }
                }
            }
        }
    }

    public static void swingAnimation(
            ModelPart part, HumanoidArm arm, float offset, float attackTime,
            boolean isMainPlayer, Matrix3f tempM, Vector3f tempV, Vector3f tempV2)
    {
        if (attackTime > 0.0F) {
            // pivot before swing
            tempM.transform(0, offset, 0, tempV2);

            swingAnimation(arm, attackTime, isMainPlayer, tempM, tempV);

            // animation offset (model-space flip)
            part.x -= tempV.x();
            part.y -= tempV.y();
            part.z += tempV.z();

            // pivot after swing
            tempM.transform(0, offset, 0, tempV);

            // correct for pivot shift
            part.x += tempV2.x() - tempV.x();
            part.y += tempV2.y() - tempV.y();
            part.z -= tempV2.z() - tempV.z();
        }
    }
    public static void controllerToModelOrientation(PoseStack poseStack) {
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
    }
}
