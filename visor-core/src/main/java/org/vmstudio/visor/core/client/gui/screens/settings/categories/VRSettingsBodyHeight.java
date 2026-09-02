package org.vmstudio.visor.core.client.gui.screens.settings.categories;

import me.phoenixra.atumvr.api.misc.color.AtumColor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vmstudio.visor.api.client.gui.helpers.GuiHelper;
import org.vmstudio.visor.api.client.gui.overlays.options.OptionTextures;
import org.vmstudio.visor.api.client.gui.widgets.ButtonImaged;
import org.vmstudio.visor.api.client.gui.widgets.SliderWidget;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoButtonImaged;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoSlider;
import org.vmstudio.visor.api.client.settings.VRClientSettings;
import org.vmstudio.visor.api.client.settings.enums.HeightMode;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.gui.screens.settings.OptionWidgetEntry;
import org.vmstudio.visor.core.client.gui.screens.settings.VROptionsSet;
import org.vmstudio.visor.core.client.gui.screens.settings.VRSettingsScreen;
import org.vmstudio.visor.core.client.player.height.HeightFormat;
import org.vmstudio.visor.core.client.player.height.PlayerHeightTracker;
import org.vmstudio.visor.core.client.settings.VROptionWidgetType;
import org.vmstudio.visor.core.client.utils.LangHelper;

import java.util.ArrayList;
import java.util.List;


public class VRSettingsBodyHeight extends VROptionsSet {
    private static final String LANG = "visor.options.vr_body.height.";

    private static final int PANEL_X = 58;
    private static final int PANEL_W = 140;
    private static final int ROW_H = 10;
    private static final int PANEL_CENTER_X = PANEL_X + PANEL_W / 2;
    private static final int GAP = 2;

    private static final AtumColor INACTIVE_TEXT = AtumColor.immutable(110, 110, 110, 255);

    private final List<AbstractWidget> widgets = new ArrayList<>();
    private ButtonImaged measureButton;
    private SliderWidget<Float> standingSlider;
    private List<Float> standingEntries;

    public VRSettingsBodyHeight(@NotNull VRSettingsScreen screen,
                                @Nullable VROptionsSet previousOptions,
                                @NotNull Runnable onWidgetsChanged) {
        super(screen, previousOptions, onWidgetsChanged);
    }

    @Override
    protected VROptionWidgetType[] getOptionTypes() {
        return null;
    }

    @Override
    protected OptionWidgetEntry[] getOptionEntries() {
        return null;
    }

    @Override
    protected boolean canLoadDefaults() {
        return false;
    }

    @Override
    public <T extends GuiEventListener
            & Renderable
            & NarratableEntry> List<T> initWidgets() {
        widgets.clear();
        HeightMode mode = VRClientSettings.getHeightMode();
        boolean auto = VRClientSettings.isHeightAuto();
        standingEntries = HeightFormat.heightEntries(120, 220);

        int buttonStartX = PANEL_X+(PANEL_W-PANEL_W/2)/2;
        widgets.add(
                optionButton(
                        VROptionWidgetType.HEIGHT_MODE,
                        buttonStartX, 32,
                        PANEL_W/2,
                        true, false
                )
        );
        widgets.add(
                optionButton(
                        VROptionWidgetType.HEIGHT_ADJUSTMENT,
                        buttonStartX, 46,
                        PANEL_W/2,
                        mode != HeightMode.REAL_SIZE,
                        false
                )
        );


        //BLOCK 2
        int sliderW = PANEL_W / 2;
        int unitsW = 18;
        int block2X = rowStartX(sliderW, unitsW);

        standingSlider = heightSlider(
                VROptionWidgetType.HEIGHT_STANDING,
                block2X, 60, sliderW,
                standingEntries, VRClientSettings.getFullHeight(), !auto
        );
        widgets.add(standingSlider);
        widgets.add(optionButton(VROptionWidgetType.HEIGHT_UNITS,
                block2X + sliderW + GAP, 60, unitsW, true, true));

        //BLOCK 3
        int autoW = 60;
        int measureW = 40;
        int block3X = rowStartX(autoW, measureW);

        widgets.add(optionButton(
                VROptionWidgetType.HEIGHT_AUTO,
                block3X, 74, autoW,
                true, false
        ));
        measureButton = new ButtonImaged(
                styledButton(block3X + autoW + GAP, 74, measureW).setText(measureLabel()),
                (it) -> ClientContext.localPlayer.getHeightTracker().startMeasure()
        );
        widgets.add(measureButton);

        return getWidgets();
    }

