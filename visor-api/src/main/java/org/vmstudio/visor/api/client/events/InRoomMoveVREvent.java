package org.vmstudio.visor.api.client.events;

import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.common.eventbus.event.VREvent;
import org.vmstudio.visor.api.common.eventbus.event.VREventCancelable;

@VREventCancelable
public class InRoomMoveVREvent extends VREvent {

    public InRoomMoveVREvent() {
        super(VisorAPI.addonManager().getCoreAddon());
    }
}
