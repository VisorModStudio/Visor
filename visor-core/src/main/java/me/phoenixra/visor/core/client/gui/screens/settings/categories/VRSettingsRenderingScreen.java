package me.phoenixra.visor.core.client.gui.screens.settings.categories;

import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.VisorState;
import me.phoenixra.visor.core.client.gui.screens.settings.categories.rendering.VRSettingsEyeEffectsScreen;
import me.phoenixra.visor.core.client.gui.screens.settings.categories.rendering.VRSettingsMixedReality;
import me.phoenixra.visor.core.client.gui.screens.settings.categories.rendering.VRSettingsThirdPerson;
import me.phoenixra.visor.core.client.settings.VROptionCategory;
import me.phoenixra.visor.core.client.settings.VRClientSettings;
import me.phoenixra.visor.core.client.settings.VROptionWidgetType;
import me.phoenixra.visor.core.client.settings.options.enums.MirrorMode;
import me.phoenixra.visor.core.client.gui.screens.settings.OptionWidgetEntry;
import me.phoenixra.visor.core.client.gui.screens.settings.OptionWidgetPosition;
import me.phoenixra.visor.core.client.gui.screens.settings.VROptionsBaseScreen;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;

import java.util.ArrayList;
import java.util.List;


public class VRSettingsRenderingScreen extends VROptionsBaseScreen {

    public VRSettingsRenderingScreen(Screen previousScreen) {
        super(VROptionCategory.RENDERING, previousScreen);
    }

    @Override
    protected void init() {
        super.init();

    }

    @Override
    protected VROptionWidgetType[] getOptionTypes() {

        return new VROptionWidgetType[0];
    }

    @Override
    protected OptionWidgetEntry[] getOptionEntries() {
        List<OptionWidgetEntry> options = new ArrayList<>();
        options.add(
                new OptionWidgetEntry(
                        VROptionWidgetType.MIRROR_MODE,
                        OptionWidgetPosition.LEFT,
                        1,
                        null
                )
        );
        options.add(
                new OptionWidgetEntry(
                        VRSettingsEyeEffectsScreen.class,
                        OptionWidgetPosition.RIGHT,
                        1,
                        "visor.options.rendering.eye_effects.button"
                )
        );

        MirrorMode mirrorMode = VRClientSettings.getMirrorMode();

        if(mirrorMode == MirrorMode.CROPPED
                || mirrorMode == MirrorMode.SINGLE){
            options.add(
                    new OptionWidgetEntry(
                            VROptionWidgetType.MIRROR_EYE,
                            OptionWidgetPosition.LEFT,
                            2,
                            null
                    )
            );
        } else if(mirrorMode == MirrorMode.THIRD_PERSON){
            options.add(
                    new OptionWidgetEntry(
                            VRSettingsThirdPerson.class,
                            OptionWidgetPosition.LEFT,
                            2,
                            "visor.options.rendering.third_person.button"
                    )
            );
        }else if(mirrorMode == MirrorMode.MIXED_REALITY){
            options.add(
                    new OptionWidgetEntry(
                            VRSettingsMixedReality.class,
                            OptionWidgetPosition.LEFT,
                            2,
                            "visor.options.rendering.mixed_reality.button"
                    )
            );
        }


        return options.toArray(new OptionWidgetEntry[0]);
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
       if(success && getFocused() instanceof AbstractWidget clicked){
           var clickedOption = getTypeFromWidget(clicked);
           if(clickedOption == null) return success;
           if(clickedOption == VROptionWidgetType.MIRROR_MODE){
               this.init();
           }
       }
       return success;
    }
}
