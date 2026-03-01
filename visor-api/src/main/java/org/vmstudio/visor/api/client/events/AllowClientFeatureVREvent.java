package org.vmstudio.visor.api.client.events;

import lombok.Getter;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.ClientFeature;
import org.vmstudio.visor.api.common.eventbus.event.VREvent;
import org.vmstudio.visor.api.common.eventbus.event.VREventCancelable;
import org.jetbrains.annotations.NotNull;

@VREventCancelable
public class AllowClientFeatureVREvent extends VREvent {

    @Getter
    private final ClientFeature feature;

    public AllowClientFeatureVREvent(@NotNull ClientFeature feature) {
        super(VisorAPI.addonManager().getCoreAddon());
        this.feature = feature;
    }

}
