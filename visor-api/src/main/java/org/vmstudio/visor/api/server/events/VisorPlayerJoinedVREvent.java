package org.vmstudio.visor.api.server.events;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.common.eventbus.event.VREvent;
import org.vmstudio.visor.api.server.player.VRServerPlayer;
import org.vmstudio.visor.api.server.player.VisorServerPlayer;

@Getter
public class VisorPlayerJoinedVREvent extends VREvent {

    @NotNull
    private final VisorServerPlayer player;

    public VisorPlayerJoinedVREvent(@NotNull VisorServerPlayer player) {
        super(VisorAPI.addonManager().getCoreAddon());
        this.player = player;
    }
}
