package me.phoenixra.visor.core.client.render.gameview.registry;

import lombok.Getter;
import me.phoenixra.visor.api.client.render.gameview.annotations.RegisterVRItemPose;
import me.phoenixra.visor.api.client.render.gameview.hand.VRHandItemPose;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.api.common.addon.VisorElementRegistry;
import me.phoenixra.visor.core.client.VisorClient;
import me.phoenixra.visor.core.common.utils.LoggerUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.util.*;

import static org.reflections.scanners.Scanners.SubTypes;
import static org.reflections.scanners.Scanners.TypesAnnotated;

public class VRHandItemPoseRegistry implements VisorElementRegistry<VRHandItemPose> {
    @Getter
    private final HashMap<String, VRHandItemPose> posesMap = new HashMap<>();

    @Getter
    private List<VRHandItemPose> posesSorted = new ArrayList<>();
    @Override
    public void registerAddonPath(@NotNull VisorAddon addon) {
        try {
            Reflections reflections = new Reflections(
                    addon.getAddonPackagePath(),
                    TypesAnnotated
            );
            Set<Class<?>> posesFound = reflections.get(
                    SubTypes.of(
                            TypesAnnotated.with(RegisterVRItemPose.class)
                    ).asClass()
            );

            VisorClient.LOGGER.info("Found " + posesFound.size() + " hand item poses to register");
            List<VRHandItemPose> posesList = new ArrayList<>();
            for (Class<?> clazz : posesFound) {
                Constructor<?> constructor = clazz.getConstructor(
                        VisorAddon.class
                );
                if (!VRHandItemPose.class.isAssignableFrom(clazz)) continue;
                try {
                    VisorClient.LOGGER.info("Loading " + clazz.getName() + " hand item pose...");
                    VRHandItemPose effect = (VRHandItemPose) constructor.newInstance(
                            addon
                    );
                    posesList.add(effect);

                } catch (InstantiationException | IllegalAccessException e) {
                    LoggerUtils.printError(e);
                    throw new RuntimeException(e);
                }
            }
            registerAddonComponent(posesList);

        } catch (Exception e) {
            LoggerUtils.printError(e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void registerAddonComponent(@NotNull VRHandItemPose pose) {
        posesMap.put(pose.getId(),pose);

        posesSorted.removeIf(
                it -> it.getId().equals(pose.getId())
        );
        posesSorted.add(pose);

        posesSorted = new ArrayList<>(
                posesSorted.stream().sorted().toList()
        );
    }

    @Override
    public VRHandItemPose unregisterAddonComponent(@NotNull String id) {
        VRHandItemPose handItemPose = posesMap.remove(id);
        if(handItemPose != null){
            posesSorted.removeIf(
                    it -> it.getId().equals(id)
            );
            posesSorted = new ArrayList<>(
                    posesSorted.stream().sorted().toList()
            );
        }
        return handItemPose;
    }

    @Override
    public @Nullable VRHandItemPose getAddonComponent(@NotNull String id) {
        return posesMap.get(id);
    }

    @Override
    public @NotNull List<VRHandItemPose> getAddonComponents(@NotNull VisorAddon addon) {
        return posesMap.values().stream()
                .filter(it->it.getOwner().getAddonId().equals(addon.getAddonId()))
                .toList();
    }

    @Override
    public @NotNull Collection<VRHandItemPose> getAllComponents() {
        return posesMap.values();
    }
}
