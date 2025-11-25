package me.phoenixra.visor.core.client.player;

import lombok.Getter;
import me.phoenixra.visor.api.client.player.VRRemotePlayer;
import me.phoenixra.visor.api.client.player.pose.PlayerPoseType;
import me.phoenixra.visor.api.common.network.buffer.PoseDataBuffer;
import me.phoenixra.visor.api.common.utils.VRMathUtils;
import me.phoenixra.visor.core.client.player.pose.RemotePlayerPose;
import me.phoenixra.visor.modified.client.entity.LocalPlayerModified;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class VRRemotePlayerImpl implements VRRemotePlayer {
    private final RemotePlayerPose roomPose;

    private final RemotePlayerPose prevPose;
    private final RemotePlayerPose pose;
    private final RemotePlayerPose renderPose;



    @Getter
    private RemotePlayer mcPlayer;


    private PoseDataBuffer poseBufferReceived;

    private float worldScaleReceived;

    @Getter
    private float height;
    @Getter
    private boolean leftHanded;


    public VRRemotePlayerImpl(RemotePlayer mcPlayer,
                              PoseDataBuffer poseBuffer,
                              float worldScale,
                              float height) {
        this.poseBufferReceived = poseBuffer;
        this.worldScaleReceived = worldScale;
        this.height = height;
        this.leftHanded = poseBuffer.leftHanded();
        this.roomPose = new RemotePlayerPose(mcPlayer, PlayerPoseType.ROOM, poseBuffer, VRMathUtils.ZERO_VECTOR,  worldScale);

        this.prevPose = new RemotePlayerPose(mcPlayer, PlayerPoseType.PREV_TICK, poseBuffer, VRMathUtils.ZERO_VECTOR, worldScale);
        this.pose = new RemotePlayerPose(mcPlayer, PlayerPoseType.TICK, poseBuffer, VRMathUtils.ZERO_VECTOR,  worldScale);
        this.renderPose  = new RemotePlayerPose(mcPlayer, PlayerPoseType.RENDER, poseBuffer,VRMathUtils.ZERO_VECTOR,worldScale);
    }


    public void receivedPacked(RemotePlayer mcPlayer,
                               PoseDataBuffer poseBuffer,
                               float worldScale,
                               float height){
        this.mcPlayer = mcPlayer;
        this.poseBufferReceived = poseBuffer;
        this.worldScaleReceived = worldScale;
        this.height = height;
        this.leftHanded = poseBuffer.leftHanded();
        this.roomPose.setMcPlayer(mcPlayer);
        this.prevPose.setMcPlayer(mcPlayer);
        this.pose.setMcPlayer(mcPlayer);
        this.renderPose.setMcPlayer(mcPlayer);
    }


    public void preTick() {

        this.prevPose.copyFrom(
                this.pose
        );

        //WORLD SCALE

        this.pose.update(
                poseBufferReceived,
                mcPlayer.getPosition(1.0f).toVector3f(),
                worldScaleReceived
        );

        this.roomPose.update(
                poseBufferReceived,
                VRMathUtils.ZERO_VECTOR,
                1.0f
        );

    }


    public void postTick() {
        this.pose.update(
                mcPlayer.getPosition(1.0f).toVector3f(),
                this.pose.getWorldScale()
        );
    }



    public void preRender(float partialTicks) {

        //Interpolated Origin
        var preTickOrigin = this.prevPose.getOrigin();
        var postTickOrigin = this.pose.getOrigin();

        Vector3fc originPartial = new Vector3f(
                preTickOrigin.x()
                        + (postTickOrigin.x() - preTickOrigin.x())
                        * partialTicks,
                preTickOrigin.y()
                        + (postTickOrigin.y() - preTickOrigin.y())
                        * partialTicks,
                preTickOrigin.z()
                        + (postTickOrigin.z() - preTickOrigin.z())
                        * partialTicks
        );

        //Interpolated World Scale
        float preTickWorld = this.prevPose.getWorldScale();
        float postTickWorld = this.pose.getWorldScale();
        float worldScalePartial = postTickWorld * partialTicks
                + preTickWorld * (1.0f - partialTicks);

        //Interpolated poses
        var prevHmdPose =  prevPose.getHmd();
        var prevMainHandPose =  prevPose.getMainHand();
        var prevOffhandPose =  prevPose.getOffhand();
        var hmdPose = pose.getHmd();
        var mainHandPose = pose.getMainHand();
        var offhandPose = pose.getOffhand();

        //hmd
        Vector3f hmdPosPartial = prevHmdPose.getRawPosition().lerp(
                hmdPose.getRawPosition(), partialTicks,
                new Vector3f()
        );
        Vector3f hmdDirPartial = prevHmdPose.getRawDirection().lerp(
                hmdPose.getRawDirection(), partialTicks,
                new Vector3f()
        );
        Matrix4f hmdRotationPartial = prevHmdPose.getRawRotation().lerp(
                hmdPose.getRawRotation(), partialTicks,
                new Matrix4f()
        );
        //main hand
        Vector3f mainHandPosPartial = prevMainHandPose.getRawPosition().lerp(
                mainHandPose.getRawPosition(), partialTicks,
                new Vector3f()
        );
        Vector3f mainHandDirPartial = prevMainHandPose.getRawDirection().lerp(
                mainHandPose.getRawDirection(), partialTicks,
                new Vector3f()
        );
        Matrix4f mainHandRotationPartial = prevMainHandPose.getRawRotation().lerp(
                mainHandPose.getRawRotation(), partialTicks,
                new Matrix4f()
        );
        //offhand
        Vector3f offhandPosPartial = prevOffhandPose.getRawPosition().lerp(
                offhandPose.getRawPosition(), partialTicks,
                new Vector3f()
        );
        Vector3f offhandDirPartial = prevOffhandPose.getRawDirection().lerp(
                offhandPose.getRawDirection(), partialTicks,
                new Vector3f()
        );
        Matrix4f offhandRotationPartial = prevOffhandPose.getRawRotation().lerp(
                offhandPose.getRawRotation(), partialTicks,
                new Matrix4f()
        );

        //Applying
        this.renderPose.update(
                hmdPosPartial,
                hmdRotationPartial,
                hmdDirPartial,
                mainHandPosPartial,
                mainHandRotationPartial,
                mainHandDirPartial,
                offhandPosPartial,
                offhandRotationPartial,
                offhandDirPartial,
                originPartial,
                worldScalePartial
        );
    }



    public void recenterOrigin(@NotNull Entity cameraEntity,
                               boolean reset) {


        var headPivot = this.prevPose.getHeadPivot();

        var headOffset = headPivot.sub(
                this.prevPose.getOrigin(),
                new Vector3f()
        );
        float x = (float) (cameraEntity.getX() - headOffset.x);
        float z = (float) (cameraEntity.getZ() - headOffset.z);
        float y = (float) (cameraEntity.getY());
        if (cameraEntity instanceof LocalPlayerModified p) {
            y += (float) p.visor$getRoomYOffset();
        }
        this.setOrigin(x, y, z, reset);
    }



    public void setOrigin(float x, float y, float z,
                          boolean reset) {
        var newOrigin = new Vector3f(x, y, z);
        if (reset) {
            this.prevPose.resetOrigin(newOrigin);
        }

        this.pose.update(
                newOrigin,
                pose.getWorldScale()
        );
    }



    @Override
    public @NotNull RemotePlayerPose getPoseData(@NotNull PlayerPoseType stage) {
        return switch (stage){
            case PREV_TICK -> prevPose;
            case TICK -> pose;
            case RENDER -> renderPose;
            default -> roomPose;
        };
    }

    public String toString() {
        return ("""
            VRRemotePlayer:
                room pose: %s
                previous pose: %s
                pose: %s
                render pose: %s"""
        ).formatted(
                this.roomPose,
                this.prevPose,
                this.pose,
                this.renderPose
        );
    }
}
