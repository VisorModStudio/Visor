package me.phoenixra.visor.api;

import me.phoenixra.atumconfig.api.ConfigManager;
import me.phoenixra.visor.api.client.ClientFeature;
import me.phoenixra.visor.api.client.VRClientPlayer;
import me.phoenixra.visor.api.client.gui.GuiManager;
import me.phoenixra.visor.api.client.input.InputManager;
import me.phoenixra.visor.api.client.render.VisorRenderer;
import me.phoenixra.visor.api.client.render.decoration.VRDecorationRenderer;
import me.phoenixra.visor.api.client.tasks.VisorTask;
import me.phoenixra.visor.api.common.addon.VisorElementRegistry;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * Access point for client-side part of the Visor
 */
public interface VisorClient {

    /**
     * If specified client feature is enabled
     *
     * @param feature the feature
     * @return if enabled
     */
    boolean isFeatureEnabled(@NotNull ClientFeature feature);


    /**
     * Get Task Registry
     *
     * @return task registry instance
     */
    @NotNull
    VisorElementRegistry<VisorTask> getTaskRegistry();


    /**
     * Get VR Client Player
     *
     * @return VRClientPlayer instance
     */
    @NotNull
    VRClientPlayer getPlayer();


    /**
     * Get Input Manager
     *
     * @return InputManager instance
     */
    @NotNull
    InputManager getInputManager();


    /**
     * Get Visor Renderer
     *
     * @return VisorRenderer instance
     */
    @NotNull
    VisorRenderer getRenderer();


    /**
     * Get VR Decoration renderer
     *
     * @return VRDecorationRenderer instance
     */
    @NotNull
    VRDecorationRenderer getDecorationRenderer();


    /**
     * Get Gui Manager
     *
     * @return GuiManager instance
     */
    @NotNull
    GuiManager getGuiManager();


    /**
     * Get Config Manager
     *
     * @return ConfigManager instance
     */
    @NotNull
    ConfigManager getConfigManager();


    /**
     * Get Logger
     *
     * @return Logger instance
     */
    @NotNull
    Logger getLogger();

}
