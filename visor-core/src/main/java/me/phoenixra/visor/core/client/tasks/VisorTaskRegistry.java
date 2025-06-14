package me.phoenixra.visor.core.client.tasks;

import lombok.Getter;
import me.phoenixra.visor.api.client.tasks.RegisterVisorTask;
import me.phoenixra.visor.api.client.tasks.TaskType;
import me.phoenixra.visor.api.client.tasks.VisorTask;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.api.common.addon.VisortRegistry;
import me.phoenixra.visor.api.common.utils.LoggerUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.util.*;

import static com.mojang.text2speech.Narrator.LOGGER;
import static org.reflections.scanners.Scanners.SubTypes;
import static org.reflections.scanners.Scanners.TypesAnnotated;

public class VisorTaskRegistry implements VisortRegistry<VisorTask> {

    private static final String REGISTRY_NAME = "Visor Tasks";

    private static final String ELEMENT_NAME = "VisorTask";
    private static final String ANNOTATION_NAME = "@RegisterVisorTask";


    private final Map<String, VisorTask> elementsMap = new LinkedHashMap<>();

    private final EnumMap<TaskType, List<VisorTask>> elementsByType = new EnumMap<>(TaskType.class);

    @Getter
    private final Collection<VisorTask> allElements =
            Collections.unmodifiableCollection(elementsMap.values());


    /** Exposed unmodifiable views onto the per-type lists. */
    @Getter private final List<VisorTask> preTick;
    @Getter private final List<VisorTask> playerTick;
    @Getter private final List<VisorTask> preRender;

    public VisorTaskRegistry() {

        for (TaskType type : TaskType.values()) {
            elementsByType.put(type, new ArrayList<>());
        }

        // Wrap in unmodifiable views for exposure
        preTick = Collections.unmodifiableList(elementsByType.get(TaskType.VR_PRE_TICK));
        playerTick = Collections.unmodifiableList(elementsByType.get(TaskType.VR_PLAYER_TICK));
        preRender = Collections.unmodifiableList(elementsByType.get(TaskType.VR_PRE_RENDER));
    }


    @Override
    public void registerAddonPath(@NotNull VisorAddon addon) {
        Reflections reflections = new Reflections(
                addon.getAddonPackagePath(),
                SubTypes, TypesAnnotated
        );

        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(RegisterVisorTask.class);

        LOGGER.info("Found {} {} to register in addon: '{}'",
                annotated.size(), ELEMENT_NAME, addon.getAddonId());

        for (Class<?> clazz : annotated) {
            if (!VisorTask.class.isAssignableFrom(clazz)) {
                LOGGER.warn(
                        "{} is annotated with {} but does not implement {}",
                        clazz.getName(), ANNOTATION_NAME, ELEMENT_NAME
                );
                continue;
            }
            try {

                @SuppressWarnings("unchecked")
                Constructor<? extends VisorTask> constructor =
                        ((Class<? extends VisorTask>) clazz)
                                .getConstructor(VisorAddon.class);
                var element = constructor.newInstance(addon);

                registerElement(element);

            } catch (Exception e) {
                LOGGER.error("Failed to register {} from class: {}", ELEMENT_NAME, clazz.getName());
                LoggerUtils.printError(e);
                // continue registering other elements
            }
        }
    }


    @Override
    public void registerElement(@NotNull VisorTask element) {

        VisorTask previous = elementsMap.put(element.getId(), element);

        if (previous != null) {
            LOGGER.info(
                    "Overriding existing {}: '{}' from addon '{}'",
                    ELEMENT_NAME,
                    previous.getId(),
                    previous.getOwner().getAddonId()
            );
            List<VisorTask> oldList = elementsByType.get(previous.getType());
            oldList.remove(previous);
            Collections.sort(oldList);
        }


        List<VisorTask> newList = elementsByType.get(element.getType());
        newList.add(element);
        Collections.sort(newList);

        if(previous == null){
            LOGGER.info("Registered {}: '{}'", ELEMENT_NAME, element.getId());
        }
    }

    @Override
    public @Nullable VisorTask unregisterElement(@NotNull String id) {
        VisorTask removed = elementsMap.remove(id);
        if (removed != null) {
            List<VisorTask> list = elementsByType.get(removed.getType());
            list.remove(removed);
            Collections.sort(list);
            LOGGER.info("Unregistered {}: '{}'", ELEMENT_NAME, removed.getId());
        }
        return removed;
    }

    @Override
    public @Nullable VisorTask getElement(@NotNull String id) {
        return elementsMap.get(id);
    }





    @Override
    public @NotNull String getRegistryName() {
        return REGISTRY_NAME;
    }
}
