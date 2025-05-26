package me.phoenixra.visor.loader.fabric;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.vertex.PoseStack;
import io.netty.buffer.Unpooled;
import me.phoenixra.visor.api.IModLoader;
import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.common.network.toclient.VisorPayloadToClient;
import me.phoenixra.visor.api.common.network.toserver.VisorPayloadToServer;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class FabricModLoader implements IModLoader {
    private final File configFolder = net.fabricmc.loader.api.FabricLoader.getInstance()
            .getConfigDir().toFile();
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
    public boolean enableRenderTargetStencil(RenderTarget renderTarget) {
        return false;
    }

    @Override
    public double getItemEntityReach(double baseRange, ItemStack itemStack, EquipmentSlot slot) {
        return baseRange;
    }

    @Override
    public Packet<?> createPacketToClient(VisorPayloadToClient payload) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        payload.write(buffer);
        return ServerPlayNetworking.createS2CPacket(payload.id(), buffer);
    }

    @Override
    public Packet<?> createPacketToServer(VisorPayloadToServer payload) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        payload.write(buffer);
        return ClientPlayNetworking.createC2SPacket(payload.id(), buffer);
    }

    @Override
    public boolean renderBlockOverlay(Player player, PoseStack mat, BlockState state, BlockPos pos) {
        return false;
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
