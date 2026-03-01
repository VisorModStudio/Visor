package org.vmstudio.visor.api.client.player;

import net.minecraft.client.player.RemotePlayer;

public interface VRRemotePlayer extends VRClientPlayer{

    /**
     * Get remote player associated with this instance
     *
     * @return mc player
     */
    RemotePlayer getMcPlayer();



}
