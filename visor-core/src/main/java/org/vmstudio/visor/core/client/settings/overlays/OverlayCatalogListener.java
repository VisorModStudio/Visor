package org.vmstudio.visor.core.client.settings.overlays;

import lombok.Getter;
import lombok.Setter;
import me.phoenixra.atumconfig.api.config.ConfigFile;
import me.phoenixra.atumconfig.api.config.catalog.ConfigCatalog;
import me.phoenixra.atumconfig.api.config.catalog.ConfigCatalogListener;
import org.vmstudio.visor.api.client.gui.overlays.VROverlayTemplateRecord;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.api.common.utils.LoggerUtils;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.VisorState;
import org.jetbrains.annotations.NotNull;

public class OverlayCatalogListener implements ConfigCatalogListener {
    private final OverlayConfigsManager manager;

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
    public void onClear(@NotNull ConfigCatalog catalog) {
        manager.onCatalogCleared(catalog);
    }

    @Override
    public void onConfigLoaded(@NotNull ConfigCatalog catalog, @NotNull ConfigFile config) {
        manager.addConfig(addon, config, builtIn);
    }


    @Override
    public void afterReload(@NotNull ConfigCatalog catalog) {
        if(builtIn){
            loadBuiltInOverlaysOptions();
        }else {
            initializeCustomOverlays();
        }
    }

    @Override
    public void afterLoadDefaults(@NotNull ConfigCatalog catalog) {
        if(builtIn){
            return;
        }
        //CUSTOM OVERLAYS DEFAULTS
        var templatesRegistry = ClientContext.overlayManager
                .getOverlayTemplatesRegistry();
        try {
            for(var entry : templatesRegistry.getAddonComponents(addon)){
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


    private void loadBuiltInOverlaysOptions(){
        var overlaysRegistry = ClientContext.overlayManager
                .getOverlaysRegistry();
        for(ConfigFile config : manager.getAddonConfigs(addon, builtIn)){
            String id = config.getId();
            var overlay = overlaysRegistry.getComponent(id);
            if(overlay == null){
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

            if(overlaysRegistry.getComponent(id) != null){
                LoggerUtils.getLogger().error(
                        "The overlay with id {} already exists!", id
                );
                continue;
            }

            String templateId  = config.getString("template");
            VROverlayTemplateRecord templateRecord = templatesRegistry.getComponent(templateId);
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
                overlaysRegistry.registerComponent(overlay);
            }catch (Throwable throwable){
                VisorState.destroyVRWithErrorScreen(throwable);
            }
        }
    }

}
