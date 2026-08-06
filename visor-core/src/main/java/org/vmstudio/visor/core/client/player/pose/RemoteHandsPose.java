package org.vmstudio.visor.core.client.player.pose;

import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.api.common.player.VRPoseHand;
import org.vmstudio.visor.api.common.player.VRPoseHands;

public class RemoteHandsPose implements VRPoseHands {
    private final RemotePlayerPose owner;

    public RemoteHandsPose(@NotNull RemotePlayerPose owner){
        this.owner = owner;
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public @NotNull VRPoseHand getLeftHand() {
        return VRPoseHand.EMPTY;
    }

    @Override
    public @NotNull VRPoseHand getRightHand() {
        return VRPoseHand.EMPTY;
    }
}
