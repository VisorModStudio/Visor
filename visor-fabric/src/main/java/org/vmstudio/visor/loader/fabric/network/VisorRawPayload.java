package org.vmstudio.visor.loader.fabric.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 1.21.1: Fabric networking is CustomPacketPayload based; this wraps
 * Visor's raw channel buffers in a payload, one payload type per
 * Visor channel id.
 */
public final class VisorRawPayload implements CustomPacketPayload {

    private static final Map<ResourceLocation, Type<VisorRawPayload>> TYPES
            = new ConcurrentHashMap<>();

    private final Type<VisorRawPayload> type;
    private final byte[] data;

    public VisorRawPayload(@NotNull Type<VisorRawPayload> type, byte[] data) {
        this.type = type;
        this.data = data;
    }

    public static @NotNull Type<VisorRawPayload> typeOf(@NotNull ResourceLocation channelId) {
        return TYPES.computeIfAbsent(channelId, Type::new);
    }

    public static @NotNull StreamCodec<RegistryFriendlyByteBuf, VisorRawPayload> codecOf(
            @NotNull Type<VisorRawPayload> type
    ) {
        return CustomPacketPayload.codec(
                (payload, buffer) -> buffer.writeBytes(payload.data),
                buffer -> {
                    byte[] bytes = new byte[buffer.readableBytes()];
                    buffer.readBytes(bytes);
                    return new VisorRawPayload(type, bytes);
                }
        );
    }

    public static @NotNull VisorRawPayload of(@NotNull ResourceLocation channelId,
                                              @NotNull FriendlyByteBuf buffer) {
        byte[] bytes = new byte[buffer.readableBytes()];
        buffer.readBytes(bytes);
        return new VisorRawPayload(typeOf(channelId), bytes);
    }

    /**
     * Fresh detached buffer over the raw bytes
     */
    public @NotNull FriendlyByteBuf toBuffer() {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer(data.length));
        buffer.writeBytes(data);
        return buffer;
    }

    @Override
    public @NotNull Type<VisorRawPayload> type() {
        return type;
    }
}
