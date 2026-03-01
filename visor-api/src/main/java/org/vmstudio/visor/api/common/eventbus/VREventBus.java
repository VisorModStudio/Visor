package org.vmstudio.visor.api.common.eventbus;

import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.api.common.eventbus.event.VREvent;
import org.vmstudio.visor.api.common.eventbus.listener.VREventListener;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface VREventBus {

    void callEvent(@NotNull VREvent event);

    /**
     * Register event listener
     *
     * @param listener The listener
     */
    void registerListener(@NotNull VisorAddon owner, @NotNull VREventListener listener);

    /**
     * Unregister event listener
     *
     * @param listener The listener
     */
    void unregisterListener(@NotNull VREventListener listener);

    /**
     * Unregister all event listeners
     * attached to addon
     *
     */
    void unregisterListeners(@NotNull VisorAddon owner);

    /**
     * Unregister all event listeners
     *
     */
    void unregisterAllListeners();

    /**
     * Get the set of registered event listeners
     *
     * @return the listeners set
     */
    @NotNull Set<VREventListener> getRegisteredListeners();
}
