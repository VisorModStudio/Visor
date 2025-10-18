package me.phoenixra.visor.core.client.gui.registry;

import lombok.Getter;
import me.phoenixra.visor.api.client.gui.overlays.VROverlay;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.api.common.addon.element.VisorRegistry;
import me.phoenixra.visor.core.client.ClientContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

import static com.mojang.text2speech.Narrator.LOGGER;


public class VROverlayRegistry implements VisorRegistry<VROverlay> {
    private static final String REGISTRY_NAME = "VR Overlays";

    private static final String ELEMENT_NAME = "VROverlay";


    private final Map<String, VROverlay> elementsMap = new LinkedHashMap<>();

    private final List<VROverlay> sortedElements = new ArrayList<>();

    @Getter
    private final Collection<VROverlay> allElements =
            Collections.unmodifiableCollection(elementsMap.values());


    public List<VROverlay> getSortedElements() {
        return Collections.unmodifiableList(sortedElements);
    }

    public List<VROverlay> getSortedByName() {
        return elementsMap.values().stream()
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
    public void registerElement(@NotNull VROverlay element) {
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
    public VROverlay unregisterElement(@NotNull String id) {
        var removed = elementsMap.remove(id);;
        if(removed != null) {
            sortedElements.remove(removed);
            Collections.sort(sortedElements);
            var template = removed.asTemplate();
            if(template != null){
                template.getOptionsConfig().getFile().delete();
                ClientContext.settingsHandler.getOverlayConfigsAccessor()
                        .removeConfig(removed.getId());
            }
            LOGGER.info("Unregistered {}: '{}'", ELEMENT_NAME, removed.getId());
        }

        return removed;
    }



    @Override
    public @Nullable VROverlay getElement(@NotNull String id) {
        return elementsMap.get(id);
    }



    @Override
    public @NotNull String getRegistryName() {
        return REGISTRY_NAME;
    }
}
