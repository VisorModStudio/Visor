package org.vmstudio.visor.loader.forge;


import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.vertex.PoseStack;
import io.netty.buffer.Unpooled;
import net.minecraft.resources.Identifier;
//? if >=1.20.2 {
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.EventNetworkChannel;
//?} else {
/*import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.event.EventNetworkChannel;
*///?}
import org.vmstudio.visor.api.ModLoader;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.render.RenderPipelineCallback;
import org.vmstudio.visor.api.client.render.RenderPipelineStage;
import org.vmstudio.visor.api.common.VRException;
import org.vmstudio.visor.api.common.network.VisorChannel;
import org.vmstudio.visor.api.common.network.VisorPayloadToClient;
import org.vmstudio.visor.api.common.network.VisorPayloadToServer;
//? if >=1.21 && <1.21.2 {
/*import net.minecraft.client.Minecraft;
*///?}
//? if >=1.21.9 {
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketListener;
import net.minecraft.network.PacketProcessor;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.PacketType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.concurrent.RejectedExecutionException;
//?}
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ForgeHooksClient;
//? if >=1.21.2 {
import org.vmstudio.visor.api.compatibility.mcversion.render.McRenderUtils;
//?} else {
/*import net.minecraftforge.client.event.RenderLevelStageEvent;
*///?}
import net.minecraftforge.common.ForgeMod;
//? if <1.21.2 {
/*import net.minecraftforge.common.MinecraftForge;
*///?}
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo;
import net.minecraftforge.forgespi.language.IModFileInfo;
import net.minecraftforge.forgespi.language.ModFileScanData;
import net.minecraftforge.network.NetworkDirection;
//? if >=1.20.5 {
import net.minecraft.world.entity.ai.attributes.Attributes;
import java.util.concurrent.ConcurrentHashMap;
//?}
//? if <1.20.2 {
/*import org.apache.commons.lang3.tuple.ImmutablePair;
*///?}
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;


public class ForgeModLoader implements ModLoader {
    private File configFolder = FMLPaths.CONFIGDIR.get().toFile();

    private final Map<RenderPipelineStage, List<RenderPipelineCallback>> pipelineCallbacks
            = new EnumMap<>(RenderPipelineStage.class);

    //? if <1.21.2 {
    /*private boolean levelStageListenerRegistered = false;
    *///?}

    //? if >=1.20.5 {
    private final Map<Identifier, EventNetworkChannel> networkChannels = new ConcurrentHashMap<>();
    //?}


    @Override
    public File getConfigFolder() {
        return configFolder;
    }

    @Override
    public boolean isModLoaded(@NotNull String id) {
        //? if >=26.1 {
        return net.minecraftforge.fml.loading.LoadingModList.getModFileById(id) != null;
        //?} else {
        /*return FMLLoader.getLoadingModList().getModFileById(id) != null;
        *///?}
    }

