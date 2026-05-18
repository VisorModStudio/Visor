package org.vmstudio.visor.core.client.gui.registry;


import org.vmstudio.visor.api.ModLoader;
import org.vmstudio.visor.api.client.gui.overlays.RegisterVROverlayTemplate;
import org.vmstudio.visor.api.client.gui.overlays.VROverlayTemplate;
import org.vmstudio.visor.api.client.gui.overlays.VROverlayTemplateRecord;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.api.common.addon.component.ComponentIds;
import org.vmstudio.visor.api.common.addon.component.ComponentRegistry;
import org.vmstudio.visor.api.common.utils.LoggerUtils;
import org.vmstudio.visor.core.client.ClientContext;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vmstudio.visor.core.client.VisorClientImpl;


import java.util.*;


public class VROverlayTemplateRegistry implements ComponentRegistry<VROverlayTemplateRecord> {
    private static final String REGISTRY_NAME = "VR Overlay Templates";

    private static final String COMPONENT_NAME = "VROverlayTemplate";
    private static final String ANNOTATION_NAME = "@RegisterVROverlayTemplate";

    private final Map<String, VROverlayTemplateRecord> componentsMap = new LinkedHashMap<>();

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

        VisorClientImpl.LOGGER.info("Found {} {} to register in addon: '{}'",
                annotated.size(), COMPONENT_NAME, addon.getAddonId());

        for (Class<?> clazz : annotated) {
            if (!VROverlayTemplate.class.isAssignableFrom(clazz)) {
                VisorClientImpl.LOGGER.warn(
                        "{} is annotated with {} but does not implement {}",
                        clazz.getName(), ANNOTATION_NAME, "VROverlayTemplate"
                );
                continue;
            }

            try {
                var annotation = clazz.getAnnotation(RegisterVROverlayTemplate.class);
                String id = annotation.id();
                boolean createDefault = annotation.isCreateDefault();

                @SuppressWarnings("unchecked")
                var componentClazz = (Class<? extends VROverlayTemplate>) clazz;
                var constructor = componentClazz.getConstructor(
                        VisorAddon.class,
                        String.class
                );
                var component = new VROverlayTemplateRecord(
                        addon,
                        id,
                        Component.translatable(annotation.name()),
                        Component.translatable(annotation.description()),
                        createDefault,
                        componentClazz,
                        constructor
                );
                registerComponent(component);
            } catch (Exception e) {
                VisorClientImpl.LOGGER.error("Failed to register {} from class: {}", COMPONENT_NAME, clazz.getName());
                LoggerUtils.printError(e);
                // continue registering other components
            }
        }
        ClientContext.overlayManager
                .getOverlayConfigAccessor()
                .reload(addon);
    }

    @Override
    public void registerComponent(@NotNull VROverlayTemplateRecord component) {
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

        }else{
            VisorClientImpl.LOGGER.info("Registered {}: '{}'", COMPONENT_NAME, component.getId());
        }
    }

    @Override
    public VROverlayTemplateRecord unregisterComponent(@NotNull String id) {
        var removed = componentsMap.remove(id);;
        if(removed != null) {
            VisorClientImpl.LOGGER.info("Unregistered {}: '{}'", COMPONENT_NAME, removed.getId());
        }
        return removed;
    }

    @Override
    public @Nullable VROverlayTemplateRecord getComponent(@NotNull String id) {
        return componentsMap.get(id);
    }


    @Override
    public @NotNull Collection<VROverlayTemplateRecord> getAllComponents() {
        return componentsMap.values()
                .stream()
                .toList();
    }

    @Override
    public @NotNull String getRegistryName() {
        return REGISTRY_NAME;
    }
}
