package me.phoenixra.visor.api.client.data;


import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4fc;
import org.joml.Vector3f;

public interface PoseElement {

    /**
     *
     * @return position of the component
     */
    @NotNull
    Vec3 getPosition();

    /**
     *
     * @return direction of the component
     */
    @NotNull
    Vec3 getDirection();

    /**
     * Get custom vector from component
     * @return vector
     */
    @NotNull
    default Vec3 getCustomVector(@NotNull Vec3 vec){
        return getCustomVector(
                new Vector3f(
                        (float) vec.x,
                        (float) vec.y,
                        (float) vec.z
                )
        );
    }
    /**
     * Get custom vector from component
     * @return vector
     */
    @NotNull
    Vec3 getCustomVector(@NotNull Vector3f vec);

    /**
     * Reverse {@link PoseElement#getCustomVector(Vec3)}
     * @param customVec vec
     * @return original vector
     */
    @NotNull Vector3f reverseCustomVector(@NotNull Vec3 customVec);

    /**
     *
     * @return rotation matrix of the component
     */
    @NotNull
    Matrix4fc getRotationMatrix();


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
