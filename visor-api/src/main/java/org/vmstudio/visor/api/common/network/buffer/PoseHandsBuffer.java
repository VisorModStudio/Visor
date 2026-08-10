package org.vmstudio.visor.api.common.network.buffer;

import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.vmstudio.visor.api.client.player.VRLocalPlayer;
import org.vmstudio.visor.api.client.player.pose.VRPlayerPoseClient;
import org.vmstudio.visor.api.client.settings.VRClientSettings;
import org.vmstudio.visor.api.common.player.VRHandJointType;
import org.vmstudio.visor.api.common.player.VRPoseHand;
import org.vmstudio.visor.api.server.VRServerSettings;

import java.util.ArrayList;
import java.util.List;

public record PoseHandsBuffer(@NotNull PoseHandBuffer leftHand,
                              @NotNull PoseHandBuffer rightHand) implements VRDataBuffer {

    @Override
    public void serialize(FriendlyByteBuf buffer) {
        this.leftHand.serialize(buffer);
        this.rightHand.serialize(buffer);
    }

    public static PoseHandsBuffer deserialize(FriendlyByteBuf byteBuf) {
        return new PoseHandsBuffer(
                PoseHandBuffer.deserialize(byteBuf),
                PoseHandBuffer.deserialize(byteBuf)
        );
    }

    public static PoseHandsBuffer create(VRLocalPlayer vrPlayer,
                                         VRPlayerPoseClient pose) {
        if(!VRClientSettings.isHandTrackingEnabled()
                || !VRServerSettings.isHandTrackersSupported()){
            return createEmpty();
        }
        var hands = pose.getHands();
        return new PoseHandsBuffer(
                createHand(hands.getLeftHand(), vrPlayer),
                createHand(hands.getRightHand(), vrPlayer)
        );
    }

    public static PoseHandsBuffer createEmpty() {
        return new PoseHandsBuffer(
                PoseHandBuffer.createEmpty(),
                PoseHandBuffer.createEmpty()
        );
    }

    private static PoseHandBuffer createHand(VRPoseHand hand,
                                             VRLocalPlayer vrPlayer) {
        if (!hand.isActive()) {
            return PoseHandBuffer.createEmpty();
        }
        var playerPos = vrPlayer.getMcPlayer().position().toVector3f();
        List<PoseHandJointBuffer> joints = new ArrayList<>();
        for (int i = 0; i < VRHandJointType.COUNT; i++) {
            VRHandJointType type = VRHandJointType.fromIndex(i);
            var jointPose = hand.getJoint(type);
            if (jointPose == null) {
                continue;
            }
            var position = jointPose.getPosition()
                    .sub(playerPos, new Vector3f());
            var orientation = jointPose.getRotation()
                    .getNormalizedRotation(new Quaternionf());
            joints.add(new PoseHandJointBuffer(type, position, orientation));
        }
        return new PoseHandBuffer(joints);
    }
}
