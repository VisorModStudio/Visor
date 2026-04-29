package org.vmstudio.visor.core.client.gui.screens.settings.categories;

import org.vmstudio.visor.core.client.gui.screens.settings.OptionWidgetEntry;
import org.vmstudio.visor.core.client.gui.screens.settings.OptionWidgetPosition;
import org.vmstudio.visor.core.client.gui.screens.settings.VROptionsSet;
import org.vmstudio.visor.core.client.gui.screens.settings.VRSettingsScreen;
import org.vmstudio.visor.core.client.settings.VROptionCategory;
import org.vmstudio.visor.core.client.settings.VROptionWidgetType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class VRSettingsImmersion extends VROptionsSet {

    public VRSettingsImmersion(@NotNull VRSettingsScreen screen,
                               @Nullable VROptionsSet previousOptions,
                               @NotNull Runnable onWidgetsChanged) {
        super(screen, previousOptions, onWidgetsChanged);
    }

    @Override
    protected VROptionWidgetType[] getOptionTypes() {
        return VROptionCategory.IMMERSION.types()
                .toArray(new VROptionWidgetType[0]);
    }

    @Override
    protected OptionWidgetEntry[] getOptionEntries() {
        return new OptionWidgetEntry[]{
            new OptionWidgetEntry(
                    this,
                    new VRSettingsImmersionAdvanced(getScreen(), this, onWidgetsChanged),
                    OptionWidgetPosition.LEFT,
                    0,
                    "visor.options.immersion.advanced.button"
            )
        };
    }
}