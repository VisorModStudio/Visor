package org.vmstudio.visor.api.client.events.provider;

import lombok.Getter;
import me.phoenixra.atumvr.core.input.haptics.XRBodyHapticsProvider;
import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.common.eventbus.event.VREvent;

import java.util.List;

/**
 * Register Hand haptic providers
 * <p>
 *     Use it if you want to add more or other devices
 * </p>
 */
@Getter
public class RegisterBodyHapticsVREvent extends VREvent {
    private final List<XRBodyHapticsProvider> providers;

    public RegisterBodyHapticsVREvent(@NotNull List<XRBodyHapticsProvider> providers){
        super(VisorAPI.addonManager().getCoreAddon());
        this.providers = providers;
    }

    public void addProvider(@NotNull XRBodyHapticsProvider provider){
        providers.add(provider);
    }
}
