package org.vmstudio.visor.core.client.player.body.full;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.vmstudio.visor.api.client.player.VRClientPlayer;
import org.vmstudio.visor.api.client.render.decoration.VRBodyRenderer;
import org.vmstudio.visor.api.client.render.decoration.VRDecorator;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.render.VRRenderState;
import org.vmstudio.visor.core.client.render.player.VRPlayerRendererFull;
import org.vmstudio.visor.core.client.settings.VRClientSettings;

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
        // Self-perspective hands only. In third-person/other-player view the body
        // model renders the arms (vanilla-looking) and the hands track yaw/pitch.
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

    @Override
    public Vector3fc getModelItemScale() {
        return new Vector3f(
                1.0f,
                VRClientSettings.getPlayerModelArmsScale(),
                1.0f
        );
    }
}