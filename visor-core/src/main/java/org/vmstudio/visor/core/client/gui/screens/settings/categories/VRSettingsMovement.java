package org.vmstudio.visor.core.client.gui.screens.settings.categories;


import org.vmstudio.visor.core.client.gui.screens.settings.VROptionsSet;
import org.vmstudio.visor.core.client.gui.screens.settings.VRSettingsScreen;
import org.vmstudio.visor.core.client.settings.VROptionCategory;
import org.vmstudio.visor.core.client.settings.VROptionWidgetType;
import org.vmstudio.visor.core.client.gui.screens.settings.OptionWidgetEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VRSettingsMovement extends VROptionsSet {

    public VRSettingsMovement(@NotNull VRSettingsScreen screen,
                              @Nullable VROptionsSet previousOptions,
                              @NotNull Runnable onWidgetsChanged) {
        super(screen, previousOptions, onWidgetsChanged);
    }

    @Override
    protected VROptionWidgetType[] getOptionTypes() {
        return VROptionCategory.MOVEMENT.types()
                .toArray(new VROptionWidgetType[0]);
    }

    @Override
    protected OptionWidgetEntry[] getOptionEntries() {
        return new OptionWidgetEntry[0];
    }
}
