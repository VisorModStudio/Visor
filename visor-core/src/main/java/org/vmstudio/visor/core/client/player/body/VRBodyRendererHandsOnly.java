package org.vmstudio.visor.core.client.player.body;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.api.client.player.VRClientPlayer;
import org.vmstudio.visor.api.client.render.decoration.VRBodyRenderer;
import org.vmstudio.visor.api.client.render.decoration.VRDecorator;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.render.player.VRPlayerRendererTest;
import org.vmstudio.visor.core.client.settings.VRClientSettings;

import java.util.*;

public class VRBodyRendererHandsOnly implements VRBodyRenderer {

    @Getter
    private final List<PlayerRenderer> modelRenderers = new ArrayList<>();


    private final Map<String, VRPlayerRendererTest> modelsMapVanilla = new HashMap<>();
    private final Map<String, VRPlayerRendererTest> modelsMapArms = new HashMap<>();
    private final Map<String, VRPlayerRendererTest> modelsMapArmsLegs = new HashMap<>();

    private VRPlayerRendererTest vanillaRenderer;
    private VRPlayerRendererTest armsRenderer;
    private VRPlayerRendererTest armsLegsRenderer;

    @Override
    public void renderDecoration(@NotNull VRDecorator decorator, @NotNull PoseStack poseStack, float partialTicks) {
        ClientContext.handRenderer.renderWorldHands(
                decorator,
                poseStack,
                ClientContext.decorationRenderer.getHandRenderState(HandType.MAIN),
                ClientContext.decorationRenderer.getHandRenderState(HandType.OFFHAND),
                partialTicks
        );

    }


    @Override
    public void initModels(EntityRendererProvider.Context context) {
        this.vanillaRenderer = new VRPlayerRendererTest(context, false,
                VRClientSettings.PlayerModelType.VANILLA);
        this.modelsMapVanilla.put(MODEL_NAME_DEFAULT, this.vanillaRenderer);
        this.modelsMapVanilla.put(MODEL_NAME_SLIM, new VRPlayerRendererTest(context, true,
                VRClientSettings.PlayerModelType.VANILLA)
        );

        this.armsRenderer = new VRPlayerRendererTest(context, false,
                VRClientSettings.PlayerModelType.SPLIT_ARMS);
        this.modelsMapArms.put(MODEL_NAME_DEFAULT, this.armsRenderer);
        this.modelsMapArms.put(MODEL_NAME_SLIM, new VRPlayerRendererTest(context, true,
                VRClientSettings.PlayerModelType.SPLIT_ARMS));

        this.armsLegsRenderer = new VRPlayerRendererTest(context, false,
                VRClientSettings.PlayerModelType.SPLIT_ARMS_LEGS);
        this.modelsMapArmsLegs.put(MODEL_NAME_DEFAULT, this.armsLegsRenderer);
        this.modelsMapArmsLegs.put(MODEL_NAME_SLIM, new VRPlayerRendererTest(context, true,
                VRClientSettings.PlayerModelType.SPLIT_ARMS_LEGS)
        );

        modelRenderers.addAll(modelsMapVanilla.values());
        modelRenderers.addAll(modelsMapArms.values());
        modelRenderers.addAll(modelsMapArmsLegs.values());
    }

    @Override
    public void clearModels() {
        VRBodyRenderer.super.clearModels();
        modelsMapVanilla.clear();
        modelsMapArms.clear();
        modelsMapArmsLegs.clear();
    }

    @Override
    public PlayerRenderer getModelRenderer(@NotNull VRClientPlayer player, @NotNull String modelName) {
        VRClientSettings.PlayerModelType type = VRClientSettings.getPlayerModelType();
        if (type == VRClientSettings.PlayerModelType.VANILLA) {
            return modelsMapVanilla.getOrDefault(modelName, vanillaRenderer);
        } else if (type == VRClientSettings.PlayerModelType.SPLIT_ARMS) {
            return modelsMapArms.getOrDefault(modelName, armsRenderer);
        } else {
            return modelsMapArmsLegs.getOrDefault(modelName, armsLegsRenderer);
        }
    }



}
