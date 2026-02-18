package me.phoenixra.visor.core.client.gui.screens.settings.categories.rendering;

import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.VisorState;
import me.phoenixra.visor.core.client.gui.overlays.builtin.VROverlayThirdPersonCamera;
import me.phoenixra.visor.core.client.gui.screens.settings.VROptionsSet;
import me.phoenixra.visor.core.client.gui.screens.settings.VRSettingsScreen;
import me.phoenixra.visor.core.client.settings.VROptionCategory;
import me.phoenixra.visor.core.client.settings.VROptionWidgetType;
import me.phoenixra.visor.core.client.gui.screens.settings.OptionWidgetEntry;
import me.phoenixra.visor.core.client.gui.screens.settings.OptionWidgetPosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class VRSettingsMixedReality extends VROptionsSet {

    public VRSettingsMixedReality(@NotNull VRSettingsScreen screen,
                                  @Nullable VROptionsSet previousOptions,
                                  @NotNull Runnable onWidgetsChanged) {
        super(screen, previousOptions, onWidgetsChanged);
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
                        this,
                        ()->{
                            if(!VisorState.get().isActive()){
                                return;
                            }
                            var cameraOverlay = ClientContext.overlayManager.getOverlay(
                                    VROverlayThirdPersonCamera.ID,
                                    VROverlayThirdPersonCamera.class
                            );
                            Objects.requireNonNull(cameraOverlay);

                            cameraOverlay.setChangingPosition(true);
                        },
                        OptionWidgetPosition.RIGHT,
                        5,
                        "visor.options.rendering.third_person.reposition_camera"
                )
        };
    }
}
