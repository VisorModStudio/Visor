package me.phoenixra.visor.api.common.addon;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public interface VRElementRegistry<T extends VRElement> {

    @ApiStatus.Internal
    void registerAddonPath(@NotNull VisorAddon addon);

    /**
     *
     * @param component to register
     */
    void registerAddonComponent(@NotNull T component);

    /**
     *
     * @param list to register
     */
    default void registerAddonComponent(@NotNull List<T> list){
        list.forEach(this::registerAddonComponent);
    }

    /**
     * Unregister component by id
     *
     * @param id the component id to unregister
     * @return removed component or null
     */
    T unregisterAddonComponent(@NotNull String id);

    /**
     * Unregister all components attached to an addon
     *
     * @param addon to unregister
     */
    default void unregisterAddon(@NotNull VisorAddon addon){
        getAddonComponents(addon).forEach(it ->
                unregisterAddonComponent(it.getId())
        );
    }

    /**
     * Get addon component from id
     *
     * @param id component id
     * @return component or null
     */
    @Nullable
    T getAddonComponent(@NotNull String id);

    /**
     * Get addon components from addon instance
     *
     * @param addon the vr addon
     * @return list of components attached to an addon
     */
    @NotNull
    Collection<T> getAddonComponents(@NotNull VisorAddon addon);

    /**
     * Get all addon components
     *
     * @return list of all addon components registered
     */
    @NotNull
    Collection<T> getAllComponents();
}
