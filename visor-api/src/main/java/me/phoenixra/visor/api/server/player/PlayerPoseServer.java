package me.phoenixra.visor.api.server.player;

import me.phoenixra.visor.api.common.player.PlayerPose;
import me.phoenixra.visor.api.common.player.VRPose;

public interface PlayerPoseServer extends PlayerPose {

    /**
     * Get pose of active hand
     *
     * @return pose
     */
    VRPose getActiveHand();
}
