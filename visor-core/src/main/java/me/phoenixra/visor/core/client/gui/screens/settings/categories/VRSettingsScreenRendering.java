package me.phoenixra.visor.core.client.gui.screens.settings.categories;

import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.VisorState;
import me.phoenixra.visor.core.client.settings.VRClientSettings;
import me.phoenixra.visor.core.client.settings.option.VRGuiOption;
import me.phoenixra.visor.core.client.settings.option.enums.MirrorMode;
import me.phoenixra.visor.core.client.settings.option.gui.VRGuiOptionEntry;
import me.phoenixra.visor.core.client.settings.option.gui.VRGuiOptionsBaseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;



public class VRSettingsScreenRendering extends VRGuiOptionsBaseScreen {

    public VRSettingsScreenRendering(Screen previousScreen) {
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
        List<VRGuiOption> options = new ArrayList<>();
        options.add(VRGuiOption.MIRROR_DISPLAY);
        if (VRClientSettings.getDisplayMirrorMode() == MirrorMode.CROPPED
                || VRClientSettings.getDisplayMirrorMode() == MirrorMode.SINGLE) {
            options.add(VRGuiOption.MIRROR_USE_LEFT_EYE);
        }
        options.add(VRGuiOption.LOW_HEALTH_INDICATOR);

        MirrorMode mirrorMode = VRClientSettings.getDisplayMirrorMode();

        if(mirrorMode == MirrorMode.THIRD_PERSON){
            options.add(VRGuiOption.THIRD_PERSON_FOV);
        }

        options.add(VRGuiOption.EYE_FOV_SCALE);
        return options.toArray(new VRGuiOption[0]);
    }

    @Override
    protected VRGuiOptionEntry[] getOptionEntries() {
        return new VRGuiOptionEntry[0];
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
