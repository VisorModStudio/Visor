package me.phoenixra.visor.api.client.data;

import me.phoenixra.visor.api.common.ControllerHand;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public interface IVRClientPose {
    /**
     *
     * @return HMD component
     */
    @NotNull
    IVRPoseElement getHmd();

    /**
     *
     * @return left eye component
     */
    @NotNull
    IVRPoseElement getEyeLeft();

    /**
     *
     * @return right eye component
     */
    @NotNull
    IVRPoseElement getEyeRight();

    /**
     *
     * @return right controller component
     */
    @NotNull
    IVRPoseElement getControllerRight();

    /**
     *
     * @return left controller component
     */
    @NotNull
    IVRPoseElement getControllerLeft();

    /**
     *
     * @return right hand component
     */
    @NotNull
    IVRPoseElement getHandRight();

    /**
     *
     * @return left hand component
     */
    @NotNull
    IVRPoseElement getHandLeft();


    /**
     *
     * @return origin of player
     */
    @NotNull
    Vec3 getOrigin();

    /**
     *
     * @return world scale
     */
    float getWorldScale();

    /**
     *
     * @return player rotation
     */
    float getRotationYaw();

    /**
     *
     * @return player head pivot
     */
    @NotNull
    Vec3 getHeadPivot();

    /**
     *
     * @return player body yaw
     */
    float getBodyYaw();

    /**
     * Converts a position vector from the coordinate system of the specified origin stage
     * to the coordinate system of this pose instance.
     *
     *
     * @param originStage the stage from which the position is defined
     * @param position the position vector in the coordinate system of the specified origin stage
     * @return the converted position vector
     */
    @NotNull Vec3 convertPosition(@NotNull VRPoseStage originStage,
                                  @NotNull Vec3 position);

    /**
     * Converts a rotation matrix from the coordinate system defined by the specified origin stage
     * to the coordinate system of this pose instance.
     *
     *
     * @param originStage the stage from which the rotation matrix is defined
     * @param rotationMatrix the rotation matrix to convert
     * @return the converted rotation matrix
     */
    @NotNull Matrix4f convertRotation(@NotNull VRPoseStage originStage,
                                      @NotNull Matrix4f rotationMatrix);
    /**
     *
     * @return controller component
     */
    @NotNull
    default IVRPoseElement getController(@NotNull ControllerHand controller) {
        return controller == ControllerHand.MAIN
                ? getControllerRight() : getControllerLeft();
    }

    /**
     *
     * @return controller component
     */
    @NotNull
    default IVRPoseElement getHand(@NotNull ControllerHand hand) {
        return hand == ControllerHand.OFFHAND
                ? this.getHandLeft() : getHandRight();
    }

}
