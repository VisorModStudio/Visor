package org.vmstudio.visor.core.client.player.body.full;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.api.client.player.VRClientPlayer;
import org.vmstudio.visor.api.client.render.decoration.VRBodyRenderer;
import org.vmstudio.visor.api.client.render.decoration.VRDecorator;
import org.vmstudio.visor.core.client.render.player.VRPlayerRendererFull;

import java.util.*;

public class VRBodyRendererFull implements VRBodyRenderer {

    @Getter
    private final List<PlayerRenderer> modelRenderers = new ArrayList<>();

    private final Map<String, VRPlayerRendererFull> modelsMap = new HashMap<>();

    private VRPlayerRendererFull defaultRenderer;

    @Override
    public void renderDecoration(@NotNull VRDecorator decorator,
                                 @NotNull PoseStack poseStack,
                                 float partialTicks) {
        //EMPTY
    }

    @Override
    public void initModels(EntityRendererProvider.Context context) {

        this.defaultRenderer = new VRPlayerRendererFull(context, false);
        this.modelsMap.put(MODEL_NAME_DEFAULT, this.defaultRenderer);
        this.modelsMap.put(MODEL_NAME_SLIM,
                new VRPlayerRendererFull(context, true));

        modelRenderers.addAll(modelsMap.values());
    }

    @Override
    public void clearModels() {
        VRBodyRenderer.super.clearModels();
        modelsMap.clear();
    }

    @Override
    public PlayerRenderer getModelRenderer(@NotNull VRClientPlayer player, @NotNull String modelName) {
        return modelsMap.getOrDefault(modelName, defaultRenderer);
    }


}
