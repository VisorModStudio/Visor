package org.vmstudio.visor.core.client.gui.screens.settings.categories;

import org.vmstudio.visor.core.client.gui.screens.settings.OptionWidgetPosition;
import org.vmstudio.visor.core.client.gui.screens.settings.VROptionsSet;
import org.vmstudio.visor.core.client.gui.screens.settings.VRSettingsScreen;
import org.vmstudio.visor.core.client.gui.screens.settings.categories.controls.VRSettingsKeyboardLayouts;
import org.vmstudio.visor.core.client.settings.VROptionCategory;
import org.vmstudio.visor.core.client.settings.VROptionWidgetType;

import org.vmstudio.visor.core.client.gui.screens.settings.OptionWidgetEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

public class VRSettingsGui extends VROptionsSet {

    public VRSettingsGui(@NotNull VRSettingsScreen screen,
                         @Nullable VROptionsSet previousOptions,
                         @NotNull Runnable onWidgetsChanged) {
        super(screen, previousOptions, onWidgetsChanged);
    }

    @Override
    protected VROptionWidgetType[] getOptionTypes() {
        return VROptionCategory.GUI.types()
                .toArray(new VROptionWidgetType[0]);
    }

    @Override
    protected OptionWidgetEntry[] getOptionEntries() {
        return new OptionWidgetEntry[]{
                new OptionWidgetEntry(
                        this,
                        new VRSettingsKeyboardLayouts(getScreen(), this, onWidgetsChanged),
                        OptionWidgetPosition.LEFT,
                        0,
                        "visor.options.controls.keyboard_layouts.button"
                )
        };
    }

    @Override
    public void loadDefaults() {
        super.loadDefaults();
        MC.options.hideGui = false;
    }
}
