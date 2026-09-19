package org.vmstudio.visor.loader.neoforge;


import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.vertex.PoseStack;
//? if >=1.21.6 {
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.textures.GpuTexture;
import net.neoforged.neoforge.client.blaze3d.validation.ValidationGpuDevice;
import net.neoforged.neoforge.client.blaze3d.validation.ValidationGpuTexture;
//?}
import io.netty.buffer.Unpooled;
import net.minecraft.resources.ResourceLocation;
//? if <1.20.4 {
/*import net.neoforged.neoforge.network.NetworkRegistry;
import net.neoforged.neoforge.network.event.EventNetworkChannel;
*///?}
import org.vmstudio.visor.api.ModLoader;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.render.RenderPipelineCallback;
import org.vmstudio.visor.api.client.render.RenderPipelineStage;
import org.vmstudio.visor.api.common.VRException;
import org.vmstudio.visor.api.common.network.VisorChannel;
import org.vmstudio.visor.api.common.network.VisorPayloadToClient;
import org.vmstudio.visor.api.common.network.VisorPayloadToServer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.fml.loading.moddiscovery.ModFileInfo;
import net.neoforged.neoforgespi.language.IModFileInfo;
import net.neoforged.neoforgespi.language.ModFileScanData;
//? if >=1.20.5 {
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
//?} elif >=1.20.4 {
/*import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
*///?} else {
/*import net.neoforged.neoforge.network.INetworkDirection;
import net.neoforged.neoforge.network.PlayNetworkDirection;
*///?}
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;


public class NeoForgeModLoader implements ModLoader {
    private File configFolder = FMLPaths.CONFIGDIR.get().toFile();

    private final Map<RenderPipelineStage, List<RenderPipelineCallback>> pipelineCallbacks
            = new EnumMap<>(RenderPipelineStage.class);

    private boolean levelStageListenerRegistered = false;

    //? if >=1.20.4 {
    private final List<VisorChannel> pendingChannels = new CopyOnWriteArrayList<>();
    //?}


    @Override
    public File getConfigFolder() {
        return configFolder;
    }

    @Override
    public boolean isModLoaded(@NotNull String id) {
        return FMLLoader.getLoadingModList().getModFileById(id) != null;
    }

    @Override
    public @NotNull String getModVersion(@NotNull String id) {
        if (isModLoaded(VisorAPI.MOD_ID)) {
            return FMLLoader.getLoadingModList()
                    .getModFileById(id).versionString();
        }
        return "no version";
    }

    @Override
    public boolean isDedicatedServer() {
        return FMLEnvironment.dist == Dist.DEDICATED_SERVER;
    }


    @Override
    public void addToRenderPipeline(@NotNull RenderPipelineStage stage,
                                    @NotNull RenderPipelineCallback callback) {
        pipelineCallbacks
                .computeIfAbsent(stage, k -> new CopyOnWriteArrayList<>())
                .add(callback);

        if (!levelStageListenerRegistered) {
            //? if >=1.21.6 {
            // 21.6 split RenderLevelStageEvent into one event per stage
            NeoForge.EVENT_BUS.addListener((RenderLevelStageEvent.AfterOpaqueBlocks event) ->
                    onRenderLevelStage(event, RenderPipelineStage.AFTER_SOLID));
            NeoForge.EVENT_BUS.addListener((RenderLevelStageEvent.AfterTranslucentBlocks event) ->
                    onRenderLevelStage(event, RenderPipelineStage.AFTER_TRANSLUCENT));
            NeoForge.EVENT_BUS.addListener((RenderLevelStageEvent.AfterLevel event) ->
                    onRenderLevelStage(event, RenderPipelineStage.AFTER_WORLD));
            //?} else {
            /*NeoForge.EVENT_BUS.addListener(this::onRenderLevelStage);
            *///?}
            levelStageListenerRegistered = true;
        }
    }



