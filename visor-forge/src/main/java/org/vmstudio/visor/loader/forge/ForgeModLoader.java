package org.vmstudio.visor.loader.forge;


import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.vertex.PoseStack;
import io.netty.buffer.Unpooled;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.EventNetworkChannel;
import net.minecraftforge.network.NetworkProtocol;
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
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo;
import net.minecraftforge.forgespi.language.IModFileInfo;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;


public class ForgeModLoader implements ModLoader {
    private File configFolder = FMLPaths.CONFIGDIR.get().toFile();

    private static final Map<RenderPipelineStage, List<RenderPipelineCallback>> pipelineCallbacks
            = new EnumMap<>(RenderPipelineStage.class);


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
    }

    /**
     * 1.21.4: Forge 54 removed RenderLevelStageEvent along with the rest of the pre-framegraph
     * level render hooks, and its only replacement (AddFramePassEvent) appends a single pass
     * after every vanilla one, so it cannot express AFTER_SOLID or AFTER_TRANSLUCENT.
     * The stages are driven by ForgeLevelRendererVRMixin instead, which calls this.
     */
    public static void fireRenderPipelineStage(@NotNull RenderPipelineStage stage,
                                               @NotNull PoseStack poseStack,
                                               float partialTicks) {
        List<RenderPipelineCallback> callbacks = pipelineCallbacks.get(stage);
        if (callbacks == null || callbacks.isEmpty()) return;

        for (RenderPipelineCallback callback : callbacks) {
            callback.render(poseStack, partialTicks);
        }
    }



    @Override
    public boolean enableRenderTargetStencil(@NotNull RenderTarget renderTarget) {
        renderTarget.enableStencil();
        return true;
    }

    /**
     * 1.21.1: Forge's ENTITY_REACH became the vanilla
     * ENTITY_INTERACTION_RANGE attribute, and item modifiers are
     * component-based (forEachModifier); AttributeModifier is a record
     * with renamed operations
     */
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

    /**
     * Forge 52: ChannelBuilder moved to its own class with int protocol
     * versions, and CustomPayloadEvent replaced NetworkEvent (context is
     * no longer behind a Supplier)
     */
    @Override
    public void registerNetworkChannel(@NotNull VisorChannel channel) {
        EventNetworkChannel eventChannel = ChannelBuilder
                .named(channel.getChannelId())
                .networkProtocolVersion(channel.getNetworkVersion())
                .optional()
                .eventNetworkChannel();

        eventChannel.addListener((CustomPayloadEvent event) -> {
            FriendlyByteBuf payload = event.getPayload();
            if (payload == null) return;

            FriendlyByteBuf copy = new FriendlyByteBuf(Unpooled.buffer());
            copy.writeBytes(payload.copy());

            CustomPayloadEvent.Context context = event.getSource();
            if (context.isServerSide()) {
                if (channel.hasPacketsToServer() && context.getSender() != null) {
                    var sender = context.getSender();
                    context.enqueueWork(() -> channel.handleToServer(copy, sender,
                            p -> context.getConnection().send(
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
    }

    @Override
    public @NotNull Packet<?> createPacketToClient(@NotNull ResourceLocation channelId,
                                                   @NotNull VisorPayloadToClient payload) {
        return NetworkProtocol.PLAY.buildPacket(PacketFlow.CLIENTBOUND, channelId, payload::write);
    }

    @Override
    public @NotNull Packet<?> createPacketToServer(@NotNull ResourceLocation channelId,
                                                   @NotNull VisorPayloadToServer payload) {
        return NetworkProtocol.PLAY.buildPacket(PacketFlow.SERVERBOUND, channelId, payload::write);
    }

    @Override
    public boolean renderWaterOverlay(Player player, PoseStack mat) {
        return ForgeHooksClient.renderWaterOverlay(player, mat);
    }
    @Override
    public boolean renderFireOverlay(Player player, PoseStack mat) {
        return ForgeHooksClient.renderFireOverlay(player, mat);
    }

    @Override
    public @NotNull LoaderType getType() {
        return LoaderType.FORGE;
    }


    // ----- INNER -----

}