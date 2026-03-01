package org.vmstudio.visor.core.client.settings.presets;

import lombok.Getter;
import org.vmstudio.visor.api.ModLoader;
import org.vmstudio.visor.api.client.gui.settings.RegisterVRSettingsPreset;
import org.vmstudio.visor.api.client.gui.settings.VRSettingsPreset;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.api.common.addon.component.ComponentRegistry;
import org.vmstudio.visor.api.common.utils.LoggerUtils;
import org.vmstudio.visor.core.client.ClientContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.util.*;

import static com.mojang.text2speech.Narrator.LOGGER;

public class VRSettingsPresetRegistry implements ComponentRegistry<VRSettingsPreset> {
    private static final String REGISTRY_NAME = "VR Game Effects";

    private static final String COMPONENT_NAME = "VRSettingsPreset";
    private static final String ANNOTATION_NAME = "@RegisterVRSettingsPreset";

    @Getter
    private final HashMap<String, VRSettingsPreset> componentsMap = new HashMap<>();

    @Getter
    private final Collection<VRSettingsPreset> allComponents =
            Collections.unmodifiableCollection(componentsMap.values());

    @Override
    public void registerAddonPath(@NotNull VisorAddon addon) {

        String path = addon.getAddonPackagePath();
        if(path == null){
            return;
        }
        List<Class<?>> annotated = ModLoader.get().getClassesAnnotated(
                RegisterVRSettingsPreset.class,
                addon.getModId(),
                path
        );

        LOGGER.info("Found {} {} to register in addon: '{}'",
                annotated.size(), COMPONENT_NAME, addon.getAddonId());

        for (Class<?> clazz : annotated) {
            if (!VRSettingsPreset.class.isAssignableFrom(clazz)) {
                LOGGER.warn(
                        "{} is annotated with {} but does not implement {}",
                        clazz.getName(), ANNOTATION_NAME, COMPONENT_NAME
                );
                continue;
            }
            try {
                @SuppressWarnings("unchecked")
                Constructor<? extends VRSettingsPreset> constructor =
                        ((Class<? extends VRSettingsPreset>) clazz)
                                .getConstructor(VisorAddon.class);

                var component = constructor.newInstance(addon);

                registerComponent(component);

            } catch (Exception e) {
                LOGGER.error("Failed to register {} from class: {}", COMPONENT_NAME, clazz.getName());
                LoggerUtils.printError(e);
                // continue registering other components
            }
        }

    }

    @Override
    public void registerComponent(@NotNull VRSettingsPreset component) {
        var previous = componentsMap.put(component.getId(), component);

        if (previous != null) {
            LOGGER.info(
                    "Overriding existing {}: '{}' from addon '{}'",
                    COMPONENT_NAME,
                    previous.getId(),
                    previous.getOwner().getAddonId()
            );

        }else{
            LOGGER.info("Registered {}: '{}'", COMPONENT_NAME, component.getId());
        }
    }

    @Override
    public VRSettingsPreset unregisterComponent(@NotNull String id) {
        var removed = componentsMap.remove(id);

        if(removed != null) {
            var catalog = ClientContext.settingsManager.getPresetsCatalog();
            catalog.getConfigFile(
                    id
            ).ifPresent(it->{
                it.getFile().delete();
                catalog.getConfigFilesMap().remove(id);
            });
            LOGGER.info("Unregistered {}: '{}'", COMPONENT_NAME, removed.getId());
        }
        return removed;
    }

    @Override
    public @Nullable VRSettingsPreset getComponent(@NotNull String id) {
        return componentsMap.get(id);
    }



    @Override
    public @NotNull String getRegistryName() {
        return REGISTRY_NAME;
    }
}
