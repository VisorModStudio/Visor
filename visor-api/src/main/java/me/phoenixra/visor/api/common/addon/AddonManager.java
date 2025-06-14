package me.phoenixra.visor.api.common.addon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Objects;

/**
 * Manages addons and element registries
 */
public interface AddonManager {

    /**
     * Get Element Registries
     *
     * @return element registries
     */
    @NotNull
    VisorRegistries getRegistries();

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
     * <p>its instance can be used to access builtin visor elements.
     * You may also override these elements with yours</p>
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

    /**
     * Load new addon
     * @param addon the addon instance
     */
    void registerAddon(@NotNull VisorAddon addon);


}
