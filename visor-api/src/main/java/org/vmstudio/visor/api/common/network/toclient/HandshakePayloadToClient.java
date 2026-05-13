package org.vmstudio.visor.api.common.network.toclient;

import org.vmstudio.visor.api.common.network.VisorCorePayloadID;
import net.minecraft.network.FriendlyByteBuf;
import org.vmstudio.visor.api.common.network.VisorPayloadToClient;

public record HandshakePayloadToClient() implements VisorPayloadToClient {


    @Override
    public void onWrite(FriendlyByteBuf buffer) {
    }

    @Override
    public byte payloadId() {
        return (byte) VisorCorePayloadID.HANDSHAKE.ordinal();
    }



    public static HandshakePayloadToClient read(FriendlyByteBuf buffer) {
        return new HandshakePayloadToClient();
    }
}
