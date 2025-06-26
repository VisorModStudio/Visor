package me.phoenixra.visor.core.common.addon;

import lombok.Getter;
import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.api.common.addon.AddonManager;
import me.phoenixra.visor.api.common.addon.element.VisorRegistry;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.common.eventbus.VREventBusImpl;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.*;


public class AddonManagerImpl implements AddonManager {

    private final Logger logger;


    private final Map<String, VisorAddon> addonsMap;

    @Getter
    private VisorRegistriesImpl registries;

    private boolean initialized;
    public AddonManagerImpl(Logger logger) {
        VisorAPI.Instance.setAddonManager(this);
        VisorAPI.Instance.setEventBus(new VREventBusImpl());

        this.logger = logger;
        this.addonsMap = new HashMap<>();


    }

    public void initialize(VisorAddon coreAddon,
                           List<VisorRegistry<?>> elementRegistries){

        this.registries = new VisorRegistriesImpl(elementRegistries);

        addonsMap.put(coreAddon.getAddonId(), coreAddon);
        loadAddon(coreAddon);

        initialized = true;

        for(var addon : addonsMap.values()){
            if(coreAddon == addon)  continue;
            loadAddon(addon);
        }
        ClientContext.overlayManager
                .getConfigOverlaysAccessor()
                .reload();

    }

    @Override
    public void registerAddon(@NotNull VisorAddon addon) {
        if(initialized){
            throw new RuntimeException(
                    "Not allowed to register addon after Visor started"
            );
        }
        if (addonsMap.containsKey(addon.getAddonId())) {
            throw new RuntimeException(
                    "Visor Addon with ID " + addon.getAddonId()
                            + " is already loaded!");
        }
        if (addon.getAddonId().equals("core")) {
            throw new RuntimeException(
                    "Not allowed to register Visor Addon with ID 'core'"
            );
        }

        logger.info("-----REGISTERED Visor Addon with ID: {}", addon.getAddonId());
        addonsMap.put(addon.getAddonId(), addon);
    }


    private void loadAddon(VisorAddon addon) {
        if(addon.getAddonPackagePath() != null) {
            for(var registry : registries.list){
                registry.registerAddonPath(addon);
            }
        }
        addon.onAddonLoad();
        logger.info("-----SUCCESS LOADING Visor Addon with ID: {}", addon.getAddonId());

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
