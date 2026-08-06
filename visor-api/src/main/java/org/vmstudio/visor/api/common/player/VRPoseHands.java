package org.vmstudio.visor.api.common.player;

import me.phoenixra.atumvr.api.enums.ControllerType;
import org.jetbrains.annotations.NotNull;

public interface VRPoseHands {

    boolean isActive();

    @NotNull VRPoseHand getLeftHand();

    @NotNull VRPoseHand getRightHand();

    default @NotNull VRPoseHand getHand(@NotNull ControllerType side){
        return side == ControllerType.LEFT ? getLeftHand() : getRightHand();
    }

}
