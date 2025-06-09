package me.phoenixra.visor.core.client.gui.registry;


import me.phoenixra.visor.api.client.gui.overlay.RegisterOverlayType;
import me.phoenixra.visor.api.client.gui.overlay.VROverlay;
import me.phoenixra.visor.api.client.gui.overlay.VROverlayType;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.api.common.addon.VisorElementRegistry;
import me.phoenixra.visor.api.common.utils.LoggerUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reflections.Reflections;

import java.util.*;

import static com.mojang.text2speech.Narrator.LOGGER;
import static org.reflections.scanners.Scanners.SubTypes;
import static org.reflections.scanners.Scanners.TypesAnnotated;

public class VROverlayTypeRegistry implements VisorElementRegistry<VROverlayType> {
    private static final String REGISTRY_NAME = "VR Overlay Types";

    private static final String ELEMENT_NAME = "VROverlay_Type";
    private static final String ANNOTATION_NAME = "@RegisterOverlayType";

    private final Map<String, VROverlayType> elementsMap = new LinkedHashMap<>();

    @Override
    public void registerAddonPath(@NotNull VisorAddon addon) {

        Reflections reflections = new Reflections(
                addon.getAddonPackagePath(),
                SubTypes, TypesAnnotated
        );
        Set<Class<?>> annotated =
                reflections.getTypesAnnotatedWith(RegisterOverlayType.class);


        LOGGER.info("Found {} {} to register in addon: '{}'",
                annotated.size(), ELEMENT_NAME, addon.getAddonId());

        for (Class<?> clazz : annotated) {
            if (!VROverlay.class.isAssignableFrom(clazz)) {
                LOGGER.warn(
                        "{} is annotated with {} but does not implement {}",
                        clazz.getName(), ANNOTATION_NAME, "VROverlay"
                );
                continue;
            }

            try {
                var id = clazz.getAnnotation(RegisterOverlayType.class).id();

                @SuppressWarnings("unchecked")
                var elementClazz = (Class<? extends VROverlay>) clazz;
                var constructor = elementClazz.getConstructor(
                        VisorAddon.class,
                        String.class
                );
                var element = new VROverlayType(
                        addon,
                        id,
                        elementClazz,
                        constructor
                );
                registerElement(element);
            } catch (Exception e) {
                LOGGER.error("Failed to register {} from class: {}", ELEMENT_NAME, clazz.getName());
                LoggerUtils.printError(e);
                // continue registering other elements
            }
        }
    }

    @Override
    public void registerElement(@NotNull VROverlayType element) {
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
    public VROverlayType unregisterElement(@NotNull String id) {
        var removed = elementsMap.remove(id);;
        if(removed != null) {
            LOGGER.info("Unregistered {}: '{}'", ELEMENT_NAME, removed.getId());
        }
        return removed;
    }

    @Override
    public @Nullable VROverlayType getElement(@NotNull String id) {
        return elementsMap.get(id);
    }


    @Override
    public @NotNull Collection<VROverlayType> getAllElements() {
        return elementsMap.values()
                .stream()
                .toList();
    }

    @Override
    public @NotNull String getRegistryName() {
        return REGISTRY_NAME;
    }
}
