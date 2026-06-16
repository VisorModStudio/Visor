package org.vmstudio.visor.core.client.settings.presets.types;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.api.client.gui.settings.RegisterVRSettingsPreset;
import org.vmstudio.visor.api.client.gui.settings.VRSettingsPresetConfigBased;
import org.vmstudio.visor.api.common.addon.VisorAddon;

@RegisterVRSettingsPreset
public class VRSettingsPresetHandHud extends VRSettingsPresetConfigBased {

    public static final String ID = "defaults_hand_hud";

    public static final Component NAME = Component.translatable("visor.options.presets.types."+ID+".name");
    public static final Component DESCRIPTION = Component.translatable("visor.options.presets.types."+ID+".description");


    public VRSettingsPresetHandHud(@NotNull VisorAddon owner) {
        super(owner, loadResourceConfig("assets/visor/presets/"+ID+".yml"));
    }


    @Override
    public @NotNull String getId() {
        return ID;
    }

    @Override
    public @NotNull Component getName() {
        return NAME;
    }

    @Override
    public @NotNull Component getDescription() {
        return DESCRIPTION;
    }
}
