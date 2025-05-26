package me.phoenixra.visor.api.common.addon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public interface IAddonManager {

    /**
     * Get addon with specified id
     *
     * @param id addon id
     * @return addon instance
     */
    @Nullable
    VisorAddon getAddon(@NotNull String id);


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
