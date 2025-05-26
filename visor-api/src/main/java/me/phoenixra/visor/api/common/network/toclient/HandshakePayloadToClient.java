package me.phoenixra.visor.api.common.network.toclient;

import me.phoenixra.visor.api.common.network.VisorPayloadID;
import net.minecraft.network.FriendlyByteBuf;

public record HandshakePayloadToClient(int networkVersion) implements VisorPayloadToClient {


    @Override
    public void onWrite(FriendlyByteBuf buffer) {
        buffer.writeByte(this.networkVersion);
    }

    @Override
    public VisorPayloadID payloadId() {
        return VisorPayloadID.HANDSHAKE;
    }


    public static HandshakePayloadToClient read(FriendlyByteBuf buffer) {
        return new HandshakePayloadToClient(buffer.readByte() & 0xFF);
    }
}
