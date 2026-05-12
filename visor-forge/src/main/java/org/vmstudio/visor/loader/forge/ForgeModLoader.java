package org.vmstudio.visor.loader.forge;


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
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.RenderBlockScreenEffectEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo;
import net.minecraftforge.forgespi.language.IModFileInfo;
import net.minecraftforge.forgespi.language.ModFileScanData;
import net.minecraftforge.network.NetworkDirection;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;


public class ForgeModLoader implements ModLoader {
    private File configFolder = FMLPaths.CONFIGDIR.get().toFile();

    private final Map<RenderPipelineStage, List<RenderPipelineCallback>> pipelineCallbacks
            = new EnumMap<>(RenderPipelineStage.class);

    private boolean levelStageListenerRegistered = false;
    private boolean guiOverlayListenerRegistered = false;


    @Override
    public File getConfigFolder() {
        return configFolder;
    }
    @Override
    public @NotNull String getId() {
        return "forge";
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
            MinecraftForge.EVENT_BUS.addListener(this::onRenderLevelStage);
            levelStageListenerRegistered = true;
        }
    }



    @Override
    public boolean enableRenderTargetStencil(@NotNull RenderTarget renderTarget) {
        renderTarget.enableStencil();
        return true;
    }

    @Override
    public double getItemEntityReach(double baseRange, ItemStack itemStack, EquipmentSlot slot) {
        Collection<AttributeModifier> attributes = itemStack.getAttributeModifiers(slot)
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
    public @NotNull Packet<?> createPacketToClient(@NotNull VisorPayloadToClient payload) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        payload.write(buffer);
        return NetworkDirection.PLAY_TO_CLIENT.buildPacket(new ImmutablePair<>(buffer, 0), payload.id()).getThis();
    }

    @Override
    public @NotNull Packet<?> createPacketToServer(@NotNull VisorPayloadToServer payload) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        payload.write(buffer);
        return NetworkDirection.PLAY_TO_SERVER.buildPacket(new ImmutablePair<>(buffer, 0), payload.id()).getThis();
    }

    @Override
    public boolean renderWaterOverlay(Player player, PoseStack mat) {
        return ForgeHooksClient.renderWaterOverlay(player, mat);
    }
    @Override
    public boolean renderFireOverlay(Player player, PoseStack mat) {
        return ForgeHooksClient.renderFireOverlay(player, mat);
    }


    // ----- INNER -----

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


    private static RenderPipelineStage mapForgeStage(RenderLevelStageEvent.Stage forgeStage) {
        if (forgeStage == RenderLevelStageEvent.Stage.AFTER_SOLID_BLOCKS) {
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
}