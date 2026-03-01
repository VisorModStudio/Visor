package org.vmstudio.visor.core.client.player.pose.raw;

import lombok.Data;
import org.vmstudio.visor.api.client.player.pose.RawController;
import org.vmstudio.visor.api.common.utils.VRMathUtils;
import org.vmstudio.visor.api.common.utils.Vector3fHistory;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;

@Data
public class RawControllerImpl implements RawController {


    private Matrix4f aimPose = new Matrix4f();
    private Matrix4f aimRotation = new Matrix4f();

    private Matrix4f gripPose = new Matrix4f();
    private Matrix4f gripRotation = new Matrix4f();

    private Vector3fHistory positionHistory = new Vector3fHistory(301);
    private Vector3fHistory forwardHistory = new Vector3fHistory(301);
    private Vector3fHistory upHistory = new Vector3fHistory(301);



    private boolean tracking;

    public RawControllerImpl() {

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


     @NotNull
     public Vector3f getAimVector() {
        return getAimRotation().transformDirection(
                VRMathUtils.BACK_VECTOR, new Vector3f()
        );
    }

     @NotNull
     public Vector3f getGripVector() {
        return getGripRotation().transformDirection(
                VRMathUtils.BACK_VECTOR, new Vector3f()
        );
    }



     @NotNull
     public Vector3f getAimPosition(){
        return getAimPose().getTranslation(new Vector3f());
    }

     @NotNull
     public Vector3f getGripPosition(){
        return getGripPose().getTranslation(new Vector3f());
    }


}
