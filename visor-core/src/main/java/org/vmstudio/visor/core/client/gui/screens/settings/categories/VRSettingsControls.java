package org.vmstudio.visor.core.client.gui.screens.settings.categories;

import org.vmstudio.visor.core.client.gui.screens.settings.VROptionsSet;
import org.vmstudio.visor.core.client.gui.screens.settings.VRSettingsScreen;
import org.vmstudio.visor.core.client.gui.screens.settings.categories.controls.VRSettingsActionSets;
import org.vmstudio.visor.core.client.gui.screens.settings.categories.controls.VRSettingsKeyboardLayouts;
import org.vmstudio.visor.core.client.settings.VROptionWidgetType;
import org.vmstudio.visor.core.client.gui.screens.settings.OptionWidgetEntry;
import org.vmstudio.visor.core.client.gui.screens.settings.OptionWidgetPosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VRSettingsControls extends VROptionsSet {

    public VRSettingsControls(@NotNull VRSettingsScreen screen,
                              @Nullable VROptionsSet previousOptions,
                              @NotNull Runnable onWidgetsChanged) {
        super(screen, previousOptions, onWidgetsChanged);
    }
    @Override
    protected VROptionWidgetType[] getOptionTypes() {
        return new VROptionWidgetType[0];
    }


    @Override
    protected OptionWidgetEntry[] getOptionEntries() {
        return new OptionWidgetEntry[]{
                new OptionWidgetEntry(
                        this,
                        VROptionWidgetType.LEFT_HANDED,
                        OptionWidgetPosition.LEFT,
                        0,
                        null
                ),
                new OptionWidgetEntry(
                        this,
                        new VRSettingsActionSets(getScreen(), this, onWidgetsChanged),
                        OptionWidgetPosition.RIGHT,
                        0,
                        "visor.options.controls.action_sets.button"
                )

        };

    }

}
