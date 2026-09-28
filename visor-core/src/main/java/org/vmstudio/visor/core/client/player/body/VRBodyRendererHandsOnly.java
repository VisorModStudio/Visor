package org.vmstudio.visor.core.client.player.body;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
//? if >=1.21.9 {
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
//?} else {
/*import net.minecraft.client.renderer.entity.player.PlayerRenderer;
*///?}
import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.api.client.player.VRClientPlayer;
import org.vmstudio.visor.api.client.render.decoration.VRBodyRenderer;
import org.vmstudio.visor.api.client.render.decoration.VRDecorator;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.render.VRRenderState;
import org.vmstudio.visor.core.client.render.player.VRPlayerRendererHandsOnly;

import java.util.*;

public class VRBodyRendererHandsOnly implements VRBodyRenderer {

    @Getter
    //? if >=1.21.9 {
    private final List<AvatarRenderer> modelRenderers = new ArrayList<>();
    //?} else {
    /*private final List<PlayerRenderer> modelRenderers = new ArrayList<>();
    *///?}

    private final Map<String, VRPlayerRendererHandsOnly> modelsMap = new HashMap<>();

    private VRPlayerRendererHandsOnly defaultRenderer;


    @Override
    public void renderDecoration(@NotNull VRDecorator decorator, @NotNull PoseStack poseStack, float partialTicks) {
        if (!VRRenderState.getRenderPass().isFirstPerson()) {
            return;
        }
        ClientContext.handRenderer.renderWorldHands(
                poseStack,
                ClientContext.decorationRenderer.getHandState(HandType.MAIN),
                ClientContext.decorationRenderer.getHandState(HandType.OFFHAND),
                partialTicks
        );

    }


    @Override
    public void initModels(EntityRendererProvider.Context context) {
        this.defaultRenderer = new VRPlayerRendererHandsOnly(context, false);
        this.modelsMap.put(
                MODEL_NAME_DEFAULT,
                this.defaultRenderer
        );
        this.modelsMap.put(
                MODEL_NAME_SLIM,
                new VRPlayerRendererHandsOnly(context, true)
        );


        modelRenderers.addAll(modelsMap.values());
    }

    @Override
    public void clearModels() {
        VRBodyRenderer.super.clearModels();
        modelsMap.clear();
    }

    @Override
    //? if >=1.21.9 {
    public AvatarRenderer getModelRenderer(@NotNull VRClientPlayer player, @NotNull String modelName) {
    //?} else {
    /*public PlayerRenderer getModelRenderer(@NotNull VRClientPlayer player, @NotNull String modelName) {
    *///?}
        return modelsMap.getOrDefault(modelName, defaultRenderer);
    }
}