    @Override
    public boolean enableRenderTargetStencil(@NotNull RenderTarget renderTarget) {
        //? if >=1.21.2 {
        // NeoForge 21.2 dropped RenderTarget.enableStencil, RenderTargetMixin attaches the stencil like on Fabric
        return false;
        //?} else {
        /*renderTarget.enableStencil();
        return true;
        *///?}
    }

    //? if >=1.21.6 {
    @Override
    public GpuDevice unwrapDevice(@NotNull GpuDevice device) {
        return B3dValidationLayer.PRESENT ? B3dValidationLayer.unwrap(device) : device;
    }

    @Override
    public GpuTexture unwrapTexture(@NotNull GpuTexture texture) {
        return B3dValidationLayer.PRESENT ? B3dValidationLayer.unwrap(texture) : texture;
    }

    // the validation layer arrived in 21.7.22-beta and its types are client-only: probed lazily, from render code only
    private static final class B3dValidationLayer {
        static final boolean PRESENT = present();

        private static boolean present() {
            try {
                Class.forName("net.neoforged.neoforge.client.blaze3d.validation.ValidationGpuDevice",
                        false, NeoForgeModLoader.class.getClassLoader());
                return true;
            } catch (ClassNotFoundException e) {
                return false;
            }
        }

        static GpuDevice unwrap(GpuDevice device) {
            return device instanceof ValidationGpuDevice validation ? validation.getRealDevice() : device;
        }

        static GpuTexture unwrap(GpuTexture texture) {
            return texture instanceof ValidationGpuTexture validation ? validation.getRealTexture() : texture;
        }
    }
    //?}

    @Override
    public double getItemEntityReach(double baseRange, ItemStack itemStack, EquipmentSlot slot) {
        //? if >=1.20.5 {
        // 1.20.5 replaced NeoForgeMod.ENTITY_REACH with vanilla ENTITY_INTERACTION_RANGE
        Collection<AttributeModifier> attributes = new ArrayList<>();
        itemStack.forEachModifier(slot, (attribute, modifier) -> {
            if (attribute == Attributes.ENTITY_INTERACTION_RANGE) {
                attributes.add(modifier);
            }
        });
        for (AttributeModifier entry : attributes) {
            if (entry.operation() == AttributeModifier.Operation.ADD_VALUE) {
                baseRange += entry.amount();
            }
        }
        double totalRange = baseRange;
        for (AttributeModifier entry : attributes) {
            if (entry.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_BASE) {
                totalRange += baseRange * entry.amount();
            }
        }
        for (AttributeModifier entry : attributes) {
            if (entry.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) {
                totalRange *= 1.0 + entry.amount();
            }
        }
        return totalRange;
        //?} else {
        /*Collection<AttributeModifier> attributes = itemStack.getAttributeModifiers(slot)
                // NeoForge exposes this as a Holder, Forge as a RegistryObject
                .get(NeoForgeMod.ENTITY_REACH.value());
        for (AttributeModifier entry : attributes) {
            if (entry.getOperation() == AttributeModifier.Operation.ADDITION) {
                baseRange += entry.getAmount();
            }
        }
        double totalRange = baseRange;
        for (AttributeModifier entry : attributes) {
            if (entry.getOperation() == AttributeModifier.Operation.MULTIPLY_BASE) {
                totalRange += baseRange * entry.getAmount();
            }
        }
        for (AttributeModifier entry : attributes) {
            if (entry.getOperation() == AttributeModifier.Operation.MULTIPLY_TOTAL) {
                totalRange *= 1.0 + entry.getAmount();
            }
        }
        return totalRange;
        *///?}
    }

