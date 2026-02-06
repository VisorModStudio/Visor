package me.phoenixra.visor.core.client.render.decoration.registry;

import lombok.Getter;
import me.phoenixra.visor.api.ModLoader;
import me.phoenixra.visor.api.client.render.decoration.VRDecorator;
import me.phoenixra.visor.api.client.render.decoration.annotations.RegisterVRDecorator;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.api.common.addon.component.ComponentRegistry;
import me.phoenixra.visor.api.common.utils.LoggerUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


import java.lang.reflect.Constructor;
import java.util.*;

import static com.mojang.text2speech.Narrator.LOGGER;

public class DecoratorRegistry implements ComponentRegistry<VRDecorator> {
    private static final String REGISTRY_NAME = "VR Decorators";

    private static final String COMPONENT_NAME = "VRDecorator";
    private static final String ANNOTATION_NAME = "@RegisterVRDecorator";


    private final Map<String, VRDecorator> componentsMap = new LinkedHashMap<>();

    private final List<VRDecorator> sortedComponents = new ArrayList<>();

    @Getter
    private final Collection<VRDecorator> allComponents =
            Collections.unmodifiableCollection(componentsMap.values());


    public List<VRDecorator> getSortedComponents() {
        return Collections.unmodifiableList(sortedComponents);
    }


    @Override
    public void registerAddonPath(@NotNull VisorAddon addon) {

        String path = addon.getAddonPackagePath();
        if(path == null){
            return;
        }
        List<Class<?>> annotated = ModLoader.get().getClassesAnnotated(
                RegisterVRDecorator.class,
                addon.getModId(),
                path
        );

        LOGGER.info("Found {} {} to register in addon: '{}'",
                annotated.size(), COMPONENT_NAME, addon.getAddonId());

        for (Class<?> clazz : annotated) {
            if (!VRDecorator.class.isAssignableFrom(clazz)) {
                LOGGER.warn(
                        "{} is annotated with {} but does not implement {}",
                        clazz.getName(), ANNOTATION_NAME, COMPONENT_NAME
                );
                continue;
            }
            try {
                @SuppressWarnings("unchecked")
                Constructor<? extends VRDecorator> constructor =
                        ((Class<? extends VRDecorator>) clazz)
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
    public void registerComponent(@NotNull VRDecorator component) {
        var previous = componentsMap.put(component.getId(), component);

        if (previous != null) {
            LOGGER.info(
                    "Overriding existing {}: '{}' from addon '{}'",
                    COMPONENT_NAME,
                    previous.getId(),
                    previous.getOwner().getAddonId()
            );
            sortedComponents.remove(previous);

        }else{
            LOGGER.info("Registered {}: '{}'", COMPONENT_NAME, component.getId());
        }
        sortedComponents.add(component);
        Collections.sort(sortedComponents);
    }

    @Override
    public @Nullable VRDecorator unregisterComponent(@NotNull String id) {
        var removed = componentsMap.remove(id);
        if(removed != null) {
            sortedComponents.remove(removed);
            Collections.sort(sortedComponents);
            LOGGER.info("Unregistered {}: '{}'", COMPONENT_NAME, removed.getId());
        }
        return removed;

    }

    @Override
    public @Nullable VRDecorator getComponent(@NotNull String id) {
        return componentsMap.get(id);
    }




    @Override
    public @NotNull String getRegistryName() {
        return REGISTRY_NAME;
    }
}
