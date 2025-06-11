package me.phoenixra.visor.core.client.render.decoration.effects;

import com.mojang.blaze3d.vertex.PoseStack;
import me.phoenixra.visor.api.client.render.VRDisplay;
import me.phoenixra.visor.api.client.render.decoration.VRDecorator;
import me.phoenixra.visor.api.client.render.decoration.annotations.RegisterVRGameEffect;
import me.phoenixra.visor.api.client.render.decoration.effects.VRGameEffect;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.render.decoration.decorators.DecoratorGame;
import me.phoenixra.visor.core.client.render.decoration.decorators.DecoratorGameMenu;
import org.jetbrains.annotations.NotNull;

import static me.phoenixra.visor.core.client.VisorClientImpl.MC;


@RegisterVRGameEffect
public class GameEffectVanilla extends VRGameEffect {
    private static final String ID = "vanilla";
    public GameEffectVanilla(@NotNull VisorAddon owner) {
        super(owner);
    }

    @Override
    public void render(@NotNull VRDisplay renderDisplay,
                       @NotNull PoseStack poseStack,
                       float partialTicks) {
        MC.gameRenderer.renderItemActivationAnimation(0, 0, partialTicks);
    }

    @Override
    public boolean isVisible(@NotNull VRDecorator currentDecorator) {
        String decoratorId = currentDecorator.getId();

        return decoratorId.equals(DecoratorGame.ID)
                || decoratorId.equals(DecoratorGameMenu.ID);
    }

    @Override
    public @NotNull String getId() {
        return ID;
    }

}
