package me.phoenixra.visor.core.client.settings.overlays;

import me.phoenixra.atumconfig.api.config.ConfigFile;
import me.phoenixra.atumconfig.api.config.ConfigType;
import me.phoenixra.atumconfig.api.config.catalog.ConfigCatalog;
import me.phoenixra.atumconfig.core.config.AtumConfigFile;
import me.phoenixra.visor.api.client.gui.overlay.template.ConfigOverlaysAccessor;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.core.client.ClientContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class OverlayCatalogsManager implements ConfigOverlaysAccessor {
    protected final Map<String, ConfigFile> configs;

    protected final Map<ConfigFile, ConfigCatalog> configCatalogMap;


    protected final Map<VisorAddon, ConfigCatalog> catalogs;


    public OverlayCatalogsManager() {
        configs = new HashMap<>();
        configCatalogMap = new HashMap<>();
        catalogs = new HashMap<>();
    }

    @Override
    public void reload(@NotNull VisorAddon addon) {
        var catalog = getCatalogOrCreate(addon);
        catalog.reload();
    }



    @Override
    public @NotNull ConfigFile getConfigOrCreate(@NotNull VisorAddon addon,
                                                 @NotNull String id) throws IOException {

        return configs.computeIfAbsent(id, key -> {
            var catalog = getCatalogOrCreate(addon);
            AtumConfigFile config;
            try {
                config = new AtumConfigFile(
                        catalog.getConfigManager(),
                        ConfigType.YAML,
                        id,
                        catalog.getDirectory().resolve(id+".yml"),
                        false
                );
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            configCatalogMap.put(config, catalog);
            return config;
        });
    }

    @Override
    public @Nullable ConfigFile getConfig(@NotNull String id) {
        return configs.get(id);
    }


    public void addConfig(@NotNull VisorAddon addon,
                          @NotNull ConfigFile config){
        var catalog = getCatalogOrCreate(addon);

        var oldConfig = configs.put(config.getId(), config);
        if(oldConfig != null) {
            configCatalogMap.remove(oldConfig);
        }
        configCatalogMap.put(config, catalog);

    }


    public void removeConfig(@NotNull String id){
        var config = configs.remove(id);
        if(config != null) {
            var catalog = configCatalogMap.remove(config);
            if(catalog != null){
                catalog.getConfigFilesMap().remove(id);
            }
        }
    }


    public void onCatalogCleared(@NotNull ConfigCatalog catalog){
        var list = configCatalogMap.entrySet().stream()
                .filter((entry)-> entry.getValue() == catalog)
                .map(Map.Entry::getKey).toList();

        for(var config : list){
            configCatalogMap.remove(config);
            configs.remove(config.getId());
        }
    }

    public List<ConfigFile> getAddonConfigs(@NotNull VisorAddon addon){
        var catalog = getCatalogOrCreate(addon);

        return configCatalogMap.entrySet().stream()
                .filter((entry)->entry.getValue()==catalog)
                .map(Map.Entry::getKey)
                .toList();
    }




    private ConfigCatalog getCatalogOrCreate(@NotNull VisorAddon addon){
        return catalogs.computeIfAbsent(addon, key -> {
            var listener = new OverlayCatalogListener(this);
            var catalog = ClientContext.visor
                    .getConfigManager()
                    .createCatalog(
                            ConfigType.YAML,
                            "overlays/" + key.getAddonId(),
                            Path.of("overlays/" + key.getAddonId()),
                            true,
                            listener
                    );
            listener.setCatalog(catalog);
            listener.setAddon(key);
            return catalog;
        });
    }
}
