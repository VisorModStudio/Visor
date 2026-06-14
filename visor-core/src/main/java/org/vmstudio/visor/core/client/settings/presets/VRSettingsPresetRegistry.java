package org.vmstudio.visor.core.client.settings.presets;

import lombok.Getter;
import org.vmstudio.visor.api.ModLoader;
import org.vmstudio.visor.api.client.gui.settings.RegisterVRSettingsPreset;
import org.vmstudio.visor.api.client.gui.settings.VRSettingsPreset;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.api.common.addon.component.ComponentIds;
import org.vmstudio.visor.api.common.addon.component.ComponentRegistry;
import org.vmstudio.visor.api.common.utils.LoggerUtils;
import org.vmstudio.visor.core.client.ClientContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vmstudio.visor.core.client.VisorClientImpl;

import java.lang.reflect.Constructor;
import java.util.*;



public class VRSettingsPresetRegistry implements ComponentRegistry<VRSettingsPreset> {
    private static final String REGISTRY_NAME = "VR Settings Presets";

    private static final String COMPONENT_NAME = "VRSettingsPreset";
    private static final String ANNOTATION_NAME = "@RegisterVRSettingsPreset";

    @Getter
    private final Map<String, VRSettingsPreset> componentsMap = new TreeMap<>();

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


        VisorClientImpl.LOGGER.info("Found {} {} to register in addon: '{}'",
                annotated.size(), COMPONENT_NAME, addon.getAddonId());

        for (Class<?> clazz : annotated) {
            if (!VRSettingsPreset.class.isAssignableFrom(clazz)) {
                VisorClientImpl.LOGGER.warn(
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
                VisorClientImpl.LOGGER.error("Failed to register {} from class: {}", COMPONENT_NAME, clazz.getName());
                LoggerUtils.printError(e);
                // continue registering other components
            }
        }

    }

    @Override
    public void registerComponent(@NotNull VRSettingsPreset component) {
        String validationError = ComponentIds.validate(component.getId());
        if(validationError != null){
            throw new RuntimeException(
                    "Tried to register "+COMPONENT_NAME+" with ID '"
                            + component.getId()
                            + "'. From addon: '"+component.getOwner().getAddonId()
                            + "'. The ID pattern is incorrect: " + validationError);
        }

        var previous = componentsMap.put(component.getId(), component);

        if (previous != null) {
            VisorClientImpl.LOGGER.info(
                    "Overriding existing {}: '{}' from addon '{}'",
                    COMPONENT_NAME,
                    previous.getId(),
                    previous.getOwner().getAddonId()
            );

        }else{
            VisorClientImpl.LOGGER.info("Registered {}: '{}'", COMPONENT_NAME, component.getId());
        }
    }

    @Override
    public VRSettingsPreset unregisterComponent(@NotNull String id) {
        var removed = componentsMap.remove(id);
        if (removed != null) {
            VisorClientImpl.LOGGER.info("Unregistered {}: '{}'", COMPONENT_NAME, removed.getId());
        }
        return removed;
    }

    public void deleteCustomPreset(@NotNull String id) {
        var removed = unregisterComponent(id);
        if (removed == null) return;
        var catalog = ClientContext.settingsManager.getPresetsCatalog();
        catalog.getConfigFile(id).ifPresent(it -> {
            if (!it.getFile().delete()) {
                VisorClientImpl.LOGGER.warn("Failed to delete preset file for '{}'", id);
            }
            catalog.getConfigFilesMap().remove(id);
        });
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
