package org.vmstudio.visor.api.client.events.provider;

import lombok.Getter;
import me.phoenixra.atumvr.api.input.body.AtumVRBodyView;
import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.common.eventbus.event.VREvent;

import java.util.List;

/**
 * Register Body tracker providers
 * <p>
 *     Use it if you want to add more or other devices
 * </p>
 */
@Getter
public class RegisterBodyTrackersVREvent extends VREvent {
    private final List<AtumVRBodyView> providers;

    public RegisterBodyTrackersVREvent(@NotNull List<AtumVRBodyView> providers){
        super(VisorAPI.addonManager().getCoreAddon());
        this.providers = providers;
    }

    public void addProvider(@NotNull AtumVRBodyView provider){
        providers.add(provider);
    }
}
