package org.vmstudio.visor.core.client.gui.screens.settings.categories.rendering;

import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.core.client.gui.overlays.builtin.VROverlayThirdPersonCamera;
import org.vmstudio.visor.core.client.gui.screens.settings.VROptionsSet;
import org.vmstudio.visor.core.client.gui.screens.settings.VRSettingsScreen;
import org.vmstudio.visor.core.client.settings.VROptionWidgetType;
import org.vmstudio.visor.core.client.gui.screens.settings.OptionWidgetEntry;
import org.vmstudio.visor.core.client.gui.screens.settings.OptionWidgetPosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class VRSettingsThirdPerson extends VROptionsSet {

    public VRSettingsThirdPerson(@NotNull VRSettingsScreen screen,
                                 @Nullable VROptionsSet previousOptions,
                                 @NotNull Runnable onWidgetsChanged) {
        super(screen, previousOptions, onWidgetsChanged);
    }

    @Override
    protected VROptionWidgetType[] getOptionTypes() {
        return new VROptionWidgetType[0];
    }

    @Override
    protected OptionWidgetEntry[] getOptionEntries() {

        return new OptionWidgetEntry[]{
                new OptionWidgetEntry(
                        this,
                        VROptionWidgetType.THIRD_PERSON_FOV,
                        OptionWidgetPosition.LEFT,
                        1,
                        null
                ),
                new OptionWidgetEntry(
                        this,
                        ()->{
                            if(!VisorState.get().isActive()){
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
