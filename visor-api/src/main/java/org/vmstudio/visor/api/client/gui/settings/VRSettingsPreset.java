package org.vmstudio.visor.api.client.gui.settings;

import lombok.Getter;
import lombok.Setter;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.api.common.addon.component.VisorComponent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

/**
 * The idea of settings preset is the ready-to-play setup,
 * that player just choose and go playing without a need to get into
 * many options.
 * Players may also make their custom setups,
 * to easily switch between them.
 * <p>
 *     Useful, for big addons and mod packs,
 *     that support different play-styles.
 * </p>
 */
public abstract class VRSettingsPreset implements VisorComponent {

    @Getter
    @NotNull
    private final VisorAddon owner;

    @Getter @Setter
    private boolean enabled;

    public VRSettingsPreset(@NotNull VisorAddon owner){
        this.owner = owner;

    }


    /**
     * Apply preset.
     * <p>
     *     This method Is intended to apply preset instantly
     *     or to open a submenu where user will configure
     *     preset options and apply.
     * </p>
     */
    public abstract void apply();

    /**
     * Get preset id
     *
     * @return the id
     */
    @NotNull
    public abstract String getId();

    /**
     * Get preset name
     *
     * @return the name
     */
    @NotNull
    public abstract Component getName();

    /**
     * Get preset description
     *
     * @return the description
     */
    @NotNull
    public abstract Component getDescription();

    /**
     * If preset is created by a user
     * @return true/false
     */
    public boolean isCustom(){
        return false;
    }

    /**
     * If preset is created by an addon or visor itself
     * @return true/false
     */
    public boolean isBuiltIn(){
        return !isCustom();
    }





}
