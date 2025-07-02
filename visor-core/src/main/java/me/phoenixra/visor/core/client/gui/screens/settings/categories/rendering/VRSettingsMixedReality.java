package me.phoenixra.visor.core.client.gui.screens.settings.categories.rendering;

import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.VisorState;
import me.phoenixra.visor.core.client.gui.overlays.builtin.VROverlayThirdPersonCamera;
import me.phoenixra.visor.core.client.settings.option.VRGuiOption;
import me.phoenixra.visor.core.client.settings.option.gui.VRGuiOptionEntry;
import me.phoenixra.visor.core.client.settings.option.gui.VRGuiOptionPosition;
import me.phoenixra.visor.core.client.settings.option.gui.VRGuiOptionsBaseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Objects;

public class VRSettingsMixedReality extends VRGuiOptionsBaseScreen {

    public VRSettingsMixedReality(Screen guiScreen) {
        super(guiScreen,
                Component.translatable("visor.option.screen.mixed_reality")
        );
    }
    @Override
    protected VRGuiOption[] getOptionTypes() {
        return new VRGuiOption[]{
                VRGuiOption.MIRROR_EYE,
                VRGuiOption.MIXED_REALITY_FOV,
                VRGuiOption.MIXED_REALITY_WITH_FIRST_PERSON,
                VRGuiOption.MIXED_REALITY_ALPHA_MASK,
                VRGuiOption.MIXED_REALITY_RENDER_HANDS,
                VRGuiOption.MIXED_REALITY_AS_GRID_2_X_2
        };
    }



    @Override
    protected VRGuiOptionEntry[] getOptionEntries() {
        return new VRGuiOptionEntry[]{
                new VRGuiOptionEntry(
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
                        VRGuiOptionPosition.RIGHT,
                        6,
                        "visor.option.third_person_camera"
                )
        };
    }
}
