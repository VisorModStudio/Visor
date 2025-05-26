package me.phoenixra.visor.core.client.data;

import lombok.Getter;
import me.phoenixra.atumvr.api.enums.EyeType;
import me.phoenixra.visor.api.client.data.IVRClientPose;
import me.phoenixra.visor.api.client.data.IVRPoseElement;
import me.phoenixra.visor.api.client.data.VRPoseStage;
import me.phoenixra.visor.api.client.render.VRDisplay;
import me.phoenixra.visor.api.common.utils.VRMathUtils;
import me.phoenixra.visor.core.client.data.raw.RawControllerPose;
import me.phoenixra.visor.core.client.data.raw.RawHmdPose;
import me.phoenixra.visor.core.client.settings.VRClientSettings;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.List;

import me.phoenixra.visor.core.client.ClientContext;

@Getter
public class VRClientPose implements IVRClientPose {

    private final VRPoseStage poseStage;

    protected final VRPoseElement hmd;
    protected final VRPoseElement eyeLeft;
    protected final VRPoseElement eyeRight;

    protected final VRPoseElement controllerLeft;
    protected final VRPoseElement controllerRight;

    protected final VRPoseElement handLeft;
    protected final VRPoseElement handRight;

    protected final VRPoseElement thirdPersonCamera;

    private final List<VRPoseElement> elements;

    private Vec3 origin;
    private float rotationYaw;
    private float worldScale;

    private float bodyYaw;
    private Vec3 headPivot;

    public VRClientPose(VRPoseStage poseStage,
                        Vec3 origin,
                        float walkMul,
                        float worldScale,
                        float rotationYaw) {
        this.poseStage = poseStage;

        this.hmd = new VRPoseElement();
        this.eyeLeft = new VRPoseElement();
        this.eyeRight = new VRPoseElement();

        this.controllerLeft = new VRPoseElement();
        this.controllerRight = new VRPoseElement();
        this.handLeft = new VRPoseElement();
        this.handRight = new VRPoseElement();

        this.thirdPersonCamera = new VRPoseElement();


        elements = List.of(
                hmd,
                eyeLeft, eyeRight,
                controllerLeft, controllerRight,
                handLeft, handRight,
                thirdPersonCamera
        );

        update(origin, walkMul, worldScale, rotationYaw);

    }

    protected void update(Vec3 origin,
                          float walkMul,
                          float worldScale,
                          float rotationY){

        RawControllerPose dataLeft = ClientContext.rawPlayerPose.getControllerLeftData();
        RawControllerPose dataRight = ClientContext.rawPlayerPose.getControllerRightData();
        this.origin = origin;
        this.worldScale = worldScale;
        this.rotationYaw = rotationY;
        RawHmdPose hmdData = ClientContext.rawPlayerPose.getHmdData();
        Vec3 centerEyePosition = hmdData.getCenterEyePosition();
        Vec3 centerPosition = new Vec3(
                centerEyePosition.x * (double) walkMul,
                centerEyePosition.y,
                centerEyePosition.z * (double) walkMul
        );

        this.hmd.update(
                this.origin,
                this.rotationYaw,
                this.worldScale,
                hmdData.getRotation(),
                centerPosition, hmdData.getVector()
        );
        this.eyeLeft.update(
                this.origin,
                this.rotationYaw,
                this.worldScale,
                hmdData.getEyeRotation(EyeType.LEFT),
                hmdData.getEyePosition(EyeType.LEFT).subtract(centerEyePosition).add(centerPosition),
                hmdData.getVector()
        );
        this.eyeRight.update(
                this.origin,
                this.rotationYaw,
                this.worldScale,
                hmdData.getEyeRotation(EyeType.RIGHT),
                hmdData.getEyePosition(EyeType.RIGHT).subtract(centerEyePosition).add(centerPosition),
                hmdData.getVector()
        );

        this.controllerLeft.update(
                this.origin,
                this.rotationYaw,
                this.worldScale,
                dataLeft.getAimRotation(),
                dataLeft.getAimOrigin().subtract(centerEyePosition).add(centerPosition),
                dataLeft.getAimVector()
        );
        this.controllerRight.update(
                this.origin,
                this.rotationYaw,
                this.worldScale,
                dataRight.getAimRotation(),
                dataRight.getAimOrigin().subtract(centerEyePosition).add(centerPosition),
                dataRight.getAimVector()
        );
        this.handLeft.update(
                this.origin,
                this.rotationYaw,
                this.worldScale,
                dataLeft.getHandRotation(),
                dataLeft.getAimOrigin().subtract(centerEyePosition).add(centerPosition),
                dataLeft.getHandVector()
        );
        this.handRight.update(
                this.origin,
                this.rotationYaw,
                this.worldScale,
                dataRight.getHandRotation(),
                dataRight.getAimOrigin().subtract(centerEyePosition).add(centerPosition),
                dataRight.getHandVector()
        );


        Vec3 camPos = new Vec3(
                VRClientSettings.getFixedCameraPosX(),
                VRClientSettings.getFixedCameraPosY(),
                VRClientSettings.getFixedCameraPosZ()
        );
        Matrix4f camRot = (new Matrix4f().set(VRClientSettings.getFixedCameraRotation())).transpose();
        Vec3 camDir = VRMathUtils.convertToMcVector(
                camRot.transformDirection(VRMathUtils.forwardVector, new Vector3f())
        );
        this.thirdPersonCamera.update(
                this.origin,
                this.rotationYaw,
                this.worldScale,
                camRot,
                camPos.subtract(centerEyePosition).add(centerPosition),
                camDir
        );
        this.bodyYaw = calcBodyYaw();
        this.headPivot = calcHeadPivot();
    }

