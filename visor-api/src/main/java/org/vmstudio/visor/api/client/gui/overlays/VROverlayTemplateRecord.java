package org.vmstudio.visor.api.client.gui.overlays;

import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.api.common.addon.component.VisorComponent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;

/**
 * Record of registered overlay template
 *
 * @param owner the owner
 * @param id the template id
 * @param isCreateDefault If overlay with default settings should be created
 * when no overlays folder found
 * @param clazz the template class
 * @param constructor the template constructor
 */
public record VROverlayTemplateRecord(@NotNull VisorAddon owner,
                                      @NotNull String id,
                                      @NotNull Component name,
                                      @NotNull Component description,
                                      boolean isCreateDefault,
                                      @NotNull Class<? extends VROverlayTemplate> clazz,
                                      @NotNull Constructor<? extends VROverlayTemplate> constructor
                            ) implements VisorComponent {
    /**
     * Overlay templates are always enabled
     *
     * @return true
     */
    @Override
    public boolean isEnabled() {
        return true;
    }

    /**
     * Overlay templates are always enabled,
     * don't use this method
     *
     */
    @Override
    public void setEnabled(boolean flag) {
        if(!flag) {
            throw new RuntimeException("Disabling of this visor component is not supported");
        }
    }

    /**
     * Get Template id
     *
     * @return the template id
     */
    @Override
    public @NotNull String getId() {
        return id;
    }

    /**
     * Get Template owner
     *
     * @return the addon
     */
    @Override
    public @NotNull VisorAddon getOwner() {
        return owner;
    }
}
