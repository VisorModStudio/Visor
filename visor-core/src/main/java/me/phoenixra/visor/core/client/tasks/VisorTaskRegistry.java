package me.phoenixra.visor.core.client.tasks;

import lombok.Getter;
import me.phoenixra.visor.api.client.tasks.RegisterVisorTask;
import me.phoenixra.visor.api.client.tasks.TaskType;
import me.phoenixra.visor.api.client.tasks.VisorTask;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.api.common.addon.VisorElementRegistry;
import me.phoenixra.visor.core.client.VisorClient;
import me.phoenixra.visor.core.common.utils.LoggerUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.util.*;

import static com.mojang.text2speech.Narrator.LOGGER;
import static org.reflections.scanners.Scanners.SubTypes;
import static org.reflections.scanners.Scanners.TypesAnnotated;

public class VisorTaskRegistry implements VisorElementRegistry<VisorTask> {

    private final Map<String, VisorTask> tasksMap = new LinkedHashMap<>();

    private final EnumMap<TaskType, List<VisorTask>> tasksByType = new EnumMap<>(TaskType.class);

    /** Exposed unmodifiable views onto the per-type lists. */
    @Getter private final List<VisorTask> preTick;
    @Getter private final List<VisorTask> playerTick;
    @Getter private final List<VisorTask> preRender;

    public VisorTaskRegistry() {

        for (TaskType type : TaskType.values()) {
            tasksByType.put(type, new ArrayList<>());
        }

        // Wrap in unmodifiable views for exposure
        preTick = Collections.unmodifiableList(tasksByType.get(TaskType.VR_PRE_TICK));
        playerTick = Collections.unmodifiableList(tasksByType.get(TaskType.VR_PLAYER_TICK));
        preRender = Collections.unmodifiableList(tasksByType.get(TaskType.VR_PRE_RENDER));
    }


    @Override
    public void registerAddonPath(@NotNull VisorAddon addon) {
        Reflections reflections = new Reflections(
                addon.getAddonPackagePath(),
                SubTypes, TypesAnnotated
        );

        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(RegisterVisorTask.class);
        LOGGER.info("Found {} VisorTask to register in addon {}",
                annotated.size(), addon.getAddonId());

        for (Class<?> clazz : annotated) {
            if (!VisorTask.class.isAssignableFrom(clazz)) {
                LOGGER.warn(
                        "{} is annotated with @RegisterVisorTask but does not implement VisorTask",
                        clazz.getName()
                );
                continue;
            }
            try {

                @SuppressWarnings("unchecked")
                Class<? extends VisorTask> taskClass = (Class<? extends VisorTask>) clazz;
                Constructor<? extends VisorTask> ctor =
                        taskClass.getConstructor(VisorAddon.class);
                VisorTask task = ctor.newInstance(addon);

                registerAddonComponent(task);

            } catch (Throwable e) {
                LOGGER.error("Failed to register VisorTask from class: {}", clazz.getName());
                LoggerUtils.printError(e);
                // skip this one, but don’t kill the entire scan
            }
        }
    }


    @Override
    public void registerAddonComponent(@NotNull VisorTask task) {
        // replace in map
        VisorTask previous = tasksMap.put(task.getId(), task);

        // if replacing, remove old from its list
        if (previous != null) {
            LOGGER.info(
                    "Overriding existing VisorTask: '{}' from addon '{}'",
                    previous.getId(),
                    previous.getOwner().getAddonId()
            );
            List<VisorTask> oldList = tasksByType.get(previous.getType());
            oldList.remove(previous);
            Collections.sort(oldList);
        }

        // add new into its list and keep sorted
        List<VisorTask> newList = tasksByType.get(task.getType());
        newList.add(task);
        Collections.sort(newList);

        if(previous == null){
            LOGGER.info("Registered VisorTask: '{}'", task.getId());
        }
    }


    @Override
    public @Nullable VisorTask getAddonComponent(@NotNull String id) {
        return tasksMap.get(id);
    }


    @Override
    public @NotNull List<VisorTask> getAddonComponents(@NotNull VisorAddon addon) {
        return tasksMap.values().stream()
                .filter(t -> t.getOwner().getAddonId().equals(addon.getAddonId()))
                .toList();
    }


    @Override
    public @NotNull Collection<VisorTask> getAllComponents() {
        return Collections.unmodifiableCollection(tasksMap.values());
    }


    @Override
    public @Nullable VisorTask unregisterAddonComponent(@NotNull String id) {
        VisorTask removed = tasksMap.remove(id);
        if (removed != null) {
            List<VisorTask> list = tasksByType.get(removed.getType());
            list.remove(removed);
        }
        return removed;
    }


    @Override
    public void unregisterAddon(@NotNull VisorAddon addon) {
        // collect IDs first to avoid concurrent-modification
        List<String> toRemove = tasksMap.values().stream()
                .filter(t -> t.getOwner().getAddonId().equals(addon.getAddonId()))
                .map(VisorTask::getId)
                .toList();

        toRemove.forEach(this::unregisterAddonComponent);
    }
}
