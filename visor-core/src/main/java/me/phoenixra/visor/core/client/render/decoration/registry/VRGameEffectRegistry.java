package me.phoenixra.visor.core.client.render.decoration.registry;

import lombok.Getter;
import me.phoenixra.visor.api.client.render.decoration.annotations.RegisterVRGameEffect;
import me.phoenixra.visor.api.client.render.decoration.effects.VRGameEffect;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.api.common.addon.element.VisorRegistry;
import me.phoenixra.visor.api.common.utils.LoggerUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.util.*;

import static com.mojang.text2speech.Narrator.LOGGER;
import static org.reflections.scanners.Scanners.SubTypes;
import static org.reflections.scanners.Scanners.TypesAnnotated;

public class VRGameEffectRegistry implements VisorRegistry<VRGameEffect> {
    private static final String REGISTRY_NAME = "VR Game Effects";

    private static final String ELEMENT_NAME = "VRGameEffect";
    private static final String ANNOTATION_NAME = "@RegisterVRGameEffect";

    @Getter
    private final HashMap<String, VRGameEffect> elementsMap = new HashMap<>();

    @Getter
    private final Collection<VRGameEffect> allElements =
            Collections.unmodifiableCollection(elementsMap.values());

    @Override
    public void registerAddonPath(@NotNull VisorAddon addon) {
        Reflections reflections = new Reflections(
                addon.getAddonPackagePath(),
                SubTypes, TypesAnnotated
        );
        Set<Class<?>> annotated =
                reflections.getTypesAnnotatedWith(RegisterVRGameEffect.class);

        LOGGER.info("Found {} {} to register in addon: '{}'",
                annotated.size(), ELEMENT_NAME, addon.getAddonId());

        for (Class<?> clazz : annotated) {
            if (!VRGameEffect.class.isAssignableFrom(clazz)) {
                LOGGER.warn(
                        "{} is annotated with {} but does not implement {}",
                        clazz.getName(), ANNOTATION_NAME, ELEMENT_NAME
                );
                continue;
            }
            try {
                @SuppressWarnings("unchecked")
                Constructor<? extends VRGameEffect> constructor =
                        ((Class<? extends VRGameEffect>) clazz)
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
    public void registerElement(@NotNull VRGameEffect element) {
        var previous = elementsMap.put(element.getId(), element);

        if (previous != null) {
            LOGGER.info(
                    "Overriding existing {}: '{}' from addon '{}'",
                    ELEMENT_NAME,
                    previous.getId(),
                    previous.getOwner().getAddonId()
            );

        }else{
            LOGGER.info("Registered {}: '{}'", ELEMENT_NAME, element.getId());
        }
    }

    @Override
    public VRGameEffect unregisterElement(@NotNull String id) {
        var removed = elementsMap.remove(id);
        if(removed != null) {
            LOGGER.info("Unregistered {}: '{}'", ELEMENT_NAME, removed.getId());
        }
        return removed;
    }

    @Override
    public @Nullable VRGameEffect getElement(@NotNull String id) {
        return elementsMap.get(id);
    }



    @Override
    public @NotNull String getRegistryName() {
        return REGISTRY_NAME;
    }
}
