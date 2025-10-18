package me.phoenixra.visor.api.common.addon;

import me.phoenixra.visor.api.client.gui.overlays.VROverlay;
import me.phoenixra.visor.api.client.gui.overlays.VROverlayTemplateRecord;
import me.phoenixra.visor.api.client.gui.overlays.RegisterVROverlayTemplate;
import me.phoenixra.visor.api.client.input.action.RegisterActionSet;
import me.phoenixra.visor.api.client.input.action.VisorActionSet;
import me.phoenixra.visor.api.client.render.decoration.VRDecorator;
import me.phoenixra.visor.api.client.render.decoration.annotations.RegisterVRDecorator;
import me.phoenixra.visor.api.client.render.decoration.annotations.RegisterVRGameEffect;
import me.phoenixra.visor.api.client.render.decoration.annotations.RegisterVRHandEffect;
import me.phoenixra.visor.api.client.render.decoration.annotations.RegisterVRItemPose;
import me.phoenixra.visor.api.client.render.decoration.effects.VRGameEffect;
import me.phoenixra.visor.api.client.render.decoration.effects.VRHandEffect;
import me.phoenixra.visor.api.client.render.decoration.hand.VRHandItemPose;
import me.phoenixra.visor.api.client.tasks.RegisterVisorTask;
import me.phoenixra.visor.api.client.tasks.VisorTask;
import me.phoenixra.visor.api.common.addon.element.VisorRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.NotNull;

/**
 * Access point for all visor element registries
 */
public interface VisorRegistries {

    /**
     * Get Task Registry
     *
     * <p>Annotation to auto-register on load: {@link RegisterVisorTask}</p>
     *
     * @return task registry instance
     */
    @NotNull
    @Environment(EnvType.CLIENT)
    VisorRegistry<VisorTask> tasks();

    /**
     * Get Action Set registry
     *
     * <p>Annotation to auto-register on load: {@link RegisterActionSet}</p>
     *
     * @return Action Set registry instance
     */
    @NotNull
    @Environment(EnvType.CLIENT)
    VisorRegistry<VisorActionSet> actionSets();



    /**
     * Get VR Decorator registry
     *
     * <p>Annotation to auto-register on load: {@link RegisterVRDecorator}</p>
     *
     * @return VR decorator registry instance
     */
    @NotNull
    @Environment(EnvType.CLIENT)
    VisorRegistry<VRDecorator> decorators();

    /**
     * Get VR Game Effect registry
     *
     * <p>Annotation to auto-register on load: {@link RegisterVRGameEffect}</p>
     *
     * @return VR game effect registry instance
     */
    @NotNull
    @Environment(EnvType.CLIENT)
    VisorRegistry<VRGameEffect> gameEffects();

    /**
     * Get VR Hand Effect registry
     *
     * <p>Annotation to auto-register on load: {@link RegisterVRHandEffect}</p>
     *
     * @return VR hand effect registry instance
     */
    @NotNull
    @Environment(EnvType.CLIENT)
    VisorRegistry<VRHandEffect> handEffects();

    /**
     * Get VR Hand Item Pose registry
     *
     * <p>Annotation to auto-register on load: {@link RegisterVRItemPose}</p>
     *
     * @return VR hand item pose registry instance
     */
    @NotNull
    @Environment(EnvType.CLIENT)
    VisorRegistry<VRHandItemPose> itemPoses();



    /**
     * Get VR Overlays registry
     *
     * <p>Auto-registering is not supported</p>
     *
     * @return VR overlays registry instance
     */
    @NotNull
    @Environment(EnvType.CLIENT)
    VisorRegistry<VROverlay> overlays();

    /**
     * Get VR Overlay templates registry
     *
     * <p>Annotation to auto-register on load: {@link RegisterVROverlayTemplate}</p>
     *
     * @return VR overlay templates registry instance
     */
    @NotNull
    @Environment(EnvType.CLIENT)
    VisorRegistry<VROverlayTemplateRecord> overlayTemplates();

}
