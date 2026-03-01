package org.vmstudio.visor.core.common.eventbus;

import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.api.common.eventbus.event.VREventPriority;
import org.vmstudio.visor.api.common.eventbus.listener.VREventListener;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

public record HandlerData(
        @NotNull VisorAddon owner,
        @NotNull VREventListener listener,
        @NotNull Method method,
        @NotNull Class<?> eventType,
        @NotNull VREventPriority priority,
        boolean ignoreCancelled
){

}
