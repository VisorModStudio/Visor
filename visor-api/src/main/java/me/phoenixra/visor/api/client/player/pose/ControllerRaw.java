package me.phoenixra.visor.api.client.player.pose;


import me.phoenixra.visor.api.common.utils.VRMathUtils;
import me.phoenixra.visor.api.common.utils.Vector3fHistory;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4fc;
import org.joml.Vector3f;

public interface ControllerRaw {


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

    default @NotNull Vector3f getAimVector() {
        return getAimRotation().transformDirection(
                VRMathUtils.BACK_VECTOR, new Vector3f()
        );
    }

    default @NotNull Vector3f getGripVector() {
        return getGripRotation().transformDirection(
                VRMathUtils.BACK_VECTOR, new Vector3f()
        );
    }



    default @NotNull Vector3f getAimPosition(){
        return getAimPose().getTranslation(new Vector3f());
    }

    default @NotNull Vector3f getGripPosition(){
        return getGripPose().getTranslation(new Vector3f());
    }

    /**
     * If controller is tracked by VR provider
     *
     * @return if tracked
     */
    boolean isTracking();
}
