package org.vmstudio.visor.api.client.gui.widgets.color;

import lombok.Getter;
import lombok.Setter;
import org.vmstudio.visor.api.client.gui.helpers.ColorsHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;


public class ColorPickerArea extends AbstractWidget {

    private final Runnable responder;

    @Setter
    private float hue;

    @Getter
    private float saturation;

    @Getter
    private float value;

    public ColorPickerArea(int x, int y,
                           int width, int height,
                           @NotNull Runnable responder) {
        super(x, y, width, height, Component.empty());
        this.responder = responder;
    }

    public void setSaturationValue(float saturation, float value) {
        this.saturation = Mth.clamp(saturation, 0f, 1f);
        this.value = Mth.clamp(value, 0f, 1f);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics,
                                int mouseX, int mouseY,
                                float partialTick) {
        int x = getX();
        int y = getY();
        int width = getWidth();
        int height = getHeight();
        if (width <= 0 || height <= 0) return;

        for (int column = 0; column < width; column++) {
            float columnSaturation = width == 1
                    ? 0f
                    : column / (float) (width - 1);
            guiGraphics.fillGradient(
                    x + column, y,
                    x + column + 1, y + height,
                    ColorsHelper.hsvToArgb(hue, columnSaturation, 1f, 255),
                    0xFF000000
            );
        }

        ColorsHelper.drawBorder(guiGraphics, x, y, width, height, 0xFF000000);

        int markerX = x + Math.round(saturation * (width - 1));
        int markerY = y + Math.round((1f - value) * (height - 1));
        drawMarker(guiGraphics, markerX, markerY);
    }

    private void drawMarker(GuiGraphics guiGraphics, int centerX, int centerY) {
        int radius = 3;
        int left = Mth.clamp(centerX - radius, getX(), getX() + getWidth() - 1);
        int top = Mth.clamp(centerY - radius, getY(), getY() + getHeight() - 1);
        int right = Mth.clamp(centerX + radius + 1, getX() + 1, getX() + getWidth());
        int bottom = Mth.clamp(centerY + radius + 1, getY() + 1, getY() + getHeight());

        guiGraphics.fill(left, top, right, top + 1, 0xFF000000);
        guiGraphics.fill(left, bottom - 1, right, bottom, 0xFF000000);
        guiGraphics.fill(left, top, left + 1, bottom, 0xFF000000);
        guiGraphics.fill(right - 1, top, right, bottom, 0xFF000000);

        if (right - left <= 2 || bottom - top <= 2) return;

        guiGraphics.fill(left + 1, top + 1, right - 1, top + 2, 0xFFFFFFFF);
        guiGraphics.fill(left + 1, bottom - 2, right - 1, bottom - 1, 0xFFFFFFFF);
        guiGraphics.fill(left + 1, top + 1, left + 2, bottom - 1, 0xFFFFFFFF);
        guiGraphics.fill(right - 2, top + 1, right - 1, bottom - 1, 0xFFFFFFFF);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        selectFromMouse(mouseX, mouseY);
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
        selectFromMouse(mouseX, mouseY);
    }


    private void selectFromMouse(double mouseX, double mouseY) {
        int width = getWidth();
        int height = getHeight();
        if (width <= 1 || height <= 1) return;

        float newSaturation = (float) ((mouseX - getX()) / (width - 1));
        float newValue = 1f - (float) ((mouseY - getY()) / (height - 1));

        float clampedSaturation = Mth.clamp(newSaturation, 0f, 1f);
        float clampedValue = Mth.clamp(newValue, 0f, 1f);

        if (clampedSaturation == saturation && clampedValue == value) {
            return;
        }
        this.saturation = clampedSaturation;
        this.value = clampedValue;
        responder.run();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }
}
