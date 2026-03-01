package org.vmstudio.visor.api.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public interface VisorPayload {

    default void write(FriendlyByteBuf buffer) {
        buffer.writeByte(payloadId().ordinal());
        onWrite(buffer);
    }

    void onWrite(FriendlyByteBuf buffer);

    static String readString(FriendlyByteBuf buffer){
        byte[] stringBytes = new byte[buffer.readableBytes()];
        buffer.readBytes(stringBytes);
        return new String(stringBytes);
    }

    VisorPayloadID payloadId();


    default ResourceLocation id() {
        return VisorNetwork.CHANNEL;
    }
}
