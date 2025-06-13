package me.phoenixra.visor.core.client.data;

import lombok.Getter;
import me.phoenixra.atumvr.api.enums.EyeType;
import me.phoenixra.visor.api.client.data.PoseData;
import me.phoenixra.visor.api.client.data.PoseElement;
import me.phoenixra.visor.api.client.data.PoseType;
import me.phoenixra.visor.api.client.render.VRDisplay;
import me.phoenixra.visor.api.common.ControllerHand;
import me.phoenixra.visor.api.common.utils.VRMathUtils;
import me.phoenixra.visor.core.client.data.raw.RawController;
import me.phoenixra.visor.core.client.data.raw.RawHmd;
import me.phoenixra.visor.core.client.settings.VRClientSettings;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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

    protected final PoseElementImpl controllerMain;
    protected final PoseElementImpl controllerOffhand;

    protected final PoseElementImpl handMain;
    protected final PoseElementImpl handOffhand;

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

        this.controllerOffhand = new PoseElementImpl();
        this.controllerMain = new PoseElementImpl();
        this.handOffhand = new PoseElementImpl();
        this.handMain = new PoseElementImpl();

        this.thirdPersonCamera = new PoseElementImpl();


        elements = List.of(
                hmd,
                eyeLeft, eyeRight,
                controllerOffhand, controllerMain,
                handOffhand, handMain,
                thirdPersonCamera
        );

        update(origin, walkMul, worldScale, rotationY);

    }

    protected void update(Vec3 origin,
                          float walkMul,
                          float worldScale,
                          float rotationY){

        RawController dataMain = ClientContext.rawPoseHandler.getControllerData(ControllerHand.MAIN);
        RawController dataOffhand = ClientContext.rawPoseHandler.getControllerData(ControllerHand.OFFHAND);
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

        this.controllerOffhand.update(
                this.origin,
                this.rotationY,
                this.worldScale,
                dataOffhand.getAimRotation(),
                dataOffhand.getAimPosition().subtract(headsetPos).add(headsetPosFinal),
                dataOffhand.getAimVector()
        );
        this.controllerMain.update(
                this.origin,
                this.rotationY,
                this.worldScale,
                dataMain.getAimRotation(),
                dataMain.getAimPosition().subtract(headsetPos).add(headsetPosFinal),
                dataMain.getAimVector()
        );
        this.handOffhand.update(
                this.origin,
                this.rotationY,
                this.worldScale,
                dataOffhand.getGripRotation(),
                dataOffhand.getAimPosition().subtract(headsetPos).add(headsetPosFinal),
                dataOffhand.getGripVector()
        );
        this.handMain.update(
                this.origin,
                this.rotationY,
                this.worldScale,
                dataMain.getGripRotation(),
                dataMain.getAimPosition().subtract(headsetPos).add(headsetPosFinal),
                dataMain.getGripVector()
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
        Vec3 bodyPos = this.controllerOffhand.getPosition()
                .subtract(this.controllerMain.getPosition())
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



    @Override
    public @NotNull PoseElement getElementForDisplay(@Nullable VRDisplay display) {
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
                controllerOffhand,
                controllerMain,
                handOffhand,
                handMain,
                thirdPersonCamera
        );
    }

}
