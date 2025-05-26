package me.phoenixra.visor.core.client.data.raw;

import lombok.Data;
import me.phoenixra.visor.api.client.data.ControllerHistory;
import me.phoenixra.visor.api.common.utils.VRMathUtils;
import me.phoenixra.visor.api.common.utils.Vec3History;
import net.minecraft.world.phys.Vec3;
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

    private Vec3History positionHistory = new Vec3History(301);
    private Vec3History forwardHistory = new Vec3History(301);
    private Vec3History upHistory = new Vec3History(301);



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

    public Vec3 getAimVector() {
        return VRMathUtils.convertToMcVector(
                aimRotation.transformDirection(
                        VRMathUtils.forwardVector, new Vector3f()
                )
        );
    }

    public Vec3 getGripVector() {
        return VRMathUtils.convertToMcVector(
                gripRotation.transformDirection(
                        VRMathUtils.forwardVector, new Vector3f()
                )
        );
    }



    public Vec3 getAimPosition(){
        return VRMathUtils.convertToMcVector(
                aimPose.getTranslation(new Vector3f())
        );
    }
    public Vec3 getGripPosition(){
        return VRMathUtils.convertToMcVector(
                gripPose.getTranslation(new Vector3f())
        );
    }
}
