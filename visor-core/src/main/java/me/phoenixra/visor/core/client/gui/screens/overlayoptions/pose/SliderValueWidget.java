package me.phoenixra.visor.core.client.gui.screens.overlayoptions.pose;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class SliderValueWidget extends AbstractWidget {
    // The current slider value.
    @Setter
    @Getter
    private float value;
    // Optional bounds: if both are non-null, the slider wraps around.
    @Nullable
    private Float minValue;
    @Nullable
    private Float maxValue;
    // Snap increment for the value.
    @Setter
    private float snapIncrement;
    // Number of pixels required for one drag step.
    @Setter
    private float pixelsPerStep;
    // Accumulates drag movement in pixels.
    private double accumulatedDelta = 0;

    // Fields to track dragging using the mouse position.
    private boolean dragging = false;
    // Holds the last mouse X position.
    private double lastMouseX = 0;
    // Time when the last significant value change occurred.
    private long lastValueChangeTime = 0;

    // Minimal movement (in pixels) required to update the active drag direction.
    private double minMovementThreshold;

    // New: if no value change occurs within this time (ms), the active arrow animation will start to fade out.
    private long animationDisableTimeout;
    // New: duration (ms) over which the arrow animation fades back to normal.
    private long animationFadeDuration;

    // Track the active drag direction.
    private DragDirection activeDragDirection = DragDirection.NONE;
    private enum DragDirection { INCREASE, DECREASE, NONE }

    @Setter
    private Consumer<Float> onValueChange;

    @Setter
    private Consumer<Float> onStartDragging;

    @Setter
    private Consumer<Float> onRelease;
    /**
     * Creates a new slider widget.
     *
     * @param x            The x position.
     * @param y            The y position.
     * @param width        The width of the widget.
     * @param height       The height of the widget.
     * @param message      The message (used for narration).
     * @param initialValue The starting value.
     * @param minValue     The minimum value (set null for no bounds).
     * @param maxValue     The maximum value (set null for no bounds).
     */
    public SliderValueWidget(int x, int y, int width, int height, Component message,
                        float initialValue, @Nullable Float minValue, @Nullable Float maxValue) {
        super(x, y, width, height, message);
        this.value = initialValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.snapIncrement = 0.1f;
        this.pixelsPerStep = 2.0f;  // Default: 10 pixels per step.
        this.minMovementThreshold = 2.0;  // Default minimal movement threshold.

        this.animationDisableTimeout = 1000;  // Default: begin fade after 500ms of no value change.
        this.animationFadeDuration = 300;    // Default fade duration: 300ms.
    }


    @Override
    public void onClick(double mouseX, double mouseY) {
        this.dragging = true;
        this.lastMouseX = mouseX;
        this.accumulatedDelta = 0;
        this.lastValueChangeTime = System.currentTimeMillis();
        if(onStartDragging != null){
            onStartDragging.accept(value);
        }
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        this.dragging = false;
        if(onRelease != null) {
            onRelease.accept(value);
        }
    }

    /**
     * Called continuously while the mouse is dragged.
     * Uses the difference between the current mouse X and the last recorded position.
     * Accumulates the pixel delta until it exceeds the configured pixelsPerStep,
     * then updates the slider's value in discrete steps.
     */
    @Override
    protected void onDrag(double mouseX, double mouseY, double ignored1, double ignored2) {
        if (!dragging) return;

        double deltaX = mouseX - lastMouseX;
        lastMouseX = mouseX;
        long currentTime = System.currentTimeMillis();

        // Update active drag direction only if movement exceeds threshold.
        if (Math.abs(deltaX) >= minMovementThreshold) {
            activeDragDirection = (deltaX > 0) ? DragDirection.INCREASE : DragDirection.DECREASE;
        }
        // Accumulate movement.
        accumulatedDelta += deltaX;
        if (Math.abs(accumulatedDelta) >= pixelsPerStep) {
            int stepCount = (int)(accumulatedDelta / pixelsPerStep);
            float deltaValue = stepCount * snapIncrement;
            this.value += deltaValue;
            accumulatedDelta -= stepCount * pixelsPerStep;
            // Update lastValueChangeTime on actual value change.
            lastValueChangeTime = currentTime;
            // If bounds are set, wrap around.
            if (minValue != null && maxValue != null) {
                float min = minValue;
                float max = maxValue;
                float range = max - min;
                if (range > 0) {
                    this.value = (this.value - min) % range;
                    if (this.value < 0) this.value += range;
                    this.value += min;
                }
            }
            if(onValueChange != null){
                onValueChange.accept(value);
            }
        }
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {

        Font font = Minecraft.getInstance().font;
        int x = getX(), y = getY(), width = getWidth(), height = getHeight();

        // Base background color and neutral highlight color.
        int baseColor = 0xFF555555;
        int highlightColor = 0xFF777777;

        // If dragging and an active direction is set, apply a gradient shift on that side.
        if (dragging && activeDragDirection != DragDirection.NONE) {
            float time = (float)(System.currentTimeMillis() % 1000L) / 1000.0f;
            float pulse = 0.5f + 0.5f * Mth.sin(time * (float)Math.PI * 2.0f);
            int gradColor = lerpColor(baseColor, highlightColor, pulse);
            if (activeDragDirection == DragDirection.INCREASE) {
                guiGraphics.fill(x, y, x + width, y + height, baseColor);
                guiGraphics.fill(x + width / 2, y, x + width, y + height, gradColor);
            } else {
                guiGraphics.fill(x, y, x + width / 2, y + height, gradColor);
                guiGraphics.fill(x + width / 2, y, x + width, y + height, baseColor);
            }
        } else {
            guiGraphics.fill(x, y, x + width, y + height, baseColor);
        }

        // Arrow definitions.
        String leftArrow = "<";
        String rightArrow = ">";
        int arrowColor = 0xFFFFFFFF;
        int arrowMargin = 4;
        int arrowY = y + (height - font.lineHeight) / 2;

        // Determine the current fade factor for the active arrow.
        // If no value change occurs for longer than animationDisableTimeout, fade out over animationFadeDuration.
        long elapsed = System.currentTimeMillis() - lastValueChangeTime;
        float fadeFactor = 1.0f;
        if (elapsed > animationDisableTimeout) {
            fadeFactor = 1.0f - Math.min((elapsed - animationDisableTimeout) / (float)animationFadeDuration, 1.0f);
        }
        // When fadeFactor is 0, we draw the arrow normally (non-animated).

        // Animation amplitude (scale increase).
        float amplitude = 0.2f;

        // Render left arrow: animate only if active drag direction is DECREASE.
        if (dragging && activeDragDirection == DragDirection.DECREASE && fadeFactor > 0) {
            float scaleAnim = 1.0f + amplitude * fadeFactor * (0.5f + 0.5f * Mth.sin((float)(System.currentTimeMillis() % 1000L) / 1000.0f * (float)Math.PI * 2.0f));
            guiGraphics.pose().pushPose();
            int leftArrowWidth = font.width(leftArrow);
            int leftArrowX = x + arrowMargin;
            guiGraphics.pose().translate(leftArrowX + leftArrowWidth / 2.0, arrowY + font.lineHeight / 2.0, 0);
            guiGraphics.pose().scale(scaleAnim, scaleAnim, scaleAnim);
            guiGraphics.pose().translate(-(leftArrowX + leftArrowWidth / 2.0), -(arrowY + font.lineHeight / 2.0), 0);
            guiGraphics.drawString(font, leftArrow, leftArrowX, arrowY, arrowColor);
            guiGraphics.pose().popPose();
        } else {
            guiGraphics.drawString(font, leftArrow, x + arrowMargin, arrowY, arrowColor);
        }

        // Render right arrow: animate only if active drag direction is INCREASE.
        if (dragging && activeDragDirection == DragDirection.INCREASE && fadeFactor > 0) {
            float scaleAnim = 1.0f + amplitude * fadeFactor * (0.5f + 0.5f * Mth.sin((float)(System.currentTimeMillis() % 1000L) / 1000.0f * (float)Math.PI * 2.0f));
            guiGraphics.pose().pushPose();
            int rightArrowWidth = font.width(rightArrow);
            int rightArrowX = x + width - arrowMargin - rightArrowWidth;
            guiGraphics.pose().translate(rightArrowX + rightArrowWidth / 2.0, arrowY + font.lineHeight / 2.0, 0);
            guiGraphics.pose().scale(scaleAnim, scaleAnim, scaleAnim);
            guiGraphics.pose().translate(-(rightArrowX + rightArrowWidth / 2.0), -(arrowY + font.lineHeight / 2.0), 0);
            guiGraphics.drawString(font, rightArrow, rightArrowX, arrowY, arrowColor);
            guiGraphics.pose().popPose();
        } else {
            guiGraphics.drawString(font, rightArrow, x + width - arrowMargin - font.width(rightArrow), arrowY, arrowColor);
        }

        if(fadeFactor == 0) {
            onRelease(0,0);
        }
        // Draw the current value in the center.
        String valueText = String.format("%.3f", value);
        int textWidth = font.width(valueText);
        int textX = x + (width - textWidth) / 2;
        guiGraphics.drawString(font, valueText, textX, arrowY, arrowColor);
    }

    /**
     * Linearly interpolates between two ARGB colors.
     */
    private int lerpColor(int color1, int color2, float t) {
        int a1 = (color1 >> 24) & 0xFF;
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;
        int a2 = (color2 >> 24) & 0xFF;
        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;
        int a = (int) (a1 + (a2 - a1) * t);
        int r = (int) (r1 + (r2 - r1) * t);
        int g = (int) (g1 + (g2 - g1) * t);
        int b = (int) (b1 + (b2 - b1) * t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, Component.literal("Slider: " + value));
        narrationElementOutput.add(NarratedElementType.USAGE, Component.literal("Drag left/right to change the value."));
    }

    public static Builder builder(){
        return new Builder();
    }

    /**
     * A Builder for constructing SliderWidget instances.
     */
    public static class Builder {
        private int x = 0;
        private int y = 0;
        private int width = 150;
        private int height = 20;

        @Setter @Accessors(chain = true)
        private Component message = Component.literal("Slider");

        @Setter @Accessors(chain = true)
        private float initialValue = 0.0f;

        @Nullable @Setter @Accessors(chain = true)
        private Float minValue = null;

        @Nullable @Setter @Accessors(chain = true)
        private Float maxValue = null;

        @Setter @Accessors(chain = true)
        private float snapIncrement = 0.1f;

        @Setter @Accessors(chain = true)
        private Tooltip tooltip;

        @Setter @Accessors(chain = true)
        private Consumer<Float> onStartDragging;

        @Setter @Accessors(chain = true)
        private Consumer<Float> onValueChange;

        @Setter @Accessors(chain = true)
        private Consumer<Float> onRelease;

        public Builder pos(int x, int y){
            this.x = x;
            this.y = y;
            return this;
        }
        public Builder size(int width, int height){
            this.width = width;
            this.height = height;
            return this;
        }


        public SliderValueWidget build() {
            SliderValueWidget slider = new SliderValueWidget(x, y, width, height, message, initialValue, minValue, maxValue);
            slider.setSnapIncrement(snapIncrement);
            slider.setTooltip(tooltip);
            slider.setOnValueChange(onValueChange);
            slider.setOnStartDragging(onStartDragging);
            slider.setOnRelease(onRelease);
            return slider;
        }
    }
}
