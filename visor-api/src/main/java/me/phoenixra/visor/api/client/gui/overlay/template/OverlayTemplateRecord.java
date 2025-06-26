package me.phoenixra.visor.api.client.gui.overlay.template;

import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.api.common.addon.VisorElement;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;

public record OverlayTemplateRecord(@NotNull VisorAddon owner,
                                    @NotNull String id,
                                    @NotNull Class<? extends OverlayTemplate> clazz,
                                    @NotNull Constructor<? extends OverlayTemplate> constructor
                            ) implements VisorElement {
    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public void setEnabled(boolean flag) {
        if(!flag) {
            throw new RuntimeException("Disabling of this visor element is not supported");
        }
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
