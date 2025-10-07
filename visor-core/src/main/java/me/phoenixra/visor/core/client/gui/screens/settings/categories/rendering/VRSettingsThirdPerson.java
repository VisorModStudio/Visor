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

public class VRSettingsThirdPerson extends VROptionsBaseScreen {

    public VRSettingsThirdPerson(Screen previousScreen) {
        super(VROptionCategory.RENDERING_THIRD_PERSON, previousScreen);
    }
    @Override
    protected VROptionWidgetType[] getOptionTypes() {
        return new VROptionWidgetType[0];
    }

    @Override
    protected OptionWidgetEntry[] getOptionEntries() {

        return new OptionWidgetEntry[]{
                new OptionWidgetEntry(
                        VROptionWidgetType.THIRD_PERSON_FOV,
                        OptionWidgetPosition.LEFT,
                        1,
                        null
                ),
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
                        1,
                        "visor.options.rendering.third_person.reposition_camera"
                )
        };
    }
}
