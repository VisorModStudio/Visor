package org.vmstudio.visor.api.client.events.render;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.render.decoration.hand.HandRenderState;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.common.eventbus.event.VREvent;

@Getter @Setter
public class HandRenderStateVREvent extends VREvent {
    private final HandType handType;
    private HandRenderState state;

    public HandRenderStateVREvent(@NotNull HandType handType, @NotNull HandRenderState state) {
        super(VisorAPI.addonManager().getCoreAddon());
        this.handType = handType;
        this.state = state;
    }

}
