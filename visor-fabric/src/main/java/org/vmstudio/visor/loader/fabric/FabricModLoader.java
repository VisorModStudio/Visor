package org.vmstudio.visor.loader.fabric;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.vertex.PoseStack;
import io.netty.buffer.Unpooled;
import org.vmstudio.visor.api.ModLoader;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.render.RenderPipelineCallback;
import org.vmstudio.visor.api.client.render.RenderPipelineStage;
import org.vmstudio.visor.api.common.VRException;
import org.vmstudio.visor.api.common.network.toclient.VisorPayloadToClient;
import org.vmstudio.visor.api.common.network.toserver.VisorPayloadToServer;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
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
    private final File configFolder = net.fabricmc.loader.api.FabricLoader.getInstance()
            .getConfigDir().toFile();

    private final Map<RenderPipelineStage, List<RenderPipelineCallback>> pipelineCallbacks
            = new EnumMap<>(RenderPipelineStage.class);

    private boolean worldEventsRegistered = false;
    private boolean hudEventRegistered = false;

    @Override
    public File getConfigFolder() {
        return configFolder;
    }
    @Override
    public @NotNull String getId() {
        return "fabric";
    }

    @Override
    public boolean isModLoaded(@NotNull String id) {
        return net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded(id);
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
                fireCallbacks(RenderPipelineStage.AFTER_SOLID, context.matrixStack(), context.tickDelta());
                return true; // don't cancel block outline
            });

            // AFTER_TRANSLUCENT
            WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> {
                fireCallbacks(RenderPipelineStage.AFTER_TRANSLUCENT, context.matrixStack(), context.tickDelta());
            });

            // AFTER_WORLD
            WorldRenderEvents.END.register(context -> {
                fireCallbacks(RenderPipelineStage.AFTER_WORLD, context.matrixStack(), context.tickDelta());
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


    @Override
    public @NotNull Packet<?> createPacketToClient(@NotNull VisorPayloadToClient payload) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        payload.write(buffer);
        return ServerPlayNetworking.createS2CPacket(payload.id(), buffer);
    }

    @Override
    public @NotNull Packet<?> createPacketToServer(@NotNull VisorPayloadToServer payload) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        payload.write(buffer);
        return ClientPlayNetworking.createC2SPacket(payload.id(), buffer);
    }


    @Override
    public boolean renderWaterOverlay(Player player, PoseStack mat) {
        return false;
    }

    @Override
    public boolean renderFireOverlay(Player player, PoseStack mat) {
        return false;
    }
}