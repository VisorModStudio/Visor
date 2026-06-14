package org.vmstudio.visor.api.client.events.input;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.ClientFeature;
import org.vmstudio.visor.api.client.input.action.framework.VRActionButton;
import org.vmstudio.visor.api.common.eventbus.event.VREvent;
import org.vmstudio.visor.api.common.eventbus.event.VREventCancelable;

@VREventCancelable
public class ActionButtonVREvent extends VREvent {

    @Getter
    private final VRActionButton actionButton;

    @Getter
    private final boolean pressed;


    public ActionButtonVREvent(@NotNull VRActionButton actionButton,
                               boolean pressed) {
        super(VisorAPI.addonManager().getCoreAddon());
        this.actionButton = actionButton;
        this.pressed = pressed;
    }
}
