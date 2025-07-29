package me.phoenixra.visor.core.client.data.raw;

import lombok.Data;
import me.phoenixra.visor.api.client.data.ControllerRaw;
import me.phoenixra.visor.api.common.utils.Vector3fHistory;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

@Data
public class RawController implements ControllerRaw {


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

    public @NotNull Matrix4fc getGripRotation() {
        return gripRotation;
    }
    public Matrix4f getGripRotationMutable() {
        return gripRotation;
    }


}
