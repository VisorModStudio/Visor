package org.vmstudio.visor.core.client.render.decoration.registry;

import lombok.Getter;
import org.vmstudio.visor.api.ModLoader;
import org.vmstudio.visor.api.client.render.decoration.annotations.RegisterVRHandEffect;
import org.vmstudio.visor.api.client.render.decoration.effects.VRHandEffect;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.api.common.addon.component.ComponentIds;
import org.vmstudio.visor.api.common.addon.component.ComponentRegistry;
import org.vmstudio.visor.api.common.utils.LoggerUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vmstudio.visor.core.client.VisorClientImpl;

import java.lang.reflect.Constructor;
import java.util.*;


public class VRHandEffectRegistry implements ComponentRegistry<VRHandEffect> {
    private static final String REGISTRY_NAME = "VR Hand Effects";

    private static final String COMPONENT_NAME = "VRHandEffect";
    private static final String ANNOTATION_NAME = "@RegisterVRHandEffect";

    @Getter
    private final HashMap<String, VRHandEffect> componentsMap = new HashMap<>();
    @Getter
    private final HashMap<String, VRHandEffect> globalComponentsMap = new HashMap<>();

    @Getter
    private final Collection<VRHandEffect> allComponents =
            Collections.unmodifiableCollection(componentsMap.values());
    @Getter
    private final Collection<VRHandEffect> globalComponents =
            Collections.unmodifiableCollection(globalComponentsMap.values());

    @Override
    public void registerAddonPath(@NotNull VisorAddon addon) {

        String path = addon.getAddonPackagePath();
        if(path == null){
            return;
        }
        List<Class<?>> annotated = ModLoader.get().getClassesAnnotated(
                RegisterVRHandEffect.class,
                addon.getModId(),
                path
        );


        VisorClientImpl.LOGGER.info("Found {} {} to register in addon: '{}'",
                annotated.size(), COMPONENT_NAME, addon.getAddonId());

        for (Class<?> clazz : annotated) {
            if (!VRHandEffect.class.isAssignableFrom(clazz)) {
                VisorClientImpl.LOGGER.warn(
                        "{} is annotated with {} but does not implement {}",
                        clazz.getName(), ANNOTATION_NAME, COMPONENT_NAME
                );
                continue;
            }
            try {
                @SuppressWarnings("unchecked")
                Constructor<? extends VRHandEffect> constructor =
                        ((Class<? extends VRHandEffect>) clazz)
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
    public void registerComponent(@NotNull VRHandEffect component) {
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

        } else {
            VisorClientImpl.LOGGER.info("Registered {}: '{}'", COMPONENT_NAME, component.getId());
        }
        if(component.isGlobal()){
            globalComponentsMap.put(component.getId(), component);
        }
    }

    @Override
    public VRHandEffect unregisterComponent(@NotNull String id) {
        var removed = componentsMap.remove(id);
        globalComponentsMap.remove(id);

        if (removed != null) {
            VisorClientImpl.LOGGER.info("Unregistered {}: '{}'", COMPONENT_NAME, removed.getId());
        }

        return removed;
    }

    @Override
    public @Nullable VRHandEffect getComponent(@NotNull String id) {
        return componentsMap.get(id);
    }


    @Override
    public @NotNull String getRegistryName() {
        return REGISTRY_NAME;
    }
}
