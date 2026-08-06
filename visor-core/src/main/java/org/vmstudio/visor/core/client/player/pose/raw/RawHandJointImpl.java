package org.vmstudio.visor.core.client.player.pose.raw;

import lombok.Getter;
import lombok.Setter;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.vmstudio.visor.api.client.player.pose.RawHandJoint;
import org.vmstudio.visor.api.common.player.VRHandJointType;
import org.vmstudio.visor.api.common.utils.VRMathUtils;

public class RawHandJointImpl implements RawHandJoint {
    private final Matrix4f devicePose = new Matrix4f();

    private final Matrix4f rotation = new Matrix4f();

    @Getter
    private final VRHandJointType type;

    @Getter @Setter
    private boolean tracking;

    @Getter @Setter
    private float radius;

    public RawHandJointImpl(VRHandJointType type){
        this.type = type;
    }


    public Matrix4fc getPose() {
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


    public Vector3f getPosition() {
        return this.devicePose.getTranslation(new Vector3f());
    }

    public Vector3f getVector() {
        return this.rotation
                .transformDirection(VRMathUtils.FORWARD_VECTOR, new Vector3f());
    }
}
