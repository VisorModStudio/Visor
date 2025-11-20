package me.phoenixra.visor.api.common.network.toserver;

import com.google.common.base.Charsets;
import me.phoenixra.visor.api.common.network.VisorPayload;
import me.phoenixra.visor.api.common.network.VisorPayloadID;
import net.minecraft.network.FriendlyByteBuf;

public record HandshakePayloadToServer(boolean vrActive,
                                       int networkVersion,
                                       String visorVersion) implements VisorPayloadToServer {


    @Override
    public void onWrite(FriendlyByteBuf buffer) {
        buffer.writeBoolean(this.vrActive);
        buffer.writeInt(networkVersion);
        buffer.writeBytes(
                this.visorVersion.getBytes(Charsets.UTF_8)
        );
    }

    @Override
    public VisorPayloadID payloadId() {
        return VisorPayloadID.HANDSHAKE;
    }


    public static VisorPayloadToServer read(FriendlyByteBuf buffer) {
        return new HandshakePayloadToServer(
                buffer.readBoolean(),
                buffer.readInt(),
                VisorPayload.readString(buffer)
        );
    }
}
