package org.vmstudio.visor.api.common.addon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Objects;

/**
 * Manages addons and component registries
 */
public interface AddonManager {

    /**
     * Get Component registries
     *
     * @return component registries
     */
    @NotNull
    ComponentRegistries getRegistries();

    /**
     * Get addon with specified id
     *
     * @param id addon id
     * @return addon instance
     */
    @Nullable
    VisorAddon getAddon(@NotNull String id);


    /**
     * Get core addon.
     * <br>
     * That is an addon registered by Visor itself.
     *
     * <p>
     *     its instance can be used to access builtin visor components.
     *     You may also override these components with yours
     * </p>
     *
     * @return addon instance
     */
    default @NotNull VisorAddon getCoreAddon(){
        return Objects.requireNonNull(getAddon("core"));
    }

    /**
     *
     * @return All loaded addons
     */
    @NotNull
    Collection<VisorAddon> getAddons();

}
