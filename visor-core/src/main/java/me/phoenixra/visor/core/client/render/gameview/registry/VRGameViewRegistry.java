package me.phoenixra.visor.core.client.render.gameview.registry;

import me.phoenixra.visor.api.client.render.gameview.VRGameView;
import me.phoenixra.visor.api.client.render.gameview.annotations.RegisterVRGameView;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.api.common.addon.VRElementRegistry;
import me.phoenixra.visor.core.client.VisorClient;
import me.phoenixra.visor.core.common.utils.LoggerUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.util.*;

import static org.reflections.scanners.Scanners.SubTypes;
import static org.reflections.scanners.Scanners.TypesAnnotated;


public class VRGameViewRegistry implements VRElementRegistry<VRGameView> {

    private final HashMap<String, VRGameView> overlayTypesMap = new HashMap<>();

    @Override
    public void registerAddonPath(@NotNull VisorAddon addon) {
        try {
            Reflections reflections = new Reflections(
                    addon.getAddonPackagePath(),
                    TypesAnnotated
            );
            Set<Class<?>> clazzList = reflections.get(
                    SubTypes.of(
                            TypesAnnotated.with(RegisterVRGameView.class)
                    ).asClass()
            );

            VisorClient.LOGGER.info("Found " + clazzList.size() + " VR game scenes to register");
            List<VRGameView> gameScenes = new ArrayList<>();
            for (Class<?> clazz : clazzList) {
                Constructor<?> constructor = clazz.getConstructor(
                        VisorAddon.class
                );
                if (!VRGameView.class.isAssignableFrom(clazz)) continue;
                try {
                    VisorClient.LOGGER.info("Loading " + clazz.getName() + " VR game scene...");
                    VRGameView effect = (VRGameView) constructor.newInstance(
                            addon
                    );
                    gameScenes.add(effect);

                } catch (InstantiationException | IllegalAccessException e) {
                    LoggerUtils.printError(e);
                    throw new RuntimeException(e);
                }
            }
            registerAddonComponent(gameScenes);

        } catch (Exception e) {
            LoggerUtils.printError(e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void registerAddonComponent(@NotNull VRGameView component) {
        overlayTypesMap.put(component.getId(),component);
    }

    @Override
    public VRGameView unregisterAddonComponent(@NotNull String id) {
        return overlayTypesMap.remove(id);
    }

    @Override
    public @Nullable VRGameView getAddonComponent(@NotNull String id) {
        return overlayTypesMap.get(id);
    }

    @Override
    public @NotNull Collection<VRGameView> getAddonComponents(@NotNull VisorAddon addon
    ) {
        return overlayTypesMap.values()
                .stream().filter(
                        it -> it.getOwner() == addon
                )
                .toList();
    }

    @Override
    public @NotNull Collection<VRGameView> getAllComponents() {
        return overlayTypesMap.values()
                .stream()
                .toList();
    }
}
