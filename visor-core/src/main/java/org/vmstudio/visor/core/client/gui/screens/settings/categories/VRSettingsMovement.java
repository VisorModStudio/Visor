package org.vmstudio.visor.core.client.gui.screens.settings.categories;


import org.vmstudio.visor.core.client.gui.screens.settings.VROptionsSet;
import org.vmstudio.visor.core.client.gui.screens.settings.VRSettingsScreen;
import org.vmstudio.visor.api.client.settings.VRClientSettings;
import org.vmstudio.visor.api.client.settings.VROptionCategory;
import org.vmstudio.visor.core.client.settings.VROptionWidgetType;
import org.vmstudio.visor.core.client.gui.screens.settings.OptionWidgetEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VRSettingsMovement extends VROptionsSet {

    private boolean shownTreadmillOptions;

    public VRSettingsMovement(@NotNull VRSettingsScreen screen,
                              @Nullable VROptionsSet previousOptions,
                              @NotNull Runnable onWidgetsChanged) {
        super(screen, previousOptions, onWidgetsChanged);
    }

    @Override
    protected VROptionWidgetType[] getOptionTypes() {
        shownTreadmillOptions = VRClientSettings.isTreadmillEnabled();
        return VROptionWidgetType.byCategory(VROptionCategory.MOVEMENT)
                .stream()
                .filter(type -> shownTreadmillOptions
                        || type != VROptionWidgetType.TREADMILL_SPEED_SCALE)
                .toArray(VROptionWidgetType[]::new);
    }

    @Override
    public void onTick() {
        if (shownTreadmillOptions != VRClientSettings.isTreadmillEnabled()) {
            reinit();
        }
    }

    @Override
    protected OptionWidgetEntry[] getOptionEntries() {
        return new OptionWidgetEntry[0];
    }
}
