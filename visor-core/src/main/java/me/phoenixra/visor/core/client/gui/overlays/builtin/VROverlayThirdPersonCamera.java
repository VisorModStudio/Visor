package me.phoenixra.visor.core.client.gui.overlays.builtin;

import me.phoenixra.visor.api.client.gui.overlay.framework.VROverlayScreen;
import me.phoenixra.visor.api.client.render.VRDisplay;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.api.common.addon.element.ElementPriority;
import me.phoenixra.visor.core.client.render.VRRenderState;
import me.phoenixra.visor.core.client.settings.VRClientSettings;
import me.phoenixra.visor.core.client.settings.option.enums.MirrorMode;
import org.jetbrains.annotations.NotNull;

public class VROverlayThirdPersonCamera extends VROverlayScreen {
    public static final String ID = "third_person_camera";

    public VROverlayThirdPersonCamera(@NotNull VisorAddon owner,
                                      @NotNull String id) {
        super(owner, id, ElementPriority.HIGHER, 1.0f);
    }

    @Override
    protected boolean updateVisibility() {
        return VRRenderState.getVRWorldDisplays().contains(VRDisplay.THIRD_PERSON);
    }

    @Override
    public void updatePose(float partialTicks) {

    }
}
