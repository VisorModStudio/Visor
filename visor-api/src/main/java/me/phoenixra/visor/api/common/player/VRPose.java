package me.phoenixra.visor.api.common.player;


import me.phoenixra.visor.api.common.utils.VRMathUtils;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * Represents a VR element's (controller, hmd, eyes etc.) spatial data, including
 * position, direction, and rotation in world space.
 */
public interface VRPose {

    /**
     * Am empty element with zero position, zero direction,
     * and identity rotation.
     */
    VRPose EMPTY = new VRPose() {
        @Override public @NotNull Vector3fc getPosition() {return VRMathUtils.ZERO_VECTOR;}
        @Override public @NotNull Vector3fc getRelativePosition() {return VRMathUtils.ZERO_VECTOR;}
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
     * @return position of the element
     */
    @NotNull
    Vector3fc getPosition();

    /**
     *
     * @return position of the element as vec3
     */
    @NotNull
    default Vec3 getPositionVec3(){
        return new Vec3((Vector3f) getPosition());
    }

    /**
     *
     * @return relative position(no origin) of the element
     */
    @NotNull
    Vector3fc getRelativePosition();

    /**
     *
     * @return relative position(no origin) of the element as vec3
     */
    @NotNull
    default Vec3 getRelativePositionVec3(){
        return new Vec3((Vector3f) getRelativePosition());
    }

    /**
     *
     * @return direction of the element
     */
    @NotNull
    Vector3fc getDirection();

    /**
     *
     * @return direction of the element as vec3
     */
    @NotNull
    default Vec3 getDirectionVec3(){
        return new Vec3((Vector3f) getDirection());
    }


    /**
     * Get custom vector from element
     * @return vector
     */
    @NotNull
    Vector3f getCustomVector(@NotNull Vector3fc vec);

    /**
     * Get custom vector from element
     * @return vector as vec3
     */
    @NotNull
    default Vec3 getCustomVector3(@NotNull Vector3fc vec){
        return new Vec3(getCustomVector(vec));
    }

    /**
     * Reverse {@link VRPose#getCustomVector(Vector3fc)}
     * @param customVec vec
     * @return original vector
     */
    @NotNull Vector3f reverseCustomVector(@NotNull Vector3fc customVec);

    /**
     * Reverse {@link VRPose#getCustomVector(Vector3fc)}
     * @param customVec vec
     * @return original vector as vec3
     */
    default @NotNull Vec3 reverseCustomVector3(@NotNull Vector3fc customVec){
        return new Vec3(reverseCustomVector(customVec));
    }


    /**
     *
     * @return rotation matrix of the element
     */
    @NotNull
    Matrix4fc getRotation();


    /**
     * Get yaw in radians
     *
     * @return yaw of the element
     */
    float getYaw();

    /**
     * Get yaw in degrees
     *
     * @return yaw of the element
     */
    default float getYawDegrees(){
        return (float) Math.toDegrees(getYaw());
    }

    /**
     * Get pitch in radians
     *
     * @return pitch of the element
     */
    float getPitch();

    /**
     * Get pitch in degrees
     *
     * @return yaw of the element
     */
    default float getPitchDegrees(){
        return (float) Math.toDegrees(getPitch());
    }

    /**
     * Get roll in radians
     *
     * @return roll of the element
     */
    float getRoll();

    /**
     * Get roll in degrees
     *
     * @return yaw of the element
     */
    default float getRollDegrees(){
        return (float) Math.toDegrees(getRoll());
    }

}
