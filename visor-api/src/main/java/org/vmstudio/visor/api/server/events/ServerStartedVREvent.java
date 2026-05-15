package org.vmstudio.visor.api.server.events;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.VisorServer;
import org.vmstudio.visor.api.common.eventbus.event.VREvent;
import org.vmstudio.visor.api.server.player.VRServerPlayer;

@Getter
public class ServerStartedVREvent extends VREvent {
    @NotNull
    private final VisorServer server;

    public ServerStartedVREvent(@NotNull VisorServer server) {
        super(VisorAPI.addonManager().getCoreAddon());
        this.server = server;
    }
}
