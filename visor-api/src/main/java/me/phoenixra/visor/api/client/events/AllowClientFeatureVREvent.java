package me.phoenixra.visor.api.client.events;

import lombok.Getter;
import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.client.ClientFeature;
import me.phoenixra.visor.api.common.eventbus.event.VREvent;
import me.phoenixra.visor.api.common.eventbus.event.VREventCancelable;
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
