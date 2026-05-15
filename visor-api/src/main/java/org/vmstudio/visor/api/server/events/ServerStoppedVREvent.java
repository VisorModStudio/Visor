package org.vmstudio.visor.api.server.events;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.VisorServer;
import org.vmstudio.visor.api.common.eventbus.event.VREvent;

@Getter
public class ServerStoppedVREvent extends VREvent {
    @NotNull
    private final VisorServer server;

    public ServerStoppedVREvent(@NotNull VisorServer server) {
        super(VisorAPI.addonManager().getCoreAddon());
        this.server = server;
    }
}
