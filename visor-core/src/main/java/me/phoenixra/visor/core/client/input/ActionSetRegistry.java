package me.phoenixra.visor.core.client.input;

import lombok.Getter;
import me.phoenixra.visor.api.client.input.action.RegisterActionSet;
import me.phoenixra.visor.api.client.input.action.VisorActionSet;
import me.phoenixra.visor.api.client.render.decoration.VRDecorator;
import me.phoenixra.visor.api.client.render.decoration.annotations.RegisterVRDecorator;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.api.common.addon.VisorElementRegistry;
import me.phoenixra.visor.core.common.utils.LoggerUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.util.*;

import static com.mojang.text2speech.Narrator.LOGGER;
import static org.reflections.scanners.Scanners.SubTypes;
import static org.reflections.scanners.Scanners.TypesAnnotated;

public class ActionSetRegistry implements VisorElementRegistry<VisorActionSet> {

    private final Map<String, VisorActionSet> actionSetMap = new LinkedHashMap<>();

    private final List<VisorActionSet> sortedActionSet = new ArrayList<>();

    private final Collection<VisorActionSet> allActionSets =
            Collections.unmodifiableCollection(actionSetMap.values());


    public List<VisorActionSet> getSortedActionSet() {
        return Collections.unmodifiableList(sortedActionSet);
    }


    @Override
    public void registerAddonPath(@NotNull VisorAddon addon) {
        Reflections reflections = new Reflections(
                addon.getAddonPackagePath(),
                SubTypes, TypesAnnotated
        );
        Set<Class<?>> annotated =
                reflections.getTypesAnnotatedWith(RegisterActionSet.class);

        LOGGER.info("Found {} VisorActionSet to register in addon {}",
                annotated.size(), addon.getAddonId());

        for (Class<?> clazz : annotated) {
            if (!VisorActionSet.class.isAssignableFrom(clazz)) {
                LOGGER.warn(
                        "{} is annotated with @RegisterActionSet but does not implement VisorActionSet",
                        clazz.getName()
                );
                continue;
            }
            try {
                @SuppressWarnings("unchecked")
                Constructor<? extends VisorActionSet> ctor =
                        ((Class<? extends VisorActionSet>) clazz)
                                .getConstructor(VisorAddon.class);

                VisorActionSet actionSet = ctor.newInstance(addon);

                registerAddonComponent(actionSet);

            } catch (ReflectiveOperationException e) {
                LOGGER.error("Failed to register VRDecorator from class: {}", clazz.getName());
                LoggerUtils.printError(e);
                // continue registering other views
            }
        }
    }

    @Override
    public void registerAddonComponent(@NotNull VisorActionSet decorator) {
        var previous = actionSetMap.put(decorator.getId(), decorator);


        if (previous == null) {
            LOGGER.info("Registered VRDecorator: '{}'", decorator.getId());

        }else{
            LOGGER.info(
                    "Overriding existing VRDecorator: '{}' from addon '{}'",
                    previous.getId(),
                    previous.getOwner().getAddonId()
            );
            sortedActionSet.remove(previous);
        }
        sortedActionSet.add(decorator);
        Collections.sort(sortedActionSet);
    }

    @Override
    public @Nullable VisorActionSet unregisterAddonComponent(@NotNull String id) {
        var removed = actionSetMap.remove(id);;
        if(removed != null) {
            sortedActionSet.remove(removed);
            Collections.sort(sortedActionSet);
        }
        return removed;

    }

    @Override
    public @Nullable VisorActionSet getAddonComponent(@NotNull String id) {
        return actionSetMap.get(id);
    }

    @Override
    public @NotNull Collection<VisorActionSet> getAddonComponents(@NotNull VisorAddon addon) {
        return actionSetMap.values().stream()
                .filter(v -> v.getOwner().equals(addon))
                .toList();
    }



    @Override
    public void unregisterAddon(@NotNull VisorAddon addon) {
        List<String> toRemove = actionSetMap.values().stream()
                .filter(v -> v.getOwner().equals(addon))
                .map(VisorActionSet::getId)
                .toList();
        toRemove.forEach(actionSetMap::remove);
    }

    @Override
    public @NotNull Collection<VisorActionSet> getAllComponents() {
        return allActionSets;
    }
}
