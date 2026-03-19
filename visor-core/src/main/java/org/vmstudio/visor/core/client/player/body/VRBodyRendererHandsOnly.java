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
import org.vmstudio.visor.core.client.render.player.VRPlayerRenderer;
import org.vmstudio.visor.core.client.settings.VRClientSettings;

import java.util.*;

public class VRBodyRendererHandsOnly implements VRBodyRenderer {

    @Getter
    private final List<PlayerRenderer> models = new ArrayList<>();


    private final Map<String, VRPlayerRenderer> modelsMapVanilla = new HashMap<>();
    private final Map<String, VRPlayerRenderer> modelsMapArms = new HashMap<>();
    private final Map<String, VRPlayerRenderer> modelsMapArmsLegs = new HashMap<>();

    private VRPlayerRenderer vanillaRenderer;
    private VRPlayerRenderer armsRenderer;
    private VRPlayerRenderer armsLegsRenderer;

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
        this.vanillaRenderer = new VRPlayerRenderer(context, false,
                VRClientSettings.PlayerModelType.VANILLA);
        this.modelsMapVanilla.put("default", this.vanillaRenderer);
        this.modelsMapVanilla.put("slim", new VRPlayerRenderer(context, true,
                VRClientSettings.PlayerModelType.VANILLA)
        );

        this.armsRenderer = new VRPlayerRenderer(context, false,
                VRClientSettings.PlayerModelType.SPLIT_ARMS);
        this.modelsMapArms.put("default", this.armsRenderer);
        this.modelsMapArms.put("slim", new VRPlayerRenderer(context, true,
                VRClientSettings.PlayerModelType.SPLIT_ARMS));

        this.armsLegsRenderer = new VRPlayerRenderer(context, false,
                VRClientSettings.PlayerModelType.SPLIT_ARMS_LEGS);
        this.modelsMapArmsLegs.put("default", this.armsLegsRenderer);
        this.modelsMapArmsLegs.put("slim", new VRPlayerRenderer(context, true,
                VRClientSettings.PlayerModelType.SPLIT_ARMS_LEGS)
        );

        models.addAll(modelsMapVanilla.values());
        models.addAll(modelsMapArms.values());
        models.addAll(modelsMapArmsLegs.values());
    }

    @Override
    public void clearModels() {
        VRBodyRenderer.super.clearModels();
        modelsMapVanilla.clear();
        modelsMapArms.clear();
        modelsMapArmsLegs.clear();
    }

    @Override
    public PlayerRenderer getModel(@NotNull VRClientPlayer player, @NotNull String modelName) {
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
