package org.vmstudio.visor.core.common.addon;

import lombok.Getter;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.api.common.addon.AddonManager;
import org.vmstudio.visor.api.common.addon.component.ComponentIds;
import org.vmstudio.visor.api.common.addon.component.ComponentRegistry;
import org.vmstudio.visor.core.common.eventbus.VREventBusImpl;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.*;


public class AddonManagerImpl implements AddonManager {

    private final Logger logger;


    private final Map<String, VisorAddon> addonsMap;

    @Getter
    private VisorRegistriesImpl registries;

    public AddonManagerImpl(Logger logger) {
        VisorAPI.Instance.setAddonManager(this);
        VisorAPI.Instance.setEventBus(new VREventBusImpl());

        this.logger = logger;
        this.addonsMap = new LinkedHashMap<>();


    }

    public void initialize(VisorAddon coreAddon,
                           List<ComponentRegistry<?>> componentRegistries){

        this.registries = new VisorRegistriesImpl(componentRegistries);

        addonsMap.put(coreAddon.getAddonId(), coreAddon);
        loadAddon(coreAddon);


        for(var addon : VisorAPI.Instance.getPreparedAddons().values()){
            String validationError = ComponentIds.validate(addon.getAddonId());
            if(validationError != null){
                throw new RuntimeException(
                        "Tried to register addon with ID '"
                                + addon.getAddonId()
                                + "'. The ID pattern is incorrect: " + validationError);
            }
            addonsMap.put(addon.getAddonId(), addon);
            loadAddon(addon);
        }

    }


    private void loadAddon(VisorAddon addon) {
        logger.info("----- LOADING Visor Addon with ID: {}", addon.getAddonId());

        if(addon.getAddonPackagePath() != null) {
            for(var registry : registries.list){
                registry.registerAddonPath(addon);
            }
        }
        addon.onAddonLoad();
        logger.info("----- SUCCESS LOADING");

    }


    @Override
    public @NotNull VisorAddon getAddon(@NotNull String id) {
        return addonsMap.get(id);
    }

    @Override
    public @NotNull Collection<VisorAddon> getAddons() {
        return addonsMap.values();
    }
}
