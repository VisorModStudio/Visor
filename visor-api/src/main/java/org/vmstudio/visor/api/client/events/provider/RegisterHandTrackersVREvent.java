package org.vmstudio.visor.api.client.events.provider;

import lombok.Getter;
import me.phoenixra.atumvr.core.input.profile.tracker.hand.XRHandsProvider;
import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.common.eventbus.event.VREvent;

import java.util.List;

/**
 * Register Hand tracker providers
 * <p>
 *     Use it if you want to add more or other devices
 * </p>
 */
@Getter
public class RegisterHandTrackersVREvent extends VREvent {
    private final List<XRHandsProvider> providers;

    public RegisterHandTrackersVREvent(@NotNull List<XRHandsProvider> providers){
        super(VisorAPI.addonManager().getCoreAddon());
        this.providers = providers;
    }

    public void addProvider(@NotNull XRHandsProvider provider){
        providers.add(provider);
    }
}
