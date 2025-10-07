package me.phoenixra.visor.core.client.gui.screens.settings.categories;

import me.phoenixra.visor.core.client.VisorState;
import me.phoenixra.visor.core.client.gui.screens.settings.categories.controls.VRActionSetsScreen;
import me.phoenixra.visor.core.client.settings.VROptionCategory;
import me.phoenixra.visor.core.client.settings.VROptionWidgetType;
import me.phoenixra.visor.core.client.gui.screens.settings.OptionWidgetEntry;
import me.phoenixra.visor.core.client.gui.screens.settings.OptionWidgetPosition;
import me.phoenixra.visor.core.client.gui.screens.settings.VROptionsBaseScreen;
import net.minecraft.client.gui.screens.Screen;

public class VRSettingsControlsScreen extends VROptionsBaseScreen {

    public VRSettingsControlsScreen(Screen previousScreen) {
        super(VROptionCategory.CONTROLS, previousScreen);
    }
    @Override
    protected VROptionWidgetType[] getOptionTypes() {
        return new VROptionWidgetType[0];
    }


    @Override
    protected OptionWidgetEntry[] getOptionEntries() {
        if(VisorState.getState().isInitialized()){
            return new OptionWidgetEntry[]{
                    new OptionWidgetEntry(
                            VROptionWidgetType.LEFT_HANDED,
                            OptionWidgetPosition.LEFT,
                            1,
                            null
                    ),
                    new OptionWidgetEntry(
                            VRActionSetsScreen.class,
                            OptionWidgetPosition.RIGHT,
                            1,
                            "visor.options.controls.action_sets.button"
                    ),

            };
        }else{
            return new OptionWidgetEntry[]{
                    new OptionWidgetEntry(
                            VROptionWidgetType.LEFT_HANDED,
                            OptionWidgetPosition.LEFT,
                            1,
                            null
                    )
            };
        }

    }

}
