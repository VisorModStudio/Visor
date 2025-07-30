package me.phoenixra.visor.api;


import lombok.Getter;
import me.phoenixra.visor.api.client.VRPlayMode;
import me.phoenixra.visor.api.client.VRStateMode;
import me.phoenixra.visor.api.client.render.RenderPhase;
import me.phoenixra.visor.api.client.render.VRDisplay;
import me.phoenixra.visor.api.common.addon.AddonManager;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.api.common.eventbus.VREventBus;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.*;


/**
 * Central access point for all Visor API functionality.
 *
 */
public interface VisorAPI {

    /** Visor mod identifier */
    String MOD_ID = "visor";

    /** Visor mod name */
    String MOD_NAME = "Visor";

    /** Base path for Visor configuration files (relative to game directory). */
    Path CONFIG_PATH = ModLoader.get().getConfigFolder().toPath().resolve(MOD_NAME);


    /**
     * Registers an addon, that will be loaded later during Visor startup.
     * <p>Use this method only during mod initialization <br>
     * or before Visor (client/server) instance is created.</p>
     * <p>Visor instance is created late, after all mods initialized</p>
     * @param addon the addon
     */
    static void registerAddon(@NotNull VisorAddon addon){
        if(addonManager() != null){
            throw new RuntimeException(
                    "Tried to register Visor addon after Visor instance is created"
            );
        }
        if(Instance.getPreparedAddons().containsKey(addon.getAddonId())){
            throw new RuntimeException(
                    "Tried to register addon with ID '"
                            + addon.getAddonId()
                            + "', that is already registered");
        }

        if (addon.getAddonId().equals("core")) {
            throw new RuntimeException(
                    "Not allowed to register Visor Addon with ID 'core'"
            );
        }
        Instance.getPreparedAddons().put(addon.getAddonId(), addon);
    }

    /**
     * Get Visor client
     *
     * @return visor client
     */
    @NotNull
    @Environment(EnvType.CLIENT)
    static VisorClient client(){
        return Instance.client;
    }

    /**
     * Get Visor client state.
     * <p>
     *     Always not null, even before Visor initialized)
     * </p>
     * @return visor client state
     */
    @NotNull
    @Environment(EnvType.CLIENT)
    static VisorClientState clientState(){
        return Instance.clientState;
    }

    /**
     * Get Visor Server.
     * <p>NOT NULL: Dedicated server environment</p>
     * <p>NULL: When client is not in a world or on a dedicated server</p>
     *
     * @return visor server
     */
    static VisorServer server(){
        return Instance.server;
    }


    /**
     * Get the Visor Addon manager.
     *
     * @return the addon manager
     */
    @NotNull
    static AddonManager addonManager(){
        return Instance.addonManager;
    }

    /**
     * Get the Event Bus
     *
     * @return the event bus
     */
    @NotNull
    static VREventBus eventBus(){
        return Instance.eventBus;
    }


    //REGISTER ADDON

    @ApiStatus.Internal
    final class Instance {
        private Instance() {
            throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
        }

        @Environment(EnvType.CLIENT)
        private static VisorClient client;

        //empty implementation, before Visor initialized
        @Environment(EnvType.CLIENT)
        private static VisorClientState clientState = new VisorClientState() {
            @Override
            public @NotNull VRPlayMode playMode() {return VRPlayMode.DISABLED;}
            @Override
            public @NotNull VRStateMode stateMode() {return VRStateMode.OFF;}
            @Override
            public @NotNull RenderPhase renderPhase() {return RenderPhase.VANILLA;}
            @Override
            public @Nullable("Not rendering VR display") VRDisplay renderingDisplay() {return null;}
        };


        private static VisorServer server;

        private static AddonManager addonManager;
        private static VREventBus eventBus;


        @Getter
        private static HashMap<String,VisorAddon> preparedAddons = new LinkedHashMap<>();

        @ApiStatus.Internal
        @Environment(EnvType.CLIENT)
        public static void setClient(final VisorClient api) {
            Instance.client = api;
        }

        @ApiStatus.Internal
        @Environment(EnvType.CLIENT)
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