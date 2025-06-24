package me.phoenixra.visor.api.client.data;


import me.phoenixra.visor.api.common.utils.Vector3fHistory;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4fc;

public interface ControllerHistory {

    @NotNull
    Matrix4fc getAimRotation();

    @NotNull
    Vector3fHistory getPositionHistory();
    @NotNull
    Vector3fHistory getForwardHistory();
    @NotNull
    Vector3fHistory getUpHistory();



    /**
     * If controller is tracked by VR provider
     *
     * @return if tracked
     */
    boolean isTracking();
}
