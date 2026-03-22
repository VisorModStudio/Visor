package org.vmstudio.visor.api.client.render.decoration;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.api.client.player.VRClientPlayer;

import java.util.Collection;

public interface VRBodyRenderer {
    String MODEL_NAME_DEFAULT = "default";
    String MODEL_NAME_SLIM = "slim";

    void renderDecoration(@NotNull VRDecorator decorator,
                          @NotNull PoseStack poseStack,
                          float partialTicks);

    void initModels(EntityRendererProvider.Context context);

    default void clearModels(){
        getModelRenderers().clear();
    }


    /**
     * Get model to use for player
     *
     * @param player the client player
     * @param modelName default or slim or smth else from mods
     */
    PlayerRenderer getModelRenderer(@NotNull VRClientPlayer player,
                                    @NotNull String modelName);

    @NotNull
    Collection<PlayerRenderer> getModelRenderers();
}
