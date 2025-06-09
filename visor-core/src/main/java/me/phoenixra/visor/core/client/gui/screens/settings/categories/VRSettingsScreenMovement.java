package me.phoenixra.visor.core.client.gui.screens.settings.categories;


import me.phoenixra.visor.core.client.settings.option.VRGuiOption;
import me.phoenixra.visor.core.client.settings.option.gui.VRGuiOptionEntry;
import me.phoenixra.visor.core.client.settings.option.gui.VRGuiOptionsBaseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class VRSettingsScreenMovement extends VRGuiOptionsBaseScreen {

    public VRSettingsScreenMovement(Screen guiScreen) {
        super(guiScreen,
                Component.translatable("visor.option.screen.movement")
        );
    }
    @Override
    protected VRGuiOption[] getOptionTypes() {
        return new VRGuiOption[]{
                VRGuiOption.ROTATION_MODE,
                VRGuiOption.ROOM_MOVEMENT_MULTIPLIER,
                VRGuiOption.WORLD_ROTATION_INCREMENT
        };
    }

    @Override
    protected VRGuiOptionEntry[] getOptionEntries() {
        return new VRGuiOptionEntry[0];
    }
}
