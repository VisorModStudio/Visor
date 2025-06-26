package me.phoenixra.visor.api.client.data;

import lombok.Getter;
import me.phoenixra.visor.api.VisorAPI;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.joml.*;

import java.lang.Math;
import java.util.function.Function;

/**
 * Enum that helps to anchor position and rotation
 * to a {@link PoseElement}
 */
public enum PoseAnchor {

    /**
     * Not anchored to anything,
     * only offset may affect.
     */
    NONE(it-> PoseElement.EMPTY),

    /**
     * Anchored to HMD
     */
    HMD(PoseData::getHmd),

    /**
     * Anchored to Main Controller
     */
    MAIN_HAND(PoseData::getControllerMain),

    /**
     * Anchored to Offhand Controller
     */
    OFFHAND(PoseData::getControllerOffhand);


    @Getter
    private final @NotNull Function<PoseData, PoseElement> supplier;

    PoseAnchor(@NotNull Function<PoseData, PoseElement> supplier){
        this.supplier = supplier;
    }


    public @NotNull PoseElement getAnchor(@NotNull PoseData poseData){
        return this.supplier.apply(poseData);
    }

    public @NotNull Component getName(){
        return Component.translatable("visor.enums.ModelViewAnchor."+name());
    }

    public @NotNull Vector3f anchorPos(@NotNull PoseData poseData,
                                       @NotNull Vector3fc offset){
        var anchor = getAnchor(poseData);
        float worldScale = poseData.getWorldScale();
        offset = new Vector3f(
                offset.x() * worldScale,
                offset.y() * worldScale,
                offset.z() * worldScale
        );
        if(anchor == PoseElement.EMPTY){
            return new Vector3f(offset.x(), offset.y(), offset.z());
        }

        return anchor
                .getCustomVector(offset)
                .add(anchor.getPosition());

    }

    public @NotNull Matrix4f anchorRotation(@NotNull PoseData poseData,
                                            @NotNull Vector3fc offset){
        var anchor = getAnchor(poseData);
        if(anchor == PoseElement.EMPTY){
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


    public @NotNull Matrix4f anchorRotationAim(@NotNull PoseData poseData,
                                               @NotNull Vector3fc offset,
                                               @NotNull Vector3fc objPosition){
        var anchor = getAnchor(poseData);
        if(anchor == PoseElement.EMPTY){
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


    public @NotNull Vector3f reverseAnchoredRotation(@NotNull Matrix4fc elementRotation,
                                                     @NotNull Matrix4fc objRotation) {

        Matrix4f invController = elementRotation.invert(new Matrix4f());


        Matrix4f matrix4f = invController.mul(objRotation);

        float offsetY = (float) Math.asin(-matrix4f.m20());
        float offsetZ = (float) Mth.atan2(matrix4f.m10(), matrix4f.m00());
        float offsetX = (float) Mth.atan2(matrix4f.m21(), matrix4f.m22());

        return new Vector3f(offsetX, offsetY, offsetZ);
    }




    public static Vector3f getAnchorPos(@NotNull Vector3fc anchorPosition,
                                        @NotNull Matrix4fc anchorRotation,
                                        @NotNull Vector3fc offset){
        PoseData renderPose = VisorAPI.client().getPlayer()
                .getPose(PoseDataType.RENDER);
        float worldScale = renderPose.getWorldScale();

        offset = new Vector3f(
                offset.x() * worldScale,
                offset.y() * worldScale,
                offset.z() * worldScale
        );
        return getCustomVector(
                offset,
                anchorRotation
        ).add(anchorPosition);
    }

    public static Matrix4f getAnchorRotation(@NotNull Matrix4fc anchorRotation,
                                             @NotNull Vector3fc offset){

        return anchorRotation.mul(
                new Matrix4f().rotationZYX(
                        offset.z(), offset.y(), offset.x()
                ),
                new Matrix4f()
        );
    }

    public static Matrix4f getAnchorRotationAimed(@NotNull Vector3fc objPosition,
                                                  @NotNull Vector3fc anchorPosition,
                                                  @NotNull Vector3fc offset){


        Vector3f directionToTarget = new Vector3f(
                objPosition.x() - anchorPosition.x(),
                objPosition.y() - anchorPosition.y(),
                objPosition.z() - anchorPosition.z()
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
