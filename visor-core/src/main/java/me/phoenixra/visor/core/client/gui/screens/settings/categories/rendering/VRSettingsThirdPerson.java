package me.phoenixra.visor.core.client.gui.screens.settings.categories.rendering;

import me.phoenixra.visor.core.client.settings.option.VRGuiOption;
import me.phoenixra.visor.core.client.settings.option.gui.VRGuiOptionEntry;
import me.phoenixra.visor.core.client.settings.option.gui.VRGuiOptionsBaseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class VRSettingsThirdPerson extends VRGuiOptionsBaseScreen {

    public VRSettingsThirdPerson(Screen guiScreen) {
        super(guiScreen,
                Component.translatable("visor.option.screen.third_person")
        );
    }
    @Override
    protected VRGuiOption[] getOptionTypes() {
        return new VRGuiOption[]{
                VRGuiOption.THIRD_PERSON_FOV,
                VRGuiOption.THIRD_PERSON_CAMERA_POS_X,
                VRGuiOption.THIRD_PERSON_CAMERA_POS_Y,
                VRGuiOption.THIRD_PERSON_CAMERA_POS_Z,
                VRGuiOption.THIRD_PERSON_CAMERA_ROTATION_X,
                VRGuiOption.THIRD_PERSON_CAMERA_ROTATION_Y,
                VRGuiOption.THIRD_PERSON_CAMERA_ROTATION_Z,
        };
    }

    @Override
    protected VRGuiOptionEntry[] getOptionEntries() {
        return new VRGuiOptionEntry[0];
    }
}
