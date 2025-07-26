package me.phoenixra.visor.api.client.data;

import me.phoenixra.visor.api.client.render.VRDisplay;
import me.phoenixra.visor.api.common.ControllerHand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * Provides detailed pose information for the player
 * and tracked VR elements
 */
public interface PoseData {

    /**
     * Get the pose data type used
     *
     * @return the pose type
     */
    PoseDataType getType();

    /**
     * Get the head-mounted display (HMD) pose element.
     *
     * @return the HMD element
     */
    @NotNull
    PoseElement getHmd();

    /**
     * Get the left eye pose element.
     *
     * @return the left eye element
     */
    @NotNull
    PoseElement getEyeLeft();

    /**
     * Get the right eye pose element.
     *
     * @return the right eye element
     */
    @NotNull
    PoseElement getEyeRight();

    /**
     * Gets the main hand controller element.
     *
     * @return the main controller element
     */
    @NotNull
    PoseElement getControllerMain();

    /**
     * Gets the offhand controller element.
     *
     * @return the offhand controller element
     */
    @NotNull
    PoseElement getControllerOffhand();

    /**
     * Get the main hand element.
     * <p>
     * Difference from {@link #getControllerMain()}
     * is that this element uses grip pose, which is better
     * for representing hand
     *</p>
     * @return the main hand element
     */
    @NotNull
    PoseElement getHandMain();

    /**
     * Get the offhand element.
     * <p>
     * Difference from {@link #getControllerOffhand()}
     * is that this element uses grip pose, which is better
     * for representing hand
     * </p>
     * @return the offhand element
     */
    @NotNull
    PoseElement getHandOffhand();


    /**
     * Get the player origin in world coordinates.
     *
     * @return the origin
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
     * Converts <code>position</code> from the coordinate system of <code>originStage</code>
     * to the coordinate system of {@link #getType() this pose data instance}.
     *
     *
     * @param originStage the stage from which the position is defined
     * @param position the position vector in the coordinate system of the specified origin stage
     * @return the converted position vector
     */
    @NotNull Vector3f convertPositionFrom(@NotNull PoseDataType originStage,
                                          @NotNull Vector3fc position);

    /**
     * Converts <code>rotationMatrix</code> from the coordinate system of <code>originStage</code>
     * to the coordinate system of {@link #getType() this pose data instance}.
     *
     *
     * @param originStage the stage from which the rotation matrix is defined
     * @param rotationMatrix the rotation matrix to convert
     * @return the converted rotation matrix
     */
    @NotNull Matrix4f convertRotationFrom(@NotNull PoseDataType originStage,
                                          @NotNull Matrix4fc rotationMatrix);

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
