package org.vmstudio.visor.api.client.events.provider;

import lombok.Getter;
import me.phoenixra.atumvr.core.input.treadmill.XRTreadmillProvider;
import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.common.eventbus.event.VREvent;

import java.util.List;

/**
 * Register Treadmill providers
 * <p>
 *     Use it if you want to add more or other devices
 * </p>
 */
@Getter
public class RegisterTreadmillsVREvent extends VREvent {
    private final List<XRTreadmillProvider> providers;

    public RegisterTreadmillsVREvent(@NotNull List<XRTreadmillProvider> providers){
        super(VisorAPI.addonManager().getCoreAddon());
        this.providers = providers;
    }

    public void addProvider(@NotNull XRTreadmillProvider provider){
        providers.add(provider);
    }
}
