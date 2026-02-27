package me.phoenixra.visor.core.common.addon;

import me.phoenixra.visor.api.client.gui.overlays.VROverlay;
import me.phoenixra.visor.api.client.gui.overlays.VROverlayTemplateRecord;
import me.phoenixra.visor.api.client.gui.settings.VRSettingsPreset;
import me.phoenixra.visor.api.client.input.action.VisorActionSet;
import me.phoenixra.visor.api.client.render.decoration.VRDecorator;
import me.phoenixra.visor.api.client.render.decoration.effects.VRGameEffect;
import me.phoenixra.visor.api.client.render.decoration.effects.VRHandEffect;
import me.phoenixra.visor.api.client.render.decoration.hand.VRHandItemPose;
import me.phoenixra.visor.api.client.tasks.VisorTask;
import me.phoenixra.visor.api.common.addon.ComponentRegistries;
import me.phoenixra.visor.api.common.addon.component.ComponentRegistry;
import me.phoenixra.visor.core.client.ClientContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class VisorRegistriesImpl implements ComponentRegistries {
    protected final List<ComponentRegistry<?>> list;

    public VisorRegistriesImpl(List<ComponentRegistry<?>> registries){
        this.list = registries;
    }

    @Override
    public @NotNull ComponentRegistry<VisorTask> tasks() {
        return ClientContext.visor.getTaskRegistry();
    }

    @Override
    public @NotNull ComponentRegistry<VisorActionSet> actionSets() {
        return ClientContext.inputManager.getActionSetRegistry();
    }

    @Override
    public @NotNull ComponentRegistry<VRDecorator> decorators() {
        return ClientContext.decorationRenderer.getRegistry();
    }

    @Override
    public @NotNull ComponentRegistry<VRGameEffect> gameEffects() {
        return ClientContext.decorationRenderer.getEffectsRegistry();
    }

    @Override
    public @NotNull ComponentRegistry<VRHandEffect> handEffects() {
        return ClientContext.handRenderer.getEffectsRegistry();
    }

    @Override
    public @NotNull ComponentRegistry<VRHandItemPose> itemPoses() {
        return ClientContext.handRenderer.getItemPosesRegistry();
    }

    @Override
    public @NotNull ComponentRegistry<VROverlay> overlays() {
        return ClientContext.guiManager
                .getOverlayManager()
                .getOverlaysRegistry();
    }

    @Override
    public @NotNull ComponentRegistry<VROverlayTemplateRecord> overlayTemplates() {
        return ClientContext.guiManager
                .getOverlayManager()
                .getOverlayTemplatesRegistry();
    }

    @Override
    public @NotNull ComponentRegistry<VRSettingsPreset> settingsPresets() {
        return ClientContext.settingsManager.getPresetsRegistry();
    }
}
