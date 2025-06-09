package me.phoenixra.visor.core.client.input;

import lombok.Getter;
import me.phoenixra.visor.api.client.input.action.RegisterActionSet;
import me.phoenixra.visor.api.client.input.action.VisorActionSet;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.api.common.addon.VisorElementRegistry;
import me.phoenixra.visor.api.common.utils.LoggerUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.util.*;

import static com.mojang.text2speech.Narrator.LOGGER;
import static org.reflections.scanners.Scanners.SubTypes;
import static org.reflections.scanners.Scanners.TypesAnnotated;

public class ActionSetRegistry implements VisorElementRegistry<VisorActionSet> {
    private static final String REGISTRY_NAME = "Visor Action Sets";

    private static final String ELEMENT_NAME = "VisorActionSet";
    private static final String ANNOTATION_NAME = "@RegisterActionSet";

    private final Map<String, VisorActionSet> elementsMap = new LinkedHashMap<>();

    private final List<VisorActionSet> sortedElements = new ArrayList<>();

    @Getter
    private final Collection<VisorActionSet> allElements =
            Collections.unmodifiableCollection(elementsMap.values());


    public List<VisorActionSet> getSortedElements() {
        return Collections.unmodifiableList(sortedElements);
    }


    @Override
    public void registerAddonPath(@NotNull VisorAddon addon) {
        Reflections reflections = new Reflections(
                addon.getAddonPackagePath(),
                SubTypes, TypesAnnotated
        );
        Set<Class<?>> annotated =
                reflections.getTypesAnnotatedWith(RegisterActionSet.class);

        LOGGER.info("Found {} {} to register in addon: '{}'",
                annotated.size(), ELEMENT_NAME, addon.getAddonId());

        for (Class<?> clazz : annotated) {
            if (!VisorActionSet.class.isAssignableFrom(clazz)) {
                LOGGER.warn(
                        "{} is annotated with {} but does not implement {}",
                        clazz.getName(), ANNOTATION_NAME, ELEMENT_NAME
                );
                continue;
            }
            try {
                @SuppressWarnings("unchecked")
                Constructor<? extends VisorActionSet> constructor =
                        ((Class<? extends VisorActionSet>) clazz)
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
    public void registerElement(@NotNull VisorActionSet element) {
        var previous = elementsMap.put(element.getId(), element);

        if (previous != null) {
            LOGGER.info(
                    "Overriding existing {}: '{}' from addon '{}'",
                    ELEMENT_NAME,
                    previous.getId(),
                    previous.getOwner().getAddonId()
            );
            sortedElements.remove(previous);

        }else{
            LOGGER.info("Registered {}: '{}'", ELEMENT_NAME, element.getId());
        }
        sortedElements.add(element);
        Collections.sort(sortedElements);
    }

    @Override
    public @Nullable VisorActionSet unregisterElement(@NotNull String id) {
        var removed = elementsMap.remove(id);;
        if(removed != null) {
            sortedElements.remove(removed);
            Collections.sort(sortedElements);
            LOGGER.info("Unregistered {}: '{}'", ELEMENT_NAME, removed.getId());
        }
        return removed;

    }

    @Override
    public @Nullable VisorActionSet getElement(@NotNull String id) {
        return elementsMap.get(id);
    }




    @Override
    public @NotNull String getRegistryName() {
        return REGISTRY_NAME;
    }
}
