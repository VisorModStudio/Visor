package me.phoenixra.visor.core.client.settings.overlays;

import lombok.Getter;
import lombok.Setter;
import me.phoenixra.atumconfig.api.config.ConfigFile;
import me.phoenixra.atumconfig.api.config.catalog.ConfigCatalog;
import me.phoenixra.atumconfig.api.config.catalog.ConfigCatalogListener;
import me.phoenixra.visor.api.client.gui.overlays.VROverlayTemplateRecord;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.api.common.utils.LoggerUtils;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.VisorState;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class OverlayCatalogListener implements ConfigCatalogListener {
    private final OverlayConfigsManager manager;

    @Setter
    private ConfigCatalog catalog;
    @Setter
    private VisorAddon addon;

    @Getter
    private boolean builtIn;

    public OverlayCatalogListener(OverlayConfigsManager manager,
                                  boolean builtIn){
        this.manager = manager;
        this.builtIn = builtIn;
    }


    @Override
    public void onConfigLoaded(@NotNull ConfigFile config) {
        manager.addConfig(addon, config, builtIn);
    }



    @Override
    public void afterReload() {
        if(builtIn){
            loadBuiltInOverlaysOptions();
        }else {
            initializeCustomOverlays();
        }
    }

    @Override
    public void afterLoadDefaults() {
        //@TODO replace this fix with newer AtumConfiguration
        Path baseDir = catalog.getConfigManager()
                .getDirectory().resolve(catalog.getDirectory());
        if (Files.notExists(baseDir)) {
            try {
                Files.createDirectories(baseDir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if(builtIn){
            return;
        }
        //CUSTOM OVERLAYS DEFAULTS
        var templatesRegistry = ClientContext.overlayManager
                .getOverlayTemplatesRegistry();
        try {
            for(var entry : templatesRegistry.getAddonElements(addon)){
                if(!entry.isCreateDefault()) continue;
                //creates file with default settings
                entry.constructor().newInstance(
                        entry.owner(),
                        entry.id()
                );

            }
        }catch (Throwable e){
            VisorState.destroyVRWithErrorScreen(e);
        }

    }

    @Override
    public void onClear() {
        manager.onCatalogCleared(catalog);
    }

    private void loadBuiltInOverlaysOptions(){
        var overlaysRegistry = ClientContext.overlayManager
                .getOverlaysRegistry();
        for(ConfigFile config : manager.getAddonConfigs(addon, builtIn)){
            String id = config.getId();
            var overlay = overlaysRegistry.getElement(id);
            if(overlay == null){
                LoggerUtils.getLogger().error(
                        "The overlay with id {} not found!", id
                );
                continue;
            }
            overlay.reloadOptions();
        }
    }

    private void initializeCustomOverlays(){
        var overlaysRegistry = ClientContext.overlayManager
                .getOverlaysRegistry();
        var templatesRegistry = ClientContext.overlayManager
                .getOverlayTemplatesRegistry();
        for(ConfigFile config : manager.getAddonConfigs(addon, builtIn)){
            String id = config.getId();

            if(overlaysRegistry.getElement(id) != null){
                LoggerUtils.getLogger().error(
                        "The overlay with id {} already exists!", id
                );
                continue;
            }

            String templateId  = config.getString("template");
            VROverlayTemplateRecord templateRecord = templatesRegistry.getElement(templateId);
            if(templateRecord == null){
                LoggerUtils.getLogger().error(
                        "Unknown overlay template {} specified for {}", templateId, id
                );
                continue;
            }
            try {
                var overlay = templateRecord.constructor().newInstance(
                        templateRecord.owner(),
                        id
                );
                overlaysRegistry.registerElement(overlay);
            }catch (Throwable throwable){
                VisorState.destroyVRWithErrorScreen(throwable);
            }
        }
    }
}
