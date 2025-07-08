package me.phoenixra.visor.core.client.gui.registry;


import me.phoenixra.visor.api.ModLoader;
import me.phoenixra.visor.api.client.gui.overlay.template.RegisterVROverlayTemplate;
import me.phoenixra.visor.api.client.gui.overlay.template.VROverlayTemplate;
import me.phoenixra.visor.api.client.gui.overlay.template.VROverlayTemplateRecord;
import me.phoenixra.visor.api.client.input.action.RegisterActionSet;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.api.common.addon.element.VisorRegistry;
import me.phoenixra.visor.api.common.utils.LoggerUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


import java.util.*;

import static com.mojang.text2speech.Narrator.LOGGER;
public class VROverlayTemplateRegistry implements VisorRegistry<VROverlayTemplateRecord> {
    private static final String REGISTRY_NAME = "VR Overlay Templates";

    private static final String ELEMENT_NAME = "VROverlayTemplate";
    private static final String ANNOTATION_NAME = "@RegisterVROverlayTemplate";

    private final Map<String, VROverlayTemplateRecord> elementsMap = new LinkedHashMap<>();

    @Override
    public void registerAddonPath(@NotNull VisorAddon addon) {

        String path = addon.getAddonPackagePath();
        if(path == null){
            return;
        }
        List<Class<?>> annotated = ModLoader.get().getClassesAnnotated(
                RegisterVROverlayTemplate.class,
                addon.getModId(),
                path
        );

        LOGGER.info("Found {} {} to register in addon: '{}'",
                annotated.size(), ELEMENT_NAME, addon.getAddonId());

        for (Class<?> clazz : annotated) {
            if (!VROverlayTemplate.class.isAssignableFrom(clazz)) {
                LOGGER.warn(
                        "{} is annotated with {} but does not implement {}",
                        clazz.getName(), ANNOTATION_NAME, "VROverlayTemplate"
                );
                continue;
            }

            try {
                var annotation = clazz.getAnnotation(RegisterVROverlayTemplate.class);
                var id = annotation.id();
                var isPublic = annotation.isPublic();

                @SuppressWarnings("unchecked")
                var elementClazz = (Class<? extends VROverlayTemplate>) clazz;
                var constructor = elementClazz.getConstructor(
                        VisorAddon.class,
                        String.class
                );
                var element = new VROverlayTemplateRecord(
                        addon,
                        id,
                        isPublic,
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
    public void registerElement(@NotNull VROverlayTemplateRecord element) {
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
    public VROverlayTemplateRecord unregisterElement(@NotNull String id) {
        var removed = elementsMap.remove(id);;
        if(removed != null) {
            LOGGER.info("Unregistered {}: '{}'", ELEMENT_NAME, removed.getId());
        }
        return removed;
    }

    @Override
    public @Nullable VROverlayTemplateRecord getElement(@NotNull String id) {
        return elementsMap.get(id);
    }


    @Override
    public @NotNull Collection<VROverlayTemplateRecord> getAllElements() {
        return elementsMap.values()
                .stream()
                .toList();
    }

    @Override
    public @NotNull String getRegistryName() {
        return REGISTRY_NAME;
    }
}
