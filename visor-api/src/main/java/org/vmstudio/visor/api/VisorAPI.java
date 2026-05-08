package org.vmstudio.visor.api;


import lombok.Getter;
import net.minecraft.world.entity.player.Player;
import org.vmstudio.visor.api.client.VRPlayMode;
import org.vmstudio.visor.api.client.VRStateMode;
import org.vmstudio.visor.api.client.gui.GuiTexture;
import org.vmstudio.visor.api.client.player.VRClientPlayer;
import org.vmstudio.visor.api.client.render.RenderPhase;
import org.vmstudio.visor.api.client.render.VRRenderPass;
import org.vmstudio.visor.api.common.addon.AddonManager;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.api.common.addon.component.ComponentIds;
import org.vmstudio.visor.api.common.eventbus.VREventBus;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vmstudio.visor.api.common.player.VRPlayer;
import org.vmstudio.visor.api.server.player.VRServerPlayer;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;


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

    /**Visor mod icon**/
    GuiTexture NOD_ICON = new GuiTexture(
            new ResourceLocation(VisorAPI.MOD_ID, "icon.png")
    );

    /**
     * Registers an addon, that will be loaded later during Visor startup.
     * <p>
     *     Use this method only
     *     before Visor instance is created.
     * </p>
     * <p>
     *     Visor instance is created late, after all mods initialized
     * </p>
     * @param addon the addon
     */
    static void registerAddon(@NotNull VisorAddon addon){
        if(addonManager() != null){
            throw new RuntimeException(
                    "Tried to register Visor addon after Visor instance is created"
            );
        }
        if (addon.getAddonId().equals("core")) {
            throw new RuntimeException(
                    "Not allowed to register Visor Addon with ID 'core'"
            );
        }
        if(Instance.getPreparedAddons().containsKey(addon.getAddonId())){
            throw new RuntimeException(
                    "Tried to register addon with ID '"
                            + addon.getAddonId()
                            + "', that is already registered");
        }
        String validationError = ComponentIds.validate(addon.getAddonId());
        if(validationError != null){
            throw new RuntimeException(
                    "Tried to register addon with ID '"
                            + addon.getAddonId()
                            + "'. The ID pattern is incorrect: " + validationError);
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
        return Objects.requireNonNullElse(
                Instance.clientState,
                VisorClientState.Empty.INSTANCE
        );
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
     * Get VR player interface that may represent
     * {@link VRServerPlayer} or {@link VRClientPlayer}
     *
     * @param mcPlayer the minecraft player instance
     * @return the VR player
     */
    @Nullable
    static VRPlayer getVRPlayer(@NotNull Player mcPlayer){
        if(Instance.vrPlayerFunction == null) return null;
        return Instance.vrPlayerFunction.apply(mcPlayer);
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
        private static VisorClientState clientState;


        private static VisorServer server;

        private static Function<Player, VRPlayer> vrPlayerFunction;
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
        public static void setVrPlayerSupplier(final Function<Player, VRPlayer> api) {
            Instance.vrPlayerFunction = api;
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