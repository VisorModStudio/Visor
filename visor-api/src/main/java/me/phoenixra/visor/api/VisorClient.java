package me.phoenixra.visor.api;

import me.phoenixra.atumconfig.api.ConfigManager;
import me.phoenixra.visor.api.client.ClientFeature;
import me.phoenixra.visor.api.client.ClientPlayer;
import me.phoenixra.visor.api.client.gui.GuiManager;
import me.phoenixra.visor.api.client.input.InputManager;
import me.phoenixra.visor.api.client.render.VisorRenderer;
import me.phoenixra.visor.api.client.render.decoration.VRDecoratorManager;
import me.phoenixra.visor.api.client.render.decoration.hand.VRHandRenderer;
import me.phoenixra.visor.api.client.tasks.VisorTask;
import me.phoenixra.visor.api.common.addon.VisorElementRegistry;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public interface VisorClient {

    ConfigManager getConfigManager();

    InputManager getInputManager();
    /**
     *
     * @return Registry for VR tasks
     */
    @NotNull
    VisorElementRegistry<VisorTask> getTaskRegistry();

    /**
     * @return Logger of client core
     */
    @NotNull
    Logger getLogger();


    boolean isFeatureEnabled(@NotNull ClientFeature feature);

    /**
     *
     * @return VRClientPlayer instance
     */
    @NotNull
    ClientPlayer getPlayer();

    /**
     *
     * @return GuiManager instance
     */
    @NotNull
    GuiManager getGuiManager();


    /**
     *
     * @return VisorRenderer instance
     */
    @NotNull
    VisorRenderer getRenderer();

    /**
     * Get VR Decorator manager.
     *
     * @return VR Decorator manager instance
     */
    @NotNull VRDecoratorManager getDecoratorManager();

    /**
     * Get Hands renderer.
     * <br>
     * Can be used to render effects attached
     * to VR hands.
     * @return Hands renderer instance
     */
    @NotNull VRHandRenderer getHandsRenderer();

}
