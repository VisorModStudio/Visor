package me.phoenixra.visor.api;


import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.vertex.PoseStack;
import me.phoenixra.visor.api.common.network.toclient.VisorPayloadToClient;
import me.phoenixra.visor.api.common.network.toserver.VisorPayloadToServer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * This class acts as an
 * accessor for specific mod-loader functionality
 */
public interface ModLoader {

    /**
     * Returns mod loader ID.
     * <br>
     * It can be 'forge' or 'fabric'
     * @return mod loader ID
     */
    @NotNull
    String getId();

    /**
     * Returns true if mod with specified id is loaded in
     * mod loader
     * @param id mod id
     * @return if specified mod is loaded
     */
    boolean isModLoaded(@NotNull String id);

    /**
     * Returns mod version loaded
     * by mod loader
     * @param id mod id
     * @return mod version
     */
    @NotNull
    String getModVersion(@NotNull String id);


    /**
     * Returns true If runtime environment
     * is on dedicated server
     *
     * @return if dedicated server environment
     */
    boolean isDedicatedServer();

    /**
     * Get config folder
     *
     * @return config folder
     */
    File getConfigFolder();


    /**
     * Create Client-To-Server packet from payload
     *
     * @param payload payload
     * @return packet
     */
    @NotNull
    Packet<?> createPacketToServer(@NotNull VisorPayloadToServer payload);

    /**
     * Create Server-To-Client packet from payload
     *
     * @param payload payload
     * @return packet
     */
    @NotNull
    Packet<?> createPacketToClient(@NotNull VisorPayloadToClient payload);



    @ApiStatus.Internal
    boolean enableRenderTargetStencil(@NotNull RenderTarget renderTarget);
    @ApiStatus.Internal
    double getItemEntityReach(double baseRange, ItemStack itemStack, EquipmentSlot slot);
    @ApiStatus.Internal
    boolean renderBlockOverlay(Player player, PoseStack mat, BlockState state, BlockPos pos);
    @ApiStatus.Internal
    boolean renderWaterOverlay(Player player, PoseStack mat);
    @ApiStatus.Internal
    boolean renderFireOverlay(Player player, PoseStack mat);



    /**
     * Get instance of this class
     *
     * @return instance
     */
    static ModLoader get() {
        return Instance.get();
    }



    @ApiStatus.Internal
    final class Instance {
        private Instance() {
            throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
        }

        private static ModLoader api;

        static ModLoader get() {
            if(api != null){
                return api;
            }

            //FORGE
            try {
                Class<?> clazz = Class.forName("me.phoenixra.visor.loader.forge.ForgeModLoader");
                api = (ModLoader) clazz.getConstructor().newInstance();
            } catch (Exception ignored) {
            }
            //FABRIC
            if(api == null){
                try {
                    Class<?> clazz = Class.forName("me.phoenixra.visor.loader.fabric.FabricModLoader");
                    api = (ModLoader) clazz.getConstructor().newInstance();
                } catch (Exception ignored) {
                }
            }

            if(api == null){
                throw new RuntimeException("SUPPORTED MOD LOADER FOR" +
                                " VISOR NOT FOUND!");
            }
            return api;
        }
    }


}
