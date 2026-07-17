package org.vmstudio.visor.api.common.addon;

import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.gui.GuiTexture;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vmstudio.visor.api.common.addon.component.ComponentIds;

/**
 * Base interface for Visor addons.
 * @see AddonManager
 */
public interface VisorAddon {

    GuiTexture MISSING_ICON = new GuiTexture(
            new ResourceLocation(VisorAPI.MOD_ID, "textures/gui/overlays/missing_icon.png")
    );

    /**
     * Called when addon is registered
     *<p>
     *     Use it if you need to load something early. For example, network channel
     *</p>
     */
    default void onAddonRegister(){}

    /**
     * Called when addon is loaded
     */
    void onAddonLoad();

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
     * Get settings screen for this addon.
     * <p>
     *     If returns non-null screen instance,
     *     it will become available in VR settings under "Addons" section
     * </p>
     * @param backScreen the screen to go back to (for "Back" button or Esc pressed)
     * @return path
     */
    @Nullable
    default Screen createAddonSettingsScreen(@NotNull Screen backScreen){
        return null;
    }


    /**
     * Get the ID of this addon
     * <p>
     *     ID value must comply with {@link ComponentIds}
     * </p>
     * @return the addon ID
     */
    @NotNull
    String getAddonId();

    /**
     * @return Name of an addon
     */
    @NotNull
    Component getAddonName();

    /**
     * @return icon of an addon
     */
    default GuiTexture getAddonIcon(){
        return MISSING_ICON;
    }

    /**
     *
     * @return ID of a mod owning this addon
     */
    String getModId();
}
