package org.vmstudio.visor.core.client.player;

import lombok.Getter;
import lombok.Setter;
import org.joml.*;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.events.BodyChangedVREvent;
import org.vmstudio.visor.api.client.player.VRRemotePlayer;
import org.vmstudio.visor.api.client.player.body.VRBodyType;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.common.network.buffer.PoseDataBuffer;
import org.vmstudio.visor.api.common.network.toclient.vrstate.other.VROtherStartTrackingPayloadToClient;
import org.vmstudio.visor.api.common.player.VRPlayer;
import org.vmstudio.visor.api.common.utils.VRMathUtils;
import org.vmstudio.visor.core.client.player.body.VRBodyTypeHandsOnly;
import org.vmstudio.visor.core.client.player.pose.RemotePlayerPose;
import org.vmstudio.visor.core.common.player.PoseHistoryImpl;
import net.minecraft.client.player.RemotePlayer;
import org.jetbrains.annotations.NotNull;

import java.lang.Math;

public class VRRemotePlayerImpl implements VRRemotePlayer {
    private final RemotePlayerPose roomPose;

    private final RemotePlayerPose prevPose;
    private final RemotePlayerPose pose;
    private final RemotePlayerPose renderPose;

    @Getter
    private final PoseHistoryImpl poseHistoryRoom;
    @Getter
    private final PoseHistoryImpl poseHistoryTick;

    @Getter
    private RemotePlayer mcPlayer;

    @Getter @Setter
    private VRBodyType bodyType;

    private boolean bodyTypeInitialized;

    @Getter
    private PoseDataBuffer poseBufferReceived;

    private float worldScaleReceived;

    @Getter
    private float rotationYReceived;

    @Getter
    private float fullHeight;
    @Getter
    private boolean leftHanded;

    @Getter
    private float gunAngle = VRPlayer.DEFAULT_GUN_ANGLE;
    @Getter @Setter
    private boolean overlayFocused;

    public VRRemotePlayerImpl(RemotePlayer mcPlayer,
                              VROtherStartTrackingPayloadToClient initialData) {
        this.mcPlayer = mcPlayer;
        this.poseBufferReceived = initialData.poseBuf().pose();
        this.worldScaleReceived = initialData.worldScaleBuf().worldScale();
        this.rotationYReceived = initialData.rotationYBuf().rotationY();
        this.fullHeight = VRPlayer.DEFAULT_FULL_HEIGHT;

        this.bodyType = VRBodyTypeHandsOnly.getInstance();

        this.roomPose = new RemotePlayerPose(this, PlayerPoseType.ROOM);
        this.roomPose.update(poseBufferReceived, VRMathUtils.ZERO_VECTOR, 1.0f, rotationYReceived);

        this.prevPose = new RemotePlayerPose(this, PlayerPoseType.PREV_TICK);
        this.prevPose.update(poseBufferReceived, VRMathUtils.ZERO_VECTOR, worldScaleReceived, rotationYReceived);

        this.pose = new RemotePlayerPose(this, PlayerPoseType.TICK);
        this.pose.update(poseBufferReceived, VRMathUtils.ZERO_VECTOR, worldScaleReceived, rotationYReceived);

        this.renderPose  = new RemotePlayerPose(this, PlayerPoseType.RENDER);
        this.renderPose.update(poseBufferReceived, VRMathUtils.ZERO_VECTOR, worldScaleReceived, rotationYReceived);

        this.poseHistoryRoom = new PoseHistoryImpl(roomPose);
        this.poseHistoryTick = new PoseHistoryImpl(pose);

        receivedBodyTypePacket(initialData.bodyTypeBuf().bodyType());
        receivedLeftHandedPacket(initialData.leftHandedBuf().leftHanded());
        /* Not needed, its already handled
        receivedRotationYPacket(initialData.rotationYBuf().rotationY());
        receivedWorldScalePacket(initialData.worldScaleBuf().worldScale());
        */
        receivedFullHeightPacket(initialData.fullHeightBuf().fullHeight());
        receivedGunAngle(initialData.gunAngleBuf().gunAngle());
        receivedOverlayFocusedPacket(initialData.overlayFocusedBuf().overlayFocused());

    }


    public void receivedPosePacket(RemotePlayer mcPlayer,
                                   PoseDataBuffer poseBuffer){
        this.mcPlayer = mcPlayer;
        this.poseBufferReceived = poseBuffer;
        this.roomPose.setMcPlayer(mcPlayer);
        this.prevPose.setMcPlayer(mcPlayer);
        this.pose.setMcPlayer(mcPlayer);
        this.renderPose.setMcPlayer(mcPlayer);
    }

