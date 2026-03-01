package org.vmstudio.visor.core.client.settings.presets;

import me.phoenixra.atumconfig.api.config.ConfigFile;
import me.phoenixra.atumconfig.api.config.catalog.ConfigCatalog;
import me.phoenixra.atumconfig.api.config.catalog.ConfigCatalogListener;
import org.vmstudio.visor.api.ModLoader;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.common.utils.LoggerUtils;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.settings.presets.types.VRSettingsPresetCustom;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class PresetsCatalogListener implements ConfigCatalogListener {

    @Override
    public void onClear(@NotNull ConfigCatalog catalog) {
        var registry = ClientContext.settingsManager.getPresetsRegistry();

        for(var entry : new ArrayList<>(registry.getAllComponents())){
            if(entry.isCustom()){
                registry.unregisterComponent(entry.getId());
            }
        }
    }

    @Override
    public void onConfigLoaded(@NotNull ConfigCatalog catalog,
                               @NotNull ConfigFile config) {
        var preset = new VRSettingsPresetCustom(
                config.getId(), config
        );
        var registry = ClientContext.settingsManager.getPresetsRegistry();
        if(registry.getComponent(preset.getId()) != null){
            LoggerUtils.getLogger().error(
                    "The preset with id {} already exists!", preset.getId()
            );
            return;
        }
        if(!preset.getOriginVisorVersion().equals(ModLoader.get().getModVersion(VisorAPI.MOD_ID))){
            LoggerUtils.getLogger().error(
                    "The preset with id {} was created for a different Visor version: {} and may work incorrectly",
                    preset.getId(),
                    preset.getOriginVisorVersion()
            );
        }
        registry.registerComponent(preset);

    }
}
