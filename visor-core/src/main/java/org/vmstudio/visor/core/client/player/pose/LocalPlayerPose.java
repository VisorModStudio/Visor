package org.vmstudio.visor.core.client.player.pose;

import lombok.Getter;
import me.phoenixra.atumvr.api.enums.EyeType;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseClient;
import org.vmstudio.visor.api.common.player.VRPose;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.client.render.VRRenderPass;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.common.utils.VRMathUtils;
import org.vmstudio.visor.core.client.player.pose.raw.RawControllerImpl;
import org.vmstudio.visor.core.client.player.pose.raw.RawHmdImpl;
import org.vmstudio.visor.core.client.settings.VRClientSettings;
import org.vmstudio.visor.core.common.player.VRPoseImpl;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;

import java.util.List;

import org.vmstudio.visor.core.client.ClientContext;
import org.joml.Vector3fc;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

@Getter
public class LocalPlayerPose implements PlayerPoseClient {

    private final PlayerPoseType type;

    protected final VRPoseImpl hmd;
    protected final VRPoseImpl eyeLeft;
    protected final VRPoseImpl eyeRight;

    protected final VRPoseImpl mainHand;
    protected final VRPoseImpl offhand;

    protected final VRPoseImpl gripMainHand;
    protected final VRPoseImpl gripOffhand;

    protected final VRPoseImpl thirdPersonCamera;

    private final List<VRPoseImpl> elements;

    private Vector3fc origin;
    private float rotationY;
    private float worldScale;

    private float bodyYaw;
    private Vector3fc headPivot;

    public LocalPlayerPose(PlayerPoseType type) {
        this.type = type;

        this.hmd = new VRPoseImpl();
        this.eyeLeft = new VRPoseImpl();
        this.eyeRight = new VRPoseImpl();

        this.mainHand = new VRPoseImpl();
        this.offhand = new VRPoseImpl();
        this.gripOffhand = new VRPoseImpl();
        this.gripMainHand = new VRPoseImpl();

        this.thirdPersonCamera = new VRPoseImpl();


        elements = List.of(
                hmd,
                eyeLeft, eyeRight,
                mainHand, offhand,
                gripOffhand, gripMainHand,
                thirdPersonCamera
        );

        update(VRMathUtils.ZERO_VECTOR, 1.0f, 1.0f, 0f);

    }

    public void update(Vector3fc origin,
                       float walkMul,
                       float worldScale,
                       float rotationY){

        RawControllerImpl dataMain = ClientContext.rawPoseHandler.getControllerData(HandType.MAIN);
        RawControllerImpl dataOffhand = ClientContext.rawPoseHandler.getControllerData(HandType.OFFHAND);
        this.origin = origin;
        this.worldScale = worldScale;
        this.rotationY = rotationY;
        RawHmdImpl hmdData = ClientContext.rawPoseHandler.getHmdData();
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
                headsetPosFinal,
                hmdData.getRotation(),
                hmdData.getVector()
        );
        this.eyeLeft.update(
                this.origin,
                this.rotationY,
                this.worldScale,
                hmdData.getEyePosition(EyeType.LEFT).sub(headsetPos).add(headsetPosFinal),
                hmdData.getEyeRotation(EyeType.LEFT),
                hmdData.getVector()
        );
        this.eyeRight.update(
                this.origin,
                this.rotationY,
                this.worldScale,
                hmdData.getEyePosition(EyeType.RIGHT).sub(headsetPos).add(headsetPosFinal),
                hmdData.getEyeRotation(EyeType.RIGHT),
                hmdData.getVector()
        );

        this.mainHand.update(
                this.origin,
                this.rotationY,
                this.worldScale,
                dataMain.getAimPosition().sub(headsetPos).add(headsetPosFinal),
                dataMain.getAimRotation(),
                dataMain.getAimVector()
        );
        this.offhand.update(
                this.origin,
                this.rotationY,
                this.worldScale,
                dataOffhand.getAimPosition().sub(headsetPos).add(headsetPosFinal),
                dataOffhand.getAimRotation(),
                dataOffhand.getAimVector()
        );

