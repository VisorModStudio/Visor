package org.vmstudio.visor.core.client.gui.screens.settings.categories.rendering;

import org.vmstudio.visor.core.client.gui.screens.settings.VROptionsSet;
import org.vmstudio.visor.core.client.gui.screens.settings.VRSettingsScreen;
import org.vmstudio.visor.core.client.settings.VROptionCategory;
import org.vmstudio.visor.core.client.settings.VROptionWidgetType;
import org.vmstudio.visor.core.client.gui.screens.settings.OptionWidgetEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VRSettingsEyeEffects extends VROptionsSet {


    public VRSettingsEyeEffects(@NotNull VRSettingsScreen screen,
                                @Nullable VROptionsSet previousOptions,
                                @NotNull Runnable onWidgetsChanged) {
        super(screen, previousOptions, onWidgetsChanged);
    }


    @Override
    protected VROptionWidgetType[] getOptionTypes() {

        return VROptionCategory.RENDERING_EYE_EFFECTS.types()
                .toArray(new VROptionWidgetType[0]);
    }

    @Override
    protected OptionWidgetEntry[] getOptionEntries() {
        return new OptionWidgetEntry[0];
    }


}
