package me.phoenixra.visor.core.client.gui.screens.settings.categories.rendering;

import me.phoenixra.visor.core.client.settings.option.VRGuiOption;
import me.phoenixra.visor.core.client.settings.option.gui.VRGuiOptionEntry;
import me.phoenixra.visor.core.client.settings.option.gui.VRGuiOptionsBaseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class VRSettingsMixedReality extends VRGuiOptionsBaseScreen {

    public VRSettingsMixedReality(Screen guiScreen) {
        super(guiScreen,
                Component.translatable("visor.option.screen.mixed_reality")
        );
    }
    @Override
    protected VRGuiOption[] getOptionTypes() {
        return new VRGuiOption[]{
                VRGuiOption.MIRROR_EYE,
                VRGuiOption.MIXED_REALITY_FOV,
                VRGuiOption.MIXED_REALITY_WITH_FIRST_PERSON,
                VRGuiOption.MIXED_REALITY_ALPHA_MASK,
                VRGuiOption.MIXED_REALITY_RENDER_HANDS,
                VRGuiOption.MIXED_REALITY_AS_GRID_2_X_2
        };
    }

    @Override
    protected VRGuiOptionEntry[] getOptionEntries() {
        return new VRGuiOptionEntry[0];
    }
}
