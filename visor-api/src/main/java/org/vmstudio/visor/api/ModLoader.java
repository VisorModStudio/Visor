package org.vmstudio.visor.api;


import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.vmstudio.visor.api.client.render.RenderPipelineCallback;
import org.vmstudio.visor.api.client.render.RenderPipelineStage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.api.common.network.VisorChannel;
import org.vmstudio.visor.api.common.network.VisorPayloadToClient;
import org.vmstudio.visor.api.common.network.VisorPayloadToServer;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.List;

/**
 * Accessor for specific mod-loader functionality
 */
public interface ModLoader {


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
     * Register Visor Channel for network
     *
     * @param channel the visor channel
     */
    void registerNetworkChannel(@NotNull VisorChannel channel);

    /**
     * Create Client-To-Server packet from payload
     *
     * @param payload payload
     * @return packet
     */
    @NotNull
    Packet<?> createPacketToServer(@NotNull ResourceLocation channelId,
                                   @NotNull VisorPayloadToServer payload);

    /**
     * Create Server-To-Client packet from payload
     *
     * @param payload payload
     * @return packet
     */
    @NotNull
    Packet<?> createPacketToClient(@NotNull ResourceLocation channelId,
                                   @NotNull VisorPayloadToClient payload);


    /**
     * Get all classes loaded by mod with <code>modId</code>
     * in <code>packagePath</code>
     * and annotated with <code>annotation</code>
     * @param annotation the annotation class
     * @param modId the id of a mod which classes should be checked (ignored on fabric)
     * @param packagePath the package path to look in, e.g. 'my.path.to'
     *
     * @return list with classes or empty
     */
    @NotNull
    List<Class<?>> getClassesAnnotated(@NotNull Class<? extends Annotation> annotation,
                                       @NotNull String modId,
                                       @NotNull String packagePath);


    /**
     * Register a callback to be invoked at a specific
     * render pipeline stage.
     *
     * <p>The mod loader maps each {@link RenderPipelineStage}
     * to its native render event (e.g. Fabric's
     * {@code WorldRenderEvents.END} or Forge's
     * {@code RenderLevelStageEvent}).</p>
     *
     * <p>This allows visor-core to inject rendering
     * at the correct point in the pipeline without
     * relying on mixin injection points that are
     * fragile across MC versions.</p>
     *
     * @param stage    the pipeline stage to register at
     * @param callback the callback to invoke at that stage
     */
    void addToRenderPipeline(@NotNull RenderPipelineStage stage,
                             @NotNull RenderPipelineCallback callback);

    @ApiStatus.Internal
    boolean enableRenderTargetStencil(@NotNull RenderTarget renderTarget);
    @ApiStatus.Internal
    double getItemEntityReach(double baseRange, ItemStack itemStack, EquipmentSlot slot);
    @ApiStatus.Internal
    boolean renderWaterOverlay(Player player, PoseStack mat);
    @ApiStatus.Internal
    boolean renderFireOverlay(Player player, PoseStack mat);



    @NotNull
    LoaderType getType();

    /**
     * Get instance of this class
     *
     * @return instance
     */
    static ModLoader get() {
        return Instance.get();
    }


    enum LoaderType{
        FABRIC,
        FORGE
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
                Class<?> clazz = Class.forName("org.vmstudio.visor.loader.forge.ForgeModLoader");
                api = (ModLoader) clazz.getConstructor().newInstance();
            } catch (Exception ignored) {
            }
            //FABRIC
            if(api == null){
                try {
                    Class<?> clazz = Class.forName("org.vmstudio.visor.loader.fabric.FabricModLoader");
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
