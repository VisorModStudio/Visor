package org.vmstudio.visor.core.client.settings.presets;

import lombok.Getter;
import me.phoenixra.atumconfig.api.config.Config;
import me.phoenixra.atumconfig.api.config.ConfigType;
import me.phoenixra.atumconfig.core.config.AtumConfigFile;
import me.phoenixra.atumconfig.core.config.AtumConfigSection;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.gui.overlays.VROverlay;
import org.vmstudio.visor.api.client.input.action.VisorActionSet;
import org.vmstudio.visor.api.common.VRException;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.core.client.ClientContext;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.function.Consumer;
import java.util.function.Supplier;
@Getter
public enum VRPresetSettingsType {
    GENERAL(
            "general",
            ()->ClientContext.settingsManager.getSettings(),
            (config)->{
                ClientContext.settingsManager
                        .loadOptionsFrom(config);
                ClientContext.settingsManager.saveOptions();
            }
    ),
    ACTION_BINDINGS(
            "action_bindings",
            ()->{
                Config config = new AtumConfigSection(
                        ClientContext.visor.getConfigManager(),
                        ConfigType.YAML,
                        null
                );
                for(var entry : ClientContext.inputManager.getActionSetRegistry().getAllComponents()){
                    config.set(entry.getId(), entry.getConfig());
                }
                return config;
            },
            (config) -> {
                for(var entry : config.getAllSubsections().entrySet()){
                    VisorActionSet actionSet =  VisorAPI.addonManager()
                            .getRegistries().actionSets().getComponent(entry.getKey());
                    if(actionSet == null) continue;
                    actionSet.load(entry.getValue());
                    actionSet.save();
                }
            }
    ),
    OVERLAYS(
            "overlays",
            ()->{
                Config config = new AtumConfigSection(
                        ClientContext.visor.getConfigManager(),
                        ConfigType.YAML,
                        null
                );
                for(var entry : ClientContext.overlayManager.getOverlaysRegistry().getAllComponents()){
                    var optionsConfig = entry.getOptionsConfig();
                    if(optionsConfig == null) continue;
                    String path;
                    if(entry.isBuiltIn()) {
                        path = "built_in."+entry.getId();
                    }else{
                        path = "custom."+entry.getId();
                    }
                    Config section = new AtumConfigSection(
                            ClientContext.visor.getConfigManager(),
                            ConfigType.YAML,
                            optionsConfig.toMap()
                    );
                    if(entry.isCustom()) {
                        section.set("presets_save_addon_id", entry.getOwner().getAddonId());
                    }
                    config.set(path, section);
                }
                return config;
            },
            (config) -> {
                Config builtInConfig = config.getSubsection("built_in");
                Config customConfig = config.getSubsection("custom");
                var registry = VisorAPI.addonManager().getRegistries().overlays();
                var configsAccessor = ClientContext.settingsManager.getOverlayConfigsAccessor();
                for(var entry : builtInConfig.getAllSubsections().entrySet()){
                    VROverlay overlay = registry.getComponent(entry.getKey());
                    if(overlay != null && overlay.isBuiltIn()){
                        var optionsConfig = overlay.getOptionsConfig();
                        if(optionsConfig == null) {
                            continue;
                        }
                        optionsConfig.applyData(
                                entry.getValue().toMap()
                        );
                        overlay.reloadOptions();
                    }
                }

                //CUSTOM OVERLAYS

                //clean up
                for(var entry : new ArrayList<>(registry.getAllComponents())){
                    if(entry.isCustom()){
                        registry.unregisterComponent(entry.getId());
                    }
                }

                //apply
                var addonsList = new HashSet<VisorAddon>();
                for(var entry : customConfig.getAllSubsections().entrySet()){
                    VROverlay overlay = registry.getComponent(entry.getKey());
                    if(overlay != null){
                        continue;
                    }
                    String addonId = entry.getValue().getStringOrNull("presets_save_addon_id");
                    if(addonId == null) {
                        continue;
                    }
                    VisorAddon addon = VisorAPI.addonManager().getAddon(addonId);
                    if(addon == null){
                        continue;
                    }
                    try {
                        var conf = new AtumConfigFile(
                                ClientContext.visor.getConfigManager(),
                                ConfigType.YAML,
                                entry.getKey(),
                                configsAccessor
                                        .getCatalogOrCreate(addon,false)
                                        .getDirectory()
                                        .resolve(entry.getKey()+".yml"),
                                false
                        );
                        conf.applyData(entry.getValue().toMap());
                        conf.set("presets_save_addon_id", null);
                        conf.save();
                        addonsList.add(addon);
                    } catch (IOException e) {
                        throw new VRException(e);
                    }
                }
                addonsList.forEach(configsAccessor::reload);
            }
    );

    private final String key;
    private final Component name;
    private final Supplier<Config> serializer;
    private final Consumer<Config> loader;


    VRPresetSettingsType(@NotNull String key,
                         @NotNull Supplier<Config> serializer,
                         @NotNull Consumer<Config> loader){
        this.key = key;
        this.serializer = serializer;
        this.loader = loader;

        this.name = Component.translatable("visor.options.presets.settings_type."+key);
    }

    public static VRPresetSettingsType fromId(@NotNull String id){
        if(VRPresetSettingsType.ACTION_BINDINGS.key.equals(id)){
            return ACTION_BINDINGS;
        }
        if(VRPresetSettingsType.OVERLAYS.key.equals(id)){
            return OVERLAYS;
        }
        return GENERAL;
    }

}
