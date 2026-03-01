package org.vmstudio.visor.core.client.render.decoration.decorators;

import com.mojang.blaze3d.vertex.*;
import org.vmstudio.visor.api.client.render.decoration.VRDecorator;
import org.vmstudio.visor.api.client.render.decoration.annotations.RegisterVRDecorator;
import org.vmstudio.visor.api.common.addon.component.ComponentPriority;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.modified.client.render.GameRendererModified;
import org.vmstudio.visor.core.client.render.decoration.effects.GameEffectVanilla;
import org.vmstudio.visor.core.client.render.helpers.VREffectsHelper;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

@RegisterVRDecorator
public class DecoratorGameMenu extends VRDecorator {
    public static final String ID = "game_menu";

    public DecoratorGameMenu(@NotNull VisorAddon owner) {
        super(owner, ID);
    }

    @Override
    public void tick() {
    }

    @Override
    public void renderAfterWorld(@NotNull PoseStack poseStack, float partialTicks) {
        boolean insideBlock = ((GameRendererModified) MC.gameRenderer).visor$isInBlock() > 0.0F;
        if (insideBlock) {
            VREffectsHelper.renderInBlockEffect();
        }
    }
    @Override
    public boolean canActivate() {
        return MC.player != null && MC.level != null && MC.screen != null;
    }

    @Override
    public List<String> gameEffects() {
        return List.of(
                GameEffectVanilla.ID
        );
    }

    @Override
    public List<String> handEffects() {
        return List.of();
    }

    @Override
    public @NotNull ComponentPriority getPriority() {
        return ComponentPriority.LOW;
    }
}