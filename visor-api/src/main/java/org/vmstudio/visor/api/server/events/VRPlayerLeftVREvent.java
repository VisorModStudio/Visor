package org.vmstudio.visor.api.server.events;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.common.eventbus.event.VREvent;
import org.vmstudio.visor.api.server.player.VRServerPlayer;

@Getter
public class VRPlayerLeftVREvent extends VREvent {

    @NotNull
    private final VRServerPlayer player;

    public VRPlayerLeftVREvent(@NotNull VRServerPlayer player) {
        super(VisorAPI.addonManager().getCoreAddon());
        this.player = player;
    }
}
