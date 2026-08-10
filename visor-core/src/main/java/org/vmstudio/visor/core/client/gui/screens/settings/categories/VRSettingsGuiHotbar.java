package org.vmstudio.visor.core.client.gui.screens.settings.categories;

import org.vmstudio.visor.core.client.gui.screens.settings.OptionWidgetEntry;
import org.vmstudio.visor.core.client.gui.screens.settings.VROptionsSet;
import org.vmstudio.visor.core.client.gui.screens.settings.VRSettingsScreen;
import org.vmstudio.visor.api.client.settings.VROptionCategory;
import org.vmstudio.visor.core.client.settings.VROptionWidgetType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VRSettingsGuiHotbar extends VROptionsSet {

    public VRSettingsGuiHotbar(@NotNull VRSettingsScreen screen,
                               @Nullable VROptionsSet previousOptions,
                               @NotNull Runnable onWidgetsChanged) {
        super(screen, previousOptions, onWidgetsChanged);
    }

    @Override
    protected VROptionWidgetType[] getOptionTypes() {
        return VROptionWidgetType.byCategory(VROptionCategory.GUI_HOTBAR)
                .toArray(new VROptionWidgetType[0]);
    }

    @Override
    protected OptionWidgetEntry[] getOptionEntries() {
        return new OptionWidgetEntry[0];
    }

}
