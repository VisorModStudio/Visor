package org.vmstudio.visor.api.client.events;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.VRStateMode;
import org.vmstudio.visor.api.common.eventbus.event.VREvent;

@Getter
public class SessionStateChangedVREvent extends VREvent {

    private final VRStateMode previousState;
    private final VRStateMode newState;

    public SessionStateChangedVREvent(@NotNull VRStateMode previousState,
                                      @NotNull VRStateMode newState) {
        super(VisorAPI.addonManager().getCoreAddon());
        this.previousState = previousState;
        this.newState = newState;
    }

    /**
     * VR session just became at-least-INITIALIZED (i.e. anything except OFF).
     */
    public boolean becameInitialized() {
        return previousState.isNotInitialized() && newState.isInitialized();
    }

    /**
     * VR session just became OFF.
     */
    public boolean becameUninitialized() {
        return previousState.isInitialized() && newState.isNotInitialized();
    }

    /**
     * VR session just became ACTIVE or FOCUSED (i.e. started rendering).
     */
    public boolean becameActive() {
        return previousState.isNotActive() && newState.isActive();
    }

    /**
     * VR session just stopped rendering (ACTIVE/FOCUSED -> INITIALIZED/OFF).
     */
    public boolean becameInactive() {
        return previousState.isActive() && newState.isNotActive();
    }

    /**
     *  VR session just became FOCUSED.
     */
    public boolean becameFocused() {
        return previousState.isNotFocused() && newState.isFocused();
    }

    /**
     * VR session just lost focus (FOCUSED -> anything else).
     */
    public boolean becameUnfocused() {
        return previousState.isFocused() && newState.isNotFocused();
    }
}