package me.phoenixra.visor.api.common.network.toserver;

import me.phoenixra.visor.api.common.network.VisorPayloadID;
import net.minecraft.network.FriendlyByteBuf;

public record UnknownPayloadToServer() implements VisorPayloadToServer {


    @Override
    public void write(FriendlyByteBuf buffer) {
        //empty
    }

    @Override
    public void onWrite(FriendlyByteBuf buffer) {

    }

    @Override
    public VisorPayloadID payloadId() {
        return null;
    }

    public static UnknownPayloadToServer read(FriendlyByteBuf buffer) {

        buffer.readBytes(new byte[buffer.readableBytes()]);
        return new UnknownPayloadToServer();
    }
}