    @Override
    public <T extends GuiEventListener
            & Renderable
            & NarratableEntry> List<T> getWidgets() {
        List<T> list = new ArrayList<>();
        for (AbstractWidget widget : widgets) {
            list.add((T) widget);
        }
        return list;
    }

    @Override
    public void onTick() {
        if (measureButton != null) {
            measureButton.setMessage(measureLabel());
        }
        if (standingSlider != null && VRClientSettings.isHeightAuto()) {
            float height = VRClientSettings.getFullHeight();
            standingSlider.setSelectedIndex(HeightFormat.nearestIndex(standingEntries, height), false);
            standingSlider.setText(sliderLabel(VROptionWidgetType.HEIGHT_STANDING, height));
        }
    }



    private ButtonImaged optionButton(VROptionWidgetType type, int x, int y, int width,
                                      boolean active, boolean valueOnly) {
        var manager = ClientContext.settingsManager;
        ButtonImaged button = new ButtonImaged(
                styledButton(x, y, width)
                        .setTextColor(active ? AtumColor.WHITE : INACTIVE_TEXT)
                        .setTooltip(tooltip(type.getKey()))
                        .setText(Component.literal(manager.getOptionButtonName(type, valueOnly))),
                (it) -> {
                    manager.nextOptionValue(type.getKey());
                    reinit();
                }
        );
        button.active = active;
        return button;
    }

    private SliderWidget<Float> heightSlider(VROptionWidgetType type, int x, int y, int width, List<Float> entries,
                                             float value, boolean active) {
        var scale = getScreen().getScaleHelper();
        var info = new WidgetInfoSlider()
                .pos(scale.scaledX(x), scale.scaledY(y))
                .size(scale.scaledSize(width), scale.scaledSize(ROW_H))
                .setTextScale(VRClientSettings.getSettingsTextScale())
                .setBackgroundTexture(OptionTextures.GRAY_TEXTURE)
                .setKnobTexture(OptionTextures.LIGHT_GRAY_TEXTURE)
                .setKnobTextureInactive(OptionTextures.LIGHT_GRAY_TEXTURE_2)
                .highlight(OptionTextures.HOVERED_HIGHLIGHT)
                .setTextColor(active ? AtumColor.WHITE : INACTIVE_TEXT)
                .setTooltip(tooltip(type.getKey()));
        SliderWidget<Float> slider = new SliderWidget<>(info, entries, (it) -> {
            ClientContext.settingsManager.setOptionValue(type.getKey(), it.getSelected());
            it.setText(sliderLabel(type, it.getSelected()));
        });
        slider.setSelectedIndex(HeightFormat.nearestIndex(entries, value), false);
        slider.setText(sliderLabel(type, value));
        slider.active = active;
        return slider;
    }

    private WidgetInfoButtonImaged styledButton(int x, int y, int width) {
        var scale = getScreen().getScaleHelper();
        return new WidgetInfoButtonImaged()
                .pos(scale.scaledX(x), scale.scaledY(y))
                .size(scale.scaledSize(width), scale.scaledSize(ROW_H))
                .setTextScale(VRClientSettings.getSettingsTextScale())
                .setDynamicTextScale(true)
                .setDynamicTextMaxScale(VRClientSettings.getSettingsTextScale())
                .setTexture(OptionTextures.GRAY_TEXTURE)
                .setHighlightEnabled(true)
                .setHighlightHovered(OptionTextures.HOVERED_HIGHLIGHT)
                .setHighlightSelected(OptionTextures.SELECTED_HIGHLIGHT);
    }

    private static Component sliderLabel(VROptionWidgetType type, float pivot) {
        return Component.literal(
                LangHelper.getText("visor.options." + type.getKey()) + ": " + HeightFormat.format(pivot)
        );
    }

    private static Component measureLabel() {
        PlayerHeightTracker tracker = ClientContext.localPlayer.getHeightTracker();
        return tracker.isMeasuring()
                ? Component.translatable("visor.messages.height_measure_countdown", tracker.getMeasureSecondsLeft())
                : Component.translatable(LANG + "measure.button");
    }

    @Nullable
    private static Tooltip tooltip(String optionKey) {
        String lang = "visor.options." + optionKey + ".tooltip";
        if (!LangHelper.existsLangKey(lang)) {
            return null;
        }
        return Tooltip.create(Component.literal(
                LangHelper.getText(lang).replace("\n", "\u00a7r\n")
        ));
    }

    private static int rowStartX(int... widths) {
        int total = GAP * (widths.length - 1);
        for (int w : widths) {
            total += w;
        }
        return PANEL_CENTER_X - total / 2;
    }
}
