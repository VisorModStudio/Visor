package org.vmstudio.visor.core.client.player.pose.raw;

import lombok.Getter;
import lombok.Setter;
import me.phoenixra.atumvr.api.enums.EyeType;
import org.vmstudio.visor.api.client.player.pose.RawHmd;
import org.vmstudio.visor.api.common.utils.QuaternionFloatHistory;
import org.vmstudio.visor.api.common.utils.VRMathUtils;
import org.vmstudio.visor.api.common.utils.Vector3fHistory;
import org.joml.*;

public class RawHmdImpl implements RawHmd {

    private final Matrix4f devicePose = new Matrix4f();

    private final Matrix4f rotation = new Matrix4f();

    private final Matrix4f leftEyePose = new Matrix4f();

    private final Matrix4f rightEyePose = new Matrix4f();
    @Getter
    private final Vector3fHistory positionHistory = new Vector3fHistory(301);
    @Getter
    private final Vector3fHistory pivotHistory = new Vector3fHistory(301);
    @Getter
    private final QuaternionFloatHistory rotationHistory = new QuaternionFloatHistory(301);


    @Getter @Setter
    private Vector3fc velocity = new Vector3f(0,0,0);

    @Getter @Setter
    private Vector3fc angularVelocity = new Vector3f(0,0,0);

    @Getter @Setter
    private boolean tracking;



    public Matrix4fc getDevicePose() {
        return devicePose;
    }
    public Matrix4f getDevicePoseMutable(){
        return devicePose;
    }

    public Matrix4fc getRotation() {
        return rotation;
    }
    public Matrix4f getRotationMutable(){
        return rotation;
    }

    public Matrix4f getLeftEyePoseMutable(){
        return leftEyePose;
    }

    public Matrix4f getRightEyePoseMutable(){
        return rightEyePose;
    }

    public Vector3f getHeadsetPosition() {
        return this.devicePose.getTranslation(new Vector3f());
    }

    public Vector3f getEyePosition(EyeType eye) {
        Matrix4f eyePose;

        if (eye == EyeType.LEFT) {
            eyePose = this.leftEyePose;
        }  else {
            eyePose = this.rightEyePose;
        }
        return eyePose.getTranslation(new Vector3f());
    }

    public Matrix4fc getEyeRotation(EyeType eye) {
        Matrix4f eyePose;

        if (eye == EyeType.LEFT) {
            eyePose = this.leftEyePose;
        } else {
            eyePose = this.rightEyePose;
        }

        return new Matrix4f().rotate(eyePose.getNormalizedRotation(new Quaternionf()));

    }

    public Vector3f getVector() {
        return this.rotation
                .transformDirection(VRMathUtils.BACK_VECTOR, new Vector3f());
    }

}
