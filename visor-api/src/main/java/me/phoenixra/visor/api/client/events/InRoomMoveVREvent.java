package me.phoenixra.visor.api.client.events;

import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.common.eventbus.event.VREvent;
import me.phoenixra.visor.api.common.eventbus.event.VREventCancelable;

@VREventCancelable
public class InRoomMoveVREvent extends VREvent {

    public InRoomMoveVREvent() {
        super(VisorAPI.addonManager().getCoreAddon());
    }
}
