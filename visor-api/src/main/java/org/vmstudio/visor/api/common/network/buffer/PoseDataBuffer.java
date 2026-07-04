package org.vmstudio.visor.api.common.network.buffer;


import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.client.player.VRLocalPlayer;
import org.vmstudio.visor.api.client.player.pose.VRPlayerPoseClient;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import net.minecraft.network.FriendlyByteBuf;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.vmstudio.visor.api.common.player.VRBodyPartType;


public record PoseDataBuffer(@NotNull PoseElementBuffer hmd,
                             @NotNull PoseElementBuffer mainHand,
                             @NotNull PoseElementBuffer offhand,
                             @NotNull PoseTrackersBuffer trackers) implements VRDataBuffer {


    @Override
    public void serialize(FriendlyByteBuf buffer) {
        this.hmd.serialize(buffer);
        this.mainHand.serialize(buffer);
        this.offhand.serialize(buffer);
        this.trackers.serialize(buffer);
    }


    public static PoseDataBuffer deserialize(FriendlyByteBuf byteBuf) {
        PoseElementBuffer hmd = PoseElementBuffer.deserialize(VRBodyPartType.HEAD, byteBuf);
        PoseElementBuffer mainHand = PoseElementBuffer.deserialize(VRBodyPartType.MAIN_HAND, byteBuf);
        PoseElementBuffer offhand = PoseElementBuffer.deserialize(VRBodyPartType.OFFHAND, byteBuf);
        PoseTrackersBuffer trackers = PoseTrackersBuffer.deserialize(byteBuf);
        return new PoseDataBuffer(hmd, mainHand, offhand, trackers);
    }

    public static PoseDataBuffer create(VRLocalPlayer vrPlayer) {
        VRPlayerPoseClient pose = vrPlayer
                .getPoseData(PlayerPoseType.TICK);
        return new PoseDataBuffer(
                createHmd(vrPlayer, pose),
                createHand(vrPlayer, pose, HandType.MAIN),
                createHand(vrPlayer, pose, HandType.OFFHAND),
                PoseTrackersBuffer.create(vrPlayer, pose)
        );
    }

    private static PoseElementBuffer createHmd(VRLocalPlayer vrPlayer,
                                               VRPlayerPoseClient pose) {

        var hmd = pose
                .getHmd();
        var position = hmd.getPosition()
                .sub(vrPlayer.getMcPlayer().position().toVector3f(), new Vector3f());
        var orientation = hmd.getRotation()
                .getNormalizedRotation(new Quaternionf());

        return new PoseElementBuffer(VRBodyPartType.HEAD, position, orientation);
    }

    private static PoseElementBuffer createHand(VRLocalPlayer vrPlayer,
                                                VRPlayerPoseClient pose,
                                                HandType handType
    ) {
        var handPose = pose
                .getHand(handType);
        var position = handPose
                .getPosition()
                .sub(vrPlayer.getMcPlayer().position().toVector3f(), new Vector3f());
        var orientation = handPose
                .getRotation()
                .getNormalizedRotation(new Quaternionf());

        return new PoseElementBuffer(handType.asBodyPart(), position, orientation);
    }



}