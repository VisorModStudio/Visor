package me.phoenixra.visor.core.common.addon;

import me.phoenixra.visor.api.client.gui.overlay.VROverlay;
import me.phoenixra.visor.api.client.gui.overlay.VROverlayType;
import me.phoenixra.visor.api.client.input.action.VisorActionSet;
import me.phoenixra.visor.api.client.render.decoration.VRDecorator;
import me.phoenixra.visor.api.client.render.decoration.effects.VRGameEffect;
import me.phoenixra.visor.api.client.render.decoration.effects.VRHandEffect;
import me.phoenixra.visor.api.client.render.decoration.hand.VRHandItemPose;
import me.phoenixra.visor.api.client.tasks.VisorTask;
import me.phoenixra.visor.api.common.addon.VisorRegistries;
import me.phoenixra.visor.api.common.addon.VisortRegistry;
import me.phoenixra.visor.core.client.ClientContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class VisorRegistriesImpl implements VisorRegistries {
    protected final List<VisortRegistry<?>> list;

    public VisorRegistriesImpl(List<VisortRegistry<?>> registries){
        this.list = registries;
    }

    @Override
    public @NotNull VisortRegistry<VisorTask> tasks() {
        return ClientContext.visor.getTaskRegistry();
    }

    @Override
    public @NotNull VisortRegistry<VisorActionSet> actionSets() {
        return ClientContext.inputManager.getActionSetRegistry();
    }

    @Override
    public @NotNull VisortRegistry<VRDecorator> decorators() {
        return ClientContext.decorationRenderer.getRegistry();
    }

    @Override
    public @NotNull VisortRegistry<VRGameEffect> gameEffects() {
        return ClientContext.decorationRenderer.getEffectsRegistry();
    }

    @Override
    public @NotNull VisortRegistry<VRHandEffect> handEffects() {
        return ClientContext.handRenderer.getEffectsRegistry();
    }

    @Override
    public @NotNull VisortRegistry<VRHandItemPose> itemPoses() {
        return ClientContext.handRenderer.getItemPosesRegistry();
    }

    @Override
    public @NotNull VisortRegistry<VROverlay> overlays() {
        return ClientContext.guiManager
                .getOverlayManager()
                .getOverlaysRegistry();
    }

    @Override
    public @NotNull VisortRegistry<VROverlayType> overlayTypes() {
        return ClientContext.guiManager
                .getOverlayManager()
                .getOverlayTypesRegistry();
    }
}
