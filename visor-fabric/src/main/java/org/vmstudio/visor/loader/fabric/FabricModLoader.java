package org.vmstudio.visor.loader.fabric;

//? if <1.21.9 {
/*import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
*///?}
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.vertex.PoseStack;
import io.netty.buffer.Unpooled;
import net.minecraft.resources.Identifier;
import org.vmstudio.visor.api.ModLoader;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.render.RenderPipelineCallback;
import org.vmstudio.visor.api.compatibility.mcversion.render.McRenderUtils;
import org.vmstudio.visor.api.client.render.RenderPipelineStage;
import org.vmstudio.visor.api.common.VRException;
import org.vmstudio.visor.api.common.network.VisorChannel;
import org.vmstudio.visor.api.common.network.VisorPayloadToClient;
import org.vmstudio.visor.api.common.network.VisorPayloadToServer;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
//? if <1.21.9 {
/*import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
*///?}
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
//? if >=1.20.5 {
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
//?}
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.*;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.annotation.Annotation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

public class FabricModLoader implements ModLoader {
    private final File configFolder = FabricLoader.getInstance()
            .getConfigDir().toFile();

    private final Map<RenderPipelineStage, List<RenderPipelineCallback>> pipelineCallbacks
            = new EnumMap<>(RenderPipelineStage.class);

    private boolean worldEventsRegistered = false;

    @Override
    public File getConfigFolder() {
        return configFolder;
    }


    @Override
    public boolean isModLoaded(@NotNull String id) {
        return FabricLoader.getInstance().isModLoaded(id);
    }
    @Override
    public @NotNull String getModVersion(@NotNull String id) {
        if (isModLoaded(VisorAPI.MOD_ID)) {
            return FabricLoader.getInstance()
                    .getModContainer(id)
                    .get().getMetadata().getVersion().getFriendlyString();
        }
        return "version not found";
    }

    @Override
    public boolean isDedicatedServer() {
        return FabricLoader.getInstance().getEnvironmentType().equals(EnvType.SERVER);
    }


    @Override
    public void addToRenderPipeline(@NotNull RenderPipelineStage stage,
                                    @NotNull RenderPipelineCallback callback) {
        pipelineCallbacks
                .computeIfAbsent(stage, k -> new CopyOnWriteArrayList<>())
                .add(callback);

        //? if <1.21.9 {
        /*if (!worldEventsRegistered) {
            // Closest equivalent of AFTER_SOLID.
            WorldRenderEvents.BEFORE_ENTITIES.register(context -> {
                fireCallbacks(RenderPipelineStage.AFTER_SOLID, visor$poseStackOf(context), visor$partialTick(context));
            });

            // AFTER_TRANSLUCENT
            WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> {
                fireCallbacks(RenderPipelineStage.AFTER_TRANSLUCENT, visor$poseStackOf(context), visor$partialTick(context));
            });

            // AFTER_WORLD
            WorldRenderEvents.END.register(context -> {
                fireCallbacks(RenderPipelineStage.AFTER_WORLD, visor$poseStackOf(context), visor$partialTick(context));
            });

            worldEventsRegistered = true;
        }
        *///?}
    }

    //? if >=1.21.9 {
    // Fabric API has no world render events on 1.21.9, the stages come from
    // FabricLevelRendererStageMixin / FabricChunkSectionsStageMixin instead
    public void fireLevelStage(RenderPipelineStage stage) {
        fireCallbacks(stage, new PoseStack(), McRenderUtils.partialTick());
    }
    //?} else {
    /*// matrixStack() is null for some events
    private static PoseStack visor$poseStackOf(WorldRenderContext context) {
        PoseStack poseStack = context.matrixStack();
        return poseStack != null ? poseStack : new PoseStack();
    }
    *///?}

    //? if >=1.21 && <1.21.9 {
    /*private static float visor$partialTick(WorldRenderContext context) {
        return context.tickCounter().getGameTimeDeltaPartialTick(true);
    }
    *///?} elif <1.21 {
    /*private static float visor$partialTick(WorldRenderContext context) {
        return context.tickDelta();
    }
    *///?}

    private void fireCallbacks(RenderPipelineStage stage, PoseStack poseStack, float partialTicks) {
        List<RenderPipelineCallback> callbacks = pipelineCallbacks.get(stage);
        if (callbacks == null || callbacks.isEmpty()) return;
        for (RenderPipelineCallback cb : callbacks) {
            cb.render(poseStack, partialTicks);
        }
    }


    @Override
    public boolean enableRenderTargetStencil(@NotNull RenderTarget renderTarget) {
        return false;
    }

    @Override
    public double getItemEntityReach(double baseRange, ItemStack itemStack, EquipmentSlot slot) {
        return baseRange;
    }


    public @NotNull List<Class<?>> getClassesAnnotated(
            @NotNull Class<? extends Annotation> annotation,
            @NotNull String modId,
            @NotNull String packagePath
    ) {
        try {
            List<Class<?>> result = new ArrayList<>();

            ModContainer container = FabricLoader.getInstance()
                    .getModContainer(modId)
                    .orElseThrow(() -> new IllegalArgumentException("Unknown mod: " + modId));

            String pkgPath = packagePath.replace('.', '/');

            for (Path root : container.getRootPaths()) {
                Path pkgRoot = root.resolve(pkgPath);
                if (!Files.exists(pkgRoot)) continue;

                try (Stream<Path> stream = Files.walk(pkgRoot)) {
                    stream
                            .filter(p -> p.getFileName().toString().endsWith(".class"))
                            .forEach(classFile -> {
                                try (InputStream in = Files.newInputStream(classFile)) {
                                    ClassReader reader = new ClassReader(in);
                                    reader.accept(new ClassVisitor(Opcodes.ASM9) {
                                        @Override
                                        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                                            String found = Type.getType(desc).getClassName();
                                            if (found.equals(annotation.getName())) {
                                                Path rel = pkgRoot.relativize(classFile);
                                                String className = packagePath + "."
                                                        + rel.toString()
                                                        .replace('/', '.')
                                                        .replace('\\', '.')
                                                        .replaceAll("\\.class$", "");
                                                try {
                                                    result.add(
                                                            Class.forName(
                                                                    className,
                                                                    false,
                                                                    Thread.currentThread().getContextClassLoader()
                                                            )
                                                    );
                                                } catch (ClassNotFoundException e) {
                                                    throw new VRException(e);
                                                }
                                            }
                                            return super.visitAnnotation(desc, visible);
                                        }
                                    }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                                } catch (IOException e) {
                                    throw new UncheckedIOException(e);
                                }
                            });
                }
            }
            return result;
        }catch (Exception e){
            throw new VRException(e);
        }
    }


