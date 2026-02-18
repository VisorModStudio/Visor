package me.phoenixra.visor.core.client.gui.screens.settings.categories;

import me.phoenixra.visor.core.client.gui.screens.settings.OptionWidgetEntry;
import me.phoenixra.visor.core.client.gui.screens.settings.VROptionsSet;
import me.phoenixra.visor.core.client.gui.screens.settings.VRSettingsScreen;
import me.phoenixra.visor.core.client.settings.VROptionCategory;
import me.phoenixra.visor.core.client.settings.VROptionWidgetType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        return new OptionWidgetEntry[0];
    }
}