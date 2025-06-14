package me.phoenixra.visor.api.common.addon;

import me.phoenixra.visor.api.client.gui.overlay.VROverlay;
import me.phoenixra.visor.api.client.gui.overlay.VROverlayType;
import me.phoenixra.visor.api.client.input.action.VisorActionSet;
import me.phoenixra.visor.api.client.render.decoration.VRDecorator;
import me.phoenixra.visor.api.client.render.decoration.effects.VRGameEffect;
import me.phoenixra.visor.api.client.render.decoration.effects.VRHandEffect;
import me.phoenixra.visor.api.client.render.decoration.hand.VRHandItemPose;
import me.phoenixra.visor.api.client.tasks.VisorTask;
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
     * @return task registry instance
     */
    @NotNull
    @Environment(EnvType.CLIENT)
    VisortRegistry<VisorTask> tasks();

    /**
     * Get Action Set registry
     *
     * @return Action Set registry instance
     */
    @NotNull
    @Environment(EnvType.CLIENT)
    VisortRegistry<VisorActionSet> actionSets();



    /**
     * Get VR Decorator registry
     *
     * @return VR decorator registry instance
     */
    @NotNull
    @Environment(EnvType.CLIENT)
    VisortRegistry<VRDecorator> decorators();

    /**
     * Get VR Game Effect registry
     *
     * @return VR game effect registry instance
     */
    @NotNull
    @Environment(EnvType.CLIENT)
    VisortRegistry<VRGameEffect> gameEffects();

    /**
     * Get VR Hand Effect registry
     *
     * @return VR hand effect registry instance
     */
    @NotNull
    @Environment(EnvType.CLIENT)
    VisortRegistry<VRHandEffect> handEffects();

    /**
     * Get VR Hand Item Pose registry
     *
     * @return VR hand item pose registry instance
     */
    @NotNull
    @Environment(EnvType.CLIENT)
    VisortRegistry<VRHandItemPose> itemPoses();



    /**
     * Get VR Overlays registry
     *
     * @return VR overlays registry instance
     */
    @NotNull
    @Environment(EnvType.CLIENT)
    VisortRegistry<VROverlay> overlays();

    /**
     * Get VR Overlay Types registry
     *
     * @return VR overlay types registry instance
     */
    @NotNull
    @Environment(EnvType.CLIENT)
    VisortRegistry<VROverlayType> overlayTypes();

}
