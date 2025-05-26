package me.phoenixra.visor.core.client.data.raw;

import lombok.Getter;
import lombok.Setter;
import me.phoenixra.atumvr.api.enums.EyeType;
import me.phoenixra.visor.api.client.data.raw.IRawHmdPose;
import me.phoenixra.visor.api.common.utils.QuaternionFloatHistory;
import me.phoenixra.visor.api.common.utils.VRMathUtils;
import me.phoenixra.visor.api.common.utils.Vec3History;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class RawHmdPose implements IRawHmdPose {

    private final Matrix4f devicePose = new Matrix4f();

    private final Matrix4f rotation = new Matrix4f();

    private final Matrix4f leftEyePose = new Matrix4f();

    private final Matrix4f rightEyePose = new Matrix4f();
    @Getter
    private final Vec3History positionHistory = new Vec3History(301);
    @Getter
    private final Vec3History pivotHistory = new Vec3History(301);
    @Getter
    private final QuaternionFloatHistory rotationHistory = new QuaternionFloatHistory(301);


    @Getter @Setter
    private Vec3 velocity = new Vec3(0,0,0);

    @Getter @Setter
    private Vec3 angularVelocity = new Vec3(0,0,0);

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

    public Vec3 getCenterEyePosition() {
        return VRMathUtils.convertToMcVector(
                this.devicePose.getTranslation(new Vector3f())
        );
    }

    public Vec3 getEyePosition(EyeType eye) {
        Matrix4f eyePose;

        if (eye == EyeType.LEFT) {
            eyePose = this.leftEyePose;
        }  else {
            eyePose = this.rightEyePose;
        }
        return VRMathUtils.convertToMcVector(
                eyePose.getTranslation(new Vector3f())
        );
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

    public Vec3 getVector() {
        return VRMathUtils.convertToMcVector(
                this.rotation
                .transformDirection(VRMathUtils.forwardVector, new Vector3f())
        );
    }

}
