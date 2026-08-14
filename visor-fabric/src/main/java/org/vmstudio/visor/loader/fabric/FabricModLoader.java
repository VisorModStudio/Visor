package org.vmstudio.visor.loader.fabric;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.vertex.PoseStack;
import io.netty.buffer.Unpooled;
import net.minecraft.resources.ResourceLocation;
import org.vmstudio.visor.api.ModLoader;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.render.RenderPipelineCallback;
import org.vmstudio.visor.api.client.render.RenderPipelineStage;
import org.vmstudio.visor.api.common.VRException;
import org.vmstudio.visor.api.common.network.VisorChannel;
import org.vmstudio.visor.api.common.network.VisorPayloadToClient;
import org.vmstudio.visor.api.common.network.VisorPayloadToServer;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import org.vmstudio.visor.loader.fabric.network.VisorRawPayload;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
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

        if (!worldEventsRegistered) {

            // Closest equivalent of AFTER_SOLID
            WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register((context, hitResult) -> {
                fireCallbacks(RenderPipelineStage.AFTER_SOLID, context.matrixStack(),
                        context.tickCounter().getGameTimeDeltaPartialTick(true));
                return true; // don't cancel block outline
            });

            // AFTER_TRANSLUCENT
            WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> {
                fireCallbacks(RenderPipelineStage.AFTER_TRANSLUCENT, context.matrixStack(),
                        context.tickCounter().getGameTimeDeltaPartialTick(true));
            });

            // AFTER_WORLD
            WorldRenderEvents.END.register(context -> {
                fireCallbacks(RenderPipelineStage.AFTER_WORLD, context.matrixStack(),
                        context.tickCounter().getGameTimeDeltaPartialTick(true));
            });

            worldEventsRegistered = true;
        }
    }

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


    /**
     * 1.21.1: Fabric networking is payload-typed; Visor's raw buffers are
     * wrapped in a VisorRawPayload per channel. Codecs must be registered
     * for every direction actually used, and play handlers already run on
     * the game thread.
     */
    @Override
    public void registerNetworkChannel(@NotNull VisorChannel channel) {
        var type = VisorRawPayload.typeOf(channel.getChannelId());
        var codec = VisorRawPayload.codecOf(type);
        if (channel.hasPacketsToServer()) {
            PayloadTypeRegistry.playC2S().register(type, codec);
            ServerPlayNetworking.registerGlobalReceiver(type,
                    (payload, context) -> channel.handleToServer(
                            payload.toBuffer(),
                            context.player(),
                            p -> context.responseSender().sendPacket(
                                    ModLoader.get().createPacketToClient(channel.getChannelId(), p)
                            )
                    ));
        }
        if (channel.hasPacketsToClient()) {
            PayloadTypeRegistry.playS2C().register(type, codec);
            if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
                ClientPlayNetworking.registerGlobalReceiver(type,
                        (payload, context) -> channel.handleToClient(payload.toBuffer()));
            }
        }
    }

    @Override
    public @NotNull Packet<?> createPacketToClient(@NotNull ResourceLocation channelId,
                                                   @NotNull VisorPayloadToClient payload) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        payload.write(buffer);
        return ServerPlayNetworking.createS2CPacket(VisorRawPayload.of(channelId, buffer));
    }

    @Override
    public @NotNull Packet<?> createPacketToServer(@NotNull ResourceLocation channelId,
                                                   @NotNull VisorPayloadToServer payload) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        payload.write(buffer);
        return ClientPlayNetworking.createC2SPacket(VisorRawPayload.of(channelId, buffer));
    }


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