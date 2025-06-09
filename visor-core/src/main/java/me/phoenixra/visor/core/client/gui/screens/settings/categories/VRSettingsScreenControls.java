package me.phoenixra.visor.core.client.gui.screens.settings.categories;

import me.phoenixra.visor.core.client.settings.option.VRGuiOption;
import me.phoenixra.visor.core.client.settings.option.gui.VRGuiOptionEntry;
import me.phoenixra.visor.core.client.settings.option.gui.VRGuiOptionsBaseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class VRSettingsScreenControls extends VRGuiOptionsBaseScreen {

    public VRSettingsScreenControls(Screen guiScreen) {
        super(guiScreen,
                Component.translatable("visor.option.screen.controls")
        );
    }
    @Override
    protected VRGuiOption[] getOptionTypes() {
        return new VRGuiOption[]{
                VRGuiOption.LEFT_HANDED
        };
    }

    @Override
    protected VRGuiOptionEntry[] getOptionEntries() {
        return new VRGuiOptionEntry[0];
    }
}
