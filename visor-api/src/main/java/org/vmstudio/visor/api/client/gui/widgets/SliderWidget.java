package org.vmstudio.visor.api.client.gui.widgets;

import lombok.Getter;
import lombok.Setter;
import org.vmstudio.visor.api.client.gui.GuiTexture;
import org.vmstudio.visor.api.client.gui.helpers.GuiHelper;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoSlider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class SliderWidget<T> extends AbstractWidget {

    @Getter
    private final WidgetInfoSlider widgetInfo;

    private final List<T> entries;

    private final Consumer<SliderWidget<T>> responder;

    private int knobX, knobY, knobWidth, knobHeight;

    @Getter @Setter
    private Component text;

    @Getter
    private int index = -1;
    @Getter
    private T selected;



    public SliderWidget(@NotNull WidgetInfoSlider widgetInfo,
                        @NotNull List<T> entries,
                        @Nullable Consumer<SliderWidget<T>> responder) {
        super(widgetInfo.getX(), widgetInfo.getY(),
                widgetInfo.getWidth(), widgetInfo.getHeight(),
                Component.empty()
        );
        if (entries.size() <= 1) {
            throw new IllegalArgumentException("entries list of slider widget cannot be empty or size less than 2");
        }
        this.widgetInfo = widgetInfo;
        this.entries = List.copyOf(entries);
        this.responder = responder != null ? responder : it -> {};
        this.knobY = getY();
        this.knobWidth = widgetInfo.getKnobWidth();
        this.knobHeight = getHeight();

        setTooltip(widgetInfo.getTooltip());

        setSelectedIndex(0, false);

    }


    public void setSelectedIndex(int newIndex, boolean callResponder) {
        int clamped = clampIndex(newIndex);
        if (clamped != this.index) {
            this.index = clamped;
            this.selected = entries.get(this.index);
            if(callResponder) {
                this.responder.accept(this);
            }
        }
    }
    public void setSelected(@Nullable T value, boolean callResponder) {
        setSelectedIndex(entries.indexOf(value), callResponder);
    }


    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Sync widgetInfo position and size for highlight drawing
        widgetInfo.pos(getX(), getY()).size(getWidth(), getHeight());
        repositionKnob();

        // Draw background
        GuiTexture bg = widgetInfo.getBackgroundTexture();
        if (bg != null) {
            bg.blit(guiGraphics, getX(), getY(), getWidth(), getHeight());
        }


        // Hover state for knob (not the whole widget)
        boolean knobHovered = this.active
                && mouseX >= knobX && mouseX < knobX + knobWidth
                && mouseY >= knobY && mouseY < knobY + knobHeight;

        // Choose knob texture
        GuiTexture knob = resolveKnobTexture(knobHovered);
        if (knob != null) {
            knob.blit(guiGraphics, knobX, knobY, knobWidth, knobHeight);
        }

        // Optional knob highlight
        widgetInfo.drawHighlight(guiGraphics, knobX, knobY, knobWidth, knobHeight, this.active, knobHovered);

        // Draw label or selected text (centered)
        String text = resolveDisplayText();
        int textX = getX();
        int textY = getY();
        int textW = getWidth();
        int textH = getHeight();
        if (!text.isEmpty()) {
            Font font = Minecraft.getInstance().font;
            int color = widgetInfo.getTextColor().asInt();

            if (widgetInfo.isDynamicTextScale()) {
                GuiHelper.renderScalableText(
                        guiGraphics, font, text, color,
                        textX, textY, textW, textH,
                        widgetInfo.getDynamicTextMaxScale(),
                        true
                );
                return;
            }

            GuiHelper.renderScrollableText(
                    guiGraphics, font, text, color,
                    textX, textY, textW, textH,
                    widgetInfo.getTextScale(),
                    true
            );
        }
    }

    private GuiTexture resolveKnobTexture(boolean knobHovered) {
        if (!this.active) {
            return widgetInfo.getKnobTextureInactive() != null
                    ? widgetInfo.getKnobTextureInactive()
                    : widgetInfo.getKnobTexture();
        }
        if (knobHovered && widgetInfo.getKnobTextureHovered() != null) {
            return widgetInfo.getKnobTextureHovered();
        }
        return widgetInfo.getKnobTexture();
    }


    private void repositionKnob() {
        double normalized = (double) index / (double) (entries.size() - 1);
        int left = getX();
        int right = getX() + getWidth() - knobWidth;
        this.knobX = left + (int) Math.round(normalized * (right - left));
        this.knobY = getY();
        this.knobWidth = widgetInfo.getKnobWidth();
        this.knobHeight = getHeight();
    }

    private int clampIndex(int idx) {
        return Mth.clamp(idx, 0, Math.max(0, entries.size() - 1));
    }

    private String resolveDisplayText() {
        if (text != null && !text.getString().isEmpty()) {
            return text.getString();
        }
        if (selected == null) return "";
        String s = String.valueOf(selected);
        return s != null ? s : "";
    }

    private void selectFromMouse(double mouseX) {
        // Map mouseX to [0,1] using the knob's center, then to the nearest entry index
        double start = getX() + (knobWidth * 0.5);
        double end = getX() + getWidth() - (knobWidth * 0.5);
        double span = end - start;

        if (span <= 0) {
            setSelectedIndex(0, true);
            return;
        }

        double t = (mouseX - start) / span;
        t = Mth.clamp(t, 0.0, 1.0);

        double scaled = t * (entries.size() - 1);
        int nearest = (int) Math.round(scaled);

        setSelectedIndex(nearest, true);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        selectFromMouse(mouseX);
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
        super.onDrag(mouseX, mouseY, dragX, dragY);
        selectFromMouse(mouseX);
    }



    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!this.active) return false;

        // Left arrow
        if (keyCode == 263) {
            setSelectedIndex(this.index - 1, true);
            return true;
        }
        // Right arrow
        if (keyCode == 262) {
            setSelectedIndex(this.index + 1, true);
            return true;
        }
        // Home
        if (keyCode == 268) {
            setSelectedIndex(0, true);
            return true;
        }
        // End
        if (keyCode == 269) {
            setSelectedIndex(entries.size() - 1, true);
            return true;
        }
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        String text = resolveDisplayText();
        Component title = Component.translatable("gui.narrate.slider", Component.literal(text));
        narrationElementOutput.add(NarratedElementType.TITLE, title);

        if (this.active) {
            if (this.isFocused()) {
                narrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.slider.usage.focused"));
            } else {
                narrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.slider.usage.hovered"));
            }
        }
    }
}