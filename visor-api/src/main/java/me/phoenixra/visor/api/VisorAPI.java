package me.phoenixra.visor.api;


import me.phoenixra.visor.api.common.addon.IAddonManager;
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
    static IVisorClient client(){
        return Instance.client;
    }

    static IVisorState clientState(){
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
    static IVisorServer server(){
        return Instance.server;
    }


    /**
     * Get VR Addons manager.
     * <br>
     * You can use it to create your own addons to extend
     * Visor features
     * @return addons manager
     */
    @NotNull
    static IAddonManager addonManager(){
        return Instance.addonManager;
    }


    @ApiStatus.Internal
    final class Instance {

        private static IVisorClient client;
        private static IVisorState clientState;

        private static IVisorServer server;

        private static IAddonManager addonManager;

        private Instance() {
            throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
        }

        @ApiStatus.Internal
        public static void setClient(final IVisorClient api) {
            Instance.client = api;
        }
        @ApiStatus.Internal
        public static void setClientState(final IVisorState api) {
            Instance.clientState = api;
        }

        @ApiStatus.Internal
        public static void setServer(final IVisorServer api) {
            Instance.server = api;
        }

        @ApiStatus.Internal
        public static void setAddonManager(final IAddonManager api) {

            Instance.addonManager = api;
        }
    }
}