package me.phoenixra.visor.core.common.addon;

import me.phoenixra.visor.api.client.gui.overlay.VROverlay;
import me.phoenixra.visor.api.client.gui.overlay.template.OverlayTemplateRecord;
import me.phoenixra.visor.api.client.input.action.VisorActionSet;
import me.phoenixra.visor.api.client.render.decoration.VRDecorator;
import me.phoenixra.visor.api.client.render.decoration.effects.VRGameEffect;
import me.phoenixra.visor.api.client.render.decoration.effects.VRHandEffect;
import me.phoenixra.visor.api.client.render.decoration.hand.VRHandItemPose;
import me.phoenixra.visor.api.client.tasks.VisorTask;
import me.phoenixra.visor.api.common.addon.VisorRegistries;
import me.phoenixra.visor.api.common.addon.element.VisorRegistry;
import me.phoenixra.visor.core.client.ClientContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class VisorRegistriesImpl implements VisorRegistries {
    protected final List<VisorRegistry<?>> list;

    public VisorRegistriesImpl(List<VisorRegistry<?>> registries){
        this.list = registries;
    }

    @Override
    public @NotNull VisorRegistry<VisorTask> tasks() {
        return ClientContext.visor.getTaskRegistry();
    }

    @Override
    public @NotNull VisorRegistry<VisorActionSet> actionSets() {
        return ClientContext.inputManager.getActionSetRegistry();
    }

    @Override
    public @NotNull VisorRegistry<VRDecorator> decorators() {
        return ClientContext.decorationRenderer.getRegistry();
    }

    @Override
    public @NotNull VisorRegistry<VRGameEffect> gameEffects() {
        return ClientContext.decorationRenderer.getEffectsRegistry();
    }

    @Override
    public @NotNull VisorRegistry<VRHandEffect> handEffects() {
        return ClientContext.handRenderer.getEffectsRegistry();
    }

    @Override
    public @NotNull VisorRegistry<VRHandItemPose> itemPoses() {
        return ClientContext.handRenderer.getItemPosesRegistry();
    }

    @Override
    public @NotNull VisorRegistry<VROverlay> overlays() {
        return ClientContext.guiManager
                .getOverlayManager()
                .getOverlaysRegistry();
    }

    @Override
    public @NotNull VisorRegistry<OverlayTemplateRecord> overlayTypes() {
        return ClientContext.guiManager
                .getOverlayManager()
                .getOverlayTypesRegistry();
    }
}
