package me.phoenixra.visor.api.client.data;


import me.phoenixra.visor.api.common.utils.VRMathUtils;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * Represents a VR element's spatial data, including
 * position, direction, and rotation in world space.
 */
public interface PoseElement {

    /**
     * Am empty element with zero position, zero direction,
     * and identity rotation.
     */
    PoseElement EMPTY = new PoseElement() {
        @Override public @NotNull Vector3fc getPosition() {return VRMathUtils.ZERO_VECTOR;}
        @Override public @NotNull Vector3fc getDirection() {return VRMathUtils.ZERO_VECTOR;}
        @Override public @NotNull Vector3f getCustomVector(@NotNull Vector3fc vec) {return new Vector3f(vec);}
        @Override public @NotNull Vector3f reverseCustomVector(@NotNull Vector3fc customVec) {return new Vector3f(customVec);}
        @Override public @NotNull Matrix4fc getRotation() {return VRMathUtils.EMPTY_MATRIX;}
        @Override public float getYaw() {return 0;}
        @Override public float getPitch() {return 0;}
        @Override public float getRoll() {return 0;}
    };

    /**
     *
     * @return position of the component
     */
    @NotNull
    Vector3fc getPosition();
    /**
     *
     * @return direction of the component
     */
    @NotNull
    Vector3fc getDirection();


    /**
     * Get custom vector from component
     * @return vector
     */
    @NotNull
    Vector3f getCustomVector(@NotNull Vector3fc vec);

    /**
     * Reverse {@link PoseElement#getCustomVector(Vector3fc)}
     * @param customVec vec
     * @return original vector
     */
    @NotNull Vector3f reverseCustomVector(@NotNull Vector3fc customVec);

    /**
     *
     * @return rotation matrix of the component
     */
    @NotNull
    Matrix4fc getRotation();


    /**
     *
     * @return yaw of the component
     */
    float getYaw();
    /**
     *
     * @return pitch of the component
     */
    float getPitch();

    /**
     *
     * @return roll of the component
     */
    float getRoll();


}
