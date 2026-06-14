package org.vmstudio.visor.api.client.events.input;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.input.action.framework.VRActionVec2;
import org.vmstudio.visor.api.common.eventbus.event.VREvent;
import org.vmstudio.visor.api.common.eventbus.event.VREventCancelable;

@VREventCancelable
public class ActionVec2VREvent extends VREvent {

    @Getter
    private final VRActionVec2 actionVec2;


    public ActionVec2VREvent(@NotNull VRActionVec2 actionVec2) {
        super(VisorAPI.addonManager().getCoreAddon());
        this.actionVec2 = actionVec2;
    }
}
