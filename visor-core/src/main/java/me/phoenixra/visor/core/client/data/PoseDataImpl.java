package me.phoenixra.visor.core.client.data;

import lombok.Getter;
import me.phoenixra.atumvr.api.enums.EyeType;
import me.phoenixra.visor.api.client.data.PoseData;
import me.phoenixra.visor.api.client.data.PoseElement;
import me.phoenixra.visor.api.client.data.PoseType;
import me.phoenixra.visor.api.client.render.VRDisplay;
import me.phoenixra.visor.api.common.utils.VRMathUtils;
import me.phoenixra.visor.core.client.data.raw.RawController;
import me.phoenixra.visor.core.client.data.raw.RawHmd;
import me.phoenixra.visor.core.client.settings.VRClientSettings;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.List;

import me.phoenixra.visor.core.client.ClientContext;

@Getter
public class PoseDataImpl implements PoseData {

    private final PoseType type;

    protected final PoseElementImpl hmd;
    protected final PoseElementImpl eyeLeft;
    protected final PoseElementImpl eyeRight;

    protected final PoseElementImpl controllerLeft;
    protected final PoseElementImpl controllerRight;

    protected final PoseElementImpl handLeft;
    protected final PoseElementImpl handRight;

    protected final PoseElementImpl thirdPersonCamera;

    private final List<PoseElementImpl> elements;

    private Vec3 origin;
    private float rotationY;
    private float worldScale;

    private float bodyYaw;
    private Vec3 headPivot;

    public PoseDataImpl(PoseType type,
                        Vec3 origin,
                        float walkMul,
                        float worldScale,
                        float rotationY) {
        this.type = type;

        this.hmd = new PoseElementImpl();
        this.eyeLeft = new PoseElementImpl();
        this.eyeRight = new PoseElementImpl();

        this.controllerLeft = new PoseElementImpl();
        this.controllerRight = new PoseElementImpl();
        this.handLeft = new PoseElementImpl();
        this.handRight = new PoseElementImpl();

        this.thirdPersonCamera = new PoseElementImpl();


        elements = List.of(
                hmd,
                eyeLeft, eyeRight,
                controllerLeft, controllerRight,
                handLeft, handRight,
                thirdPersonCamera
        );

        update(origin, walkMul, worldScale, rotationY);

    }

    protected void update(Vec3 origin,
                          float walkMul,
                          float worldScale,
                          float rotationY){

        RawController dataLeft = ClientContext.rawPoseHandler.getControllerLeftData();
        RawController dataRight = ClientContext.rawPoseHandler.getControllerRightData();
        this.origin = origin;
        this.worldScale = worldScale;
        this.rotationY = rotationY;
        RawHmd hmdData = ClientContext.rawPoseHandler.getHmdData();
        Vec3 headsetPos = hmdData.getHeadsetPosition();
        Vec3 headsetPosFinal = new Vec3(
                headsetPos.x * (double) walkMul,
                headsetPos.y,
                headsetPos.z * (double) walkMul
        );

        this.hmd.update(
                this.origin,
                this.rotationY,
                this.worldScale,
                hmdData.getRotation(),
                headsetPosFinal, hmdData.getVector()
        );
        this.eyeLeft.update(
                this.origin,
                this.rotationY,
                this.worldScale,
                hmdData.getEyeRotation(EyeType.LEFT),
                hmdData.getEyePosition(EyeType.LEFT).subtract(headsetPos).add(headsetPosFinal),
                hmdData.getVector()
        );
        this.eyeRight.update(
                this.origin,
                this.rotationY,
                this.worldScale,
                hmdData.getEyeRotation(EyeType.RIGHT),
                hmdData.getEyePosition(EyeType.RIGHT).subtract(headsetPos).add(headsetPosFinal),
                hmdData.getVector()
        );

        this.controllerLeft.update(
                this.origin,
                this.rotationY,
                this.worldScale,
                dataLeft.getAimRotation(),
                dataLeft.getAimPosition().subtract(headsetPos).add(headsetPosFinal),
                dataLeft.getAimVector()
        );
        this.controllerRight.update(
                this.origin,
                this.rotationY,
                this.worldScale,
                dataRight.getAimRotation(),
                dataRight.getAimPosition().subtract(headsetPos).add(headsetPosFinal),
                dataRight.getAimVector()
        );
        this.handLeft.update(
                this.origin,
                this.rotationY,
                this.worldScale,
                dataLeft.getGripRotation(),
                dataLeft.getAimPosition().subtract(headsetPos).add(headsetPosFinal),
                dataLeft.getGripVector()
        );
        this.handRight.update(
                this.origin,
                this.rotationY,
                this.worldScale,
                dataRight.getGripRotation(),
                dataRight.getAimPosition().subtract(headsetPos).add(headsetPosFinal),
                dataRight.getGripVector()
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
                this.rotationY,
                this.worldScale,
                camRot,
                camPos.subtract(headsetPos).add(headsetPosFinal),
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



    public PoseElement getElementForDisplay(VRDisplay display) {
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
    public @NotNull Vec3 convertPosition(@NotNull PoseType originStage,
                                         @NotNull Vec3 position){
        if(originStage == type) {
            return new Vec3(
                    position.x,
                    position.y,
                    position.z
            );
        }
        if (originStage == PoseType.ROOM) {
            return position.scale(worldScale)
                    .yRot(rotationY)
                    .add(origin);
        }

        PoseDataImpl originPose = ClientContext.player
                .getPose(originStage);

        Vec3 roomPose = position
                .subtract(originPose.origin)
                .scale(1.0 / originPose.worldScale)
                .yRot(-originPose.rotationY);

        if(type == PoseType.ROOM){
            return roomPose;
        }

        return roomPose.scale(worldScale)
                .yRot(rotationY)
                .add(origin);
    }


    @Override
    public @NotNull Matrix4f convertRotation(@NotNull PoseType originStage,
                                              @NotNull Matrix4f rotationMatrix) {
        if (originStage == this.type) {
            return rotationMatrix;
        }



        if (originStage == PoseType.ROOM) {
            return new Matrix4f().rotationY(rotationY).mul(rotationMatrix);
        }


        PoseDataImpl originPose = ClientContext.player.getPose(originStage);

        if (this.type == PoseType.ROOM) {
            return new Matrix4f().rotationY(-originPose.rotationY).mul(rotationMatrix);
        }

        return new Matrix4f().rotationY(this.rotationY - originPose.rotationY)
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
                type,
                origin,
                Math.toDegrees(rotationY),
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
