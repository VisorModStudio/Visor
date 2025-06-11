package me.phoenixra.visor.core.client.render.decoration.decorators;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import me.phoenixra.visor.api.client.ClientFeature;
import me.phoenixra.visor.api.client.render.decoration.VRDecorator;
import me.phoenixra.visor.api.client.render.decoration.annotations.RegisterVRDecorator;
import me.phoenixra.visor.api.common.addon.ElementPriority;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.core.client.mcmodified.render.GameRendererModified;
import me.phoenixra.visor.core.client.render.helpers.VREffectsHelper;
import me.phoenixra.visor.core.client.render.helpers.VRScreenHelper;
import net.minecraft.client.renderer.GameRenderer;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11C;

import me.phoenixra.visor.core.client.ClientContext;
import static me.phoenixra.visor.core.client.VisorClientImpl.MC;

@RegisterVRDecorator
public class DecoratorGameMenu extends VRDecorator {
    public static final String ID = "game_menu";



    public DecoratorGameMenu(@NotNull VisorAddon owner) {
        super(owner, ID);
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onExit() {

    }

    @Override
    public void tick() {

    }

    @Override
    public void render(PoseStack poseStack, float partialTicks) {
        boolean insideBlock = ((GameRendererModified) MC.gameRenderer).visor$isInBlock() > 0.0F;
        if (insideBlock) {
            VREffectsHelper.renderInsideBlockOverlay();
        }

        MC.gameRenderer.lightTexture().turnOffLightLayer();

        ClientContext.guiManager.renderGUI(poseStack, partialTicks, !VRScreenHelper.shouldOccludeGui());

        if (ClientContext.visor.isFeatureEnabled(ClientFeature.VR_HANDS)) {
            ClientContext.handRenderer.renderGuiHands(
                    poseStack, partialTicks,
                    true, true
            );
        }
        ClientContext.decoratorManager.renderGameEffects(
                poseStack, partialTicks
        );
    }


    @Override
    public boolean canActivate() {
        return MC.level != null && MC.screen != null;
    }

    @Override
    public @NotNull ElementPriority getPriority() {
        return ElementPriority.LOW;
    }
}