        this.gripMainHand.update(
                this.origin,
                this.rotationY,
                this.worldScale,
                dataMain.getAimPosition().sub(headsetPos).add(headsetPosFinal),
                dataMain.getGripRotation(),
                dataMain.getGripVector()
        );
        this.gripOffhand.update(
                this.origin,
                this.rotationY,
                this.worldScale,
                dataOffhand.getAimPosition().sub(headsetPos).add(headsetPosFinal),
                dataOffhand.getGripRotation(),
                dataOffhand.getGripVector()
        );


        Vector3f camPos = new Vector3f(
                VRClientSettings.getThirdPersonCameraPosX(),
                VRClientSettings.getThirdPersonCameraPosY(),
                VRClientSettings.getThirdPersonCameraPosZ()
        );
        Matrix4fc camRot = VRClientSettings.getThirdPersonCameraRotation()
                .get(new Matrix4f());
        Vector3f camDir = camRot.transformDirection(VRMathUtils.BACK_VECTOR, new Vector3f());
        this.thirdPersonCamera.update(
                this.origin,
                this.rotationY,
                this.worldScale,
                camPos.sub(headsetPos).add(headsetPosFinal),
                camRot,
                camDir
        );
        this.bodyYaw = calcBodyYaw();
        this.headPivot = calcHeadPivot();
    }

    public void update(Vector3fc newOrigin,
                       float newWorldScale,
                       float newRotationY){
        if (newWorldScale == this.worldScale
                && newRotationY == this.rotationY
                && newOrigin.equals(this.origin)) {
            return;
        }

        Vector3f adjustedOrigin = new Vector3f(newOrigin);

        if (newWorldScale != this.worldScale) {
            Vector3f scaleOffset = hmd.getScalePosOffset(
                    this.rotationY,
                    this.worldScale,
                    newWorldScale
            );

            adjustedOrigin.sub(scaleOffset);
        }

        Vector3f pivot;
        boolean originChanged = !adjustedOrigin.equals(this.origin);
        boolean scaleChanged = (newWorldScale != this.worldScale);

        if (originChanged || scaleChanged) {
            pivot = createNewHeadPivot(adjustedOrigin, newWorldScale);
        } else {
            pivot = new Vector3f(this.headPivot);
        }

        if (newRotationY != this.rotationY) {
            float deltaRotation = this.rotationY - newRotationY;
            rotateOriginAround(adjustedOrigin, pivot, deltaRotation);
        }

        this.origin = adjustedOrigin;
        this.rotationY = newRotationY;
        this.worldScale = newWorldScale;

        for (VRPoseImpl it : elements) {
            it.update(
                    this.origin,
                    this.rotationY,
                    this.worldScale
            );
        }

        this.headPivot = calcHeadPivot();
        this.bodyYaw = calcBodyYaw();
    }

    public void copyFrom(LocalPlayerPose other){
        this.origin = new Vector3f(other.origin);
        this.rotationY = other.rotationY;
        this.bodyYaw = other.bodyYaw;
        this.worldScale = other.worldScale;
        this.headPivot = new Vector3f(other.headPivot);

        hmd.copyFrom(other.hmd);
        eyeLeft.copyFrom(other.eyeLeft);
        eyeRight.copyFrom(other.eyeRight);
        mainHand.copyFrom(other.mainHand);
        offhand.copyFrom(other.offhand);
        gripMainHand.copyFrom(other.gripMainHand);
        gripOffhand.copyFrom(other.gripOffhand);
        thirdPersonCamera.copyFrom(other.thirdPersonCamera);
    }

    public Vector3f createNewHeadPivot(Vector3fc newOrigin, float newWorldScale) {
        Vector3f hmdPosition = this.hmd.getPosition()
                .add(
                        newOrigin.x() - this.origin.x(),
                        newOrigin.y() - this.origin.y(),
                        newOrigin.z() - this.origin.z(),
                        new Vector3f()
                );
        Vector3f headPivotOffset = this.hmd.getRotation()
                .transformPosition(
                        new Vector3f(
                                0.0F,
                                -0.1F * newWorldScale,
                                0.1F * newWorldScale
                        )
                );
        return hmdPosition.add(headPivotOffset);
    }

    private float calcBodyYaw() {
        Vector3f bodyPos = this.offhand.getPosition()
                .sub(this.mainHand.getPosition(), new Vector3f())
                .normalize()
                .rotateY((-(float) Math.PI / 2F));
        var hmdDirection = this.hmd.getDirection();

        if (bodyPos.dot(hmdDirection) < 0.0D) {
            bodyPos = bodyPos.mul(-1);
        }

        bodyPos = hmdDirection.lerp(bodyPos, 0.7f, new Vector3f());
        return (float) Mth.atan2(-bodyPos.x, bodyPos.z);
    }

    private Vector3f calcHeadPivot() {
        var hmdPosition = this.hmd.getPosition();
        Vector3f transform = this.hmd.getRotation()
                .transformPosition(
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


    public void resetOrigin(Vector3fc newOrigin){
        this.origin = newOrigin;
        elements.forEach(
                it->it.onOriginChanged(this.origin)
        );
        this.headPivot = calcHeadPivot();
        this.bodyYaw = calcBodyYaw();
    }

    private static void rotateOriginAround(Vector3f originToMutate, Vector3fc anchor, float radians) {
        float radSin = Mth.sin(radians);
        float radCos = Mth.cos(radians);

        float ox = originToMutate.x() - anchor.x();
        float oz = originToMutate.z() - anchor.z();

        float rx = radCos * ox - radSin * oz + anchor.x();
        float rz = radSin * ox + radCos * oz + anchor.z();

        originToMutate.set(rx, originToMutate.y(), rz);
    }

    public VRPose getActiveHand() {
        return getPoseElement(ClientContext.localPlayer.getActiveHand().asBodyPart());
    }

    @Override
    public @NotNull VRPose getCameraPose(@Nullable VRRenderPass renderPass) {
        if(renderPass == null){
            return hmd;
        }
        return switch (renderPass) {
            case CENTER -> this.hmd;
            case THIRD_PERSON -> this.thirdPersonCamera;
            case EYE_LEFT -> this.eyeLeft;
            case EYE_RIGHT -> this.eyeRight;
            default -> this.hmd;
        };
    }

    @Override
    public @NotNull Vector3f convertPositionFrom(@NotNull PlayerPoseType originType,
                                                 @NotNull Vector3fc position){
        if(originType == type) {
            return new Vector3f(
                    position.x(),
                    position.y(),
                    position.z()
            );
        }
        if (originType == PlayerPoseType.RELATIVE) {
            return position.mul(worldScale, new Vector3f())
                    .rotateY(rotationY)
                    .add(origin);
        }

        LocalPlayerPose originPose = ClientContext.localPlayer
                .getPoseData(originType);

        Vector3f roomPose = position
                .sub(originPose.origin, new Vector3f())
                .mul(1.0f / originPose.worldScale)
                .rotateY(-originPose.rotationY);

        if(type == PlayerPoseType.RELATIVE){
            return roomPose;
        }

        return roomPose.mul(worldScale)
                .rotateY(rotationY)
                .add(origin);
    }


    @Override
    public @NotNull Matrix4f convertRotationFrom(@NotNull PlayerPoseType originType,
                                                 @NotNull Matrix4fc rotationMatrix) {
        if (originType == this.type) {
            return new Matrix4f(rotationMatrix);
        }



        if (originType == PlayerPoseType.RELATIVE) {
            return new Matrix4f().rotationY(rotationY).mul(rotationMatrix);
        }


        LocalPlayerPose originPose = ClientContext.localPlayer.getPoseData(originType);

        if (this.type == PlayerPoseType.RELATIVE) {
            return new Matrix4f().rotationY(-originPose.rotationY).mul(rotationMatrix);
        }

        return new Matrix4f().rotationY(this.rotationY - originPose.rotationY)
                .mul(rotationMatrix);

    }


    @Override
    public Player getMcPlayer() {
        return MC.player;
    }

    @Override
    public String toString() {
        return String.format(
                "LocalPlayerPose:%n" +
                        "  Pose Stage         : %s%n" +
                        "  Origin             : %s%n" +
                        "  Rotation           : %.2f°%n" +
                        "  World Scale        : %.2f%n" +
                        "  Body Yaw           : %.2f°%n" +
                        "  Head Pivot         : %s%n" +
                        "  HMD                : %s%n" +
                        "  Eye Left           : %s%n" +
                        "  Eye Right          : %s%n" +
                        "  Main hand          : %s%n" +
                        "  Offhand            : %s%n" +
                        "  Main hand grip     : %s%n" +
                        "  Offhand grip       : %s%n" +
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
                mainHand,
                offhand,
                gripMainHand,
                gripOffhand,
                thirdPersonCamera
        );
    }

}
