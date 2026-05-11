package org.vmstudio.visor.api;

import me.phoenixra.atumconfig.api.ConfigManager;
import me.phoenixra.atumvr.api.AtumVRProvider;
import org.vmstudio.visor.api.client.ClientFeature;
import org.vmstudio.visor.api.client.player.VRClientPlayer;
import org.vmstudio.visor.api.client.player.VRLocalPlayer;
import org.vmstudio.visor.api.client.gui.VRGuiManager;
import org.vmstudio.visor.api.client.input.VRInputManager;
import org.vmstudio.visor.api.client.player.VRRemotePlayer;
import org.vmstudio.visor.api.client.render.VRRenderer;
import org.vmstudio.visor.api.client.render.decoration.VRDecorationRenderer;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Access point for client-side part of the Visor
 */
public interface VisorClient {

    /**
     * If specified client feature is enabled
     *
     * @param feature the client feature to check
     * @return true if enabled; false otherwise
     */
    boolean isFeatureEnabled(@NotNull ClientFeature feature);

    /**
     * If specified client feature is disabled
     *
     * @param feature the client feature to check
     * @return true if disabled; false otherwise
     */
    default boolean isFeatureDisabled(@NotNull ClientFeature feature){
        return !isFeatureEnabled(feature);
    }


    /**
     * If joined Visor supported server.
     *
     * @return true/false
     */
    boolean isInVisorServer();

    /**
     * Get VR Client Player from uuid.
     *
     * <p>
     *     if <code>uuid</code> is associated with
     *     mc local or remote player the method will return
     *     {@link VRLocalPlayer} or {@link VRRemotePlayer} instance,
     *     otherwise null
     * </p>
     *
     * @return VRClientPlayer instance or null
     */
    @Nullable
    VRClientPlayer getVRPlayer(@NotNull UUID uuid);

    /**
     * Get VR Local Player
     *
     * @return VRLocalPlayer instance
     */
    @NotNull
    VRLocalPlayer getVRLocalPlayer();


    /**
     * Get Input Manager
     *
     * @return InputManager instance
     */
    @NotNull
    VRInputManager getInputManager();


    /**
     * Get Visor Renderer
     *
     * @return VisorRenderer instance
     */
    @NotNull
    VRRenderer getRenderer();


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
    VRGuiManager getGuiManager();


    /**
     * Get Config Manager
     *
     * @return ConfigManager instance
     */
    @NotNull
    ConfigManager getConfigManager();

    /**
     * Get VR Provider
     *
     * @return VRProvider instance
     */
    AtumVRProvider getVrProvider();

    /**
     * Get Logger
     *
     * @return Logger instance
     */
    @NotNull
    Logger getLogger();

}
