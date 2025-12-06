package me.phoenixra.visor.api.common.network.toclient;

import me.phoenixra.visor.api.common.network.VisorPayloadID;
import net.minecraft.network.FriendlyByteBuf;

public record HandshakePayloadToClient() implements VisorPayloadToClient {


    @Override
    public void onWrite(FriendlyByteBuf buffer) {
    }

    @Override
    public VisorPayloadID payloadId() {
        return VisorPayloadID.HANDSHAKE;
    }


    public static HandshakePayloadToClient read(FriendlyByteBuf buffer) {
        return new HandshakePayloadToClient();
    }
}
