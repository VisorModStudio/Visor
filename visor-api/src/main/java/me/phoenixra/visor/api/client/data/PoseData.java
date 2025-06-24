package me.phoenixra.visor.api.client.data;

import me.phoenixra.visor.api.client.render.VRDisplay;
import me.phoenixra.visor.api.common.ControllerHand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public interface PoseData {
    PoseType getType();

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
    PoseElement getControllerMain();

    /**
     *
     * @return left controller component
     */
    @NotNull
    PoseElement getControllerOffhand();

    /**
     *
     * @return right hand component
     */
    @NotNull
    PoseElement getHandMain();

    /**
     *
     * @return left hand component
     */
    @NotNull
    PoseElement getHandOffhand();


    /**
     *
     * @return origin of player
     */
    @NotNull
    Vector3fc getOrigin();

    /**
     *
     * @return world scale
     */
    float getWorldScale();

    /**
     *
     * @return player rotation
     */
    float getRotationY();

    /**
     *
     * @return player head pivot
     */
    @NotNull
    Vector3fc getHeadPivot();

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
    @NotNull Vector3f convertPosition(@NotNull PoseType originStage,
                                        @NotNull Vector3fc position);

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
                ? getControllerMain() : getControllerOffhand();
    }

    /**
     *
     * @return controller component
     */
    @NotNull
    default PoseElement getHand(@NotNull ControllerHand hand) {
        return hand == ControllerHand.MAIN
                ? getHandMain() : this.getHandOffhand();
    }

    @NotNull
    PoseElement getElementForDisplay(@Nullable VRDisplay display);

}
