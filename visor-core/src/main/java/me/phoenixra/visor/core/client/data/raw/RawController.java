package me.phoenixra.visor.core.client.data.raw;

import lombok.Data;
import me.phoenixra.visor.api.client.data.ControllerHistory;
import me.phoenixra.visor.api.common.utils.VRMathUtils;
import me.phoenixra.visor.api.common.utils.Vector3fHistory;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;

@Data
public class RawController implements ControllerHistory {


    private Matrix4f aimPose = new Matrix4f();
    private Matrix4f aimRotation = new Matrix4f();

    private Matrix4f gripPose = new Matrix4f();
    private Matrix4f gripRotation = new Matrix4f();

    private Vector3fHistory positionHistory = new Vector3fHistory(301);
    private Vector3fHistory forwardHistory = new Vector3fHistory(301);
    private Vector3fHistory upHistory = new Vector3fHistory(301);



    private boolean tracking;

    public RawController() {

    }

    public @NotNull Matrix4fc getAimPose() {
        return aimPose;
    }
    public @NotNull Matrix4f getAimPoseMutable() {
        return aimPose;
    }

    public @NotNull Matrix4fc getAimRotation() {
        return aimRotation;
    }
    public @NotNull Matrix4f getAimRotationMutable() {
        return aimRotation;
    }


    public @NotNull Matrix4fc getGripPose() {
        return gripPose;
    }
    public @NotNull Matrix4f getGripPoseMutable() {
        return gripPose;
    }

    public Matrix4fc getGripRotation() {
        return gripRotation;
    }
    public Matrix4f getGripRotationMutable() {
        return gripRotation;
    }

    public Vector3f getAimVector() {
        return  aimRotation.transformDirection(
                VRMathUtils.FORWARD_VECTOR, new Vector3f()
        );
    }

    public Vector3f getGripVector() {
        return gripRotation.transformDirection(
                VRMathUtils.FORWARD_VECTOR, new Vector3f()
        );
    }



    public Vector3f getAimPosition(){
        return aimPose.getTranslation(new Vector3f());
    }
    public Vector3f getGripPosition(){
        return gripPose.getTranslation(new Vector3f());
    }
}
