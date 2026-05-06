package org.vmstudio.visor.api.common.network.buffer;


import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.client.player.VRLocalPlayer;
import org.vmstudio.visor.api.client.player.pose.VRPlayerPoseClient;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import net.minecraft.network.FriendlyByteBuf;
import org.joml.Quaternionf;
import org.joml.Vector3f;


public record PoseDataBuffer(PoseElementBuffer hmd,
                             PoseElementBuffer mainHand,
                             PoseElementBuffer offhand,
                             float gunAngle) implements BufferSerializable {


    public static final float DEFAULT_GUN_ANGLE = 60.0F;

    @Override
    public void serialize(FriendlyByteBuf buffer) {
        this.hmd.serialize(buffer);
        this.mainHand.serialize(buffer);
        this.offhand.serialize(buffer);
        buffer.writeFloat(this.gunAngle);
    }


    public static PoseDataBuffer deserialize(FriendlyByteBuf byteBuf) {
        PoseElementBuffer hmd = PoseElementBuffer.deserialize(byteBuf);
        PoseElementBuffer mainHand = PoseElementBuffer.deserialize(byteBuf);
        PoseElementBuffer offhand = PoseElementBuffer.deserialize(byteBuf);
        // Defensive: stay readable against older senders that don't emit gunAngle.
        float gunAngle = byteBuf.isReadable(Float.BYTES)
                ? byteBuf.readFloat()
                : DEFAULT_GUN_ANGLE;
        return new PoseDataBuffer(hmd, mainHand, offhand, gunAngle);
    }

    /**
     * Convenience overload — uses the {@link #DEFAULT_GUN_ANGLE Quest 3 reference}.
     * Prefer {@link #create(VRLocalPlayer, float)} so each client's true gunAngle
     * reaches remote viewers and item poses look right on their hardware.
     */
    public static PoseDataBuffer create(VRLocalPlayer vrPlayer) {
        return create(vrPlayer, DEFAULT_GUN_ANGLE);
    }

    public static PoseDataBuffer create(VRLocalPlayer vrPlayer, float gunAngle) {
        return new PoseDataBuffer(
                getHmdPose(vrPlayer),
                getHandPose(vrPlayer, HandType.MAIN),
                getHandPose(vrPlayer, HandType.OFFHAND),
                gunAngle
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