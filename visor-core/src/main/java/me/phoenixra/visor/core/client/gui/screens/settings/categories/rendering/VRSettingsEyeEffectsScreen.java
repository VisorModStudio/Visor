package me.phoenixra.visor.core.client.gui.screens.settings.categories.rendering;

import me.phoenixra.visor.core.client.settings.VROptionCategory;
import me.phoenixra.visor.core.client.settings.VROptionWidgetType;
import me.phoenixra.visor.core.client.gui.screens.settings.OptionWidgetEntry;
import me.phoenixra.visor.core.client.gui.screens.settings.VROptionsBaseScreen;
import net.minecraft.client.gui.screens.Screen;

public class VRSettingsEyeEffectsScreen extends VROptionsBaseScreen {

    public VRSettingsEyeEffectsScreen(Screen previousScreen) {
        super(VROptionCategory.RENDERING_EYE_EFFECTS, previousScreen);
    }

    @Override
    protected void init() {
        super.init();

    }

    @Override
    protected VROptionWidgetType[] getOptionTypes() {

        return VROptionCategory.RENDERING_EYE_EFFECTS.types()
                .toArray(new VROptionWidgetType[0]);
    }

    @Override
    protected OptionWidgetEntry[] getOptionEntries() {
        return new OptionWidgetEntry[0];
    }


}
