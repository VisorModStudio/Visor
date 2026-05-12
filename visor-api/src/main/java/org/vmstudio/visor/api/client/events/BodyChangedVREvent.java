package org.vmstudio.visor.api.client.events;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.player.VRClientPlayer;
import org.vmstudio.visor.api.client.player.body.VRBodyType;
import org.vmstudio.visor.api.common.eventbus.event.VREvent;

@Getter
public class BodyChangedVREvent extends VREvent {
    private VRClientPlayer player;
    private VRBodyType vrBodyType;
    public BodyChangedVREvent(@NotNull VRClientPlayer player,
                              @NotNull VRBodyType vrBodyType) {
        super(VisorAPI.addonManager().getCoreAddon());
        this.player = player;
        this.vrBodyType = vrBodyType;
    }
}