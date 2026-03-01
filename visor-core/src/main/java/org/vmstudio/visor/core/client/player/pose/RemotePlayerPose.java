package org.vmstudio.visor.core.client.player.pose;

import lombok.Getter;
import lombok.Setter;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseClient;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.client.render.VRCameraType;
import org.vmstudio.visor.api.common.player.VRPose;
import org.vmstudio.visor.api.common.network.buffer.PoseDataBuffer;
import org.vmstudio.visor.api.common.utils.VRMathUtils;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.common.player.VRPoseImpl;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.List;


@Getter
public class RemotePlayerPose implements PlayerPoseClient {

    @Setter
    private RemotePlayer mcPlayer;

    private final PlayerPoseType type;

    protected final VRPoseImpl hmd;

    protected final VRPoseImpl mainHand;
    protected final VRPoseImpl offhand;



    private final List<VRPoseImpl> elements;

    private Vector3fc origin;
    private final float rotationY = 0.0f;
    private float worldScale;

    private float bodyYaw;
    private Vector3fc headPivot;

    public RemotePlayerPose(RemotePlayer mcPlayer,
                            PlayerPoseType type) {
        this.mcPlayer = mcPlayer;
        this.type = type;

        this.hmd = new VRPoseImpl();

        this.mainHand = new VRPoseImpl();
        this.offhand = new VRPoseImpl();



        elements = List.of(
                hmd,
                mainHand,
                offhand
        );


    }

    public void update(Vector3fc hmdPos,
                       Matrix4fc hmdRotation,
                       Vector3fc hmdDir,
                       Vector3fc mainHandPos,
                       Matrix4fc mainHandRotation,
                       Vector3fc mainHandDir,
                       Vector3fc offhandPos,
                       Matrix4fc offhandRotation,
                       Vector3fc offhandDir,
                       Vector3fc origin,
                       float worldScale){

        this.origin = origin;
        this.worldScale = worldScale;

        this.hmd.update(
                this.origin,
                this.rotationY,
                this.worldScale,
                hmdPos,
                hmdRotation,
                hmdDir
        );

        this.mainHand.update(
                this.origin,
                this.rotationY,
                this.worldScale,
                mainHandPos,
                mainHandRotation,
                mainHandDir
        );
        this.offhand.update(
                this.origin,
                this.rotationY,
                this.worldScale,
                offhandPos,
                offhandRotation,
                offhandDir
        );

        this.bodyYaw = calcBodyYaw();
        this.headPivot = calcHeadPivot();
    }
    public void update(PoseDataBuffer poseBuffer,
                       Vector3fc origin,
                       float worldScale){

        var hmdPose = poseBuffer.hmd();
        var mainHandPose = poseBuffer.mainHand();
        var offhandPose = poseBuffer.offhand();

        Vector3f hmdDir = hmdPose
                .orientation().transform(VRMathUtils.BACK_VECTOR, new Vector3f());
        Vector3f mainHandDir = mainHandPose
                .orientation().transform(VRMathUtils.BACK_VECTOR, new Vector3f());
        Vector3f offhandDir = offhandPose
                .orientation().transform(VRMathUtils.BACK_VECTOR, new Vector3f());


        update(
                hmdPose.position(),
                hmdPose.orientation().get(new Matrix4f()),
                hmdDir,
                mainHandPose.position(),
                mainHandPose.orientation().get(new Matrix4f()),
                mainHandDir,
                offhandPose.position(),
                offhandPose.orientation().get(new Matrix4f()),
                offhandDir,
                origin,
                worldScale
        );
    }

    public void update(Vector3fc newOrigin,
                       float newWorldScale){
        if (newWorldScale == this.worldScale
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

        this.origin = adjustedOrigin;
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

    public void copyFrom(RemotePlayerPose other){
        this.origin = new Vector3f(other.origin);
        this.bodyYaw = other.bodyYaw;
        this.worldScale = other.worldScale;
        this.headPivot = new Vector3f(other.headPivot);

        hmd.copyFrom(other.hmd);
        mainHand.copyFrom(other.mainHand);
        offhand.copyFrom(other.offhand);
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




    @Override
    public @NotNull VRPose getCameraPose(@Nullable VRCameraType cameraType) {
        return hmd;
    }

    @Override
    public @NotNull VRPose getEyeLeft() {
        return hmd;
    }

    @Override
    public @NotNull VRPose getEyeRight() {
        return hmd;
    }

    @Override
    public @NotNull VRPose getGripMainHand() {
        return mainHand;
    }

    @Override
    public @NotNull VRPose getGripOffhand() {
        return offhand;
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

        var vrPlayer =  ClientContext.visor
                .getVRPlayer(mcPlayer.getUUID());
        if(vrPlayer == null){
            return new Vector3f(
                    position.x(),
                    position.y(),
                    position.z()
            );
        }

        PlayerPoseClient originPose = vrPlayer.getPoseData(originType);

        Vector3f roomPose = position
                .sub(originPose.getOrigin(), new Vector3f())
                .mul(1.0f / originPose.getWorldScale())
                .rotateY(-originPose.getRotationY());

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

        var vrPlayer =  ClientContext.visor
                .getVRPlayer(mcPlayer.getUUID());
        if(vrPlayer == null){
            return new Matrix4f(rotationMatrix);
        }

        PlayerPoseClient originPose = vrPlayer.getPoseData(originType);

        if (this.type == PlayerPoseType.RELATIVE) {
            return new Matrix4f().rotationY(-originPose.getRotationY()).mul(rotationMatrix);
        }

        return new Matrix4f().rotationY(this.rotationY - originPose.getRotationY())
                .mul(rotationMatrix);

    }


    @Override
    public String toString() {
        return String.format(
                "RemotePlayerPose:%n" +
                        "  Pose Stage         : %s%n" +
                        "  Origin             : %s%n" +
                        "  Rotation           : %.2f°%n" +
                        "  World Scale        : %.2f%n" +
                        "  Body Yaw           : %.2f°%n" +
                        "  Head Pivot         : %s%n" +
                        "  HMD                : %s%n" +
                        "  Main hand          : %s%n" +
                        "  Offhand            : %s%n",
                type,
                origin,
                Math.toDegrees(rotationY),
                worldScale,
                Math.toDegrees(bodyYaw),
                headPivot,
                hmd,
                mainHand,
                offhand
        );
    }
}
