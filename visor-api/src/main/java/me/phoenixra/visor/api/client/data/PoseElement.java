package me.phoenixra.visor.api.client.data;


import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public interface PoseElement {

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
