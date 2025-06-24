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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;

import java.util.List;

import me.phoenixra.visor.core.client.ClientContext;
import org.joml.Vector3fc;

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

    private Vector3fc origin;
    private float rotationY;
    private float worldScale;

    private float bodyYaw;
    private Vector3fc headPivot;

    public PoseDataImpl(PoseType type,
                        Vector3fc origin,
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

    protected void update(Vector3fc origin,
                          float walkMul,
                          float worldScale,
                          float rotationY){

        RawController dataMain = ClientContext.rawPoseHandler.getControllerData(ControllerHand.MAIN);
        RawController dataOffhand = ClientContext.rawPoseHandler.getControllerData(ControllerHand.OFFHAND);
        this.origin = origin;
        this.worldScale = worldScale;
        this.rotationY = rotationY;
        RawHmd hmdData = ClientContext.rawPoseHandler.getHmdData();
        Vector3f headsetPos = hmdData.getHeadsetPosition();
        Vector3f headsetPosFinal = new Vector3f(
                headsetPos.x *  walkMul,
                headsetPos.y,
                headsetPos.z * walkMul
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
                hmdData.getEyePosition(EyeType.LEFT).sub(headsetPos).add(headsetPosFinal),
                hmdData.getVector()
        );
        this.eyeRight.update(
                this.origin,
                this.rotationY,
                this.worldScale,
                hmdData.getEyeRotation(EyeType.RIGHT),
                hmdData.getEyePosition(EyeType.RIGHT).sub(headsetPos).add(headsetPosFinal),
                hmdData.getVector()
        );

        this.controllerOffhand.update(
                this.origin,
                this.rotationY,
                this.worldScale,
                dataOffhand.getAimRotation(),
                dataOffhand.getAimPosition().sub(headsetPos).add(headsetPosFinal),
                dataOffhand.getAimVector()
        );
        this.controllerMain.update(
                this.origin,
                this.rotationY,
                this.worldScale,
                dataMain.getAimRotation(),
                dataMain.getAimPosition().sub(headsetPos).add(headsetPosFinal),
                dataMain.getAimVector()
        );
        this.handOffhand.update(
                this.origin,
                this.rotationY,
                this.worldScale,
                dataOffhand.getGripRotation(),
                dataOffhand.getAimPosition().sub(headsetPos).add(headsetPosFinal),
                dataOffhand.getGripVector()
        );
        this.handMain.update(
                this.origin,
                this.rotationY,
                this.worldScale,
                dataMain.getGripRotation(),
                dataMain.getAimPosition().sub(headsetPos).add(headsetPosFinal),
                dataMain.getGripVector()
        );


        Vector3f camPos = new Vector3f(
                VRClientSettings.getFixedCameraPosX(),
                VRClientSettings.getFixedCameraPosY(),
                VRClientSettings.getFixedCameraPosZ()
        );
        Matrix4fc camRot = (new Matrix4f().set(VRClientSettings.getFixedCameraRotation())).transpose();
        Vector3f camDir = camRot.transformDirection(VRMathUtils.forwardVector, new Vector3f());
        this.thirdPersonCamera.update(
                this.origin,
                this.rotationY,
                this.worldScale,
                camRot,
                camPos.sub(headsetPos).add(headsetPosFinal),
                camDir
        );
        this.bodyYaw = calcBodyYaw();
        this.headPivot = calcHeadPivot();
    }

    private float calcBodyYaw() {
        Vector3f bodyPos = this.controllerOffhand.getPosition()
                .sub(this.controllerMain.getPosition(), new Vector3f())
                .normalize()
                .rotateY((-(float) Math.PI / 2F));
        var hmdDirection = this.hmd.getDirection();

        if (bodyPos.dot(hmdDirection) < 0.0D) {
            bodyPos = bodyPos.mul(-1);
        }

        bodyPos = VRMathUtils.lerpVector(hmdDirection, bodyPos, 0.7f);
        return (float) Mth.atan2(-bodyPos.x, bodyPos.z);
    }

    private Vector3f calcHeadPivot() {
        var hmdPosition = this.hmd.getPosition();
        Vector3f transform = this.hmd.getRotation()
                .transformDirection(
                        new Vector3f(
                                0.0F,
                                -0.1F * worldScale,
                                0.1F * worldScale
                        )
                );
        return new Vector3f(
                transform.x() + hmdPosition.x(),
                transform.y() + hmdPosition.y(),
                transform.z() + hmdPosition.z()
        );
    }

    protected void resetOrigin(Vector3fc newOrigin){
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
    public @NotNull Vector3f convertPosition(@NotNull PoseType originStage,
                                               @NotNull Vector3fc position){
        if(originStage == type) {
            return new Vector3f(
                    position.x(),
                    position.y(),
                    position.z()
            );
        }
        if (originStage == PoseType.ROOM) {
            return position.mul(worldScale, new Vector3f())
                    .rotateY(rotationY)
                    .add(origin);
        }

        PoseDataImpl originPose = ClientContext.player
                .getPose(originStage);

        Vector3f roomPose = position
                .sub(originPose.origin, new Vector3f())
                .mul(1.0f / originPose.worldScale)
                .rotateY(-originPose.rotationY);

        if(type == PoseType.ROOM){
            return roomPose;
        }

        return roomPose.mul(worldScale)
                .rotateY(rotationY)
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
