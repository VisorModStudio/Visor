package org.vmstudio.visor.loader.neoforge.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;


public record VisorRawPayload(ResourceLocation channelId, byte[] data) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<VisorRawPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("visor", "tunnel"));

    public static final StreamCodec<ByteBuf, VisorRawPayload> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, VisorRawPayload::channelId,
            ByteBufCodecs.BYTE_ARRAY, VisorRawPayload::data,
            VisorRawPayload::new
    );

    public static @NotNull VisorRawPayload of(@NotNull ResourceLocation channelId,
                                              @NotNull FriendlyByteBuf buffer) {
        byte[] bytes = new byte[buffer.readableBytes()];
        buffer.readBytes(bytes);
        return new VisorRawPayload(channelId, bytes);
    }


    public @NotNull FriendlyByteBuf toBuffer() {
        return new FriendlyByteBuf(Unpooled.wrappedBuffer(data));
    }

    @Override
    public @NotNull Type<VisorRawPayload> type() {
        return TYPE;
    }
}
