package me.phoenixra.visor.core.client.settings;

import me.phoenixra.atumconfig.api.ConfigManager;
import me.phoenixra.atumconfig.api.config.Config;
import me.phoenixra.atumconfig.api.config.ConfigFile;
import me.phoenixra.atumconfig.api.config.ConfigType;
import me.phoenixra.atumconfig.api.config.catalog.ConfigCatalog;
import me.phoenixra.atumconfig.api.config.catalog.ConfigCatalogListener;
import me.phoenixra.atumconfig.core.config.AtumConfigFile;
import me.phoenixra.visor.api.client.gui.overlay.OverlayCatalog;
import me.phoenixra.visor.api.client.gui.overlay.VROverlay;
import me.phoenixra.visor.api.client.gui.overlay.VROverlayType;
import me.phoenixra.visor.api.common.ControllerHand;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.VisorState;
import me.phoenixra.visor.core.client.gui.overlays.builtin.VROverlayDraggedItem;
import me.phoenixra.visor.core.client.gui.overlays.builtin.VROverlayGameScreen;
import me.phoenixra.visor.core.client.gui.overlays.builtin.hotbar.VROverlayHotBar;
import me.phoenixra.visor.core.client.gui.overlays.builtin.keyboard.VROverlayKeyboard;
import me.phoenixra.visor.core.client.gui.overlays.builtin.settings.VROverlayDemo;
import me.phoenixra.visor.core.client.gui.overlays.builtin.settings.VROverlayOptionsMenu;
import me.phoenixra.visor.core.client.gui.overlays.builtin.settings.VROverlaySettings;
import me.phoenixra.visor.api.common.utils.LoggerUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class OverlaysCatalogListener implements OverlayCatalog, ConfigCatalogListener {
    private final HashMap<String, ConfigFile> configs;

    protected ConfigCatalog catalog;
    public OverlaysCatalogListener() {
        configs = new HashMap<>();
    }

    @Override
    public void onClear() {
        configs.clear();
    }

    @Override
    public void onConfigLoaded(@NotNull ConfigFile config) {
        configs.put(config.getId(), config);
    }

    //@TODO change approach to registering overlays
    @Override
    public void reload() {
        catalog.reload();
    }

    @Override
    public void afterReload() {
        ClientContext.overlayManager.getOverlaysRegistry()
                .registerElements(
                        List.of(
                                new VROverlayGameScreen(
                                        ClientContext.coreAddon,
                                        VROverlayGameScreen.ID
                                ),
                                new VROverlayHotBar(
                                        ClientContext.coreAddon,
                                        ControllerHand.MAIN,
                                        VROverlayHotBar.ID_MAIN
                                ),
                                new VROverlayHotBar(
                                        ClientContext.coreAddon,
                                        ControllerHand.OFFHAND,
                                        VROverlayHotBar.ID_OFFHAND
                                ),
                                new VROverlayKeyboard(
                                        ClientContext.coreAddon,
                                        VROverlayKeyboard.ID
                                ),
                                new VROverlayDraggedItem(
                                        ClientContext.coreAddon,
                                        VROverlayDraggedItem.ID
                                ),
                                new VROverlaySettings(
                                        ClientContext.coreAddon,
                                        VROverlaySettings.ID
                                ),
                                new VROverlayOptionsMenu(
                                        ClientContext.coreAddon,
                                        VROverlayOptionsMenu.ID
                                ),
                                new VROverlayDemo(
                                        ClientContext.coreAddon,
                                        VROverlayDemo.ID
                                )
                        )
                );
        loadOverlays();
    }

    private void loadOverlays(){
        var overlaysRegistry = ClientContext.overlayManager
                .getOverlaysRegistry();
        var typesRegistry = ClientContext.overlayManager
                .getOverlayTypesRegistry();
        for(Map.Entry<String,ConfigFile> entry : configs.entrySet()){
            String id = entry.getKey();
            Config config = entry.getValue();
            if(config.getBool("only_settings")
                    || config.getStringOrNull("type") == null){
                continue;
            }

            if(overlaysRegistry.getElement(id) != null){
                LoggerUtils.getLogger().error(
                        "The overlay with id "+ id + " already exists!"
                );
                continue;
            }
            String type  = config.getString("type");
            VROverlayType overlayType = typesRegistry.getElement(type);
            if(overlayType == null){
                LoggerUtils.getLogger().error(
                        "Unknown overlay type "+ type+" specified for "+ id
                );
                continue;
            }
            try {
                VROverlay overlay = overlayType.constructor().newInstance(
                        overlayType.owner(),
                        id
                );

                overlaysRegistry.registerElement(overlay);
            }catch (Throwable throwable){
                VisorState.destroyVRWithError(throwable);
            }
        }
    }

    @Override
    public void beforeLoadDefaults() {
        ConfigManager configManager = catalog.getConfigManager();
        try {
            //that will create$load config files
            // without registering them in manager
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
                    "player_inventory",
                    catalog.getDirectory().resolve("player_inventory.yml"),
                    true
            );
        }catch (Throwable e){
            VisorState.destroyVRWithError(e);
        }

    }


    @Override
    public @Nullable ConfigFile getConfig(@NotNull String id) {
        return configs.get(id);
    }
    public void removeConfig(@NotNull String id){
        configs.remove(id);
        catalog.getConfigFilesMap().remove(id);
    }

    @Override
    public @NotNull ConfigFile getConfigOrCreate(@NotNull String id) throws IOException {
        ConfigFile config = configs.get(id);
        if(config == null){
            config = new AtumConfigFile(
                    catalog.getConfigManager(),
                    ConfigType.YAML,
                    id,
                    catalog.getDirectory().resolve("auto-generated/"+id+".yml"),
                    false
            );
            configs.put(id, config);
        }
        return config;
    }
}
