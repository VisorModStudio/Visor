package org.vmstudio.visor.core.client.input;

import lombok.Getter;
import org.vmstudio.visor.api.ModLoader;
import org.vmstudio.visor.api.client.input.action.RegisterActionSet;
import org.vmstudio.visor.api.client.input.action.VRActionSet;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.api.common.addon.component.ComponentIds;
import org.vmstudio.visor.api.common.addon.component.ComponentRegistry;
import org.vmstudio.visor.api.common.utils.LoggerUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vmstudio.visor.core.client.VisorClientImpl;


import java.lang.reflect.Constructor;
import java.util.*;



public class ActionSetRegistry implements ComponentRegistry<VRActionSet> {
    private static final String REGISTRY_NAME = "VR Action Sets";

    private static final String COMPONENT_NAME = "VRActionSet";
    private static final String ANNOTATION_NAME = "@RegisterVRActionSet";

    private final Map<String, VRActionSet> componentsMap = new LinkedHashMap<>();

    private final List<VRActionSet> sortedComponents = new ArrayList<>();

    @Getter
    private final Collection<VRActionSet> allComponents =
            Collections.unmodifiableCollection(componentsMap.values());


    public List<VRActionSet> getSortedComponents() {
        return Collections.unmodifiableList(sortedComponents);
    }


    @Override
    public void registerAddonPath(@NotNull VisorAddon addon) {

        String path = addon.getAddonPackagePath();
        if(path == null){
            return;
        }
        List<Class<?>> annotated = ModLoader.get().getClassesAnnotated(
                RegisterActionSet.class,
                addon.getModId(),
                path
        );

        VisorClientImpl.LOGGER.info("Found {} {} to register in addon: '{}'",
                annotated.size(), COMPONENT_NAME, addon.getAddonId());

        for (Class<?> clazz : annotated) {
            if (!VRActionSet.class.isAssignableFrom(clazz)) {
                VisorClientImpl.LOGGER.warn(
                        "{} is annotated with {} but does not implement {}",
                        clazz.getName(), ANNOTATION_NAME, COMPONENT_NAME
                );
                continue;
            }
            try {
                @SuppressWarnings("unchecked")
                Constructor<? extends VRActionSet> constructor =
                        ((Class<? extends VRActionSet>) clazz)
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
    public void registerComponent(@NotNull VRActionSet component) {
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
            sortedComponents.remove(previous);

        }else{
            VisorClientImpl.LOGGER.info("Registered {}: '{}'", COMPONENT_NAME, component.getId());
        }
        sortedComponents.add(component);
        Collections.sort(sortedComponents);
    }

    @Override
    public @Nullable VRActionSet unregisterComponent(@NotNull String id) {
        var removed = componentsMap.remove(id);;
        if(removed != null) {
            sortedComponents.remove(removed);
            Collections.sort(sortedComponents);
            VisorClientImpl.LOGGER.info("Unregistered {}: '{}'", COMPONENT_NAME, removed.getId());
        }
        return removed;

    }

    @Override
    public @Nullable VRActionSet getComponent(@NotNull String id) {
        return componentsMap.get(id);
    }




    @Override
    public @NotNull String getRegistryName() {
        return REGISTRY_NAME;
    }
}
