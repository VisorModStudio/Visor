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

public class VRSettingsThirdPerson extends VRGuiOptionsBaseScreen {

    public VRSettingsThirdPerson(Screen guiScreen) {
        super(guiScreen,
                Component.translatable("visor.option.screen.third_person")
        );
    }
    @Override
    protected VRGuiOption[] getOptionTypes() {
        return new VRGuiOption[0];
    }

    @Override
    protected VRGuiOptionEntry[] getOptionEntries() {

        return new VRGuiOptionEntry[]{
                new VRGuiOptionEntry(
                        VRGuiOption.THIRD_PERSON_FOV,
                        VRGuiOptionPosition.LEFT,
                        1,
                        null
                ),
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
                        1,
                        "visor.option.third_person_camera"
                )
        };
    }
}
