package org.vmstudio.visor.api.client.player.pose;

import me.phoenixra.atumvr.api.enums.ControllerType;
import org.jetbrains.annotations.NotNull;

public interface RawHands {

    boolean isTracking();

    @NotNull RawHand getLeftHand();
    @NotNull RawHand getRightHand();

    default @NotNull RawHand getHand(@NotNull ControllerType side){
        return side == ControllerType.LEFT ? getLeftHand() : getRightHand();
    }
}
