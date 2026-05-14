package org.vmstudio.visor.api.common.network.toserver;

import net.minecraft.network.FriendlyByteBuf;
import org.vmstudio.visor.api.common.network.VisorPayloadToServer;

public record UnknownPayloadToServer() implements VisorPayloadToServer {


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

    public static UnknownPayloadToServer read(FriendlyByteBuf buffer) {

        buffer.readBytes(new byte[buffer.readableBytes()]);
        return new UnknownPayloadToServer();
    }
}
