package org.vmstudio.visor.api.common.player;


import org.vmstudio.visor.api.common.utils.VRMathUtils;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.function.Supplier;

/**
 * Represents a VR spatial data (controller, hmd, eyes, body parts etc.)
 */
public interface VRPose {

    /**
     * Am empty element with zero position, zero direction,
     * and identity rotation.
     */
    VRPose EMPTY = new VRPose() {
        @Override public void update(@NotNull Vector3fc rawPosition, @NotNull Matrix4fc rawMatrix, @NotNull Vector3fc rawDirection, @NotNull Vector3fc origin, float rotationY, float worldScale) {}
        @Override public void updateModifiers(@NotNull Vector3fc newOrigin, float newRotationY, float newWorldScale) {}
        @Override public void onOriginChanged(Vector3fc newOrigin) {}
        @Override public void copyFrom(@NotNull VRPose pose) {}
        @Override public @NotNull Vector3fc getPosition() {return VRMathUtils.ZERO_VECTOR;}
        @Override public @NotNull Vector3fc getRelativePosition() {return VRMathUtils.ZERO_VECTOR;}
        @Override public @NotNull Vector3fc getDirection() {return VRMathUtils.ZERO_VECTOR;}
        @Override public @NotNull Vector3f getCustomVector(@NotNull Vector3fc vec) {return new Vector3f(vec);}
        @Override public @NotNull Vector3f reverseCustomVector(@NotNull Vector3fc customVec) {return new Vector3f(customVec);}
        @Override public @NotNull Matrix4fc getRotation() {return VRMathUtils.EMPTY_MATRIX;}
        @Override public @NotNull Matrix4fc getInvertedRotation() {return VRMathUtils.EMPTY_MATRIX;}
        @Override public float getYaw() {return 0;}
        @Override public float getPitch() {return 0;}
        @Override public float getRoll() {return 0;}
        @Override public @NotNull Vector3fc getRawPosition() {return VRMathUtils.ZERO_VECTOR;}
        @Override public @NotNull Vector3fc getRawDirection() {return VRMathUtils.ZERO_VECTOR;}
        @Override public @NotNull Matrix4fc getRawRotation() {return VRMathUtils.EMPTY_MATRIX;}
        @Override public @NotNull Vector3fc getUsedOrigin() {return VRMathUtils.ZERO_VECTOR;}
        @Override public float getUsedRotationY() {return 0;}
        @Override public float getUsedWorldScale() {return 0;}
    };

    static @NotNull VRPose create(){
        return Instance.supplier.get();
    }




    void update(@NotNull Vector3fc rawPosition,
                @NotNull Matrix4fc rawMatrix,
                @NotNull Vector3fc rawDirection,
                @NotNull Vector3fc origin,
                float rotationY, float worldScale);

    void updateModifiers(@NotNull Vector3fc newOrigin,
                         float newRotationY,
                         float newWorldScale);

    void onOriginChanged(Vector3fc newOrigin);

    void copyFrom(@NotNull VRPose pose);



    /**
     *
     * @return position
     */
    @NotNull
    Vector3fc getPosition();

    /**
     *
     * @return position as vec3
     */
    @NotNull
    default Vec3 getPositionVec3(){
        return new Vec3((Vector3f) getPosition());
    }

    /**
     *
     * @return relative position(no origin)
     */
    @NotNull
    Vector3fc getRelativePosition();

    /**
     *
     * @return relative position(no origin) as vec3
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
     * @return direction as vec3
     */
    @NotNull
    default Vec3 getDirectionVec3(){
        return new Vec3((Vector3f) getDirection());
    }


    /**
     * Get custom vector
     * @return vector
     */
    @NotNull
    Vector3f getCustomVector(@NotNull Vector3fc vec);

    /**
     * Get custom vector
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
     * @return rotation matrix
     */
    @NotNull
    Matrix4fc getRotation();

    /**
     *
     * @return inverted rotation matrix
     */
    @NotNull
    Matrix4fc getInvertedRotation();



    /**
     * Get yaw in radians
     *
     * @return yaw
     */
    float getYaw();

    /**
     * Get yaw in degrees
     *
     * @return yaw in degrees
     */
    default float getYawDegrees(){
        return (float) Math.toDegrees(getYaw());
    }

    /**
     * Get pitch in radians
     *
     * @return pitch
     */
    float getPitch();

    /**
     * Get pitch in degrees
     *
     * @return yaw in degrees
     */
    default float getPitchDegrees(){
        return (float) Math.toDegrees(getPitch());
    }

    /**
     * Get roll in radians
     *
     * @return roll
     */
    float getRoll();

    /**
     * Get roll in degrees
     *
     * @return roll in degrees
     */
    default float getRollDegrees(){
        return (float) Math.toDegrees(getRoll());
    }




    /**
     * Get raw position
     *
     * @return position
     */
    @NotNull
    Vector3fc getRawPosition();

    /**
     * Get raw direction
     *
     * @return direction of the element
     */
    @NotNull
    Vector3fc getRawDirection();

    /**
     * Get raw rotation
     *
     * @return rotation matrix
     */
    @NotNull
    Matrix4fc getRawRotation();


    /**
     * Get used origin
     *
     * @return origin
     */
    @NotNull
    Vector3fc getUsedOrigin();

    /**
     * Get used rotation y
     *
     * @return rotation y
     */
    float getUsedRotationY();

    /**
     * Get used world scale
     *
     * @return world scale
     */
    float getUsedWorldScale();

    class Instance{
        public static Supplier<VRPose> supplier;
    }
}
