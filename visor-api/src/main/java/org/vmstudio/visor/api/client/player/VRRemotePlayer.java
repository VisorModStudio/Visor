package org.vmstudio.visor.api.client.player;

import net.minecraft.client.player.RemotePlayer;
import org.jetbrains.annotations.NotNull;

public interface VRRemotePlayer extends VRClientPlayer{

    /**
     * Get remote player associated with this instance
     *
     * @return mc player
     */
    @NotNull RemotePlayer getMcPlayer();

    @Override
    default boolean isRemote() {
        return true;
    }
}