    @Override
    public void registerNetworkChannel(@NotNull VisorChannel channel) {
        //? if >=1.20.5 {
        CustomPacketPayload.Type<RawPayload> type = payloadType(channel.getChannelId());
        StreamCodec<FriendlyByteBuf, RawPayload> codec = RawPayload.codec(type);
        // from 1.20.5 both sender and receiver need same
        //? if >=26.1 {
        PayloadTypeRegistry.serverboundPlay().register(type, codec);
        PayloadTypeRegistry.clientboundPlay().register(type, codec);
        //?} else {
        /*PayloadTypeRegistry.playC2S().register(type, codec);
        PayloadTypeRegistry.playS2C().register(type, codec);
        *///?}
        if (channel.hasPacketsToServer()) {
            ServerPlayNetworking.registerGlobalReceiver(type, (payload, context) ->
                    channel.handleToServer(
                            payload.buffer(),
                            context.player(),
                            p -> context.responseSender().sendPacket(
                                    ModLoader.get().createPacketToClient(channel.getChannelId(), p))
                    ));
        }
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT
                && channel.hasPacketsToClient()) {
            ClientPlayNetworking.registerGlobalReceiver(type, (payload, context) ->
                    channel.handleToClient(payload.buffer()));
        }
        //?} else {
        /*if (channel.hasPacketsToServer()) {
            ServerPlayNetworking.registerGlobalReceiver(channel.getChannelId(),
                    (server, player, handler, buffer, responseSender) -> {
                        // Copy the buffer immediately — Fabric reclaims it after this callback returns.
                        FriendlyByteBuf copy = new FriendlyByteBuf(Unpooled.buffer());
                        copy.writeBytes(buffer.copy());
                        server.execute(() -> channel.handleToServer(
                                copy, player, p -> responseSender.sendPacket(ModLoader.get().createPacketToClient(channel.getChannelId(), p))
                        ));
                    });
        }
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT
                && channel.hasPacketsToClient()) {
            ClientPlayNetworking.registerGlobalReceiver(channel.getChannelId(),
                    (client, handler, buffer, responseSender) -> {
                        FriendlyByteBuf copy = new FriendlyByteBuf(Unpooled.buffer());
                        copy.writeBytes(buffer.copy());
                        client.execute(() -> channel.handleToClient(copy));
                    });
        }
        *///?}
    }

    @Override
    public @NotNull Packet<?> createPacketToClient(@NotNull Identifier channelId,
                                                   @NotNull VisorPayloadToClient payload) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        payload.write(buffer);
        //? if >=26.1 {
        return ServerPlayNetworking.createClientboundPacket(
                new RawPayload(payloadType(channelId), buffer));
        //?} elif >=1.20.5 {
        /*return ServerPlayNetworking.createS2CPacket(
                new RawPayload(payloadType(channelId), buffer));
        *///?} else {
        /*return ServerPlayNetworking.createS2CPacket(channelId, buffer);
        *///?}
    }

    @Override
    public @NotNull Packet<?> createPacketToServer(@NotNull Identifier channelId,
                                                   @NotNull VisorPayloadToServer payload) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        payload.write(buffer);
        //? if >=26.1 {
        return ClientPlayNetworking.createServerboundPacket(
                new RawPayload(payloadType(channelId), buffer));
        //?} elif >=1.20.5 {
        /*return ClientPlayNetworking.createC2SPacket(
                new RawPayload(payloadType(channelId), buffer));
        *///?} else {
        /*return ClientPlayNetworking.createC2SPacket(channelId, buffer);
        *///?}
    }

    //? if >=1.20.5 {
    private static CustomPacketPayload.Type<RawPayload> payloadType(Identifier channelId) {
        return new CustomPacketPayload.Type<>(channelId);
    }

    private record RawPayload(CustomPacketPayload.Type<RawPayload> type, FriendlyByteBuf buffer)
            implements CustomPacketPayload {

        private static StreamCodec<FriendlyByteBuf, RawPayload> codec(
                CustomPacketPayload.Type<RawPayload> type) {
            return CustomPacketPayload.codec(
                    (payload, target) -> target.writeBytes(payload.buffer().slice()),
                    source -> {
                        // decoded off the game thread, the source buffer is reclaimed after this
                        FriendlyByteBuf copy = new FriendlyByteBuf(Unpooled.buffer());
                        copy.writeBytes(source, source.readableBytes());
                        return new RawPayload(type, copy);
                    });
        }
    }
    //?}


    @Override
    public boolean renderWaterOverlay(Player player, PoseStack mat) {
        return false;
    }

    @Override
    public boolean renderFireOverlay(Player player, PoseStack mat) {
        return false;
    }

    @Override
    public @NotNull LoaderType getType() {
        return LoaderType.FABRIC;
    }
}