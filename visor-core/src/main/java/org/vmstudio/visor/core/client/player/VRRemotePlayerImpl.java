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
import org.vmstudio.visor.api.common.player.VRPlayer;
import org.vmstudio.visor.api.common.utils.VRMathUtils;
import org.vmstudio.visor.core.client.player.body.VRBodyTypeHandsOnly;
import org.vmstudio.visor.core.client.player.pose.RemotePlayerPose;
import org.vmstudio.visor.core.common.player.PoseHistoryImpl;
import net.minecraft.client.player.RemotePlayer;
import org.jetbrains.annotations.NotNull;

public class VRRemotePlayerImpl implements VRRemotePlayer {
    private final RemotePlayerPose playerRelativePose;

    private final RemotePlayerPose prevPose;
    private final RemotePlayerPose pose;
    private final RemotePlayerPose renderPose;

    @Getter
    private final PoseHistoryImpl poseHistoryRelative;
    @Getter
    private final PoseHistoryImpl poseHistoryTick;

    @Getter
    private RemotePlayer mcPlayer;

    @Getter @Setter
    private VRBodyType bodyType;

    @Getter
    private PoseDataBuffer poseBufferReceived;

    private float worldScaleReceived;

    @Getter
    private float fullHeight;
    @Getter
    private boolean leftHanded;

    @Getter
    private float gunAngle = VRPlayer.DEFAULT_GUN_ANGLE;
    @Getter @Setter
    private boolean overlayFocused;

    public VRRemotePlayerImpl(RemotePlayer mcPlayer,
                              PoseDataBuffer poseBuffer) {
        this.mcPlayer = mcPlayer;
        this.poseBufferReceived = poseBuffer;
        this.worldScaleReceived = 1.0f;
        this.fullHeight = VRPlayer.DEFAULT_FULL_HEIGHT;

        this.bodyType = VRBodyTypeHandsOnly.getInstance();

        this.playerRelativePose = new RemotePlayerPose(this, PlayerPoseType.RELATIVE);
        this.playerRelativePose.update(poseBuffer, VRMathUtils.ZERO_VECTOR, 1.0f);

        this.prevPose = new RemotePlayerPose(this, PlayerPoseType.PREV_TICK);
        this.prevPose.update(poseBuffer, VRMathUtils.ZERO_VECTOR, 1.0f);

        this.pose = new RemotePlayerPose(this, PlayerPoseType.TICK);
        this.pose.update(poseBuffer, VRMathUtils.ZERO_VECTOR, 1.0f);

        this.renderPose  = new RemotePlayerPose(this, PlayerPoseType.RENDER);
        this.renderPose.update(poseBuffer, VRMathUtils.ZERO_VECTOR, 1.0f);

        this.poseHistoryRelative = new PoseHistoryImpl(playerRelativePose);
        this.poseHistoryTick = new PoseHistoryImpl(pose);

    }


    public void receivedPosePacked(RemotePlayer mcPlayer,
                                   PoseDataBuffer poseBuffer){
        this.mcPlayer = mcPlayer;
        this.poseBufferReceived = poseBuffer;
        this.playerRelativePose.setMcPlayer(mcPlayer);
        this.prevPose.setMcPlayer(mcPlayer);
        this.pose.setMcPlayer(mcPlayer);
        this.renderPose.setMcPlayer(mcPlayer);
    }
    public void receivedLeftHandedPacket(boolean leftHanded){
        this.leftHanded = leftHanded;
    }
    public void receivedBodyTypePacket(String vrBodyTypeId){
        var registry = VisorAPI.addonManager().getRegistries()
                .vrBodyTypes();
        var newBodyType = registry.getComponent(vrBodyTypeId);
        if(newBodyType == null){
            newBodyType = VRBodyType.FALLBACK_BODY_TYPE;
        }
        this.bodyType = newBodyType;

        this.prevPose.bodyTypeChanged(bodyType);
        this.pose.bodyTypeChanged(bodyType);
        this.renderPose.bodyTypeChanged(bodyType);

        this.poseHistoryRelative.clear();
        this.poseHistoryTick.clear();

        if(newBodyType != bodyType) {
            VisorAPI.eventBus().callEvent(
                    new BodyChangedVREvent(this, bodyType)
            );
        }
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

        this.playerRelativePose.update(
                poseBufferReceived,
                VRMathUtils.ZERO_VECTOR,
                1.0f
        );

        var historyEntry = new RemotePlayerPose(this, PlayerPoseType.RELATIVE);
        historyEntry.copyFrom(playerRelativePose);
        poseHistoryRelative.addEntry(historyEntry);

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
                worldScalePartial
        );
    }

    private static Quaternionf slerpRotation(Matrix4fc from,
                                             Matrix4fc to,
                                             float t) {
        Quaternionf q0 = from.getNormalizedRotation(new Quaternionf());
        Quaternionf q1 = to.getNormalizedRotation(new Quaternionf());
        // JOML slerp picks the shortest arc (handles dot<0) and
        // falls back to nlerp when the rotations are near-parallel.
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
            default -> playerRelativePose;
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
                this.playerRelativePose,
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

    public void receivedGuiStatePacket(boolean opened) {
        this.overlayFocused = opened;
    }
}
