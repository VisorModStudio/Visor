package org.vmstudio.visor.core.client.render.decoration.registry;

import lombok.Getter;
import org.vmstudio.visor.api.ModLoader;
import org.vmstudio.visor.api.client.render.decoration.annotations.RegisterVRGameEffect;
import org.vmstudio.visor.api.client.render.decoration.effects.VRGameEffect;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.api.common.addon.component.ComponentIds;
import org.vmstudio.visor.api.common.addon.component.ComponentRegistry;
import org.vmstudio.visor.api.common.utils.LoggerUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vmstudio.visor.core.client.VisorClientImpl;


import java.lang.reflect.Constructor;
import java.util.*;

public class VRGameEffectRegistry implements ComponentRegistry<VRGameEffect> {
    private static final String REGISTRY_NAME = "VR Game Effects";

    private static final String COMPONENT_NAME = "VRGameEffect";
    private static final String ANNOTATION_NAME = "@RegisterVRGameEffect";

    @Getter
    private final HashMap<String, VRGameEffect> componentsMap = new HashMap<>();

    @Getter
    private final HashMap<String, VRGameEffect> globalComponentsMap = new HashMap<>();


    @Getter
    private final Collection<VRGameEffect> allComponents =
            Collections.unmodifiableCollection(componentsMap.values());
    @Getter
    private final Collection<VRGameEffect> globalComponents =
            Collections.unmodifiableCollection(globalComponentsMap.values());

    @Override
    public void registerAddonPath(@NotNull VisorAddon addon) {

        String path = addon.getAddonPackagePath();
        if(path == null){
            return;
        }
        List<Class<?>> annotated = ModLoader.get().getClassesAnnotated(
                RegisterVRGameEffect.class,
                addon.getModId(),
                path
        );

        VisorClientImpl.LOGGER.info("Found {} {} to register in addon: '{}'",
                annotated.size(), COMPONENT_NAME, addon.getAddonId());

        for (Class<?> clazz : annotated) {
            if (!VRGameEffect.class.isAssignableFrom(clazz)) {
                VisorClientImpl.LOGGER.warn(
                        "{} is annotated with {} but does not implement {}",
                        clazz.getName(), ANNOTATION_NAME, COMPONENT_NAME
                );
                continue;
            }
            try {
                @SuppressWarnings("unchecked")
                Constructor<? extends VRGameEffect> constructor =
                        ((Class<? extends VRGameEffect>) clazz)
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
    public void registerComponent(@NotNull VRGameEffect component) {
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
        if(component.isGlobal()){
            globalComponentsMap.put(component.getId(), component);
        }
    }

    @Override
    public VRGameEffect unregisterComponent(@NotNull String id) {
        var removed = componentsMap.remove(id);
        globalComponentsMap.remove(id);
        if(removed != null) {
            VisorClientImpl.LOGGER.info("Unregistered {}: '{}'", COMPONENT_NAME, removed.getId());
        }
        return removed;
    }

    @Override
    public @Nullable VRGameEffect getComponent(@NotNull String id) {
        return componentsMap.get(id);
    }



    @Override
    public @NotNull String getRegistryName() {
        return REGISTRY_NAME;
    }
}
