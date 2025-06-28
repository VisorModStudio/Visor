package me.phoenixra.visor.core.client.gui.screens.settings.categories.rendering;

import me.phoenixra.visor.core.client.settings.VRClientSettings;
import me.phoenixra.visor.core.client.settings.option.VRGuiOption;
import me.phoenixra.visor.core.client.settings.option.enums.MirrorMode;
import me.phoenixra.visor.core.client.settings.option.gui.VRGuiOptionEntry;
import me.phoenixra.visor.core.client.settings.option.gui.VRGuiOptionsBaseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class VRSettingsEyeEffectsScreen extends VRGuiOptionsBaseScreen {

    public VRSettingsEyeEffectsScreen(Screen previousScreen) {
        super(previousScreen,
                Component.translatable("visor.option.screen.rendering.eyeEffects")
        );
    }

    @Override
    protected void init() {
        super.init();

    }

    @Override
    protected VRGuiOption[] getOptionTypes() {

        return new VRGuiOption[]{
                VRGuiOption.LOW_HEALTH_INDICATOR,
                VRGuiOption.HIT_INDICATOR,
                VRGuiOption.FREEZE_EFFECT,
                VRGuiOption.PUMPKIN_EFFECT
        };
    }

    @Override
    protected VRGuiOptionEntry[] getOptionEntries() {
        return new VRGuiOptionEntry[0];
    }


}
