package me.phoenixra.visor.core.client.settings;

import me.phoenixra.atumconfig.api.ConfigManager;
import me.phoenixra.atumconfig.api.config.Config;
import me.phoenixra.atumconfig.api.config.ConfigFile;
import me.phoenixra.atumconfig.api.config.ConfigType;
import me.phoenixra.atumconfig.api.config.catalog.ConfigCatalog;
import me.phoenixra.atumconfig.api.config.catalog.ConfigCatalogListener;
import me.phoenixra.atumconfig.core.config.AtumConfigFile;
import me.phoenixra.visor.api.client.gui.overlay.template.ConfigOverlaysAccessor;
import me.phoenixra.visor.api.client.gui.overlay.template.OverlayTemplateRecord;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.VisorState;
import me.phoenixra.visor.api.common.utils.LoggerUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class OverlaysCatalogListener implements ConfigOverlaysAccessor, ConfigCatalogListener {
    private final HashMap<String, ConfigFile> configs;

    protected ConfigCatalog catalog;
    public OverlaysCatalogListener() {
        configs = new HashMap<>();
    }



    @Override
    public void onConfigLoaded(@NotNull ConfigFile config) {
        configs.put(config.getId(), config);
    }

    @Override
    public void reload() {
        catalog.reload();
    }

    @Override
    public void afterReload() {
        loadOverlays();
    }

    @Override
    public void beforeLoadDefaults() {
        ConfigManager configManager = catalog.getConfigManager();
        try {
            //create default overlay types
            new AtumConfigFile(
                    configManager,
                    ConfigType.YAML,
                    "chat",
                    catalog.getDirectory().resolve("chat.yml"),
                    true
            );
            new AtumConfigFile(
                    configManager,
                    ConfigType.YAML,
                    "hud",
                    catalog.getDirectory().resolve("hud.yml"),
                    true
            );
        }catch (Throwable e){
            VisorState.destroyVRWithErrorScreen(e);
        }
    }

    @Override
    public void onClear() {
        configs.clear();
    }


    @Override
    public @NotNull ConfigFile getConfigOrCreate(@NotNull String id) throws IOException {
        ConfigFile config = configs.get(id);
        if(config == null){
            config = new AtumConfigFile(
                    catalog.getConfigManager(),
                    ConfigType.YAML,
                    id,
                    catalog.getDirectory().resolve(id+".yml"),
                    false
            );
            configs.put(id, config);
        }
        return config;
    }

    @Override
    public @Nullable ConfigFile getConfig(@NotNull String id) {
        return configs.get(id);
    }


    public void removeConfig(@NotNull String id){
        configs.remove(id);
        catalog.getConfigFilesMap().remove(id);
    }



    private void loadOverlays(){
        var overlaysRegistry = ClientContext.overlayManager
                .getOverlaysRegistry();
        var typesRegistry = ClientContext.overlayManager
                .getOverlayTypesRegistry();
        for(Map.Entry<String,ConfigFile> entry : configs.entrySet()){
            String id = entry.getKey();
            Config config = entry.getValue();

            if(overlaysRegistry.getElement(id) != null){
                LoggerUtils.getLogger().error(
                        "The overlay with id {} already exists!", id
                );
                continue;
            }
            String type  = config.getString("template");
            OverlayTemplateRecord overlayType = typesRegistry.getElement(type);
            if(overlayType == null){
                LoggerUtils.getLogger().error(
                        "Unknown overlay type {} specified for {}", type, id
                );
                continue;
            }
            try {
                var overlay = overlayType.constructor().newInstance(
                        overlayType.owner(),
                        id
                );
                overlaysRegistry.registerElement(overlay);
            }catch (Throwable throwable){
                VisorState.destroyVRWithErrorScreen(throwable);
            }
        }
    }
}
