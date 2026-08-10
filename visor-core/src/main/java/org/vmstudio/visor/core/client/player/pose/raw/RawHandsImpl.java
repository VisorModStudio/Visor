package org.vmstudio.visor.core.client.player.pose.raw;

import lombok.Getter;
import lombok.Setter;
import me.phoenixra.atumvr.api.enums.ControllerType;
import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.api.client.player.pose.RawHands;

public class RawHandsImpl implements RawHands {

    @Getter @Setter
    private boolean tracking;

    @Getter
    private final RawHandImpl leftHand = new RawHandImpl();
    @Getter
    private final RawHandImpl rightHand = new RawHandImpl();


    @Override
    public @NotNull RawHandImpl getHand(@NotNull ControllerType side) {
        return (RawHandImpl) RawHands.super.getHand(side);
    }
}
