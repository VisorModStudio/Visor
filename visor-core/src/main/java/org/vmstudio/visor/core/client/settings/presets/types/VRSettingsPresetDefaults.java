package org.vmstudio.visor.core.client.settings.presets.types;

import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.gui.settings.RegisterVRSettingsPreset;
import org.vmstudio.visor.api.client.gui.settings.VRSettingsPreset;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.settings.VRClientSettings;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

@RegisterVRSettingsPreset
public class VRSettingsPresetDefaults extends VRSettingsPreset {

    public static final String ID = "defaults";

    public static final Component NAME = Component.translatable("visor.options.presets.types."+ID+".name");
    public static final Component DESCRIPTION = Component.translatable("visor.options.presets.types."+ID+".description");



    public VRSettingsPresetDefaults(@NotNull VisorAddon owner) {
        super(owner);
    }

    @Override
    public void apply() {
        var playMode = VRClientSettings.getVrPlayMode();

        ClientContext.settingsManager.loadDefaults();

        VRClientSettings.setVrPlayMode(playMode);
        ClientContext.settingsManager.saveOptions();

        for(var actionSet : ClientContext.inputManager.getActionSetRegistry().getAllComponents()){
            actionSet.loadDefaults();
        }



        var overlayRegistry = ClientContext.overlayManager
                .getOverlaysRegistry();
        for(var overlay : new ArrayList<>(overlayRegistry.getAllComponents())){
            if(overlay.isCustom()){
                overlayRegistry.unregisterComponent(overlay.getId());
                continue;
            }
            overlay.getOptions().forEach(
                    option -> {
                        option.loadDefaults();
                        option.save();
                    }
            );
        }
        var overlayConfigAccessor =  ClientContext.settingsManager
                .getOverlayConfigsAccessor();
        VisorAPI.addonManager().getAddons()
                .forEach(it->{
                    overlayConfigAccessor.loadDefaults(it, false);
                });

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
