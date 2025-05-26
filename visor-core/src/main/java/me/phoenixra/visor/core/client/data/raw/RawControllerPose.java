package me.phoenixra.visor.core.client.data.raw;

import lombok.Data;
import me.phoenixra.visor.api.client.data.raw.IRawControllerPose;
import me.phoenixra.visor.api.common.utils.VRMathUtils;
import me.phoenixra.visor.api.common.utils.Vec3History;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;

@Data
public class RawControllerPose implements IRawControllerPose {


    private Matrix4f devicePose = new Matrix4f();
    private Matrix4f aimRotation = new Matrix4f();
    private Matrix4f handRotation = new Matrix4f();

    private Vec3History positionHistory = new Vec3History(301);
    private Vec3History forwardHistory = new Vec3History(301);
    private Vec3History upHistory = new Vec3History(301);


    private Vec3 aimOrigin = new Vec3(0, 0, 0);



    private int deviceIndex;

    private boolean tracking;

    public RawControllerPose() {

    }

    public Matrix4fc getDevicePose() {
        return devicePose;
    }
    public Matrix4f getDevicePoseMutable() {
        return devicePose;
    }

    public @NotNull Matrix4fc getAimRotation() {
        return aimRotation;
    }
    public @NotNull Matrix4f getAimRotationMutable() {
        return aimRotation;
    }

    public Matrix4fc getHandRotation() {
        return handRotation;
    }
    public Matrix4f getHandRotationMutable() {
        return handRotation;
    }

    public Vec3 getAimVector() {
        return VRMathUtils.convertToMcVector(
                aimRotation.transformDirection(
                        VRMathUtils.forwardVector, new Vector3f()
                )
        );
    }

    public Vec3 getHandVector() {
        return VRMathUtils.convertToMcVector(
                handRotation.transformDirection(
                        VRMathUtils.forwardVector, new Vector3f()
                )
        );
    }

}
