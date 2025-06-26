package me.phoenixra.visor.api.common.addon.element;

import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.api.common.utils.LoggerUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public interface VisorRegistry<T extends VisorElement> {

    @ApiStatus.Internal
    void registerAddonPath(@NotNull VisorAddon addon);

    /**
     *
     * @param component to register
     */
    void registerElement(@NotNull T component);

    /**
     *
     * @param list to register
     */
    default void registerElements(@NotNull List<T> list){
        list.forEach(this::registerElement);
    }

    /**
     * Unregister component by id
     *
     * @param id the component id to unregister
     * @return removed component or null
     */
    T unregisterElement(@NotNull String id);

    /**
     * Unregister all components attached to an addon
     *
     * @param addon to unregister
     */
    default void unregisterAddon(@NotNull VisorAddon addon){
        LoggerUtils.getLogger().info("Unregistering addon from registry: {}", getRegistryName());

        getAddonElements(addon).forEach(it ->
                unregisterElement(it.getId())
        );
    }

    /**
     * Get addon component from id
     *
     * @param id component id
     * @return component or null
     */
    @Nullable
    T getElement(@NotNull String id);

    /**
     * Get addon components from addon instance
     *
     * @param addon the visor addon
     * @return list of components attached to an addon
     */
    @NotNull
    default Collection<T> getAddonElements(@NotNull VisorAddon addon){
        return getAllElements().stream()
                .filter(t -> t.getOwner().equals(addon))
                .toList();
    }

    /**
     * Get all addon components
     *
     * @return list of all addon components registered
     */
    @NotNull
    Collection<T> getAllElements();

    @NotNull
    String getRegistryName();
}
