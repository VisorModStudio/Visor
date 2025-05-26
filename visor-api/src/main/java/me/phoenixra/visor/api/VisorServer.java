package me.phoenixra.visor.api;

import me.phoenixra.atumconfig.api.ConfigManager;

import me.phoenixra.visor.api.server.player.VRServerPlayer;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface VisorServer {

    ConfigManager getConfigManager();

    /**
     * @return Logger of server core
     */
    @NotNull
    Logger getLogger();

    /**
     * Returns true if specified player is registered
     * in server core, i.e. has VR mod
     * <br><br>
     * Registering happens when mod receives VR-packet
     * from player when he logs in
     *
     * @return If player has VR mod
     */
    boolean isVRPlayer(@NotNull ServerPlayer player);

    /**
     * Returns VRServerPlayer instance if specified player
     * is registered in server core, i.e. has VR mod.
     * Otherwise, NULL is returned
     * <br><br>
     * Registering happens when mod receives VR-packet
     * from player when he logs in
     *
     * @return VR server player instance
     * or NULL if player don't have VR mod
     */
    @Nullable
    VRServerPlayer getVrPlayer(@NotNull ServerPlayer player);

    @NotNull
    Collection<VRServerPlayer> getVrPlayers();
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
}
