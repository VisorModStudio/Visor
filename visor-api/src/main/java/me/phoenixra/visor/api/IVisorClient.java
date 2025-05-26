package me.phoenixra.visor.api;

import me.phoenixra.atumconfig.api.ConfigManager;
import me.phoenixra.visor.api.client.ClientPlayer;
import me.phoenixra.visor.api.client.IClientProperties;
import me.phoenixra.visor.api.client.gui.GuiManager;
import me.phoenixra.visor.api.client.render.IVisorRenderer;
import me.phoenixra.visor.api.client.render.gameview.IVRGameViewHandler;
import me.phoenixra.visor.api.client.render.gameview.hand.IVRHandRenderer;
import me.phoenixra.visor.api.client.tasks.VisorTask;
import me.phoenixra.visor.api.common.addon.VisorElementRegistry;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public interface IVisorClient {

    ConfigManager getConfigManager();

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

    @NotNull
    IClientProperties getProperties();
    /**
     *
     * @return VRClientPlayer instance
     */
    @NotNull
    ClientPlayer getClientPlayer();

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
    IVisorRenderer getVrRenderer();

    /**
     * Get Self View renderer.
     * <br>
     * Can be used to render effects attached
     * to VR component like HMD
     * @return Self View renderer instance
     */
    @NotNull IVRGameViewHandler getGameViewHandler();

    /**
     * Get Hands renderer.
     * <br>
     * Can be used to render effects attached
     * to VR hands.
     * @return Hands renderer instance
     */
    @NotNull IVRHandRenderer getHandsRenderer();

}
