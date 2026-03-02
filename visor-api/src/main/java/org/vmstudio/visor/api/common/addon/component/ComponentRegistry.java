package org.vmstudio.visor.api.common.addon.component;

import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.api.common.utils.LoggerUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;


/**
 * Generic registry for visor components.
 */
public interface ComponentRegistry<T extends VisorComponent> {

    /**
     * Validate a component ID using the standard rules.
     * Returns null if valid, or an error message if invalid.
     *
     * @param id the component id to validate
     * @return error message or null if valid
     */
    @Nullable
    default String validateId(@Nullable String id) {
        return ComponentIds.validate(id);
    }

    /**
     * Scan and register all found addon components for specified addon.
     * <p>
     *     Uses {@link VisorAddon#getAddonPackagePath()}
     * </p>
     * @param addon the addon to register
     */
    @ApiStatus.Internal
    void registerAddonPath(@NotNull VisorAddon addon);

    /**
     * Register specified component
     *
     * @param component to register
     */
    void registerComponent(@NotNull T component);

    /**
     * Register specified component list
     *
     * @param list to register
     */
    default void registerComponents(@NotNull List<T> list){
        list.forEach(this::registerComponent);
    }

    /**
     * Unregister component by id
     *
     * @param id the component id
     * @return unregistered component or null
     */
    T unregisterComponent(@NotNull String id);

    /**
     * Unregister all components attached to an addon
     *
     * @param addon to unregister
     */
    default void unregisterAddon(@NotNull VisorAddon addon){
        LoggerUtils.getLogger().info("Unregistering addon from registry: {}", getRegistryName());

        getAddonComponents(addon).forEach(it ->
                unregisterComponent(it.getId())
        );
    }

    /**
     * Get addon component by id
     *
     * @param id component id
     * @return component or null
     */
    @Nullable
    T getComponent(@NotNull String id);

    /**
     * Get addon component from addon instance
     *
     * @param addon the visor addon
     * @return list of components attached to an addon
     */
    @NotNull
    default Collection<T> getAddonComponents(@NotNull VisorAddon addon){
        return getAllComponents().stream()
                .filter(t -> t.getOwner().equals(addon))
                .toList();
    }

    /**
     * Get all addon components
     *
     * @return list of all addon components registered
     */
    @NotNull
    Collection<T> getAllComponents();

    @NotNull
    String getRegistryName();
}
