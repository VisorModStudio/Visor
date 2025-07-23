package me.phoenixra.visor.core.client.settings.overlays;

import lombok.Setter;
import me.phoenixra.atumconfig.api.config.ConfigFile;
import me.phoenixra.atumconfig.api.config.catalog.ConfigCatalog;
import me.phoenixra.atumconfig.api.config.catalog.ConfigCatalogListener;
import me.phoenixra.visor.api.client.gui.overlay.template.VROverlayTemplateRecord;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.api.common.utils.LoggerUtils;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.VisorState;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class OverlayCatalogListener implements ConfigCatalogListener {
    private final OverlayCatalogsManager manager;

    @Setter
    private ConfigCatalog catalog;
    @Setter
    private VisorAddon addon;
    public OverlayCatalogListener(OverlayCatalogsManager manager){
        this.manager = manager;
    }


    @Override
    public void onConfigLoaded(@NotNull ConfigFile config) {
        manager.addConfig(addon, config);
    }



    @Override
    public void afterReload() {
        initializeOverlays();
    }

    @Override
    public void afterLoadDefaults() {
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

        //@TODO replace with newer AtumConfiguration
        Path baseDir = catalog.getConfigManager()
                .getDirectory().resolve(catalog.getDirectory());
        if (Files.notExists(baseDir)) {
            try {
                Files.createDirectory(baseDir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void onClear() {
        manager.onCatalogCleared(catalog);
    }


    private void initializeOverlays(){
        var overlaysRegistry = ClientContext.overlayManager
                .getOverlaysRegistry();
        var templatesRegistry = ClientContext.overlayManager
                .getOverlayTemplatesRegistry();
        for(ConfigFile config : manager.getAddonConfigs(addon)){
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
