package org.vmstudio.visor.api.common.network.buffer;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;

public interface BufferSerializable {
    void serialize(FriendlyByteBuf buf);

    static byte[] toBytes(BufferSerializable bufferSerializable) {
        FriendlyByteBuf tempBuffer = new FriendlyByteBuf(Unpooled.buffer());
        bufferSerializable.serialize(tempBuffer);
        byte[] out = new byte[tempBuffer.readableBytes()];
        tempBuffer.readBytes(out);
        tempBuffer.release();
        return out;
    }
}
