package me.phoenixra.visor.core.client.gui.screens.settings.categories;


import me.phoenixra.visor.core.client.settings.VROptionCategory;
import me.phoenixra.visor.core.client.settings.VROptionWidgetType;
import me.phoenixra.visor.core.client.gui.screens.settings.OptionWidgetEntry;
import me.phoenixra.visor.core.client.gui.screens.settings.VROptionsBaseScreen;
import net.minecraft.client.gui.screens.Screen;

public class VRSettingsMovementScreen extends VROptionsBaseScreen {

    public VRSettingsMovementScreen(Screen previousScreen) {
        super(VROptionCategory.MOVEMENT, previousScreen);
    }
    @Override
    protected VROptionWidgetType[] getOptionTypes() {
        return VROptionCategory.MOVEMENT.types()
                .toArray(new VROptionWidgetType[0]);
    }

    @Override
    protected OptionWidgetEntry[] getOptionEntries() {
        return new OptionWidgetEntry[0];
    }
}
