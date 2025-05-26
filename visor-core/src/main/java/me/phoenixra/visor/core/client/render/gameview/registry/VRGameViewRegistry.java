package me.phoenixra.visor.core.client.render.gameview.registry;

import lombok.Getter;
import me.phoenixra.visor.api.client.render.gameview.VRGameView;
import me.phoenixra.visor.api.client.render.gameview.annotations.RegisterVRGameView;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.api.common.addon.VisorElementRegistry;
import me.phoenixra.visor.core.client.VisorClient;
import me.phoenixra.visor.core.common.utils.LoggerUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.util.*;

import static com.mojang.text2speech.Narrator.LOGGER;
import static org.reflections.scanners.Scanners.SubTypes;
import static org.reflections.scanners.Scanners.TypesAnnotated;


public class VRGameViewRegistry implements VisorElementRegistry<VRGameView> {

    private final Map<String, VRGameView> views = new LinkedHashMap<>();

    /** Exposed unmodifiable view of all registered game views. */
    @Getter
    private final Collection<VRGameView> allViews =
            Collections.unmodifiableCollection(views.values());

    @Override
    public void registerAddonPath(@NotNull VisorAddon addon) {
        Reflections reflections = new Reflections(
                addon.getAddonPackagePath(),
                SubTypes, TypesAnnotated
        );
        Set<Class<?>> annotated =
                reflections.getTypesAnnotatedWith(RegisterVRGameView.class);

        LOGGER.info("Found {} VRGameView to register in addon {}",
                annotated.size(), addon.getAddonId());

        for (Class<?> clazz : annotated) {
            if (!VRGameView.class.isAssignableFrom(clazz)) {
                LOGGER.warn(
                        "{} is annotated with @RegisterVRGameView but does not implement VRGameView",
                        clazz.getName()
                );
                continue;
            }
            try {
                @SuppressWarnings("unchecked")
                Constructor<? extends VRGameView> ctor =
                        ((Class<? extends VRGameView>) clazz)
                                .getConstructor(VisorAddon.class);

                VRGameView view = ctor.newInstance(addon);

                registerAddonComponent(view);

            } catch (ReflectiveOperationException e) {
                LOGGER.error("Failed to register VRGameView from class: {}", clazz.getName());
                LoggerUtils.printError(e);
                // continue registering other views
            }
        }
    }

    @Override
    public void registerAddonComponent(@NotNull VRGameView view) {
        var previous = views.put(view.getId(), view);

        if (previous == null) {
            LOGGER.info("Registered VRGameView: '{}'", view.getId());

        }else{
            LOGGER.info(
                    "Overriding existing VRGameView: '{}' from addon '{}'",
                    previous.getId(),
                    previous.getOwner().getAddonId()
            );
        }
    }

    @Override
    public @Nullable VRGameView unregisterAddonComponent(@NotNull String id) {
        return views.remove(id);
    }

    @Override
    public @Nullable VRGameView getAddonComponent(@NotNull String id) {
        return views.get(id);
    }

    @Override
    public @NotNull Collection<VRGameView> getAddonComponents(@NotNull VisorAddon addon) {
        return views.values().stream()
                .filter(v -> v.getOwner().equals(addon))
                .toList();
    }

    @Override
    public @NotNull Collection<VRGameView> getAllComponents() {
        return allViews;
    }


    @Override
    public void unregisterAddon(@NotNull VisorAddon addon) {
        List<String> toRemove = views.values().stream()
                .filter(v -> v.getOwner().equals(addon))
                .map(VRGameView::getId)
                .toList();
        toRemove.forEach(views::remove);
    }
}
