package me.phoenixra.visor.api;


import me.phoenixra.visor.api.common.addon.AddonManager;
import me.phoenixra.visor.api.common.eventbus.VREventBus;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public interface VisorAPI {
    String MOD_ID = "visor";
    String MOD_NAME = "Visor";
    Path CONFIG_PATH = Path.of("config/"+MOD_NAME);



    /**
     * Get Visor client
     * <br>
     * @return visor client or null if on dedicated server
     */
    static VisorClient client(){
        return Instance.client;
    }

    static VisorClientState clientState(){
        return Instance.clientState;
    }

    /**
     * Get VR Server Core
     * <br>
     * NULL WHEN:<br>
     * Environment is not dedicated server <br>
     * AND <br>
     * Client is playing on non-local server
     * <br><br>
     * Its instance is cleared when <br>
     * player left his local server, so,<br>
     * don't save server core instance on client side,<br>
     * only access it from here.
     * @return server core or null
     */
    static VisorServer server(){
        return Instance.server;
    }


    /**
     * Get Visor Addon manager.
     * <br>
     * You can use it to create your own addons to extend
     * Visor features
     * @return addons manager
     */
    @NotNull
    static AddonManager addonManager(){
        return Instance.addonManager;
    }

    static VREventBus getEventBus(){
        return Instance.eventBus;
    }

    @ApiStatus.Internal
    final class Instance {

        private static VisorClient client;
        private static VisorClientState clientState;

        private static VisorServer server;

        private static AddonManager addonManager;
        private static VREventBus eventBus;

        private Instance() {
            throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
        }

        @ApiStatus.Internal
        public static void setClient(final VisorClient api) {
            Instance.client = api;
        }
        @ApiStatus.Internal
        public static void setClientState(final VisorClientState api) {
            Instance.clientState = api;
        }

        @ApiStatus.Internal
        public static void setServer(final VisorServer api) {
            Instance.server = api;
        }

        @ApiStatus.Internal
        public static void setAddonManager(final AddonManager api) {
            Instance.addonManager = api;
        }
        @ApiStatus.Internal
        public static void setEventBus(final VREventBus api) {
            Instance.eventBus = api;
        }
    }
}