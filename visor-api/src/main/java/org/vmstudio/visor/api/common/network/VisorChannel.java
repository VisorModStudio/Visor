package org.vmstudio.visor.api.common.network;

import lombok.Getter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vmstudio.visor.api.ModLoader;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.common.addon.VisorAddon;

import java.util.function.Consumer;
import java.util.function.Function;

public final class VisorChannel {

    @Getter
    private final VisorAddon owner;
    @Getter
    private final ResourceLocation channelId;
    @Getter
    private final int networkVersion;

    @Nullable private final PayloadReader<VisorPayloadToServer> toServerReader;
    @Nullable private final PacketHandlerToServer<VisorPayloadToServer> toServerHandler;

    @Nullable private final PayloadReader<VisorPayloadToClient> toClientReader;
    @Nullable private final PacketHandlerToClient<VisorPayloadToClient> toClientHandler;

    private VisorChannel(Builder builder) {
        this.owner = builder.owner;
        this.channelId = builder.channelId;
        this.networkVersion = builder.networkVersion;
        this.toServerReader = builder.toServerReader;
        this.toServerHandler = builder.toServerHandler;
        this.toClientReader = builder.toClientReader;
        this.toClientHandler = builder.toClientHandler;
    }

    public boolean hasPacketsToServer() {
        return toServerReader != null;
    }
    public boolean hasPacketsToClient() {
        return toClientReader != null;
    }


    @Environment(EnvType.CLIENT)
    public void handleToClient(@NotNull FriendlyByteBuf buffer) {
        if (toClientReader == null || toClientHandler == null) return;
        byte payloadId;
        try {
            payloadId = buffer.readByte();
        }catch (IndexOutOfBoundsException e){
            VisorAPI.client().getLogger().error(
                    "VisorChannel '"+channelId+"': Unidentified payload received on client"
            );
            return;
        }
        VisorPayloadToClient payload = toClientReader.read(payloadId, buffer);
        if (payload == null) {
            VisorAPI.client().getLogger().error(
                    "VisorChannel '"+channelId+"': Got unexpected payload identifier on client: {}", payloadId
            );
            return;
        }
        toClientHandler.handle(payload);
    }

    public void handleToServer(@NotNull FriendlyByteBuf buffer,
                               @NotNull ServerPlayer sender,
                               @NotNull Consumer<VisorPayloadToClient> responseSender) {
        if (toServerReader == null || toServerHandler == null) return;
        byte payloadId;
        try {
            payloadId = buffer.readByte();
        }catch (IndexOutOfBoundsException e){
            VisorAPI.server().getLogger().error(
                    "VisorChannel '"+channelId+"': Unidentified payload received on server"
            );
            return;
        }
        VisorPayloadToServer payload = toServerReader.read(payloadId, buffer);
        if (payload == null) {
            VisorAPI.server().getLogger().error(
                    "VisorChannel '"+channelId+"': Got unexpected payload identifier on server: {}", payloadId
            );
            return;
        }
        toServerHandler.handle(payload, sender, responseSender);
    }

    // CLIENT -> SERVER

    @Environment(EnvType.CLIENT)
    public void sendToServer(@NotNull VisorPayloadToServer payload) {
        if (Minecraft.getInstance().getConnection() == null) return;
        Minecraft.getInstance().getConnection().send(
                ModLoader.get().createPacketToServer(channelId, payload));
    }



    // SERVER -> CLIENT

    public void sendToClient(@NotNull ServerPlayer player,
                             @NotNull VisorPayloadToClient payload) {
        player.connection.send(ModLoader.get().createPacketToClient(channelId, payload));
    }


    public void sendToAllClients(@NotNull MinecraftServer server,
                                 @NotNull VisorPayloadToClient payload) {
        var packet = ModLoader.get().createPacketToClient(channelId, payload);
        server.getPlayerList().getPlayers().forEach(p -> p.connection.send(packet));
    }
    public void sendToAllClientsFiltered(@NotNull MinecraftServer server,
                                         @NotNull VisorPayloadToClient payload,
                                         @NotNull Function<ServerPlayer, Boolean> filter) {
        var packet = ModLoader.get().createPacketToClient(channelId, payload);
        server.getPlayerList().getPlayers().forEach(p -> {
            if(filter.apply(p)) {
                p.connection.send(packet);
            }
        });
    }


    public void sendToClientTracking(@NotNull Entity entity,
                                     @NotNull VisorPayloadToClient payload) {
        if (!(entity.level() instanceof ServerLevel sl)) return;
        var packet = ModLoader.get().createPacketToClient(channelId, payload);
        sl.getChunkSource().chunkMap.getPlayers(entity.chunkPosition(), false)
                .forEach(p -> p.connection.send(packet));
    }
    public void sendToClientTrackingFiltered(@NotNull Entity entity,
                                             @NotNull VisorPayloadToClient payload,
                                             @NotNull Function<ServerPlayer, Boolean> filter) {
        if (!(entity.level() instanceof ServerLevel sl)) return;
        var packet = ModLoader.get().createPacketToClient(channelId, payload);
        sl.getChunkSource().chunkMap.getPlayers(entity.chunkPosition(), false)
                .forEach(p -> {
                    if(filter.apply(p)) {
                        p.connection.send(packet);
                    }
                });
    }



    public static @NotNull Builder builder(@NotNull VisorAddon owner,
                                           @NotNull ResourceLocation id,
                                           int networkVersion) {
        return new Builder(owner, id, networkVersion);
    }

    public static final class Builder {
        private final VisorAddon owner;
        private final ResourceLocation channelId;
        private final int networkVersion;

        @Nullable private PayloadReader<VisorPayloadToServer> toServerReader;
        @Nullable private VisorChannel.PacketHandlerToServer<VisorPayloadToServer> toServerHandler;

        @Nullable private PayloadReader<VisorPayloadToClient> toClientReader;
        @Nullable private VisorChannel.PacketHandlerToClient<VisorPayloadToClient> toClientHandler;

        private Builder(VisorAddon owner, ResourceLocation channelId, int networkVersion) {
            this.owner = owner;
            this.channelId = channelId;
            this.networkVersion = networkVersion;
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        public <T extends VisorPayloadToServer> @NotNull Builder toServer(
                @NotNull PayloadReader<T> reader,
                @NotNull VisorChannel.PacketHandlerToServer<T> handler) {
            this.toServerReader = (PayloadReader) reader;
            this.toServerHandler = (PacketHandlerToServer) handler;
            return this;
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        public <T extends VisorPayloadToClient> @NotNull Builder toClient(
                @NotNull PayloadReader<T> reader,
                @NotNull VisorChannel.PacketHandlerToClient<T> handler) {
            this.toClientReader = (PayloadReader) reader;
            this.toClientHandler = (PacketHandlerToClient) handler;
            return this;
        }

        public @NotNull VisorChannel build() {
            if (toServerReader == null && toClientReader == null) {
                throw new IllegalStateException(
                        "VisorAddonChannel " + channelId + " must declare at least one direction");
            }
            return new VisorChannel(this);
        }
    }

    @FunctionalInterface
    public interface PayloadReader<T extends VisorPayload> {
        T read(byte payloadId, FriendlyByteBuf buffer);
    }


    @FunctionalInterface
    public interface PacketHandlerToServer<T extends VisorPayloadToServer>{

        void handle(T payload,
                    ServerPlayer sender,
                    Consumer<VisorPayloadToClient> responseSender);
    }

    @FunctionalInterface
    public interface PacketHandlerToClient<T extends VisorPayloadToClient> {

        void handle(T t);
    }
}