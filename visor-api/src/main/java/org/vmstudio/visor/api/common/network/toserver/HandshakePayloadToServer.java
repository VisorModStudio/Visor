package org.vmstudio.visor.api.common.network.toserver;

import org.vmstudio.visor.api.common.network.VisorPayloadToServer;
import org.vmstudio.visor.api.common.network.VisorCorePayloadID;
import net.minecraft.network.FriendlyByteBuf;
import com.google.common.base.Charsets;
import org.vmstudio.visor.api.common.network.VisorPayload;

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
    public byte payloadId() {
        return VisorCorePayloadID.HANDSHAKE.byteOrdinal();
    }



    public static VisorPayloadToServer read(FriendlyByteBuf buffer) {
        return new HandshakePayloadToServer(
                buffer.readBoolean(),
                buffer.readInt(),
                VisorPayload.readString(buffer)
        );
    }
}
