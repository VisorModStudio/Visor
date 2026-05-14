package org.vmstudio.visor.api.client.events.gui;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.gui.overlays.VROverlay;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.common.eventbus.event.VREvent;
import org.vmstudio.visor.api.common.eventbus.event.VREventCancelable;



@Getter
@VREventCancelable
public class CursorFocusChangedVREvent extends VREvent {

    @NotNull private final HandType hand;
    @Nullable private final VROverlay previousOverlay;
    @Nullable private final VROverlay newOverlay;

    public CursorFocusChangedVREvent(@NotNull HandType hand,
                                     @Nullable VROverlay previousOverlay,
                                     @Nullable VROverlay newOverlay) {
        super(VisorAPI.addonManager().getCoreAddon());
        this.hand = hand;
        this.previousOverlay = previousOverlay;
        this.newOverlay = newOverlay;
    }
}