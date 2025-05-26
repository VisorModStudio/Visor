package me.phoenixra.visor.api.common.network.toclient.vrstate;

import me.phoenixra.visor.api.common.network.VisorPayloadID;
import me.phoenixra.visor.api.common.network.toclient.VisorPayloadToClient;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

public record VRActivePayloadToClient(UUID playerUUID,
                                      boolean hasVr) implements VisorPayloadToClient {


    @Override
    public void onWrite(FriendlyByteBuf buffer) {
        buffer.writeUUID(playerUUID);
        buffer.writeBoolean(hasVr);
    }

    @Override
    public VisorPayloadID payloadId() {
        return VisorPayloadID.PLAYER_VR_ACTIVE;
    }


    public static VRActivePayloadToClient read(FriendlyByteBuf buffer) {
        return new VRActivePayloadToClient(
                buffer.readUUID(),
                buffer.readBoolean()
        );
    }
}
