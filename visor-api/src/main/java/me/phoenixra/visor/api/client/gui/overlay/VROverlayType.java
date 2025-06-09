package me.phoenixra.visor.api.client.gui.overlay;

import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.api.common.addon.VisorElement;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;

public record VROverlayType(VisorAddon owner,
                            String id,
                            Class<? extends VROverlay> clazz,
                            Constructor<? extends VROverlay> constructor
                            ) implements VisorElement {
    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public void setEnabled(boolean flag) {

    }

    @Override
    public @NotNull String getId() {
        return id;
    }

    @Override
    public @NotNull VisorAddon getOwner() {
        return owner;
    }
}
