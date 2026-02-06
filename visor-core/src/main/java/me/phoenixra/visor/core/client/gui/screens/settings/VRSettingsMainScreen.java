package me.phoenixra.visor.core.client.gui.screens.settings;

import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.gui.overlays.builtin.settings.VROverlaySettings;
import me.phoenixra.visor.core.client.gui.screens.settings.categories.*;
import me.phoenixra.visor.core.client.settings.VROptionCategory;
import me.phoenixra.visor.core.client.settings.VROptionWidgetType;
import net.minecraft.client.gui.screens.Screen;

import java.util.Objects;



public class VRSettingsMainScreen extends VROptionsBaseScreen {
    public VRSettingsMainScreen(Screen previousScreen) {
        super(VROptionCategory.MAIN, previousScreen);
    }

    @Override
    protected VROptionWidgetType[] getOptionTypes() {
        return new VROptionWidgetType[0];
    }

    @Override
    protected OptionWidgetEntry[] getOptionEntries() {
        return new OptionWidgetEntry[]{
                new OptionWidgetEntry(
                        VRSettingsGuiScreen.class,
                        OptionWidgetPosition.LEFT,
                        1,
                        "visor.options.gui.button"
                ),
                new OptionWidgetEntry(
                        VRSettingsMovementScreen.class,
                        OptionWidgetPosition.RIGHT,
                        1,
                        "visor.options.movement.button"
                ),
                new OptionWidgetEntry(
                        VRSettingsRenderingScreen.class,
                        OptionWidgetPosition.LEFT,
                        2,
                        "visor.options.rendering.button"
                ),
                new OptionWidgetEntry(
                        VRSettingsControlsScreen.class,
                        OptionWidgetPosition.RIGHT,
                        2,
                        "visor.options.controls.button"
                ),
                new OptionWidgetEntry(
                        VRSettingsImmersionScreen.class,
                        OptionWidgetPosition.LEFT,
                        3,
                        "visor.options.immersion.button"
                ),
                new OptionWidgetEntry(
                        VRSettingsAddonsScreen.class,
                        OptionWidgetPosition.LEFT,
                        6,
                        "visor.options.main.addons"
                ),
                new OptionWidgetEntry(
                        Objects.requireNonNull(
                                ClientContext.overlayManager
                                        .getOverlay(VROverlaySettings.ID)
                        ),
                        OptionWidgetPosition.RIGHT,
                        6,
                        "visor.options.main.overlay_settings"
                )
        };
    }

    @Override
    protected void loadDefaultSettings() {
        super.loadDefaultSettings();
    }
}
