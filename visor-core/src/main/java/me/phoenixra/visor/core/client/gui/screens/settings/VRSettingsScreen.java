package me.phoenixra.visor.core.client.gui.screens.settings;

import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.gui.overlays.builtin.settings.VROverlaySettings;
import me.phoenixra.visor.core.client.gui.screens.settings.categories.VRSettingsControlsScreen;
import me.phoenixra.visor.core.client.gui.screens.settings.categories.VRSettingsGuiScreen;
import me.phoenixra.visor.core.client.gui.screens.settings.categories.VRSettingsMovementScreen;
import me.phoenixra.visor.core.client.gui.screens.settings.categories.VRSettingsRenderingScreen;
import me.phoenixra.visor.core.client.settings.option.VRGuiOption;
import me.phoenixra.visor.core.client.settings.option.gui.VRGuiOptionEntry;
import me.phoenixra.visor.core.client.settings.option.gui.VRGuiOptionPosition;
import me.phoenixra.visor.core.client.settings.option.gui.VRGuiOptionsBaseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Objects;



public class VRSettingsScreen extends VRGuiOptionsBaseScreen {
    public VRSettingsScreen(Screen lastScreen) {
        super(lastScreen,
                Component.translatable("visor.option.screen.main")
        );
    }

    @Override
    protected VRGuiOption[] getOptionTypes() {
        return new VRGuiOption[0];
    }

    @Override
    protected VRGuiOptionEntry[] getOptionEntries() {
        return new VRGuiOptionEntry[]{
                new VRGuiOptionEntry(
                        VRSettingsGuiScreen.class,
                        VRGuiOptionPosition.LEFT,
                        1,
                        "visor.option.screen.gui.button"
                ),
                new VRGuiOptionEntry(
                        VRSettingsMovementScreen.class,
                        VRGuiOptionPosition.RIGHT,
                        1,
                        "visor.option.screen.movement.button"
                ),
                new VRGuiOptionEntry(
                        VRSettingsRenderingScreen.class,
                        VRGuiOptionPosition.LEFT,
                        2,
                        "visor.option.screen.rendering.button"
                ),
                new VRGuiOptionEntry(
                        VRSettingsControlsScreen.class,
                        VRGuiOptionPosition.RIGHT,
                        2,
                        "visor.option.screen.controls.button"
                ),
                new VRGuiOptionEntry(
                        VRGuiOption.WORLD_SCALE,
                        VRGuiOptionPosition.LEFT,
                        6,
                        null
                ),
                new VRGuiOptionEntry(
                        Objects.requireNonNull(
                                ClientContext.overlayManager
                                        .getOverlay(VROverlaySettings.ID)
                        ),
                        VRGuiOptionPosition.RIGHT,
                        6,
                        "Setup Overlays (VR)"
                )
        };
    }

    @Override
    protected void loadDefaultSettings() {
        super.loadDefaultSettings();
    }
}