    @Override
    public @NotNull List<Class<?>> getClassesAnnotated(@NotNull Class<? extends Annotation> annotation,
                                                       @NotNull String modId,
                                                       @NotNull String packagePath) {
        List<Class<?>> result = new ArrayList<>();
        IModFileInfo info = ModList.get().getModFileById(modId);
        if (!(info instanceof ModFileInfo modFileInfo)) {
            return result;
        }

        ModFileScanData scanData = modFileInfo.getFile().getScanResult();
        String annotationName = annotation.getName();

        for (var annotationData : scanData.getAnnotations()) {
            String className = annotationData.clazz().getClassName();

            if (!className.startsWith(packagePath)) {
                continue;
            }
            if (!annotationData.annotationType()
                    .getClassName().equals(annotationName)) {
                continue;
            }

            try {
                Class<?> cls = Class.forName(className, false,
                        Thread.currentThread().getContextClassLoader());
                result.add(cls);
            } catch (ClassNotFoundException e) {
                throw new VRException(e);
            }
        }

        return result;

    }

    @Override
    public void registerNetworkChannel(@NotNull VisorChannel channel) {
        //? if >=1.20.4 {
        pendingChannels.add(channel);
        //?} else {
        /*String version = String.valueOf(channel.getNetworkVersion());
        EventNetworkChannel eventChannel = NetworkRegistry.ChannelBuilder
                .named(channel.getChannelId())
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .networkProtocolVersion(() -> version)
                .eventNetworkChannel();

        eventChannel.addListener(event -> {
            FriendlyByteBuf payload = event.getPayload();
            if (payload == null) return;

            FriendlyByteBuf copy = new FriendlyByteBuf(Unpooled.buffer());
            copy.writeBytes(payload.copy());

            var context = event.getSource();
            if (context.getDirection().getOriginationSide().isClient()) {
                if (channel.hasPacketsToServer() && context.getSender() != null) {
                    var sender = context.getSender();
                    context.enqueueWork(() -> channel.handleToServer(copy, sender,
                            p -> context.getNetworkManager().send(
                                    ModLoader.get().createPacketToClient(channel.getChannelId(), p)
                            )));
                }
            } else {
                if (channel.hasPacketsToClient()) {
                    context.enqueueWork(() -> channel.handleToClient(copy));
                }
            }
            context.setPacketHandled(true);
        });
        *///?}
    }

