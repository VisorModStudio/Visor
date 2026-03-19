package org.vmstudio.visor.api.common.network.buffer;


import com.google.common.base.Charsets;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.client.player.VRLocalPlayer;
import org.vmstudio.visor.api.client.player.pose.VRPlayerPoseClient;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import net.minecraft.network.FriendlyByteBuf;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.vmstudio.visor.api.common.network.VisorPayload;


public record PoseDataBuffer(PoseElementBuffer hmd,
                             PoseElementBuffer mainHand,
                             PoseElementBuffer offhand,
                             boolean leftHanded,
                             String bodyType) implements BufferSerializable {


    @Override
    public void serialize(FriendlyByteBuf buffer) {
        this.hmd.serialize(buffer);
        this.mainHand.serialize(buffer);
        this.offhand.serialize(buffer);
        buffer.writeBoolean(this.leftHanded);
        buffer.writeBytes(
                bodyType.getBytes(Charsets.UTF_8)
        );
    }


    public static PoseDataBuffer deserialize(FriendlyByteBuf byteBuf) {
        return new PoseDataBuffer(
                PoseElementBuffer.deserialize(byteBuf),
                PoseElementBuffer.deserialize(byteBuf),
                PoseElementBuffer.deserialize(byteBuf),
                byteBuf.readBoolean(),
                VisorPayload.readString(byteBuf)
        );
    }

    public static PoseDataBuffer create(VRLocalPlayer vrPlayer,
                                        boolean leftHanded,
                                        String bodyType) {
        return new PoseDataBuffer(
                getHmdPose(vrPlayer),
                getHandPose(vrPlayer, HandType.MAIN),
                getHandPose(vrPlayer, HandType.OFFHAND),
                leftHanded,
                bodyType
        );
    }

    private static PoseElementBuffer getHmdPose(VRLocalPlayer vrPlayer) {

        VRPlayerPoseClient postTickPose = vrPlayer
                .getPoseData(PlayerPoseType.TICK);
        var hmd = postTickPose
                .getHmd();
        var position = hmd.getPosition()
                .sub(vrPlayer.getMcPlayer().position().toVector3f(), new Vector3f());
        var orientation = hmd.getRotation()
                .getNormalizedRotation(new Quaternionf());

        return new PoseElementBuffer(position,  orientation);
    }

    private static PoseElementBuffer getHandPose(VRLocalPlayer vrPlayer,
                                                 HandType handType
    ) {
        VRPlayerPoseClient postTickPose = vrPlayer
            .getPoseData(PlayerPoseType.TICK);
        var handPose = postTickPose
                .getHand(handType);
        var position = handPose
                .getPosition()
                .sub(vrPlayer.getMcPlayer().position().toVector3f(), new Vector3f());
        var orientation = handPose
                .getRotation()
                .getNormalizedRotation(new Quaternionf());

        return new PoseElementBuffer(position, orientation);
    }


}
