package org.vmstudio.visor.core.client.render.decoration.effects;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import org.vmstudio.visor.api.client.render.VRRenderPass;
import org.vmstudio.visor.api.client.render.decoration.VRDecorator;
import org.vmstudio.visor.api.client.render.decoration.annotations.RegisterVRGameEffect;
import org.vmstudio.visor.api.client.render.decoration.effects.VRGameEffect;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.extensions.client.render.GameRendererExtension;

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
        ((GameRendererExtension) MC.gameRenderer).visor$renderItemActivationAnimation(
                new GuiGraphics(MC, MC.renderBuffers().bufferSource()),
                partialTicks
        );
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
