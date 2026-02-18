package me.phoenixra.visor.core.client.gui.screens.settings;

import lombok.Getter;
import me.phoenixra.visor.core.client.gui.screens.settings.categories.*;
import me.phoenixra.visor.core.client.settings.VROptionCategory;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.function.Supplier;

public enum VRSettingsType {
    RENDERING(VROptionCategory.RENDERING, (settingsScreen)->new VRSettingsRendering(settingsScreen, null, settingsScreen::repopulateWidgets)),
    CONTROLS(VROptionCategory.CONTROLS, (settingsScreen)->new VRSettingsControls(settingsScreen, null, settingsScreen::repopulateWidgets)),
    MOVEMENT(VROptionCategory.MOVEMENT, (settingsScreen)->new VRSettingsMovement(settingsScreen, null, settingsScreen::repopulateWidgets)),
    IMMERSION(VROptionCategory.IMMERSION, (settingsScreen)->new VRSettingsImmersion(settingsScreen, null, settingsScreen::repopulateWidgets)),
    GUI(VROptionCategory.GUI, (settingsScreen)->new VRSettingsGui(settingsScreen, null, settingsScreen::repopulateWidgets));

    @Getter
    private final Function<VRSettingsScreen,VROptionsSet> supplier;
    @Getter
    private final VROptionCategory category;
    VRSettingsType(@NotNull VROptionCategory category,
                   @NotNull Function<VRSettingsScreen,VROptionsSet> supplier){
        this.category = category;
        this.supplier = supplier;
    }
}
