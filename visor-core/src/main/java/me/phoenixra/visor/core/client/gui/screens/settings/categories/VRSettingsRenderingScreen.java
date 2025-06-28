package me.phoenixra.visor.core.client.gui.screens.settings.categories;

import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.VisorState;
import me.phoenixra.visor.core.client.gui.overlays.builtin.settings.VROverlaySettings;
import me.phoenixra.visor.core.client.gui.screens.settings.categories.rendering.VRSettingsEyeEffectsScreen;
import me.phoenixra.visor.core.client.settings.VRClientSettings;
import me.phoenixra.visor.core.client.settings.option.VRGuiOption;
import me.phoenixra.visor.core.client.settings.option.enums.MirrorMode;
import me.phoenixra.visor.core.client.settings.option.gui.VRGuiOptionEntry;
import me.phoenixra.visor.core.client.settings.option.gui.VRGuiOptionPosition;
import me.phoenixra.visor.core.client.settings.option.gui.VRGuiOptionsBaseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class VRSettingsRenderingScreen extends VRGuiOptionsBaseScreen {

    public VRSettingsRenderingScreen(Screen previousScreen) {
        super(previousScreen,
                Component.translatable("visor.option.screen.rendering")
        );
    }

    @Override
    protected void init() {
        super.init();

    }

    @Override
    protected VRGuiOption[] getOptionTypes() {

        return new VRGuiOption[0];
    }

    @Override
    protected VRGuiOptionEntry[] getOptionEntries() {
        List<VRGuiOptionEntry> options = new ArrayList<>();
        options.add(
                new VRGuiOptionEntry(
                        VRGuiOption.MIRROR_DISPLAY,
                        VRGuiOptionPosition.LEFT,
                        1,
                        null
                )
        );
        options.add(
                new VRGuiOptionEntry(
                        VRSettingsEyeEffectsScreen.class,
                        VRGuiOptionPosition.RIGHT,
                        1,
                        "visor.option.screen.rendering.eyeEffects.button"
                )
        );

        MirrorMode mirrorMode = VRClientSettings.getDisplayMirrorMode();

        if(mirrorMode == MirrorMode.THIRD_PERSON){
            options.add(
                    new VRGuiOptionEntry(
                            VRGuiOption.THIRD_PERSON_FOV,
                            VRGuiOptionPosition.LEFT,
                            2,
                            null
                    )
            );
        }


        return options.toArray(new VRGuiOptionEntry[0]);
    }


    @Override
    protected void loadDefaultSettings() {
        super.loadDefaultSettings();
        this.minecraft.options.fov().set(70);
        if(VisorState.getState().isActive()) {
            ClientContext.renderer.prepareReinit("Defaults Loaded");
        }
    }
}
