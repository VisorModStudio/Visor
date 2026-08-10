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


public class ColorPickerStrip extends AbstractWidget {

    private static final int HUE_SEGMENTS = 6;

    private final Mode mode;

    private final boolean vertical;

    private final Runnable responder;


    @Getter
    private float progress;

    @Setter
    private int baseColorArgb = 0xFFFFFFFF;

    public ColorPickerStrip(int x, int y,
                            int width, int height,
                            @NotNull Mode mode,
                            boolean vertical,
                            @NotNull Runnable responder) {
        super(x, y, width, height, Component.empty());
        this.mode = mode;
        this.vertical = vertical;
        this.responder = responder;
    }


    public void setProgress(float progress) {
        this.progress = Mth.clamp(progress, 0f, 1f);
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

        if (mode == Mode.HUE) {
            renderHue(guiGraphics, x, y, width, height);
        } else {
            renderAlpha(guiGraphics, x, y, width, height);
        }

        ColorsHelper.drawBorder(guiGraphics, x, y, width, height, 0xFF000000);
        drawKnob(guiGraphics, x, y, width, height);
    }

    private void renderHue(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        int span = vertical ? height : width;

        for (int segment = 0; segment < HUE_SEGMENTS; segment++) {
            int from = segmentOffset(span, segment, HUE_SEGMENTS);
            int to = segmentOffset(span, segment + 1, HUE_SEGMENTS);
            if (to <= from) continue;

            int colorFrom = ColorsHelper.hsvToArgb(segment / (float) HUE_SEGMENTS, 1f, 1f, 255);
            int colorTo = ColorsHelper.hsvToArgb((segment + 1) / (float) HUE_SEGMENTS, 1f, 1f, 255);

            if (vertical) {
                guiGraphics.fillGradient(x, y + from, x + width, y + to, colorFrom, colorTo);
                continue;
            }
            for (int column = from; column < to; column++) {
                float local = (column - from) / (float) (to - from);
                int color = ColorsHelper.hsvToArgb(
                        (segment + local) / (float) HUE_SEGMENTS, 1f, 1f, 255
                );
                guiGraphics.fill(x + column, y, x + column + 1, y + height, color);
            }
        }
    }

    private void renderAlpha(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        ColorsHelper.drawTransparencyChecker(guiGraphics, x, y, width, height);

        int rgb = baseColorArgb & 0x00FFFFFF;
        int span = vertical ? height : width;
        if (span <= 0) return;

        for (int step = 0; step < span; step++) {
            int alpha = Math.round((step / (float) Math.max(1, span - 1)) * 255f);
            int color = (alpha << 24) | rgb;
            if (vertical) {
                guiGraphics.fill(x, y + step, x + width, y + step + 1, color);
                continue;
            }
            guiGraphics.fill(x + step, y, x + step + 1, y + height, color);
        }
    }

    private void drawKnob(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        int span = vertical ? height : width;
        if (span <= 0) return;

        int offset = Math.round(progress * (span - 1));

        if (vertical) {
            int knobY = y + offset;
            guiGraphics.fill(x - 1, knobY - 1, x + width + 1, knobY + 2, 0xFF000000);
            guiGraphics.fill(x, knobY, x + width, knobY + 1, 0xFFFFFFFF);
            return;
        }
        int knobX = x + offset;
        guiGraphics.fill(knobX - 1, y - 1, knobX + 2, y + height + 1, 0xFF000000);
        guiGraphics.fill(knobX, y, knobX + 1, y + height, 0xFFFFFFFF);
    }

    private static int segmentOffset(int span, int segment, int segmentCount) {
        return Math.round(span * (segment / (float) segmentCount));
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
        int span = vertical ? getHeight() : getWidth();
        if (span <= 1) return;

        double local = vertical
                ? mouseY - getY()
                : mouseX - getX();

        float newProgress = Mth.clamp((float) (local / (span - 1)), 0f, 1f);
        if (newProgress == progress) return;

        this.progress = newProgress;
        responder.run();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }

    public enum Mode {
        HUE
        //Add Alpha maybe? Or we don't need it,
        // alpha is bad in combination with depth
    }
}
