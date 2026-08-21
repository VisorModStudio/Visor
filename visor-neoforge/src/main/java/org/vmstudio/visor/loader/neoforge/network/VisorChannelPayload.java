package org.vmstudio.visor.loader.neoforge.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * One Visor channel on the wire.
 *
 * <p>
 *     The custom payload id IS the Visor channel id (e.g. {@code visor:channel}) and the
 *     body is the channel's raw bytes (payload id byte + fields) with nothing around
 *     them - byte for byte what every Visor since 1.20.1 sent through the old
 *     {@code (channel, buffer)} networking API. VisorPlugin (Paper/Spigot/Folia),
 *     ViaVersion cross-version play and Visor servers on the other loaders all expect
 *     exactly this, so no wrapper, length prefix or shared "tunnel" payload may be
 *     introduced around it.
 * </p>
 */
public record VisorChannelPayload(Type<VisorChannelPayload> type,
                                  byte[] data) implements CustomPacketPayload {

    public static @NotNull Type<VisorChannelPayload> typeOf(@NotNull ResourceLocation channelId) {
        return new Type<>(channelId);
    }

    /**
     * Raw-bytes codec: writes the body as-is and reads everything that is left in the buffer
     */
    public static @NotNull StreamCodec<ByteBuf, VisorChannelPayload> codecOf(
            @NotNull Type<VisorChannelPayload> type) {
        return CustomPacketPayload.codec(
                (payload, buf) -> buf.writeBytes(payload.data),
                buf -> {
                    byte[] bytes = new byte[buf.readableBytes()];
                    buf.readBytes(bytes);
                    return new VisorChannelPayload(type, bytes);
                }
        );
    }

    public static @NotNull VisorChannelPayload of(@NotNull ResourceLocation channelId,
                                                  @NotNull FriendlyByteBuf buffer) {
        byte[] bytes = new byte[buffer.readableBytes()];
        buffer.readBytes(bytes);
        return new VisorChannelPayload(typeOf(channelId), bytes);
    }

    public @NotNull ResourceLocation channelId() {
        return type.id();
    }

    /**
     * Fresh buffer view over the raw bytes
     */
    public @NotNull FriendlyByteBuf toBuffer() {
        return new FriendlyByteBuf(Unpooled.wrappedBuffer(data));
    }

    @Override
    public @NotNull Type<VisorChannelPayload> type() {
        return type;
    }
}
