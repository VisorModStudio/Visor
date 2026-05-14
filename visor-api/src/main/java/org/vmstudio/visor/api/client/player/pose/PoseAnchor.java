package org.vmstudio.visor.api.client.player.pose;

import lombok.Getter;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.common.player.VRPose;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.joml.*;

import java.lang.Math;
import java.util.function.Function;

/**
 * Enum that helps to anchor position and rotation
 * to a {@link VRPose}
 */
public enum PoseAnchor {

    /**
     * Not anchored to anything,
     * only offset may affect.
     */
    NONE(it-> VRPose.EMPTY),

    /**
     * Anchored to HMD
     */
    HMD(VRPlayerPoseClient::getHmd),

    /**
     * Anchored to Main Hand
     */
    MAIN_HAND(VRPlayerPoseClient::getMainHand),

    /**
     * Anchored to Offhand
     */
    OFFHAND(VRPlayerPoseClient::getOffhand);


    @Getter
    private final @NotNull Function<VRPlayerPoseClient, VRPose> supplier;

    PoseAnchor(@NotNull Function<VRPlayerPoseClient, VRPose> supplier){
        this.supplier = supplier;
    }


    public @NotNull VRPose getAnchor(@NotNull VRPlayerPoseClient poseData){
        return this.supplier.apply(poseData);
    }

    public @NotNull Component getName(){
        return Component.translatable("visor.options.enums.PoseAnchor."+name());
    }

    public @NotNull Vector3f anchorPos(@NotNull VRPlayerPoseClient poseData,
                                       @NotNull Vector3fc offset){
        var anchor = getAnchor(poseData);
        float worldScale = poseData.getWorldScale();
        offset = new Vector3f(
                offset.x() * worldScale,
                offset.y() * worldScale,
                offset.z() * worldScale
        );
        if(anchor == VRPose.EMPTY){
            return new Vector3f(offset.x(), offset.y(), offset.z());
        }

        return anchor
                .getCustomVector(offset)
                .add(anchor.getPosition());

    }

    public @NotNull Matrix4f anchorRotation(@NotNull VRPlayerPoseClient poseData,
                                            @NotNull Vector3fc offset){
        var anchor = getAnchor(poseData);
        if(anchor == VRPose.EMPTY){
            return new Matrix4f().rotationZYX(offset.z(), offset.y(), offset.x());
        }
        return anchor.getRotation().mul(
                new Matrix4f().rotationZYX(
                        offset.z(),
                        offset.y(),
                        offset.x()
                ),
                new Matrix4f()
        );
    }


    public @NotNull Matrix4f anchorRotationAim(@NotNull VRPlayerPoseClient poseData,
                                               @NotNull Vector3fc offset,
                                               @NotNull Vector3fc objPosition){
        var anchor = getAnchor(poseData);
        if(anchor == VRPose.EMPTY){
            return new Matrix4f().rotationZYX(offset.z(), offset.y(), offset.x());
        }

        Vector3fc anchorPos = anchor.getPosition();
        Vector3f directionToTarget = new Vector3f(
                objPosition.x() - anchorPos.x(),
                objPosition.y() - anchorPos.y(),
                objPosition.z() - anchorPos.z()
        );
        float rotationX = (float) Math.asin(
                directionToTarget.y() / directionToTarget.length()
        );
        float rotationY = (float) (
                (double) (float) Math.PI +
                        Mth.atan2(
                                directionToTarget.x(),
                                directionToTarget.z()
                        )
        );
        return new Matrix4f().rotationZYX(
                offset.z(),
                rotationY + offset.y(),
                rotationX + offset.x()
        );
    }

    public @NotNull Matrix4f anchorRotation(@NotNull VRPlayerPoseClient poseData,
                                            @NotNull Quaternionfc offset){
        var anchor = getAnchor(poseData);
        Matrix4f offsetMatrix = new Matrix4f().rotation(offset);
        if(anchor == VRPose.EMPTY){
            return offsetMatrix;
        }
        return anchor.getRotation().mul(offsetMatrix, new Matrix4f());
    }


    public @NotNull Matrix4f anchorRotationAim(@NotNull VRPlayerPoseClient poseData,
                                               @NotNull Quaternionfc offset,
                                               @NotNull Vector3fc objPosition){
        var anchor = getAnchor(poseData);
        if(anchor == VRPose.EMPTY){
            return new Matrix4f().rotation(offset);
        }

        Vector3fc anchorPos = anchor.getPosition();
        Vector3f directionToTarget = new Vector3f(
                objPosition.x() - anchorPos.x(),
                objPosition.y() - anchorPos.y(),
                objPosition.z() - anchorPos.z()
        );
        float rotationX = (float) Math.asin(
                directionToTarget.y() / directionToTarget.length()
        );
        float rotationY = (float) (
                (double) (float) Math.PI +
                        Mth.atan2(
                                directionToTarget.x(),
                                directionToTarget.z()
                        )
        );
        return new Matrix4f().rotationZYX(0, rotationY, rotationX)
                .rotate(offset);
    }
    private static @NotNull Vector3f getCustomVector(@NotNull Vector3fc vec,
                                                     @NotNull Matrix4fc rotationMatrix) {
        return rotationMatrix
                .transformDirection(
                        new Vector3f(
                                vec.x(),
                                vec.y(),
                                vec.z()
                        )
                );
    }

}
