package org.vmstudio.visor.core.client.gui.registry;

import lombok.Getter;
import org.vmstudio.visor.api.client.gui.overlays.VROverlay;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.api.common.addon.component.ComponentIds;
import org.vmstudio.visor.api.common.addon.component.ComponentRegistry;
import org.vmstudio.visor.core.client.ClientContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vmstudio.visor.core.client.VisorClientImpl;

import java.util.*;
import java.util.stream.Collectors;



public class VROverlayRegistry implements ComponentRegistry<VROverlay> {
    private static final String REGISTRY_NAME = "VR Overlays";

    private static final String COMPONENT_NAME = "VROverlay";


    private final Map<String, VROverlay> componentsMap = new LinkedHashMap<>();

    private final List<VROverlay> sortedComponents = new ArrayList<>();

    @Getter
    private final Collection<VROverlay> allComponents =
            Collections.unmodifiableCollection(componentsMap.values());


    public List<VROverlay> getSortedComponents() {
        return Collections.unmodifiableList(sortedComponents);
    }

    public List<VROverlay> getSortedByName() {
        return componentsMap.values().stream()
                .sorted(Comparator.comparing(
                        (it)->it.getName().getString(),
                        String.CASE_INSENSITIVE_ORDER
                ))
                .collect(Collectors.toList());
    }
    @Override
    public void registerAddonPath(@NotNull VisorAddon addon) {
        //empty, registered only manually
    }

    @Override
    public void registerComponent(@NotNull VROverlay component) {
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
            sortedComponents.remove(previous);

        }else{
            VisorClientImpl.LOGGER.info("Registered {}: '{}'", COMPONENT_NAME, component.getId());
        }
        sortedComponents.add(component);
        Collections.sort(sortedComponents);

    }

    @Override
    public VROverlay unregisterComponent(@NotNull String id) {
        var removed = componentsMap.remove(id);;
        if(removed != null) {
            sortedComponents.remove(removed);
            Collections.sort(sortedComponents);
            var template = removed.asTemplate();
            if(template != null){
                template.getOptionsConfig().getFile().delete();
                ClientContext.settingsManager.getOverlayConfigsAccessor()
                        .removeConfig(removed.getId());
            }
            VisorClientImpl.LOGGER.info("Unregistered {}: '{}'", COMPONENT_NAME, removed.getId());
        }

        return removed;
    }



    @Override
    public @Nullable VROverlay getComponent(@NotNull String id) {
        return componentsMap.get(id);
    }



    @Override
    public @NotNull String getRegistryName() {
        return REGISTRY_NAME;
    }
}
