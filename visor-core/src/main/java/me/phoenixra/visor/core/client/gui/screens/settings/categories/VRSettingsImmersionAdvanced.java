package me.phoenixra.visor.core.client.gui.screens.settings.categories;

import me.phoenixra.visor.core.client.gui.screens.settings.OptionWidgetEntry;
import me.phoenixra.visor.core.client.gui.screens.settings.OptionWidgetPosition;
import me.phoenixra.visor.core.client.gui.screens.settings.VROptionsSet;
import me.phoenixra.visor.core.client.gui.screens.settings.VRSettingsScreen;
import me.phoenixra.visor.core.client.gui.screens.settings.categories.rendering.VRSettingsThirdPerson;
import me.phoenixra.visor.core.client.settings.VROptionCategory;
import me.phoenixra.visor.core.client.settings.VROptionWidgetType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class VRSettingsImmersionAdvanced extends VROptionsSet {

    public VRSettingsImmersionAdvanced(@NotNull VRSettingsScreen screen,
                                       @Nullable VROptionsSet previousOptions,
                                       @NotNull Runnable onWidgetsChanged) {
        super(screen, previousOptions, onWidgetsChanged);
    }

    @Override
    protected VROptionWidgetType[] getOptionTypes() {
        return VROptionCategory.IMMERSION_ADVANCED.types()
                .toArray(new VROptionWidgetType[0]);
    }


    @Override
    protected OptionWidgetEntry[] getOptionEntries() {
        return new OptionWidgetEntry[0];
    }

}
