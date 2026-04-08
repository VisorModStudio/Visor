package org.vmstudio.visor.core.client.player.body.full;

import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.api.client.player.VRClientPlayer;
import org.vmstudio.visor.api.client.player.body.VRBody;
import org.vmstudio.visor.api.client.player.body.VRBodyType;
import org.vmstudio.visor.api.client.player.pose.VRPlayerPoseClient;

public class VRBodyFull extends VRBody {
    public VRBodyFull(@NotNull VRBodyType type,
                      @NotNull VRClientPlayer vrPlayer,
                      @NotNull VRPlayerPoseClient vrPlayerPose) {
        super(type, vrPlayer, vrPlayerPose);
    }
}
