package org.vmstudio.visor.loader.neoforge;


import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.vertex.PoseStack;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforgespi.language.IModFileInfo;
import net.neoforged.neoforgespi.language.ModFileScanData;
import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.api.ModLoader;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.render.RenderPipelineCallback;
import org.vmstudio.visor.api.client.render.RenderPipelineStage;
import org.vmstudio.visor.api.common.VRException;
import org.vmstudio.visor.api.common.network.VisorChannel;
import org.vmstudio.visor.api.common.network.VisorPayload;
import org.vmstudio.visor.api.common.network.VisorPayloadToClient;
import org.vmstudio.visor.api.common.network.VisorPayloadToServer;
import org.vmstudio.visor.loader.neoforge.network.ClientChannelSupport;
import org.vmstudio.visor.loader.neoforge.network.VisorChannelPayload;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;


public class NeoForgeModLoader implements ModLoader {


    private static final String NETWORK_VERSION = "1";

    private final File configFolder = FMLPaths.CONFIGDIR.get().toFile();

    private final Map<RenderPipelineStage, List<RenderPipelineCallback>> pipelineCallbacks
            = new EnumMap<>(RenderPipelineStage.class);

    private final Map<ResourceLocation, VisorChannel> networkChannels = new HashMap<>();
    /** set once RegisterPayloadHandlersEvent has run - NeoForge accepts no payload types after it */
    private boolean payloadsRegistered = false;

