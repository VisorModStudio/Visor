package me.phoenixra.visor.api.common.network.toclient.vrstate;

import me.phoenixra.visor.api.common.network.VisorPayloadID;
import me.phoenixra.visor.api.common.network.buffer.PoseDataBuffer;
import me.phoenixra.visor.api.common.network.toclient.VisorPayloadToClient;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

public record VROtherStatePayloadToClient(UUID playerUUID,
                                          PoseDataBuffer pose,
                                          float worldScale,
                                          float height) implements VisorPayloadToClient {


    @Override
    public void onWrite(FriendlyByteBuf buffer) {
        buffer.writeUUID(playerUUID);
        pose.serialize(buffer);
        buffer.writeFloat(worldScale);
        buffer.writeFloat(height);
    }

    @Override
    public VisorPayloadID payloadId() {
        return VisorPayloadID.OTHER_VR_STATE;
    }


    public static VROtherStatePayloadToClient read(FriendlyByteBuf buffer) {
        UUID playerUUID = buffer.readUUID();
        var pose = PoseDataBuffer.deserialize(buffer);
        float worldScale = buffer.readFloat();
        float height = buffer.readFloat();
        return new VROtherStatePayloadToClient(
                playerUUID,
                pose,
                worldScale,
                height
        );
    }
}
