package org.vmstudio.visor.core.client.gui.screens.settings.categories.rendering;

import org.vmstudio.visor.core.client.gui.screens.settings.VROptionsSet;
import org.vmstudio.visor.core.client.gui.screens.settings.VRSettingsScreen;
import org.vmstudio.visor.core.client.settings.VROptionWidgetType;
import org.vmstudio.visor.core.client.gui.screens.settings.OptionWidgetEntry;
import org.vmstudio.visor.core.client.gui.screens.settings.OptionWidgetPosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VRSettingsEffects extends VROptionsSet {

    public VRSettingsEffects(@NotNull VRSettingsScreen screen,
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
                        new VRSettingsEyeEffects(getScreen(), this, onWidgetsChanged),
                        OptionWidgetPosition.LEFT,
                        0,
                        "visor.options.rendering.effects.eye_effects.button"
                ),
                new OptionWidgetEntry(
                        this,
                        VROptionWidgetType.SELF_SHADOW,
                        OptionWidgetPosition.RIGHT,
                        0,
                        null
                )
        };
    }
}
