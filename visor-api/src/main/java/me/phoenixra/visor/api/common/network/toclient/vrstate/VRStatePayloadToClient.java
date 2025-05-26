package me.phoenixra.visor.api.common.network.toclient.vrstate;

import me.phoenixra.visor.api.common.network.VisorPayloadID;
import me.phoenixra.visor.api.common.network.buffer.PlayerPoseBuffer;
import me.phoenixra.visor.api.common.network.toclient.VisorPayloadToClient;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

public record VRStatePayloadToClient(UUID playerUUID,
                                     PlayerPoseBuffer pose,
                                     float worldScale,
                                     float heightScale) implements VisorPayloadToClient {


    @Override
    public void onWrite(FriendlyByteBuf buffer) {
        buffer.writeUUID(playerUUID);
        pose.serialize(buffer);
        buffer.writeFloat(worldScale);
        buffer.writeFloat(heightScale);
    }

    @Override
    public VisorPayloadID payloadId() {
        return VisorPayloadID.PLAYER_VR_STATE;
    }


    public static VRStatePayloadToClient read(FriendlyByteBuf buffer) {
        UUID playerUUID = buffer.readUUID();
        var pose = PlayerPoseBuffer.deserialize(buffer);
        float worldScale = buffer.readFloat();
        float height = buffer.readFloat();
        return new VRStatePayloadToClient(
                playerUUID,
                pose,
                worldScale,
                height
        );
    }
}
