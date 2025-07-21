package me.phoenixra.visor.core.client.render.decoration.decorators;

import com.mojang.blaze3d.vertex.*;
import me.phoenixra.visor.api.client.ClientFeature;
import me.phoenixra.visor.api.client.render.decoration.VRDecorator;
import me.phoenixra.visor.api.client.render.decoration.annotations.RegisterVRDecorator;
import me.phoenixra.visor.api.common.addon.element.ElementPriority;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.core.client.mcmodified.render.GameRendererModified;
import me.phoenixra.visor.core.client.render.decoration.effects.GameEffectOnFire;
import me.phoenixra.visor.core.client.render.decoration.effects.GameEffectShadow;
import me.phoenixra.visor.core.client.render.decoration.effects.GameEffectVanilla;
import me.phoenixra.visor.core.client.render.decoration.effects.hand.HandEffectCrosshair;
import me.phoenixra.visor.core.client.render.helpers.VREffectsHelper;
import org.jetbrains.annotations.NotNull;

import me.phoenixra.visor.core.client.ClientContext;

import java.util.List;

import static me.phoenixra.visor.core.client.VisorClientImpl.MC;

@RegisterVRDecorator
public class DecoratorGame extends VRDecorator {
    public static final String ID = "game";



    public DecoratorGame(@NotNull VisorAddon owner) {
        super(owner, ID);
    }



    @Override
    public void tick() {

    }

    @Override
    public void render(PoseStack poseStack, float partialTicks) {
        boolean insideBlock = ((GameRendererModified) MC.gameRenderer).visor$isInBlock() > 0.0F;
        if (insideBlock) {
            VREffectsHelper.renderInBlockEffect();
        }

        MC.gameRenderer.lightTexture().turnOffLightLayer();


        ClientContext.guiManager.renderGUI(poseStack, partialTicks);

        ClientContext.handRenderer.renderWorldHands(
                this,
                poseStack, partialTicks,
                true, true
        );


        ClientContext.decorationRenderer.renderGameEffects(
                this,
                poseStack, partialTicks
        );
    }





    @Override
    public boolean canActivate() {
        return MC.level != null && MC.screen == null;
    }

    @Override
    public List<String> gameEffects() {
        return List.of(
                GameEffectOnFire.ID,
                GameEffectShadow.ID,
                GameEffectVanilla.ID
        );
    }

    @Override
    public List<String> handEffects() {
        return List.of(
                HandEffectCrosshair.ID
        );
    }

    @Override
    public @NotNull ElementPriority getPriority() {
        return ElementPriority.LOW;
    }
}
