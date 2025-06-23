package me.phoenixra.visor.core.client.gui.screens.settings.categories;

import me.phoenixra.visor.core.client.settings.option.VRGuiOption;

import me.phoenixra.visor.core.client.settings.option.gui.VRGuiOptionEntry;
import me.phoenixra.visor.core.client.settings.option.gui.VRGuiOptionsBaseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class VRSettingsGuiScreen extends VRGuiOptionsBaseScreen {

    public VRSettingsGuiScreen(Screen previousScreen) {
        super(previousScreen,
                Component.translatable("visor.option.screen.gui")
        );
    }
    @Override
    protected VRGuiOption[] getOptionTypes() {
        return new VRGuiOption[]{
                VRGuiOption.GUI_SCALE,
                VRGuiOption.SHADER_GUI_RENDER,
                VRGuiOption.HUD_DISABLED_HOTBAR
        };
    }

    @Override
    protected VRGuiOptionEntry[] getOptionEntries() {
        return new VRGuiOptionEntry[0];
    }

    @Override
    protected void loadDefaultSettings() {
        super.loadDefaultSettings();
        this.minecraft.options.hideGui = false;
    }
}
