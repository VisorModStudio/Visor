package org.vmstudio.visor.core.client.player.pose.raw;

import lombok.Getter;
import lombok.Setter;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.vmstudio.visor.api.client.player.pose.RawTracker;
import org.vmstudio.visor.api.common.player.VRBodyPartType;
import org.vmstudio.visor.api.common.utils.QuaternionFloatHistory;
import org.vmstudio.visor.api.common.utils.VRMathUtils;
import org.vmstudio.visor.api.common.utils.Vector3fHistory;

public class RawTrackerImpl implements RawTracker {
    private final Matrix4f devicePose = new Matrix4f();

    private final Matrix4f rotation = new Matrix4f();
    @Getter
    private final Vector3fHistory positionHistory = new Vector3fHistory(301);
    @Getter
    private final QuaternionFloatHistory rotationHistory = new QuaternionFloatHistory(301);

    @Getter
    private final VRBodyPartType type;

    @Getter @Setter
    private boolean tracking;

    public RawTrackerImpl(VRBodyPartType type){
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
                .transformDirection(VRMathUtils.BACK_VECTOR, new Vector3f());
    }
}
