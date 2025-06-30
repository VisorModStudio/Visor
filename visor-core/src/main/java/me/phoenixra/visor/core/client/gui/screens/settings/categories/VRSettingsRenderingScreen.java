package me.phoenixra.visor.core.client.gui.screens.settings.categories;

import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.VisorState;
import me.phoenixra.visor.core.client.gui.screens.settings.categories.controls.VRActionSetsScreen;
import me.phoenixra.visor.core.client.gui.screens.settings.categories.rendering.VRSettingsEyeEffectsScreen;
import me.phoenixra.visor.core.client.gui.screens.settings.categories.rendering.VRSettingsMixedReality;
import me.phoenixra.visor.core.client.gui.screens.settings.categories.rendering.VRSettingsThirdPerson;
import me.phoenixra.visor.core.client.settings.VRClientSettings;
import me.phoenixra.visor.core.client.settings.option.VRGuiOption;
import me.phoenixra.visor.core.client.settings.option.enums.MirrorMode;
import me.phoenixra.visor.core.client.settings.option.gui.VRGuiOptionEntry;
import me.phoenixra.visor.core.client.settings.option.gui.VRGuiOptionPosition;
import me.phoenixra.visor.core.client.settings.option.gui.VRGuiOptionsBaseScreen;
import me.phoenixra.visor.core.client.settings.option.gui.elements.VRGuiOptionElement;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;


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

        MirrorMode mirrorMode = VRClientSettings.getMirrorMode();

        if(mirrorMode == MirrorMode.CROPPED
                || mirrorMode == MirrorMode.SINGLE){
            options.add(
                    new VRGuiOptionEntry(
                            VRGuiOption.MIRROR_EYE,
                            VRGuiOptionPosition.LEFT,
                            2,
                            null
                    )
            );
        } else if(mirrorMode == MirrorMode.THIRD_PERSON){
            options.add(
                    new VRGuiOptionEntry(
                            VRSettingsThirdPerson.class,
                            VRGuiOptionPosition.LEFT,
                            2,
                            "visor.option.screen.third_person.button"
                    )
            );
        }else if(mirrorMode == MirrorMode.MIXED_REALITY){
            options.add(
                    new VRGuiOptionEntry(
                            VRSettingsMixedReality.class,
                            VRGuiOptionPosition.LEFT,
                            2,
                            "visor.option.screen.mixed_reality.button"
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

    @Override
    public boolean mouseClicked(double d, double e, int i) {
       boolean success =  super.mouseClicked(d, e, i);
       if(success && getFocused() instanceof VRGuiOptionElement option){
           if(option.getGuiOptionType() == VRGuiOption.MIRROR_DISPLAY){
               this.init();
           }
       }
       return success;
    }
}
