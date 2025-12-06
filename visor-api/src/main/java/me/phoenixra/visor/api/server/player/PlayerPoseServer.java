package me.phoenixra.visor.api.server.player;

import me.phoenixra.visor.api.common.player.PlayerPose;
import me.phoenixra.visor.api.common.player.PoseElement;

public interface PlayerPoseServer extends PlayerPose {

    /**
     * Get pose element of active hand
     *
     * @return pose element
     */
    PoseElement getActiveHand();
}