    @Override
    public @NotNull Packet<?> createPacketToClient(@NotNull ResourceLocation channelId,
                                                   @NotNull VisorPayloadToClient payload) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        payload.write(buffer);
        //? if >=1.20.5 {
        return new ClientboundCustomPayloadPacket(RawPayload.of(channelId, buffer));
        //?} elif >=1.20.4 {
        /*return new ClientboundCustomPayloadPacket(new RawPayload(channelId, buffer));
        *///?} else {
        /*return PlayNetworkDirection.PLAY_TO_CLIENT.buildPacket(
                new INetworkDirection.PacketData(buffer, 0), channelId);
        *///?}
    }

    @Override
    public @NotNull Packet<?> createPacketToServer(@NotNull ResourceLocation channelId,
                                                   @NotNull VisorPayloadToServer payload) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        payload.write(buffer);
        //? if >=1.20.5 {
        return new ServerboundCustomPayloadPacket(RawPayload.of(channelId, buffer));
        //?} elif >=1.20.4 {
        /*return new ServerboundCustomPayloadPacket(new RawPayload(channelId, buffer));
        *///?} else {
        /*return PlayNetworkDirection.PLAY_TO_SERVER.buildPacket(
                new INetworkDirection.PacketData(buffer, 0), channelId);
        *///?}
    }

    @Override
    public boolean renderWaterOverlay(Player player, PoseStack mat) {
        return ClientHooks.renderWaterOverlay(player, mat);
    }
    @Override
    public boolean renderFireOverlay(Player player, PoseStack mat) {
        return ClientHooks.renderFireOverlay(player, mat);
    }

    @Override
    public @NotNull LoaderType getType() {
        return LoaderType.NEOFORGE;
    }


    // ----- INNER -----

    //? if >=1.20.5 {
    void registerPayloads(@NotNull RegisterPayloadHandlersEvent event) {
        for (VisorChannel channel : pendingChannels) {
            ResourceLocation channelId = channel.getChannelId();
            CustomPacketPayload.Type<RawPayload> type = new CustomPacketPayload.Type<>(channelId);
            playBidirectional(event.registrar(channelId.getNamespace()).optional(), type, RawPayload.codec(type),
                    (payload, context) -> handlePayload(channel, payload, context));
        }
        pendingChannels.clear();
    }

    private static void handlePayload(VisorChannel channel,
                                      RawPayload payload,
                                      IPayloadContext context) {
        FriendlyByteBuf buffer = payload.buffer();
        if (context.flow() == PacketFlow.SERVERBOUND) {
            if (channel.hasPacketsToServer()
                    && context.player() instanceof ServerPlayer sender) {
                context.enqueueWork(() -> channel.handleToServer(buffer, sender,
                        p -> context.reply(RawPayload.of(channel.getChannelId(), p))));
            }
        } else {
            if (channel.hasPacketsToClient()) {
                context.enqueueWork(() -> channel.handleToClient(buffer));
            }
        }
    }


    private record RawPayload(CustomPacketPayload.Type<RawPayload> type, FriendlyByteBuf buffer)
            implements CustomPacketPayload {

        private static StreamCodec<FriendlyByteBuf, RawPayload> codec(
                CustomPacketPayload.Type<RawPayload> type) {
            return CustomPacketPayload.codec(
                    (payload, target) -> target.writeBytes(payload.buffer().slice()),
                    source -> {
                        FriendlyByteBuf copy = new FriendlyByteBuf(Unpooled.buffer());
                        copy.writeBytes(source, source.readableBytes());
                        return new RawPayload(type, copy);
                    });
        }

        private static RawPayload of(ResourceLocation id, FriendlyByteBuf buffer) {
            return new RawPayload(new CustomPacketPayload.Type<>(id), buffer);
        }

        private static RawPayload of(ResourceLocation id, VisorPayloadToClient payload) {
            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
            payload.write(buffer);
            return of(id, buffer);
        }
    }
    //?} elif >=1.20.4 {
    /*void registerPayloads(@NotNull RegisterPayloadHandlerEvent event) {
        for (VisorChannel channel : pendingChannels) {
            ResourceLocation channelId = channel.getChannelId();
            event.registrar(channelId.getNamespace())
                    .optional()
                    .play(channelId,
                            buffer -> RawPayload.read(channelId, buffer),
                            (payload, context) -> handlePayload(channel, payload, context));
        }
        pendingChannels.clear();
    }

    private static void handlePayload(VisorChannel channel,
                                      RawPayload payload,
                                      PlayPayloadContext context) {
        FriendlyByteBuf buffer = payload.buffer();
        if (context.flow() == PacketFlow.SERVERBOUND) {
            if (channel.hasPacketsToServer()
                    && context.player().orElse(null) instanceof ServerPlayer sender) {
                context.workHandler().execute(() -> channel.handleToServer(buffer, sender,
                        p -> context.replyHandler().send(
                                RawPayload.of(channel.getChannelId(), p))));
            }
        } else {
            if (channel.hasPacketsToClient()) {
                context.workHandler().execute(() -> channel.handleToClient(buffer));
            }
        }
    }


    private record RawPayload(ResourceLocation id, FriendlyByteBuf buffer)
            implements CustomPacketPayload {

        private static RawPayload read(ResourceLocation id, FriendlyByteBuf source) {
            FriendlyByteBuf copy = new FriendlyByteBuf(Unpooled.buffer());
            copy.writeBytes(source, source.readableBytes());
            return new RawPayload(id, copy);
        }

        private static RawPayload of(ResourceLocation id, VisorPayloadToClient payload) {
            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
            payload.write(buffer);
            return new RawPayload(id, buffer);
        }

        @Override
        public void write(@NotNull FriendlyByteBuf target) {
            target.writeBytes(buffer.slice());
        }
    }
    *///?}

    //? if >=1.21.6 {
    // 21.7 stopped registering the client side from the bidirectional handler alone
    private static <T extends CustomPacketPayload> void playBidirectional(PayloadRegistrar registrar,
                                                                          CustomPacketPayload.Type<T> type,
                                                                          StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
                                                                          IPayloadHandler<T> handler) {
        registrar.playBidirectional(type, codec, handler, handler);
    }

    private void onRenderLevelStage(RenderLevelStageEvent event, RenderPipelineStage stage) {
        List<RenderPipelineCallback> callbacks = pipelineCallbacks.get(stage);
        if (callbacks == null || callbacks.isEmpty()) return;

        PoseStack poseStack = event.getPoseStack();
        float partialTicks = event.getPartialTick().getGameTimeDeltaPartialTick(true);

        for (RenderPipelineCallback callback : callbacks) {
            callback.render(poseStack, partialTicks);
        }
    }
    //?} elif >=1.21 {
    /*private static <T extends CustomPacketPayload> void playBidirectional(PayloadRegistrar registrar,
                                                                          CustomPacketPayload.Type<T> type,
                                                                          StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
                                                                          IPayloadHandler<T> handler) {
        registrar.playBidirectional(type, codec, handler);
    }

    private void onRenderLevelStage(RenderLevelStageEvent event) {
        RenderPipelineStage stage = mapForgeStage(event.getStage());
        if (stage == null) return;

        List<RenderPipelineCallback> callbacks = pipelineCallbacks.get(stage);
        if (callbacks == null || callbacks.isEmpty()) return;

        PoseStack poseStack = event.getPoseStack();
        float partialTicks = event.getPartialTick().getGameTimeDeltaPartialTick(true);

        for (RenderPipelineCallback callback : callbacks) {
            callback.render(poseStack, partialTicks);
        }
    }
    *///?} elif >=1.20.5 {
    /*private static <T extends CustomPacketPayload> void playBidirectional(PayloadRegistrar registrar,
                                                                          CustomPacketPayload.Type<T> type,
                                                                          StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
                                                                          IPayloadHandler<T> handler) {
        registrar.playBidirectional(type, codec, handler);
    }

    private void onRenderLevelStage(RenderLevelStageEvent event) {
        RenderPipelineStage stage = mapForgeStage(event.getStage());
        if (stage == null) return;

        List<RenderPipelineCallback> callbacks = pipelineCallbacks.get(stage);
        if (callbacks == null || callbacks.isEmpty()) return;

        PoseStack poseStack = event.getPoseStack();
        float partialTicks = event.getPartialTick();

        for (RenderPipelineCallback callback : callbacks) {
            callback.render(poseStack, partialTicks);
        }
    }
    *///?} else {
    /*private void onRenderLevelStage(RenderLevelStageEvent event) {
        RenderPipelineStage stage = mapForgeStage(event.getStage());
        if (stage == null) return;

        List<RenderPipelineCallback> callbacks = pipelineCallbacks.get(stage);
        if (callbacks == null || callbacks.isEmpty()) return;

        PoseStack poseStack = event.getPoseStack();
        float partialTicks = event.getPartialTick();

        for (RenderPipelineCallback callback : callbacks) {
            callback.render(poseStack, partialTicks);
        }
    }
    *///?}

    //? if <1.21.6 {
    /*private static RenderPipelineStage mapForgeStage(RenderLevelStageEvent.Stage forgeStage) {
        // must stay ahead of entity rendering
        if (forgeStage == RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS) {
            return RenderPipelineStage.AFTER_SOLID;
        }
        if (forgeStage == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return RenderPipelineStage.AFTER_TRANSLUCENT;
        }
        if (forgeStage == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            return RenderPipelineStage.AFTER_WORLD;
        }
        return null;
    }
    *///?}
}