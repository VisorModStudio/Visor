package me.phoenixra.visor.api.common.addon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public interface VisorAddon {


    /**
     * Called when addon is loaded
     */
    void onAddonLoad();

    /**
     * Called when addon is removed
     */
    void onAddonRemove();

    /**
     * Get package that will be used to detect
     * VR-annotations
     * @return path
     */
    @Nullable
    default String getAddonPackagePath(){
        return null;
    }


    /**
     * @return ID of an addon
     */
    @NotNull
    String getAddonId();
}
