package org.vmstudio.visor.core.client.settings.presets.types;

import lombok.Getter;
import me.phoenixra.atumconfig.api.config.Config;
import me.phoenixra.atumconfig.api.config.ConfigType;
import me.phoenixra.atumconfig.core.config.AtumConfigFile;
import net.minecraft.network.chat.Component;
import org.vmstudio.visor.api.ModLoader;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.gui.settings.VRPresetSettingsType;
import org.vmstudio.visor.api.client.gui.settings.VRSettingsPresetConfigBased;
import org.vmstudio.visor.core.client.ClientContext;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

//@TODO
public class VRSettingsPresetCustom extends VRSettingsPresetConfigBased {

    @Getter
    private final String id;
    @Getter
    private final Component name;
    @Getter
    private final Component description;

    @Getter
    private final String originVisorVersion;


    public VRSettingsPresetCustom(@NotNull String id,
                                  @NotNull Config config) {
        super(ClientContext.coreAddon, config);
        this.id = id;
        this.name = Component.translatable(config.getString("name"));
        this.description = Component.translatable(config.getString("description"));
        this.originVisorVersion = config.getStringOrDefault("origin_version", "Unknown");
    }

    @Override
    public boolean isCustom() {
        return true;
    }

    public static VRSettingsPresetCustom createNew(@NotNull String id,
                                                   @NotNull String name,
                                                   @NotNull String description,
                                                   @NotNull List<VRPresetSettingsType> settingTypes) throws IOException {
        var catalog = ClientContext.settingsManager.getPresetsCatalog();
        if(catalog.getConfigFile(id).isPresent()){
            throw new RuntimeException("Tried to create preset with already existing id");
        }

        var config = new AtumConfigFile(
                ClientContext.visor.getConfigManager(),
                ConfigType.YAML,
                id,
                catalog.getDirectory().resolve(id+".yml"),
                false
        );
        config.set("name", name);
        config.set("description", description);
        config.set("origin_version", ModLoader.get().getModVersion(VisorAPI.MOD_ID));
        for(var type : settingTypes){
            config.set(type.getKey(), type.getSerializer().get());
        }
        config.save();
        config.reload();
        catalog.getConfigFilesMap().put(id, config);
        var preset = new VRSettingsPresetCustom(
                id, config
        );
        ClientContext.settingsManager.getPresetsRegistry()
                .registerComponent(preset);

        return preset;
    }
}
