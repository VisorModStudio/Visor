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

public interface VisorClient {

    ConfigManager getConfigManager();

    InputManager getInputManager();
    /**
     *
     * @return Registry for VR tasks
     */
    @NotNull
    VisorElementRegistry<VisorTask> getTaskRegistry();


    boolean isFeatureEnabled(@NotNull ClientFeature feature);

    /**
     *
     * @return VRClientPlayer instance
     */
    @NotNull
    VRClientPlayer getPlayer();

    /**
     *
     * @return GuiManager instance
     */
    @NotNull
    GuiManager getGuiManager();


    /**
     *
     * @return VisorRenderer
     */
    @NotNull
    VisorRenderer getRenderer();

    /**
     * Get VR Decoration renderer
     *
     * @return VR Decoration renderer
     */
    @NotNull
    VRDecorationRenderer getDecorationRenderer();



    /**
     * @return Logger of visor client
     */
    @NotNull
    Logger getLogger();

}
