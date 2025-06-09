package me.phoenixra.visor.core.common.eventbus;

import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.api.common.eventbus.event.VREventPriority;
import me.phoenixra.visor.api.common.eventbus.listener.VREventListener;
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
