package org.vmstudio.visor.api.client.gui.settings;

import lombok.Getter;
import me.phoenixra.atumconfig.api.config.Config;
import me.phoenixra.atumconfig.api.config.ConfigType;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.common.VRException;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public abstract class VRSettingsPresetConfigBased extends VRSettingsPreset {

    protected final Config config;

    @Getter
    private final List<VRPresetSettingsType> settingTypes;


    protected VRSettingsPresetConfigBased(@NotNull VisorAddon owner,
                                          @NotNull Config config) {
        super(owner);
        this.config = config;

        settingTypes = new ArrayList<>();
        for(var entry : VRPresetSettingsType.values()){
            if(config.hasPath(entry.getKey())){
                settingTypes.add(entry);
            }
        }
    }

    @Override
    public void apply() {
        for(var type : settingTypes){
            type.getLoader().accept(
                    config.getSubsection(type.getKey())
            );
        }
    }
    protected static Config loadResourceConfig(@NotNull String resourcePath) {
        String path = resourcePath.startsWith("/") ? resourcePath : "/" + resourcePath;
        try (InputStream in = VRSettingsPresetConfigBased.class
                .getResourceAsStream(path)) {
            if (in == null) {
                throw new IOException(
                        "Preset resource not found: " + path
                );
            }
            return VisorAPI.client().getConfigManager()
                    .createConfigFromStream(ConfigType.YAML, in);
        } catch (IOException e) {
            throw new VRException(e);
        }
    }
}
