package me.phoenixra.visor.core.common.addon;

import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.api.common.addon.AddonManager;
import me.phoenixra.visor.api.common.addon.VisorElementRegistry;
import me.phoenixra.visor.core.client.exceptions.VRInitException;
import me.phoenixra.visor.core.common.utils.LoggerUtils;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.*;


public class AddonManagerImpl implements AddonManager {
    private final Logger logger;

    private List<VisorElementRegistry<?>> elementRegistries;

    private final Map<String, VisorAddon> addonsMap;


    private boolean initialized;
    public AddonManagerImpl(Logger logger) {
        VisorAPI.Instance.setAddonManager(this);
        this.logger = logger;
        this.addonsMap = new HashMap<>();


    }

    public void initialize(VisorAddon coreAddon,
                           List<VisorElementRegistry<?>> elementRegistries){
        this.elementRegistries = elementRegistries;

        addonsMap.put(coreAddon.getAddonId(), coreAddon);
        loadAddon(coreAddon);

        initialized = true;

        for(var addon : addonsMap.values()){
            if(coreAddon == addon)  continue;
            loadAddon(addon);
        }

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
        try {
            if(addon.getAddonPackagePath() != null) {
                for(var registry : elementRegistries){
                    registry.registerAddonPath(addon);
                }
            }
            addon.onAddonLoad();
            logger.info("-----SUCCESS LOADING Visor Addon with ID: {}", addon.getAddonId());
        } catch (Throwable throwable) {
            addonsMap.remove(addon.getAddonId());
            LoggerUtils.printError(logger, throwable);
            logger.info("-----FAILED LOADING Visor Addon with ID: {}", addon.getAddonId());
            if(addon.getAddonId().equals("core")){
                throw new VRInitException(
                        Component.literal("Core addon init failed"),
                        Component.literal(""),
                        throwable
                );
            }
        }

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
