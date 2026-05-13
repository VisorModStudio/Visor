package org.vmstudio.visor.api.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public interface VisorPayload {

    default void write(FriendlyByteBuf buffer) {
        buffer.writeByte(payloadId());
        onWrite(buffer);
    }

    void onWrite(FriendlyByteBuf buffer);

    static String readString(FriendlyByteBuf buffer){
        byte[] stringBytes = new byte[buffer.readableBytes()];
        buffer.readBytes(stringBytes);
        return new String(stringBytes);
    }

    byte payloadId();
}
