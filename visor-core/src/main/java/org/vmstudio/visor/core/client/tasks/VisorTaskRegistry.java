package org.vmstudio.visor.core.client.tasks;

import lombok.Getter;
import org.vmstudio.visor.api.ModLoader;
import org.vmstudio.visor.api.client.tasks.RegisterVisorTask;
import org.vmstudio.visor.api.client.tasks.TaskType;
import org.vmstudio.visor.api.client.tasks.VisorTask;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.api.common.addon.component.ComponentIds;
import org.vmstudio.visor.api.common.addon.component.ComponentRegistry;
import org.vmstudio.visor.api.common.utils.LoggerUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vmstudio.visor.core.client.VisorClientImpl;


import java.lang.reflect.Constructor;
import java.util.*;


public class VisorTaskRegistry implements ComponentRegistry<VisorTask> {

    private static final String REGISTRY_NAME = "Visor Tasks";

    private static final String COMPONENT_NAME = "VisorTask";
    private static final String ANNOTATION_NAME = "@RegisterVisorTask";


    private final Map<String, VisorTask> componentsMap = new LinkedHashMap<>();

    private final EnumMap<TaskType, List<VisorTask>> componentsByType = new EnumMap<>(TaskType.class);

    @Getter
    private final Collection<VisorTask> allComponents =
            Collections.unmodifiableCollection(componentsMap.values());


    /** Exposed unmodifiable views onto the per-type lists. */
    @Getter private final List<VisorTask> preTick;
    @Getter private final List<VisorTask> playerTick;
    @Getter private final List<VisorTask> preRender;

    public VisorTaskRegistry() {

        for (TaskType type : TaskType.values()) {
            componentsByType.put(type, new ArrayList<>());
        }

        // Wrap in unmodifiable views for exposure
        preTick = Collections.unmodifiableList(componentsByType.get(TaskType.VR_PRE_TICK));
        playerTick = Collections.unmodifiableList(componentsByType.get(TaskType.VR_PLAYER_TICK));
        preRender = Collections.unmodifiableList(componentsByType.get(TaskType.VR_PRE_RENDER));
    }


    @Override
    public void registerAddonPath(@NotNull VisorAddon addon) {

        String path = addon.getAddonPackagePath();
        if(path == null){
            return;
        }
        List<Class<?>> annotated = ModLoader.get().getClassesAnnotated(
                RegisterVisorTask.class,
                addon.getModId(),
                path
        );

        VisorClientImpl.LOGGER.info("Found {} {} to register in addon: '{}'",
                annotated.size(), COMPONENT_NAME, addon.getAddonId());

        for (Class<?> clazz : annotated) {
            if (!VisorTask.class.isAssignableFrom(clazz)) {
                VisorClientImpl.LOGGER.warn(
                        "{} is annotated with {} but does not implement {}",
                        clazz.getName(), ANNOTATION_NAME, COMPONENT_NAME
                );
                continue;
            }
            try {

                @SuppressWarnings("unchecked")
                Constructor<? extends VisorTask> constructor =
                        ((Class<? extends VisorTask>) clazz)
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
    public void registerComponent(@NotNull VisorTask component) {
        String validationError = ComponentIds.validate(component.getId());
        if(validationError != null){
            throw new RuntimeException(
                    "Tried to register "+COMPONENT_NAME+" with ID '"
                            + component.getId()
                            + "'. From addon: '"+component.getOwner().getAddonId()
                            + "'. The ID pattern is incorrect: " + validationError);
        }

        VisorTask previous = componentsMap.put(component.getId(), component);

        if (previous != null) {
            VisorClientImpl.LOGGER.info(
                    "Overriding existing {}: '{}' from addon '{}'",
                    COMPONENT_NAME,
                    previous.getId(),
                    previous.getOwner().getAddonId()
            );
            List<VisorTask> oldList = componentsByType.get(previous.getType());
            oldList.remove(previous);
            Collections.sort(oldList);
        }


        List<VisorTask> newList = componentsByType.get(component.getType());
        newList.add(component);
        Collections.sort(newList);

        if(previous == null){
            VisorClientImpl.LOGGER.info("Registered {}: '{}'", COMPONENT_NAME, component.getId());
        }
    }

    @Override
    public @Nullable VisorTask unregisterComponent(@NotNull String id) {
        VisorTask removed = componentsMap.remove(id);
        if (removed != null) {
            List<VisorTask> list = componentsByType.get(removed.getType());
            list.remove(removed);
            Collections.sort(list);
            VisorClientImpl.LOGGER.info("Unregistered {}: '{}'", COMPONENT_NAME, removed.getId());
        }
        return removed;
    }

    @Override
    public @Nullable VisorTask getComponent(@NotNull String id) {
        return componentsMap.get(id);
    }





    @Override
    public @NotNull String getRegistryName() {
        return REGISTRY_NAME;
    }
}
