package org.vmstudio.visor.api.client.events.gui;

import lombok.Getter;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.gui.VRKeyboardAccessor;
import org.vmstudio.visor.api.common.eventbus.event.VREvent;
import org.vmstudio.visor.api.common.eventbus.event.VREventCancelable;

@Getter
@VREventCancelable
public class KeyboardStateChangedVREvent extends VREvent {

    private final VRKeyboardAccessor keyboardAccessor;
    private final boolean visible;
    @Nullable private final Screen attachedTo;

    public KeyboardStateChangedVREvent(@NotNull VRKeyboardAccessor keyboardAccessor,
                                       boolean visible,
                                       @Nullable Screen attachedTo) {
        super(VisorAPI.addonManager().getCoreAddon());
        this.keyboardAccessor = keyboardAccessor;
        this.visible = visible;
        this.attachedTo = attachedTo;
    }
}