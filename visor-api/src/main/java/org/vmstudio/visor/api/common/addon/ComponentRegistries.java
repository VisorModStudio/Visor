package org.vmstudio.visor.api.common.addon;

import org.vmstudio.visor.api.client.gui.overlays.VROverlay;
import org.vmstudio.visor.api.client.gui.overlays.VROverlayTemplateRecord;
import org.vmstudio.visor.api.client.gui.overlays.RegisterVROverlayTemplate;
import org.vmstudio.visor.api.client.gui.settings.RegisterVRSettingsPreset;
import org.vmstudio.visor.api.client.gui.settings.VRSettingsPreset;
import org.vmstudio.visor.api.client.input.action.RegisterActionSet;
import org.vmstudio.visor.api.client.input.action.VRActionSet;
import org.vmstudio.visor.api.client.render.decoration.VRDecorator;
import org.vmstudio.visor.api.client.render.decoration.annotations.RegisterVRDecorator;
import org.vmstudio.visor.api.client.render.decoration.annotations.RegisterVRGameEffect;
import org.vmstudio.visor.api.client.render.decoration.annotations.RegisterVRHandEffect;
import org.vmstudio.visor.api.client.render.decoration.annotations.RegisterVRItemPose;
import org.vmstudio.visor.api.client.render.decoration.effects.VRGameEffect;
import org.vmstudio.visor.api.client.render.decoration.effects.VRHandEffect;
import org.vmstudio.visor.api.client.render.decoration.hand.VRHandItemPose;
import org.vmstudio.visor.api.client.tasks.RegisterVisorTask;
import org.vmstudio.visor.api.client.tasks.VisorTask;
import org.vmstudio.visor.api.common.addon.component.ComponentRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.NotNull;

/**
 * Access point for all visor component registries
 * @see AddonManager
 */
public interface ComponentRegistries {

    /**
     * Get Task Registry
     *
     * <p>Annotation to auto-register on load: {@link RegisterVisorTask}</p>
     *
     * @return task registry instance
     */
    @NotNull
    @Environment(EnvType.CLIENT)
    ComponentRegistry<VisorTask> tasks();

    /**
     * Get Action Set registry
     *
     * <p>Annotation to auto-register on load: {@link RegisterActionSet}</p>
     *
     * @return Action Set registry instance
     */
    @NotNull
    @Environment(EnvType.CLIENT)
    ComponentRegistry<VRActionSet> actionSets();



    /**
     * Get VR Decorator registry
     *
     * <p>Annotation to auto-register on load: {@link RegisterVRDecorator}</p>
     *
     * @return VR decorator registry instance
     */
    @NotNull
    @Environment(EnvType.CLIENT)
    ComponentRegistry<VRDecorator> decorators();

    /**
     * Get VR Game Effect registry
     *
     * <p>Annotation to auto-register on load: {@link RegisterVRGameEffect}</p>
     *
     * @return VR game effect registry instance
     */
    @NotNull
    @Environment(EnvType.CLIENT)
    ComponentRegistry<VRGameEffect> gameEffects();

    /**
     * Get VR Hand Effect registry
     *
     * <p>Annotation to auto-register on load: {@link RegisterVRHandEffect}</p>
     *
     * @return VR hand effect registry instance
     */
    @NotNull
    @Environment(EnvType.CLIENT)
    ComponentRegistry<VRHandEffect> handEffects();

    /**
     * Get VR Hand Item Pose registry
     *
     * <p>Annotation to auto-register on load: {@link RegisterVRItemPose}</p>
     *
     * @return VR hand item pose registry instance
     */
    @NotNull
    @Environment(EnvType.CLIENT)
    ComponentRegistry<VRHandItemPose> itemPoses();



    /**
     * Get VR Overlays registry
     *
     * <p>Auto-registering is not supported</p>
     *
     * @return VR overlays registry instance
     */
    @NotNull
    @Environment(EnvType.CLIENT)
    ComponentRegistry<VROverlay> overlays();

    /**
     * Get VR Overlay templates registry
     *
     * <p>Annotation to auto-register on load: {@link RegisterVROverlayTemplate}</p>
     *
     * @return VR overlay templates registry instance
     */
    @NotNull
    @Environment(EnvType.CLIENT)
    ComponentRegistry<VROverlayTemplateRecord> overlayTemplates();



    /**
     * Get Settings presets registry
     *
     * <p>Annotation to auto-register on load: {@link RegisterVRSettingsPreset}</p>
     *
     * @return VR settings presets registry instance
     */
    @NotNull
    @Environment(EnvType.CLIENT)
    ComponentRegistry<VRSettingsPreset> settingsPresets();
}
