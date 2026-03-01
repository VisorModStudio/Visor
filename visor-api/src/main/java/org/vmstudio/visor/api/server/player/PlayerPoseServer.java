package org.vmstudio.visor.api.server.player;

import org.vmstudio.visor.api.common.player.PlayerPose;
import org.vmstudio.visor.api.common.player.VRPose;

public interface PlayerPoseServer extends PlayerPose {

    /**
     * Get pose of active hand
     *
     * @return pose
     */
    VRPose getActiveHand();
}
