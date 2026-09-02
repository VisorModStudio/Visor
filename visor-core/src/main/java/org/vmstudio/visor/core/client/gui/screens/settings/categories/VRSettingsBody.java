package org.vmstudio.visor.core.client.gui.screens.settings.categories;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import org.vmstudio.visor.api.client.settings.VRClientSettings;
import org.vmstudio.visor.api.client.settings.VROptionCategory;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.gui.screens.settings.OptionWidgetEntry;
import org.vmstudio.visor.core.client.gui.screens.settings.OptionWidgetPosition;
import org.vmstudio.visor.core.client.gui.screens.settings.VROptionsSet;
import org.vmstudio.visor.core.client.gui.screens.settings.VRSettingsScreen;
import org.vmstudio.visor.core.client.settings.VROptionWidgetType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VRSettingsBody extends VROptionsSet {
    private static final String MEASURE_KEY = "visor.options.vr_body.measure_height.button";

    private AbstractWidget measureHeightButton;
    private boolean shownHeightAuto;

    public VRSettingsBody(@NotNull VRSettingsScreen screen,
                          @Nullable VROptionsSet previousOptions,
                          @NotNull Runnable onWidgetsChanged) {
        super(screen, previousOptions, onWidgetsChanged);
    }

    @Override
    protected VROptionWidgetType[] getOptionTypes() {
        shownHeightAuto = VRClientSettings.isHeightAuto();
        return VROptionWidgetType.byCategory(VROptionCategory.VR_BODY)
                .toArray(new VROptionWidgetType[0]);
    }

    @Override
    protected OptionWidgetEntry[] getOptionEntries() {
        return new OptionWidgetEntry[]{
                new OptionWidgetEntry(
                        this,
                        new VRSettingsBodySelect(getScreen(), this, onWidgetsChanged),
                        OptionWidgetPosition.LEFT,
                        0,
                        "visor.options.vr_body.select.button"
                ),
                new OptionWidgetEntry(
                        this,
                        () -> ClientContext.localPlayer.getHeightTracker().startMeasure(),
                        OptionWidgetPosition.RIGHT,
                        0,
                        MEASURE_KEY
                ) {
                    @Override
                    public AbstractWidget createWidget() {
                        measureHeightButton = super.createWidget();
                        return measureHeightButton;
                    }
                }
        };
    }

    @Override
    public void onTick() {
        if (shownHeightAuto != VRClientSettings.isHeightAuto()) {
            reinit();
            return;
        }
        if (measureHeightButton == null) {
            return;
        }
        var tracker = ClientContext.localPlayer.getHeightTracker();
        measureHeightButton.setMessage(tracker.isMeasuring()
                ? Component.translatable("visor.messages.height_measure_countdown", tracker.getMeasureSecondsLeft())
                : Component.translatable(MEASURE_KEY));
    }
}
