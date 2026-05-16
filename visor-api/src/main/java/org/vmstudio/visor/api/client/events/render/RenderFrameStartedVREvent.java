package org.vmstudio.visor.api.client.events.render;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.api.common.eventbus.event.VREvent;

/**
 * Fired once at the start of every client render frame, before any VR phase
 * has been entered.
 * <p>
 *    Use this as a per-frame driver for addon-side state
 *    machines that need a single, predictable update right before VR rendering
 * </p>
 */
@Getter
public class RenderFrameStartedVREvent extends VREvent {

    private final float partialTicks;

    public RenderFrameStartedVREvent(float partialTicks) {
        super(VisorAPI.addonManager().getCoreAddon());
        this.partialTicks = partialTicks;
    }

}