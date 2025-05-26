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
    PoseElement getHmd();

    /**
     *
     * @return left eye component
     */
    @NotNull
    PoseElement getEyeLeft();

    /**
     *
     * @return right eye component
     */
    @NotNull
    PoseElement getEyeRight();

    /**
     *
     * @return right controller component
     */
    @NotNull
    PoseElement getControllerRight();

    /**
     *
     * @return left controller component
     */
    @NotNull
    PoseElement getControllerLeft();

    /**
     *
     * @return right hand component
     */
    @NotNull
    PoseElement getHandRight();

    /**
     *
     * @return left hand component
     */
    @NotNull
    PoseElement getHandLeft();


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
    @NotNull Vec3 convertPosition(@NotNull PoseType originStage,
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
    @NotNull Matrix4f convertRotation(@NotNull PoseType originStage,
                                      @NotNull Matrix4f rotationMatrix);
    /**
     *
     * @return controller component
     */
    @NotNull
    default PoseElement getController(@NotNull ControllerHand controller) {
        return controller == ControllerHand.MAIN
                ? getControllerRight() : getControllerLeft();
    }

    /**
     *
     * @return controller component
     */
    @NotNull
    default PoseElement getHand(@NotNull ControllerHand hand) {
        return hand == ControllerHand.OFFHAND
                ? this.getHandLeft() : getHandRight();
    }

}