    private boolean levelStageListenerRegistered = false;


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
            NeoForge.EVENT_BUS.addListener(this::onRenderLevelStage);
            levelStageListenerRegistered = true;
        }
    }


    /**
     * 1.21.4: NeoForge dropped {@code RenderTarget#enableStencil()} - stencil is now a final
     * flag taken by the {@code RenderTarget(useDepth, useStencil)} constructor, which
     * visor-core cannot call because it compiles against unpatched vanilla. Report no loader
     * support so visor-core falls back to its own RenderTargetMixin, same as on Fabric.
     */
    @Override
    public boolean enableRenderTargetStencil(@NotNull RenderTarget renderTarget) {
        return false;
    }


    @Override
    public double getItemEntityReach(double baseRange, ItemStack itemStack, EquipmentSlot slot) {
        List<AttributeModifier> attributes = new ArrayList<>();
        itemStack.forEachModifier(slot, (holder, modifier) -> {
            if (holder == Attributes.ENTITY_INTERACTION_RANGE) {
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
    }

    @Override
    public @NotNull List<Class<?>> getClassesAnnotated(@NotNull Class<? extends Annotation> annotation,
                                                       @NotNull String modId,
                                                       @NotNull String packagePath) {
        List<Class<?>> result = new ArrayList<>();
        IModFileInfo info = ModList.get().getModFileById(modId);
        if (info == null) {
            return result;
        }

        ModFileScanData scanData = info.getFile().getScanResult();
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


    /**
     * NeoForge only takes payload types inside {@link RegisterPayloadHandlersEvent}, and that
     * event fires after {@code FMLLoadCompleteEvent} - where {@code AddonManagerImpl.register()}
     * runs every addon's {@code onAddonRegister()} - so by the time it fires all Visor channels
     * are in {@link #networkChannels} and {@link #registerPayloads} registers each one as its
     * own raw payload type ({@link VisorChannelPayload}). A channel created any later cannot be
     * put on the wire any more, hence the hard failure: build channels in
     * {@code VisorAddon#onAddonRegister()}, as the API documents.
     */
    @Override
    public void registerNetworkChannel(@NotNull VisorChannel channel) {
        if (payloadsRegistered) {
            throw new IllegalStateException(
                    "VisorChannel " + channel.getChannelId() + " was registered after NeoForge's "
                            + "payload registration phase; create VisorChannels in "
                            + "VisorAddon#onAddonRegister()");
        }
        networkChannels.put(channel.getChannelId(), channel);
    }

    @Override
    public @NotNull Packet<?> createPacketToClient(@NotNull ResourceLocation channelId,
                                                   @NotNull VisorPayloadToClient payload) {
        return new ClientboundCustomPayloadPacket(
                VisorChannelPayload.of(channelId, writePayload(payload)));
    }

    @Override
    public @NotNull Packet<?> createPacketToServer(@NotNull ResourceLocation channelId,
                                                   @NotNull VisorPayloadToServer payload) {
        return new ServerboundCustomPayloadPacket(
                VisorChannelPayload.of(channelId, writePayload(payload)));
    }


    @Override
    public boolean canSendToServer(@NotNull ResourceLocation channelId) {
        return ClientChannelSupport.serverAccepts(channelId);
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


    /**
     * One raw payload type per Visor channel, id = channel id, {@code optional()} so that
     * vanilla/Paper/Fabric servers and clients (which never negotiate it) still connect.
     * NeoForge 21.4 takes one handler for both directions; it branches on the packet flow.
     */
    static void registerPayloads(@NotNull RegisterPayloadHandlersEvent event) {
        if (!(ModLoader.get() instanceof NeoForgeModLoader loader)) {
            return;
        }
        PayloadRegistrar registrar = event.registrar(NETWORK_VERSION).optional();
        for (VisorChannel channel : loader.networkChannels.values()) {
            var type = VisorChannelPayload.typeOf(channel.getChannelId());
            var codec = VisorChannelPayload.codecOf(type);
            if (channel.hasPacketsToServer() && channel.hasPacketsToClient()) {
                registrar.playBidirectional(type, codec, loader::onChannelPayload);
            } else if (channel.hasPacketsToServer()) {
                registrar.playToServer(type, codec, loader::onChannelPayload);
            } else {
                registrar.playToClient(type, codec, loader::onChannelPayload);
            }
        }
        loader.payloadsRegistered = true;
    }

    private void onChannelPayload(VisorChannelPayload payload, IPayloadContext context) {
        VisorChannel channel = networkChannels.get(payload.channelId());
        if (channel == null) {
            return;
        }

        FriendlyByteBuf buffer = payload.toBuffer();
        try {
            if (context.flow() == PacketFlow.SERVERBOUND) {
                if (!channel.hasPacketsToServer()
                        || !(context.player() instanceof ServerPlayer sender)) {
                    return;
                }
                channel.handleToServer(buffer, sender,
                        response -> context.reply(
                                VisorChannelPayload.of(channel.getChannelId(), writePayload(response))
                        ));
            } else if (channel.hasPacketsToClient()) {
                channel.handleToClient(buffer);
            }
        } finally {
            buffer.release();
        }
    }

    private static @NotNull FriendlyByteBuf writePayload(@NotNull VisorPayload payload) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        payload.write(buffer);
        return buffer;
    }


    private void onRenderLevelStage(RenderLevelStageEvent event) {
        RenderPipelineStage stage = mapNeoForgeStage(event.getStage());
        if (stage == null) return;

        List<RenderPipelineCallback> callbacks = pipelineCallbacks.get(stage);
        if (callbacks == null || callbacks.isEmpty()) return;

        // Identity basis on purpose: the decoration renderers build their own camera transform
        // (see DecorationRendererImpl#runStageWithVRContract, which resets the model-view stack),
        // so seeding the view matrix here double-transforms them. Fabric has always handed over
        // LevelRenderer's fresh PoseStack, and the 1.21.4 Forge port does the same now that
        // RenderLevelStageEvent is gone - this keeps all three loaders on one contract.
        PoseStack poseStack = new PoseStack();
        float partialTicks = event.getPartialTick().getGameTimeDeltaPartialTick(true);

        for (RenderPipelineCallback callback : callbacks) {
            callback.render(poseStack, partialTicks);
        }
    }


    private static RenderPipelineStage mapNeoForgeStage(RenderLevelStageEvent.Stage neoForgeStage) {
        if (neoForgeStage == RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS) {
            return RenderPipelineStage.AFTER_SOLID;
        }
        if (neoForgeStage == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return RenderPipelineStage.AFTER_TRANSLUCENT;
        }
        if (neoForgeStage == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            return RenderPipelineStage.AFTER_WORLD;
        }
        return null;
    }
}
