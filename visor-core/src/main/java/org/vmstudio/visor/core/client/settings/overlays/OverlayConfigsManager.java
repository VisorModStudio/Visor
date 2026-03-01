package org.vmstudio.visor.core.client.settings.overlays;

import me.phoenixra.atumconfig.api.config.ConfigFile;
import me.phoenixra.atumconfig.api.config.ConfigType;
import me.phoenixra.atumconfig.api.config.catalog.ConfigCatalog;
import me.phoenixra.atumconfig.core.config.AtumConfigFile;
import org.vmstudio.visor.api.client.gui.overlays.VROverlay;
import org.vmstudio.visor.api.client.gui.OverlayConfigAccessor;
import org.vmstudio.visor.api.common.VRException;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.core.client.ClientContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class OverlayConfigsManager implements OverlayConfigAccessor {

    protected final Map<String, ConfigFile> configs;
    protected final Map<ConfigFile, ConfigCatalog> configCatalogMap;


    protected final Map<VisorAddon, ConfigCatalog> catalogsBuiltIn;
    protected final Map<VisorAddon, ConfigCatalog> catalogsCustom;


    public OverlayConfigsManager() {
        configs = new HashMap<>();
        configCatalogMap = new HashMap<>();

        catalogsBuiltIn = new HashMap<>();
        catalogsCustom = new HashMap<>();
    }

    @Override
    public void reload(@NotNull VisorAddon addon, boolean builtIn) {
        var catalog = getCatalogOrCreate(addon, builtIn);
        catalog.reload();
    }
    @Override
    public void loadDefaults(@NotNull VisorAddon addon, boolean builtIn) {
        var catalog = getCatalogOrCreate(addon, builtIn);
        var dir = catalog.getConfigManager()
                .getDirectory().resolve(
                        catalog.getDirectory()
                ).toFile();
        if(dir.exists()){
            dir.delete();
        }
        catalog.reload();

    }



    @Override
    public @NotNull ConfigFile getConfigOrCreate(@NotNull VROverlay overlay) throws IOException {

        String id = overlay.getId();
        VisorAddon addon = overlay.getOwner();
        boolean builtIn = overlay.isBuiltIn();

        return configs.computeIfAbsent(id, key -> {
            var catalog = getCatalogOrCreate(addon, builtIn);
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
                throw new VRException(e);
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
                          @NotNull ConfigFile config,
                          boolean builtIn){
        var catalog = getCatalogOrCreate(addon, builtIn);

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

    public List<ConfigFile> getAddonConfigs(@NotNull VisorAddon addon, boolean builtIn){
        var catalog = getCatalogOrCreate(addon, builtIn);

        return configCatalogMap.entrySet().stream()
                .filter((entry)->entry.getValue()==catalog)
                .map(Map.Entry::getKey)
                .toList();
    }




    public ConfigCatalog getCatalogOrCreate(@NotNull VisorAddon addon,
                                            boolean builtIn){
        Map<VisorAddon, ConfigCatalog> catalogs = builtIn
                ? catalogsBuiltIn
                : catalogsCustom;
        String pathPrefix = builtIn
                ? "overlays/built_in/"
                : "overlays/custom/";
        String path = pathPrefix + addon.getAddonId();

        return catalogs.computeIfAbsent(addon, key -> {
            var listener = new OverlayCatalogListener(this, builtIn);
            var catalog = ClientContext.visor
                    .getConfigManager()
                    .createCatalog(
                            ConfigType.YAML,
                            path,
                            Path.of(path),
                            true,
                            listener
                    );
            listener.setAddon(key);
            return catalog;
        });
    }
}
