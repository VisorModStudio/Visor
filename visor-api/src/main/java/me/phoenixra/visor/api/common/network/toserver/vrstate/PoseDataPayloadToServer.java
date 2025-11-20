package me.phoenixra.visor.api.common.network.toserver.vrstate;

import me.phoenixra.visor.api.common.network.VisorPayloadID;
import me.phoenixra.visor.api.common.network.buffer.PlayerPoseBuffer;
import me.phoenixra.visor.api.common.network.toserver.VisorPayloadToServer;
import net.minecraft.network.FriendlyByteBuf;


public record PoseDataPayloadToServer(PlayerPoseBuffer pose) implements VisorPayloadToServer {


    @Override
    public void onWrite(FriendlyByteBuf buffer) {
        pose.serialize(buffer);
    }

    @Override
    public VisorPayloadID payloadId() {
        return VisorPayloadID.POSE_DATA;
    }


    public static PoseDataPayloadToServer read(FriendlyByteBuf buffer) {
        var pose = PlayerPoseBuffer.deserialize(buffer);
        return new PoseDataPayloadToServer(
                pose
        );
    }
}