    public void receivedBodyTypePacket(String vrBodyTypeId){
        var registry = VisorAPI.addonManager().getRegistries()
                .vrBodyTypes();
        var newBodyType = registry.getComponent(vrBodyTypeId);
        if(newBodyType == null){
            newBodyType = VRBodyType.FALLBACK_BODY_TYPE;
        }
        var oldBodyType = this.bodyType;
        this.bodyType = newBodyType;

        this.roomPose.bodyTypeChanged(bodyType);
        this.prevPose.bodyTypeChanged(bodyType);
        this.pose.bodyTypeChanged(bodyType);
        this.renderPose.bodyTypeChanged(bodyType);

        this.poseHistoryRoom.clear();
        this.poseHistoryTick.clear();

        if(bodyTypeInitialized && newBodyType != oldBodyType) {
            VisorAPI.eventBus().callEvent(
                    new BodyChangedVREvent(this, bodyType)
            );
        }
        bodyTypeInitialized = true;
    }
    public void receivedLeftHandedPacket(boolean leftHanded){
        this.leftHanded = leftHanded;
    }
    public void receivedRotationYPacket(float rotationY){
        this.rotationYReceived = rotationY;
    }
    public void receivedWorldScalePacket(float worldScale){
        this.worldScaleReceived = worldScale;
    }
    public void receivedFullHeightPacket(float fullHeight){
        this.fullHeight = fullHeight;
    }
    public void receivedGunAngle(float gunAngle){
        this.gunAngle = gunAngle;
    }
    public void receivedOverlayFocusedPacket(boolean opened) {
        this.overlayFocused = opened;
    }

    public void preTick() {

        this.prevPose.copyFrom(
                this.pose
        );

        //WORLD SCALE

        this.pose.update(
                poseBufferReceived,
                mcPlayer.getPosition(1.0f).toVector3f(),
                worldScaleReceived,
                rotationYReceived
        );

        this.roomPose.update(
                poseBufferReceived,
                VRMathUtils.ZERO_VECTOR,
                1.0f,
                rotationYReceived
        );

        var historyEntry = new RemotePlayerPose(this, PlayerPoseType.ROOM);
        historyEntry.copyFrom(roomPose);
        poseHistoryRoom.addEntry(historyEntry);

        historyEntry = new RemotePlayerPose(this, PlayerPoseType.PREV_TICK);
        historyEntry.copyFrom(prevPose);
        poseHistoryTick.addEntry(historyEntry);
    }


    public void postTick() {
        this.pose.updateModifiers(
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

        //Interpolated Rotation
        float rotationPre = this.prevPose.getRotationY();
        float rotationPost = this.pose.getRotationY();
        if (Math.abs(rotationPost - rotationPre) > Math.PI) {
            if (rotationPost > rotationPre) {
                rotationPre = (float) (rotationPre + (Math.PI * 2));
            } else {
                rotationPost = (float) (rotationPost + (Math.PI * 2));
            }
        }
        float rotationPartial = rotationPost * partialTicks
                + rotationPre * (1.0f - partialTicks);

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
        Quaternionf hmdQ = slerpRotation(
                prevHmdPose.getRawRotation(),
                hmdPose.getRawRotation(),
                partialTicks
        );
        Matrix4f hmdRotationPartial = hmdQ.get(new Matrix4f());
        Vector3f hmdDirPartial = hmdQ.transform(
                VRMathUtils.BACK_VECTOR, new Vector3f()
        );

        //main hand
        Vector3f mainHandPosPartial = prevMainHandPose.getRawPosition().lerp(
                mainHandPose.getRawPosition(), partialTicks,
                new Vector3f()
        );
        Quaternionf mainHandQ = slerpRotation(
                prevMainHandPose.getRawRotation(),
                mainHandPose.getRawRotation(),
                partialTicks
        );
        Matrix4f mainHandRotationPartial = mainHandQ.get(new Matrix4f());
        Vector3f mainHandDirPartial = mainHandQ.transform(
                VRMathUtils.BACK_VECTOR, new Vector3f()
        );

        //offhand
        Vector3f offhandPosPartial = prevOffhandPose.getRawPosition().lerp(
                offhandPose.getRawPosition(), partialTicks,
                new Vector3f()
        );
        Quaternionf offhandQ = slerpRotation(
                prevOffhandPose.getRawRotation(),
                offhandPose.getRawRotation(),
                partialTicks
        );
        Matrix4f offhandRotationPartial = offhandQ.get(new Matrix4f());
        Vector3f offhandDirPartial = offhandQ.transform(
                VRMathUtils.BACK_VECTOR, new Vector3f()
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
                worldScalePartial,
                rotationPartial
        );
    }

    private static Quaternionf slerpRotation(Matrix4fc from,
                                             Matrix4fc to,
                                             float t) {
        Quaternionf q0 = from.getNormalizedRotation(new Quaternionf());
        Quaternionf q1 = to.getNormalizedRotation(new Quaternionf());
        return q0.slerp(q1, t).normalize();
    }



    public void setOrigin(float x, float y, float z,
                          boolean reset) {
        var newOrigin = new Vector3f(x, y, z);
        if (reset) {
            this.prevPose.resetOrigin(newOrigin);
        }

        this.pose.updateModifiers(
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



    @Override
    public int getOffhandSlot() {
        return -1;
    }

    @Override
    public @NotNull HandType getActiveHand() {
        return HandType.MAIN;
    }

}
