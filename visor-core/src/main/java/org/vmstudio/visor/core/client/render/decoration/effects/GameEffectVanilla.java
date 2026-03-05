package org.vmstudio.visor.core.client.render.decoration.effects;

import com.mojang.blaze3d.vertex.PoseStack;
import org.vmstudio.visor.api.client.render.VRRenderPass;
import org.vmstudio.visor.api.client.render.decoration.VRDecorator;
import org.vmstudio.visor.api.client.render.decoration.annotations.RegisterVRGameEffect;
import org.vmstudio.visor.api.client.render.decoration.effects.VRGameEffect;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.jetbrains.annotations.NotNull;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;


@RegisterVRGameEffect
public class GameEffectVanilla extends VRGameEffect {
    public static final String ID = "vanilla";
    public GameEffectVanilla(@NotNull VisorAddon owner) {
        super(owner);
    }

    @Override
    public void render(@NotNull VRRenderPass renderPass,
                       @NotNull PoseStack poseStack,
                       float partialTicks) {
        MC.gameRenderer.renderItemActivationAnimation(0, 0, partialTicks);
    }

    @Override
    public boolean isVisible(@NotNull VRDecorator currentDecorator) {
        return true;
    }

    @Override
    public @NotNull String getId() {
        return ID;
    }

}
