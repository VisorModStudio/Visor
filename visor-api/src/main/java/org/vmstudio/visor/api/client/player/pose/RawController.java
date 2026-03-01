package org.vmstudio.visor.api.client.player.pose;


import org.vmstudio.visor.api.common.utils.Vector3fHistory;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4fc;
import org.joml.Vector3f;

/**
 * Holds pose data received from VR about Controller.
 * Updated every frame, at the beginning of game loop
 * <p>
 *     Useful when you need to use most recent data directly from VR
 * </p>
 */
public interface RawController {


    @NotNull
    Vector3fHistory getPositionHistory();
    @NotNull
    Vector3fHistory getForwardHistory();
    @NotNull
    Vector3fHistory getUpHistory();



    @NotNull Matrix4fc getAimPose();

    @NotNull Matrix4fc getAimRotation();


    @NotNull Matrix4fc getGripPose();

    @NotNull Matrix4fc getGripRotation();

    @NotNull Vector3f getAimVector();

    @NotNull Vector3f getGripVector();



    @NotNull Vector3f getAimPosition();

    @NotNull Vector3f getGripPosition();

    /**
     * If controller is tracked by VR provider
     *
     * @return if tracked
     */
    boolean isTracking();
}
