package org.vmstudio.visor.api;

import me.phoenixra.atumconfig.api.ConfigManager;

import org.vmstudio.visor.api.server.player.VRServerPlayer;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vmstudio.visor.api.server.player.VisorServerPlayer;

import java.util.Collection;
import java.util.UUID;

/**
 * Access point for server part of the Visor
 */
public interface VisorServer {


    /**
     * Get player as VisorServerPlayer
     *
     * @return Visor server player instance or NULL if player don't have Visor
     */
    @Nullable
    VisorServerPlayer getVisorPlayer(@NotNull ServerPlayer mcPlayer);

    /**
     * Get player as VisorServerPlayer
     *
     * @return Visor server player instance or NULL if player don't have Visor
     */
    @Nullable
    VisorServerPlayer getVisorPlayer(@NotNull UUID playerUuid);


    /**
     * Get player as VRServerPlayer
     *
     * @return VR server player instance or NULL if player don't have Visor or not in VR
     */
    @Nullable
    default VRServerPlayer getVRPlayer(@NotNull ServerPlayer mcPlayer){
        var visorPlayer = getVisorPlayer(mcPlayer);
        if(visorPlayer == null) return null;
        return visorPlayer.asVR();
    }

    /**
     * Get player as VRServerPlayer
     *
     * @return VR server player instance or NULL if player don't have Visor or not in VR
     */
    @Nullable
    default VRServerPlayer getVRPlayer(@NotNull UUID playerUuid){
        var visorPlayer = getVisorPlayer(playerUuid);
        if(visorPlayer == null) return null;
        return visorPlayer.asVR();
    }

    /**
     * Get all VR server players
     *
     * @return VR server players collection
     */
    @NotNull
    Collection<? extends VisorServerPlayer> getAllVisorPlayers();


    /**
     * @return If in dedicated server environment
     */
    default boolean isDedicatedServer(){
        return ModLoader.get().isDedicatedServer();
    }

    /**
     * @return If in local server environment
     */
    default boolean isLocalServer(){
        return !ModLoader.get().isDedicatedServer();
    }


    /**
     * Get Config Manager
     *
     * @return ConfigManager instance
     */
    ConfigManager getConfigManager();

    /**
     * @return Logger of server core
     */
    @NotNull
    Logger getLogger();

}
