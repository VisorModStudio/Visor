package me.phoenixra.visor.api.client.data;


import me.phoenixra.visor.api.common.utils.Vec3History;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4fc;

public interface ControllerHistory {

    @NotNull
    Matrix4fc getAimRotation();

    @NotNull
    Vec3History getPositionHistory();
    @NotNull
    Vec3History getForwardHistory();
    @NotNull
    Vec3History getUpHistory();



    /**
     * If controller is tracked by VR provider
     *
     * @return if tracked
     */
    boolean isTracking();
}
