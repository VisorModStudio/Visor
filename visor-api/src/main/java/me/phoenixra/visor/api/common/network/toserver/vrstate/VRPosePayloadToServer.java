package me.phoenixra.visor.api.common.network.toserver.vrstate;

import me.phoenixra.visor.api.common.network.VisorPayloadID;
import me.phoenixra.visor.api.common.network.buffer.PlayerPoseBuffer;
import me.phoenixra.visor.api.common.network.toserver.VisorPayloadToServer;
import net.minecraft.network.FriendlyByteBuf;


public record VRPosePayloadToServer(PlayerPoseBuffer pose) implements VisorPayloadToServer {


    @Override
    public void onWrite(FriendlyByteBuf buffer) {
        pose.serialize(buffer);
    }

    @Override
    public VisorPayloadID payloadId() {
        return VisorPayloadID.VR_POSE;
    }


    public static VRPosePayloadToServer read(FriendlyByteBuf buffer) {
        var pose = PlayerPoseBuffer.deserialize(buffer);
        return new VRPosePayloadToServer(
                pose
        );
    }
}