    @Override
    public @NotNull String getModVersion(@NotNull String id) {
        if (isModLoaded(VisorAPI.MOD_ID)) {
            //? if >=26.1 {
            return net.minecraftforge.fml.loading.LoadingModList
                    .getModFileById(id).versionString();
            //?} else {
            /*return FMLLoader.getLoadingModList()
                    .getModFileById(id).versionString();
            *///?}
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

        //? if <1.21.2 {
        /*if (!levelStageListenerRegistered) {
            MinecraftForge.EVENT_BUS.addListener(this::onRenderLevelStage);
            levelStageListenerRegistered = true;
        }
        *///?}
    }



    @Override
    public boolean enableRenderTargetStencil(@NotNull RenderTarget renderTarget) {
        renderTarget.enableStencil();
        return true;
    }

    @Override
    public double getItemEntityReach(double baseRange, ItemStack itemStack, EquipmentSlot slot) {
        //? if >=1.20.5 {
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
                .get(ForgeMod.ENTITY_REACH.get());
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
        //? if >=26.1 {
        IModFileInfo info = ModList.getModFileById(modId);
        //?} else {
        /*IModFileInfo info = ModList.get().getModFileById(modId);
        *///?}
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
        //? if >=1.20.2 {
        EventNetworkChannel eventChannel = ChannelBuilder
                .named(channel.getChannelId())
                .clientAcceptedVersions((status, version) -> true)
                .serverAcceptedVersions((status, version) -> true)
                .networkProtocolVersion(channel.getNetworkVersion())
                .eventNetworkChannel();
        //? if >=1.20.5 {
        networkChannels.put(channel.getChannelId(), eventChannel);
        //?}

        eventChannel.addListener(event -> {
            FriendlyByteBuf payload = event.getPayload();
            if (payload == null) return;

            FriendlyByteBuf copy = new FriendlyByteBuf(Unpooled.buffer());
            copy.writeBytes(payload.copy());

            var context = event.getSource();
            if (context.isServerSide()) {
                if (channel.hasPacketsToServer() && context.getSender() != null) {
                    var sender = context.getSender();
                    enqueuePayloadWork(context, channel.getChannelId(), () -> channel.handleToServer(copy, sender,
                            p -> context.getConnection().send(
                                    ModLoader.get().createPacketToClient(channel.getChannelId(), p)
                            )));
                }
            } else {
                if (channel.hasPacketsToClient()) {
                    enqueuePayloadWork(context, channel.getChannelId(), () -> channel.handleToClient(copy));
                }
            }
            context.setPacketHandled(true);
        });
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

            var context = event.getSource().get();
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
    public @NotNull Packet<?> createPacketToClient(@NotNull Identifier channelId,
                                                   @NotNull VisorPayloadToClient payload) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        payload.write(buffer);
        //? if >=1.20.5 {
        return NetworkDirection.PLAY_TO_CLIENT.buildPacket(networkChannel(channelId), buffer);
        //?} elif >=1.20.2 {
        /*return NetworkDirection.PLAY_TO_CLIENT.buildPacket(buffer, channelId).getThis();
        *///?} else {
        /*return NetworkDirection.PLAY_TO_CLIENT.buildPacket(new ImmutablePair<>(buffer, 0), channelId).getThis();
        *///?}
    }

    @Override
    public @NotNull Packet<?> createPacketToServer(@NotNull Identifier channelId,
                                                   @NotNull VisorPayloadToServer payload) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        payload.write(buffer);
        //? if >=1.20.5 {
        return NetworkDirection.PLAY_TO_SERVER.buildPacket(networkChannel(channelId), buffer);
        //?} elif >=1.20.2 {
        /*return NetworkDirection.PLAY_TO_SERVER.buildPacket(buffer, channelId).getThis();
        *///?} else {
        /*return NetworkDirection.PLAY_TO_SERVER.buildPacket(new ImmutablePair<>(buffer, 0), channelId).getThis();
        *///?}
    }

    @Override
    public boolean renderWaterOverlay(Player player, PoseStack mat) {
        //? if >=26.3 {
        return ClientOverlays.water(player, mat);
        //?} else {
        /*return ForgeHooksClient.renderWaterOverlay(player, mat);
        *///?}
    }
    @Override
    public boolean renderFireOverlay(Player player, PoseStack mat) {
        //? if >=26.3 {
        return ClientOverlays.fire(player, mat);
        //?} else {
        /*return ForgeHooksClient.renderFireOverlay(player, mat);
        *///?}
    }

    //? if >=26.3 {
    // the 26.3 overlay hooks take client-only types: a holder that only a client ever loads
    private static final class ClientOverlays {
        static boolean water(Player player, PoseStack mat) {
            return submit(ForgeHooksClient.renderWaterOverlay(player,
                    Minecraft.getInstance().gameRenderer.gameRenderState().levelRenderState.playerRenderState), mat);
        }

        static boolean fire(Player player, PoseStack mat) {
            return submit(ForgeHooksClient.renderFireOverlay(player), mat);
        }

        private static boolean submit(net.minecraftforge.client.IRenderCallback renderer, PoseStack mat) {
            if (renderer == null) {
                return false;
            }
            renderer.render(mat, org.vmstudio.visor.api.compatibility.mcversion.render.McFeatureRenderer.collector());
            return true;
        }
    }
    //?}

