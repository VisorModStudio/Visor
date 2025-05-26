package me.phoenixra.visor.api.client.data.raw;


import me.phoenixra.visor.api.common.utils.Vec3History;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4fc;

public interface IRawControllerPose {

    @NotNull
    Matrix4fc getAimRotation();

    @NotNull
    Vec3History getPositionHistory();
    @NotNull
    Vec3History getForwardHistory();
    @NotNull
    Vec3History getUpHistory();


    /**
     * Get device index in OpenVR
     *
     * @return device index
     */
    int getDeviceIndex();

    /**
     * If controller is tracked by OpenVR
     * and pose data is valid
     *
     * @return if tracked
     */
    boolean isTracking();
}
