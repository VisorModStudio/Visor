package org.vmstudio.visor.api.common.network.toclient;


import net.minecraft.network.FriendlyByteBuf;
import org.vmstudio.visor.api.common.network.VisorPayloadToClient;

public record UnknownPayloadToClient() implements VisorPayloadToClient {


    @Override
    public void write(FriendlyByteBuf buffer) {
        //empty
    }

    @Override
    public void onWrite(FriendlyByteBuf buffer) {

    }

    @Override
    public byte payloadId() {
        return Byte.MIN_VALUE;
    }

    public static UnknownPayloadToClient read(FriendlyByteBuf buffer) {

        buffer.readBytes(new byte[buffer.readableBytes()]);
        return new UnknownPayloadToClient();
    }
}
