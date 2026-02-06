package me.phoenixra.visor.core.client.gui.screens.settings.categories;

import me.phoenixra.visor.core.client.gui.screens.settings.OptionWidgetEntry;
import me.phoenixra.visor.core.client.gui.screens.settings.VROptionsBaseScreen;
import me.phoenixra.visor.core.client.settings.VROptionCategory;
import me.phoenixra.visor.core.client.settings.VROptionWidgetType;
import net.minecraft.client.gui.screens.Screen;

public class VRSettingsImmersionScreen extends VROptionsBaseScreen {

    public VRSettingsImmersionScreen(Screen previousScreen) {
        super(VROptionCategory.IMMERSION, previousScreen);
    }
    @Override
    protected VROptionWidgetType[] getOptionTypes() {
        return VROptionCategory.IMMERSION.types()
                .toArray(new VROptionWidgetType[0]);
    }

    @Override
    protected OptionWidgetEntry[] getOptionEntries() {
        return new OptionWidgetEntry[0];
    }
}