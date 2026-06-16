package org.vmstudio.visor.core.client.settings.presets;

import me.phoenixra.atumconfig.api.config.Config;
import me.phoenixra.atumconfig.api.config.ConfigType;
import me.phoenixra.atumconfig.core.config.AtumConfigFile;
import me.phoenixra.atumconfig.core.config.AtumConfigSection;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.gui.overlays.VROverlay;
import org.vmstudio.visor.api.client.gui.settings.VRPresetSettingsType;
import org.vmstudio.visor.api.client.input.action.VRActionSet;
import org.vmstudio.visor.api.common.VRException;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.core.client.ClientContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;


public final class VRPresetSettingsTypeImpl {

    private VRPresetSettingsTypeImpl() {
    }

    public static void init() {
        registerGeneral();
        registerActionBindings();
        registerObverlays();
    }

    private static void registerGeneral() {
        VRPresetSettingsType.GENERAL.setSerializer(
                () -> ClientContext.settingsManager.getConfig()
        );
        VRPresetSettingsType.GENERAL.setLoader(
                (config) -> {
                    ClientContext.settingsManager
                            .loadOptionsFrom(config, true);
                    ClientContext.settingsManager.saveOptions();
                }
        );
    }

    private static void registerActionBindings() {
        VRPresetSettingsType.ACTION_BINDINGS.setSerializer(
                () -> {
                    Config config = new AtumConfigSection(
                            ClientContext.visor.getConfigManager(),
                            ConfigType.YAML,
                            null
                    );
                    for(var entry : ClientContext.inputManager.getActionSetRegistry().getAllComponents()){
                        config.set(entry.getId(), entry.getConfig());
                    }
                    return config;
                }
        );
        VRPresetSettingsType.ACTION_BINDINGS.setLoader(
                (config) -> {
                    for(var entry : config.getAllSubsections().entrySet()){
                        VRActionSet actionSet =  VisorAPI.addonManager()
                                .getRegistries().actionSets().getComponent(entry.getKey());
                        if(actionSet == null) continue;
                        actionSet.load(entry.getValue());
                        actionSet.save();
                    }
                }
        );
    }

    private static void registerObverlays() {
        VRPresetSettingsType.OVERLAYS.setSerializer(
                () -> {
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
                }
        );
        VRPresetSettingsType.OVERLAYS.setLoader(
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
                            try {
                                optionsConfig.save();
                            } catch (IOException e) {
                                throw new VRException(e);
                            }
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
    }

}
