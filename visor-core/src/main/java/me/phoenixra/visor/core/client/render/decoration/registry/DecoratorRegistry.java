package me.phoenixra.visor.core.client.render.decoration.registry;

import lombok.Getter;

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


public class DecoratorRegistry implements VisorElementRegistry<VRDecorator> {

    private final Map<String, VRDecorator> decoratorsMap = new LinkedHashMap<>();

    private final List<VRDecorator> sortedDecorators = new ArrayList<>();

    private final Collection<VRDecorator> allDecorators =
            Collections.unmodifiableCollection(decoratorsMap.values());


    public List<VRDecorator> getSortedDecorators() {
        return Collections.unmodifiableList(sortedDecorators);
    }


    @Override
    public void registerAddonPath(@NotNull VisorAddon addon) {
        Reflections reflections = new Reflections(
                addon.getAddonPackagePath(),
                SubTypes, TypesAnnotated
        );
        Set<Class<?>> annotated =
                reflections.getTypesAnnotatedWith(RegisterVRDecorator.class);

        LOGGER.info("Found {} VRDecorator to register in addon {}",
                annotated.size(), addon.getAddonId());

        for (Class<?> clazz : annotated) {
            if (!VRDecorator.class.isAssignableFrom(clazz)) {
                LOGGER.warn(
                        "{} is annotated with @RegisterVRDecorator but does not implement VRDecorator",
                        clazz.getName()
                );
                continue;
            }
            try {
                @SuppressWarnings("unchecked")
                Constructor<? extends VRDecorator> ctor =
                        ((Class<? extends VRDecorator>) clazz)
                                .getConstructor(VisorAddon.class);

                VRDecorator view = ctor.newInstance(addon);

                registerAddonComponent(view);

            } catch (ReflectiveOperationException e) {
                LOGGER.error("Failed to register VRDecorator from class: {}", clazz.getName());
                LoggerUtils.printError(e);
                // continue registering other views
            }
        }
    }

    @Override
    public void registerAddonComponent(@NotNull VRDecorator decorator) {
        var previous = decoratorsMap.put(decorator.getId(), decorator);


        if (previous == null) {
            LOGGER.info("Registered VRDecorator: '{}'", decorator.getId());

        }else{
            LOGGER.info(
                    "Overriding existing VRDecorator: '{}' from addon '{}'",
                    previous.getId(),
                    previous.getOwner().getAddonId()
            );
            sortedDecorators.remove(previous);
        }
        sortedDecorators.add(decorator);
        Collections.sort(sortedDecorators);
    }

    @Override
    public @Nullable VRDecorator unregisterAddonComponent(@NotNull String id) {
        var removed = decoratorsMap.remove(id);;
        if(removed != null) {
            sortedDecorators.remove(removed);
            Collections.sort(sortedDecorators);
        }
        return removed;

    }

    @Override
    public @Nullable VRDecorator getAddonComponent(@NotNull String id) {
        return decoratorsMap.get(id);
    }

    @Override
    public @NotNull Collection<VRDecorator> getAddonComponents(@NotNull VisorAddon addon) {
        return decoratorsMap.values().stream()
                .filter(v -> v.getOwner().equals(addon))
                .toList();
    }

    @Override
    public @NotNull Collection<VRDecorator> getAllComponents() {
        return allDecorators;
    }


    @Override
    public void unregisterAddon(@NotNull VisorAddon addon) {
        List<String> toRemove = decoratorsMap.values().stream()
                .filter(v -> v.getOwner().equals(addon))
                .map(VRDecorator::getId)
                .toList();
        toRemove.forEach(decoratorsMap::remove);
    }


}