    private float calcBodyYaw() {
        Vec3 bodyPos = this.controllerLeft.getPosition()
                .subtract(this.controllerRight.getPosition())
                .normalize()
                .yRot((-(float) Math.PI / 2F));
        Vec3 hmdDirection = this.hmd.getDirection();

        if (bodyPos.dot(hmdDirection) < 0.0D) {
            bodyPos = bodyPos.reverse();
        }

        bodyPos = VRMathUtils.lerpVector(hmdDirection, bodyPos, 0.7D);
        return (float) Mth.atan2(-bodyPos.x, bodyPos.z);
    }

    private Vec3 calcHeadPivot() {
        Vec3 hmdPosition = this.hmd.getPosition();
        Vector3f transform = this.hmd.getRotationMatrix()
                .transformDirection(
                        new Vector3f(
                                0.0F,
                                -0.1F * worldScale,
                                0.1F * worldScale
                        )
                );
        return new Vec3(
                (double) transform.x() + hmdPosition.x,
                (double) transform.y() + hmdPosition.y,
                (double) transform.z() + hmdPosition.z
        );
    }

    protected void resetOrigin(Vec3 newOrigin){
        this.origin = newOrigin;
        elements.forEach(
                it->it.onOriginChanged(this.origin)
        );
    }



    public IVRPoseElement getElementForDisplay(VRDisplay display) {
        if(display == null){
            return hmd;
        }
        return switch (display) {
            case FIRST_PERSON -> this.hmd;
            case THIRD_PERSON -> this.thirdPersonCamera;
            case EYE_LEFT -> this.eyeLeft;
            case EYE_RIGHT -> this.eyeRight;
            default -> this.hmd;
        };
    }

    @Override
    public @NotNull Vec3 convertPosition(@NotNull VRPoseStage originStage,
                                         @NotNull Vec3 position){
        if(originStage == poseStage) {
            return new Vec3(
                    position.x,
                    position.y,
                    position.z
            );
        }
        if (originStage == VRPoseStage.ROOM) {
            return position.scale(worldScale)
                    .yRot(rotationYaw)
                    .add(origin);
        }

        VRClientPose originPose = ClientContext.player
                .getPose(originStage);

        Vec3 roomPose = position
                .subtract(originPose.origin)
                .scale(1.0 / originPose.worldScale)
                .yRot(-originPose.rotationYaw);

        if(poseStage == VRPoseStage.ROOM){
            return roomPose;
        }

        return roomPose.scale(worldScale)
                .yRot(rotationYaw)
                .add(origin);
    }


    @Override
    public @NotNull Matrix4f convertRotation(@NotNull VRPoseStage originStage,
                                              @NotNull Matrix4f rotationMatrix) {
        if (originStage == this.poseStage) {
            return rotationMatrix;
        }



        if (originStage == VRPoseStage.ROOM) {
            return new Matrix4f().rotationY(rotationYaw).mul(rotationMatrix);
        }


        VRClientPose originPose = ClientContext.player.getPose(originStage);

        if (this.poseStage == VRPoseStage.ROOM) {
            return new Matrix4f().rotationY(-originPose.rotationYaw).mul(rotationMatrix);
        }

        return new Matrix4f().rotationY(this.rotationYaw - originPose.rotationYaw)
                .mul(rotationMatrix);

    }


    @Override
    public String toString() {
        return String.format(
                "VRClientPose:%n" +
                        "  Pose Stage         : %s%n" +
                        "  Origin             : %s%n" +
                        "  Rotation           : %.2f°%n" +
                        "  World Scale        : %.2f%n" +
                        "  Body Yaw           : %.2f°%n" +
                        "  Head Pivot         : %s%n" +
                        "  HMD                : %s%n" +
                        "  Eye Left           : %s%n" +
                        "  Eye Right          : %s%n" +
                        "  Controller Left    : %s%n" +
                        "  Controller Right   : %s%n" +
                        "  Hand Left          : %s%n" +
                        "  Hand Right         : %s%n" +
                        "  Third Person Camera: %s",
                poseStage,
                origin,
                Math.toDegrees(rotationYaw),
                worldScale,
                Math.toDegrees(bodyYaw),
                headPivot,
                hmd,
                eyeLeft,
                eyeRight,
                controllerLeft,
                controllerRight,
                handLeft,
                handRight,
                thirdPersonCamera
        );
    }

}
