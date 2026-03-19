package org.vmstudio.visor.core.client.render.decoration.registry;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vmstudio.visor.api.ModLoader;
import org.vmstudio.visor.api.client.player.body.RegisterVRBody;
import org.vmstudio.visor.api.client.player.body.VRBody;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.api.common.addon.component.ComponentIds;
import org.vmstudio.visor.api.common.addon.component.ComponentRegistry;
import org.vmstudio.visor.api.common.utils.LoggerUtils;

import java.lang.reflect.Constructor;
import java.util.*;

import static com.mojang.text2speech.Narrator.LOGGER;

public class VRBodyRegistry implements ComponentRegistry<VRBody> {
    private static final String REGISTRY_NAME = "VR Body Item Poses";

    private static final String COMPONENT_NAME = "VRBody";
    private static final String ANNOTATION_NAME = "@RegisterVRBody";

    @Getter
    private final HashMap<String, VRBody> componentsMap = new HashMap<>();


    @Getter
    private final Collection<VRBody> allComponents =
            Collections.unmodifiableCollection(componentsMap.values());



    @Override
    public void registerAddonPath(@NotNull VisorAddon addon) {

        String path = addon.getAddonPackagePath();
        if(path == null){
            return;
        }
        List<Class<?>> annotated = ModLoader.get().getClassesAnnotated(
                RegisterVRBody.class,
                addon.getModId(),
                path
        );

        LOGGER.info("Found {} {} to register in addon: '{}'",
                annotated.size(), COMPONENT_NAME, addon.getAddonId());

        for (Class<?> clazz : annotated) {
            if (!VRBody.class.isAssignableFrom(clazz)) {
                LOGGER.warn(
                        "{} is annotated with {} but does not implement {}",
                        clazz.getName(), ANNOTATION_NAME, COMPONENT_NAME
                );
                continue;
            }
            try {
                @SuppressWarnings("unchecked")
                Constructor<? extends VRBody> constructor =
                        ((Class<? extends VRBody>) clazz)
                                .getConstructor(VisorAddon.class);

                var component = constructor.newInstance(addon);

                registerComponent(component);

            } catch (Exception e) {
                LOGGER.error("Failed to register {} from class: {}", COMPONENT_NAME, clazz.getName());
                LoggerUtils.printError(e);
                // continue registering other components
            }
        }
    }

    @Override
    public void registerComponent(@NotNull VRBody component) {
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
            LOGGER.info(
                    "Overriding existing {}: '{}' from addon '{}'",
                    COMPONENT_NAME,
                    previous.getId(),
                    previous.getOwner().getAddonId()
            );

        }else{
            LOGGER.info("Registered {}: '{}'", COMPONENT_NAME, component.getId());
        }
    }

    @Override
    public VRBody unregisterComponent(@NotNull String id) {
        var removed = componentsMap.remove(id);
        if(removed != null) {
            LOGGER.info("Unregistered {}: '{}'", COMPONENT_NAME, removed.getId());
        }
        return removed;
    }

    @Override
    public @Nullable VRBody getComponent(@NotNull String id) {
        return componentsMap.get(id);
    }


    @Override
    public @NotNull String getRegistryName() {
        return REGISTRY_NAME;
    }
}
