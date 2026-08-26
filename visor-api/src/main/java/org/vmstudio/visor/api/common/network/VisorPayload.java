package org.vmstudio.visor.api.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.nio.charset.StandardCharsets;

public interface VisorPayload {

    default void write(FriendlyByteBuf buffer) {
        buffer.writeByte(payloadId());
        onWrite(buffer);
    }

    void onWrite(FriendlyByteBuf buffer);

    // drains the whole remaining buffer,
    // only use as the last field of a payload
    static String readString(FriendlyByteBuf buffer){
        return buffer.readCharSequence(buffer.readableBytes(), StandardCharsets.UTF_8).toString();
    }

    byte payloadId();
}
