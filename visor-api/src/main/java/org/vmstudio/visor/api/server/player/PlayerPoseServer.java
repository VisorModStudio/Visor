package org.vmstudio.visor.api.server.player;

import org.vmstudio.visor.api.common.player.VRPlayerPose;
import org.vmstudio.visor.api.common.player.VRPose;

public interface PlayerPoseServer extends VRPlayerPose {

    /**
     * Get pose of active hand
     *
     * @return pose
     */
    VRPose getActiveHand();
}
