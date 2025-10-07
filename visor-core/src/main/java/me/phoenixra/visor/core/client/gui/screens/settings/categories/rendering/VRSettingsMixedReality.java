package me.phoenixra.visor.core.client.gui.screens.settings.categories.rendering;

import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.VisorState;
import me.phoenixra.visor.core.client.gui.overlays.builtin.VROverlayThirdPersonCamera;
import me.phoenixra.visor.core.client.settings.VROptionCategory;
import me.phoenixra.visor.core.client.settings.VROptionWidgetType;
import me.phoenixra.visor.core.client.gui.screens.settings.OptionWidgetEntry;
import me.phoenixra.visor.core.client.gui.screens.settings.OptionWidgetPosition;
import me.phoenixra.visor.core.client.gui.screens.settings.VROptionsBaseScreen;
import net.minecraft.client.gui.screens.Screen;

import java.util.Objects;

public class VRSettingsMixedReality extends VROptionsBaseScreen {

    public VRSettingsMixedReality(Screen previousScreen) {
        super(VROptionCategory.RENDERING_MIXED_REALITY, previousScreen);
    }
    @Override
    protected VROptionWidgetType[] getOptionTypes() {
        return VROptionCategory.RENDERING_MIXED_REALITY.types()
                .toArray(new VROptionWidgetType[0]);
    }



    @Override
    protected OptionWidgetEntry[] getOptionEntries() {
        return new OptionWidgetEntry[]{
                new OptionWidgetEntry(
                        ()->{
                            if(!VisorState.getState().isActive()){
                                return;
                            }
                            var camOverlay = ClientContext.overlayManager.getOverlay(
                                    VROverlayThirdPersonCamera.ID,
                                    VROverlayThirdPersonCamera.class
                            );
                            Objects.requireNonNull(camOverlay);

                            camOverlay.setChangingPosition(true);
                        },
                        OptionWidgetPosition.RIGHT,
                        6,
                        "visor.options.rendering.third_person.reposition_camera"
                )
        };
    }
}
