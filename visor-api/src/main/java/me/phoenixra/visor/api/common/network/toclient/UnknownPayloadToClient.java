package me.phoenixra.visor.api.common.network.toclient;

import me.phoenixra.visor.api.common.network.VisorPayloadID;
import net.minecraft.network.FriendlyByteBuf;

public record UnknownPayloadToClient() implements VisorPayloadToClient {


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

    public static UnknownPayloadToClient read(FriendlyByteBuf buffer) {

        buffer.readBytes(new byte[buffer.readableBytes()]);
        return new UnknownPayloadToClient();
    }
}