    @Override
    public @NotNull LoaderType getType() {
        return LoaderType.FORGE;
    }


    // ----- INNER -----

    //? if >=1.20.5 {
    private EventNetworkChannel networkChannel(Identifier channelId) {
        EventNetworkChannel channel = networkChannels.get(channelId);
        if (channel == null) {
            throw new IllegalStateException("No Visor network channel registered for " + channelId);
        }
        return channel;
    }
    //?}

    //? if >=1.21.9 {
    private static void enqueuePayloadWork(CustomPayloadEvent.Context context, Identifier channelId, Runnable work) {
        PacketListener listener = context.getConnection().getPacketListener();
        if (listener == null) {
            context.enqueueWork(work);
            return;
        }
        PacketProcessor processor = context.isServerSide()
                ? context.getSender().level().getServer().packetProcessor()
                : Minecraft.getInstance().packetProcessor();
        if (processor.isSameThread()) {
            work.run();
            return;
        }
        PacketFlow flow = context.isServerSide() ? PacketFlow.SERVERBOUND : PacketFlow.CLIENTBOUND;
        try {
            processor.scheduleIfPossible(listener, new QueuedPayload(new PacketType<>(flow, channelId), work));
        } catch (RejectedExecutionException ignored) {
            // shutting down
        }
    }

    private record QueuedPayload(PacketType<QueuedPayload> type, Runnable work) implements Packet<PacketListener> {
        private static final Logger LOGGER = LogManager.getLogger(VisorAPI.MOD_NAME);

        @Override
        public void handle(PacketListener listener) {
            // enqueueWork swallowed handler errors, from here they would crash the server or kick the client
            try {
                work.run();
            } catch (Exception e) {
                LOGGER.error("Failed to handle a {} payload", type.id(), e);
            }
        }
    }
    //?} elif >=1.20.2 {
    /*private static void enqueuePayloadWork(CustomPayloadEvent.Context context, ResourceLocation channelId, Runnable work) {
        context.enqueueWork(work);
    }
    *///?}

    //? if >=1.21.2 {
    // Forge 53 dropped RenderLevelStageEvent, ForgeLevelRendererStageMixin fires the stages from LevelRenderer
    //? if >=26.3 {
    @Override
    //?}
    public void fireLevelStage(RenderPipelineStage stage) {
        fireCallbacks(stage, new PoseStack(), McRenderUtils.partialTick());
    }
    //?} else {
    /*private void onRenderLevelStage(RenderLevelStageEvent event) {
        RenderPipelineStage stage = mapForgeStage(event.getStage());
        if (stage == null) return;
        fireCallbacks(stage, levelStagePoseStack(event), levelStagePartialTick(event));
    }

    private static RenderPipelineStage mapForgeStage(RenderLevelStageEvent.Stage forgeStage) {
        // must stay ahead of entity rendering:
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

    //? if >=1.20.5 && <1.21.2 {
    
    /*private static PoseStack levelStagePoseStack(RenderLevelStageEvent event) {
        return new PoseStack();
    }
    *///?} elif <1.20.5 {
    /*private static PoseStack levelStagePoseStack(RenderLevelStageEvent event) {
        return event.getPoseStack();
    }
    *///?}

    //? if >=1.21 && <1.21.2 {
    /*private static float levelStagePartialTick(RenderLevelStageEvent event) {
        return Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
    }
    *///?} elif <1.21 {
    /*private static float levelStagePartialTick(RenderLevelStageEvent event) {
        return event.getPartialTick();
    }
    *///?}

    private void fireCallbacks(RenderPipelineStage stage, PoseStack poseStack, float partialTicks) {
        List<RenderPipelineCallback> callbacks = pipelineCallbacks.get(stage);
        if (callbacks == null || callbacks.isEmpty()) return;

        for (RenderPipelineCallback callback : callbacks) {
            callback.render(poseStack, partialTicks);
        }
    }
}