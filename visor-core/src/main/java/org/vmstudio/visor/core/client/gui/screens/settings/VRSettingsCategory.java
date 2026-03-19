package org.vmstudio.visor.core.client.gui.screens.settings;

import lombok.Getter;
import org.vmstudio.visor.core.client.gui.screens.settings.categories.*;
import org.vmstudio.visor.core.client.settings.VROptionCategory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public enum VRSettingsCategory {
    PRESETS(null, (settingsScreen -> new VRSettingsPresets(settingsScreen, null,settingsScreen::repopulateWidgets))),
    RENDERING(VROptionCategory.RENDERING, (settingsScreen)->new VRSettingsRendering(settingsScreen, null, settingsScreen::repopulateWidgets)),
    VR_BODY(VROptionCategory.VR_BODY, (settingsScreen)->new VRSettingsBody(settingsScreen, null, settingsScreen::repopulateWidgets)),
    CONTROLS(VROptionCategory.CONTROLS, (settingsScreen)->new VRSettingsControls(settingsScreen, null, settingsScreen::repopulateWidgets)),
    MOVEMENT(VROptionCategory.MOVEMENT, (settingsScreen)->new VRSettingsMovement(settingsScreen, null, settingsScreen::repopulateWidgets)),
    IMMERSION(VROptionCategory.IMMERSION, (settingsScreen)->new VRSettingsImmersion(settingsScreen, null, settingsScreen::repopulateWidgets)),
    GUI(VROptionCategory.GUI, (settingsScreen)->new VRSettingsGui(settingsScreen, null, settingsScreen::repopulateWidgets));

    @Getter
    private final Function<VRSettingsScreen,VROptionsSet> supplier;
    @Getter
    private final VROptionCategory category;
    VRSettingsCategory(@Nullable VROptionCategory category,
                       @NotNull Function<VRSettingsScreen,VROptionsSet> supplier){
        this.category = category;
        this.supplier = supplier;
    }
}
