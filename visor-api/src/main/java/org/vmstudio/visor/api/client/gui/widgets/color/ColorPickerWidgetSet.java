package org.vmstudio.visor.api.client.gui.widgets.color;

import org.vmstudio.visor.api.compatibility.mcversion.McVersionUtilsClient;

import lombok.Getter;
import me.phoenixra.atumvr.api.misc.color.AtumColor;
import org.vmstudio.visor.api.client.gui.helpers.ColorsHelper;
import org.vmstudio.visor.api.client.gui.overlays.options.OptionTextures;
import org.vmstudio.visor.api.client.gui.widgets.ButtonImaged;
import org.vmstudio.visor.api.client.gui.widgets.EditBoxImaged;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoButtonImaged;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoEditBox;
import org.vmstudio.visor.api.client.gui.widgets.sets.WidgetSet;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ColorPickerWidgetSet implements WidgetSet {

    private static final int STRIP_THICKNESS = 10;
    private static final int GAP = 4;
    private static final int AREA_HEIGHT = 78;
    private static final int FIELD_HEIGHT = 16;

    private static final int ALPHA_OPAQUE = 255;
    private static final int ALPHA_TRANSPARENT = 0;


    private final int x, y, width;

    private final boolean allowTransparency;

    private final Consumer<AtumColor> responder;

    private final int toggleY;
    private final int fieldY;
    private final int presetsY;


    @Getter
    private final int height;

    private ColorPickerArea area;
    private ColorPickerStrip hueStrip;
    private ButtonImaged transparentToggle;
    private EditBoxImaged hexField;
    private final List<ColorSampleButton> presetSwatches = new ArrayList<>();

    private float hue;
    private float saturation;
    private float value;
    private int alpha = ALPHA_OPAQUE;


    private boolean syncing;

    public ColorPickerWidgetSet(int x, int y, int width,
                                @NotNull AtumColor initial,
                                boolean allowTransparency,
                                @NotNull Consumer<AtumColor> responder) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.allowTransparency = allowTransparency;
        this.responder = responder;

        applyColorToState(initial);

        this.toggleY = y + AREA_HEIGHT + GAP;
        this.fieldY = allowTransparency
                ? toggleY + FIELD_HEIGHT + GAP
                : y + AREA_HEIGHT + GAP;
        this.presetsY = fieldY + FIELD_HEIGHT + GAP;

        this.height = presetsY - y;
    }

    @Override
    public <T extends GuiEventListener
            & Renderable
            & NarratableEntry> List<T> initWidgets() {

        int areaWidth = width - STRIP_THICKNESS - GAP;

        area = new ColorPickerArea(
                x, y,
                areaWidth, AREA_HEIGHT,
                this::onAreaChanged
        );

        hueStrip = new ColorPickerStrip(
                x + areaWidth + GAP, y,
                STRIP_THICKNESS, AREA_HEIGHT,
                ColorPickerStrip.Mode.HUE,
                true,
                this::onHueChanged
        );

        if (allowTransparency) {
            transparentToggle = new ButtonImaged(
                    new WidgetInfoButtonImaged()
                            .pos(x, toggleY)
                            .size(width, FIELD_HEIGHT)
                            .setTexture(OptionTextures.GRAY_TEXTURE)
                            .setText(transparentText())
                            .highlight(
                                    OptionTextures.HOVERED_HIGHLIGHT,
                                    OptionTextures.SELECTED_HIGHLIGHT
                            ),
                    button -> {
                        this.alpha = isTransparent() ? ALPHA_OPAQUE : ALPHA_TRANSPARENT;
                        onStateChanged(false);
                    }
            );
        }

        hexField = new EditBoxImaged(
                new WidgetInfoEditBox()
                        .pos(x + FIELD_HEIGHT + GAP, fieldY)
                        .size(width - (FIELD_HEIGHT + GAP), FIELD_HEIGHT)
                        .setTexture(OptionTextures.GRAY_TEXTURE)
                        .setHint(Component.translatable("visor.widgets.color_picker.hex"))
                        .setFilter(s -> s.matches("#?[0-9a-fA-F]{0,6}"))
        );
        hexField.setMaxLength(7);
        hexField.setValue(getColor().asHex(false));
        hexField.moveCursorToStart(false);
        hexField.setResponder(this::onHexTyped);

        presetSwatches.clear();

        pushStateToWidgets();

        return getWidgets();
    }

    @Override
    public <T extends GuiEventListener
            & Renderable
            & NarratableEntry> List<T> getWidgets() {
        List<T> widgets = new ArrayList<>();
        widgets.add((T) area);
        widgets.add((T) hueStrip);
        if (transparentToggle != null) {
            widgets.add((T) transparentToggle);
        }
        widgets.add((T) hexField);
        presetSwatches.forEach(it -> widgets.add((T) it));
        return widgets;
    }

    @Override
    public void onTick() {
    }

    @Override
    public void onPreRender(@NotNull GuiGraphics guiGraphics,
                            int mouseX, int mouseY,
                            float partialTicks) {
        drawPreviewSwatch(guiGraphics);
    }


    // ----- PUBLIC -----
    public @NotNull AtumColor getColor() {
        int argb = ColorsHelper.hsvToArgb(hue, saturation, value, alpha);
        return AtumColor.immutable(
                (argb >> 16) & 0xFF,
                (argb >> 8) & 0xFF,
                argb & 0xFF,
                alpha
        );
    }

    public void setColor(@NotNull AtumColor color) {
        applyColorToState(color);
        pushStateToWidgets();
        updateHexField();
    }

    public boolean isTransparent() {
        return alpha == ALPHA_TRANSPARENT;
    }
    // ----------


    //----- Events -----
    private void onAreaChanged() {
        this.saturation = area.getSaturation();
        this.value = area.getValue();
        onStateChanged(true);
    }

    private void onHueChanged() {
        this.hue = hueStrip.getProgress();
        onStateChanged(true);
    }

    private void onStateChanged(boolean updateHexField) {
        pushStateToWidgets();
        if (updateHexField) {
            updateHexField();
        }
        responder.accept(getColor());
    }

    private void onPresetPicked(@NotNull AtumColor preset) {
        float[] hsv = ColorsHelper.toHsv(preset);
        this.hue = hsv[0];
        this.saturation = hsv[1];
        this.value = hsv[2];
        onStateChanged(true);
    }
    //----------

    private void applyColorToState(@NotNull AtumColor color) {
        float[] hsv = ColorsHelper.toHsv(color);
        this.hue = hsv[0];
        this.saturation = hsv[1];
        this.value = hsv[2];
        this.alpha = allowTransparency && color.getAlphaInt() == ALPHA_TRANSPARENT
                ? ALPHA_TRANSPARENT
                : ALPHA_OPAQUE;
    }


    private Component transparentText() {
        return Component.translatable(
                "visor.widgets.color_picker.transparent",
                Component.translatable(isTransparent()
                        ? "visor.widgets.color_picker.transparent.on"
                        : "visor.widgets.color_picker.transparent.off")
        );
    }


    private void onHexTyped(String text) {
        if (syncing) return;

        AtumColor parsed = ColorsHelper.parseHex(text, alpha);
        if (parsed == null) return;

        float[] hsv = ColorsHelper.toHsv(parsed);
        this.hue = hsv[0];
        this.saturation = hsv[1];
        this.value = hsv[2];
        onStateChanged(false);
    }


    private void pushStateToWidgets() {
        if (area != null) {
            area.setHue(hue);
            area.setSaturationValue(saturation, value);
        }
        if (hueStrip != null) {
            hueStrip.setProgress(hue);
        }
        if (transparentToggle != null) {
            transparentToggle.setMessage(transparentText());
        }
    }

    private void updateHexField() {
        if (hexField == null) return;

        String hex = getColor().asHex(false);
        if (hex.equalsIgnoreCase(hexField.getValue())) {
            return;
        }
        syncing = true;
        try {
            hexField.setValue(hex);
        } finally {
            syncing = false;
        }
    }


    private void drawPreviewSwatch(@NotNull GuiGraphics guiGraphics) {
        AtumColor color = getColor();
        if (color.getAlphaInt() < ALPHA_OPAQUE) {
            ColorsHelper.drawTransparencyChecker(
                    guiGraphics, x, fieldY, FIELD_HEIGHT, FIELD_HEIGHT
            );
        }
        guiGraphics.fill(
                x, fieldY,
                x + FIELD_HEIGHT, fieldY + FIELD_HEIGHT,
                color.asInt()
        );
        ColorsHelper.drawBorder(
                guiGraphics, x, fieldY, FIELD_HEIGHT, FIELD_HEIGHT, 0xFF000000
        );
    }

}
