package org.vmstudio.visor.api.client.gui.widgets.info;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import me.phoenixra.atumvr.api.misc.color.AtumColor;
import org.vmstudio.visor.api.client.gui.GuiTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import org.jetbrains.annotations.NotNull;



public class WidgetInfoSlider extends WidgetInfo{

    @Setter @Getter
    @Accessors(chain = true)
    private GuiTexture backgroundTexture;

    @Setter @Getter
    @Accessors(chain = true)
    private GuiTexture knobTexture;

    @Setter @Getter
    @Accessors(chain = true)
    private GuiTexture knobTextureHovered;

    @Setter @Getter
    @Accessors(chain = true)
    private GuiTexture knobTextureInactive;

    @Setter @Getter
    @Accessors(chain = true)
    private boolean knobHighlightEnabled = false;
    @Setter @Getter
    @Accessors(chain = true)
    private boolean knobHighlightCorners = true;
    @Getter
    @Accessors(chain = true)
    private AtumColor knobHighlightHovered = AtumColor.WHITE;

    @Setter @Getter
    @Accessors(chain = true)
    private float knobHighlightThickness = 0.65f;

    @Setter @Getter
    @Accessors(chain = true)
    private int knobWidth = 6;

    @Setter @Getter
    @Accessors(chain = true)
    private Font textFont = Minecraft.getInstance().font;

    @Setter @Getter
    @Accessors(chain = true)
    private AtumColor textColor = AtumColor.WHITE;

    /**
     * If text should be scaled to fit its bounds.
     * <p>
     *     When true, {@link #textScale} will be ignored
     * </p>
     */
    @Setter @Getter
    @Accessors(chain = true)
    private boolean dynamicTextScale = false;

    /**
     * Max scale for the dynamic scaled text
     */
    @Setter @Getter
    @Accessors(chain = true)
    private float dynamicTextMaxScale = 1.0f;

    /**
     * Text scale to use
     */
    @Setter @Getter
    @Accessors(chain = true)
    private float textScale = 1.0f;

    @Setter @Getter
    @Accessors(chain = true)
    private Tooltip tooltip;


    private int knobHighlightHoveredInt = AtumColor.WHITE.asInt();

    private int knobHighlightCornerHoveredInt = AtumColor.WHITE.asInt();

    public WidgetInfoSlider(@NotNull WidgetInfoSlider copyFrom) {
        super(copyFrom);
        backgroundTexture = copyFrom.backgroundTexture;
        knobTexture = copyFrom.knobTexture;
        knobTextureHovered = copyFrom.knobTextureHovered;
        knobTextureInactive = copyFrom.knobTextureInactive;
        knobHighlightEnabled = copyFrom.knobHighlightEnabled;
        knobHighlightCorners = copyFrom.knobHighlightCorners;
        knobHighlightHovered = copyFrom.knobHighlightHovered;
        knobHighlightThickness = copyFrom.knobHighlightThickness;
        knobHighlightHoveredInt = copyFrom.knobHighlightHoveredInt;
        knobHighlightCornerHoveredInt = copyFrom.knobHighlightCornerHoveredInt;
        knobWidth = copyFrom.knobWidth;
        textFont = copyFrom.textFont;
        textColor = copyFrom.textColor;
        dynamicTextScale = copyFrom.dynamicTextScale;
        tooltip = copyFrom.tooltip;
    }

    public WidgetInfoSlider() {

    }

    public WidgetInfoSlider textures(GuiTexture background,
                                     GuiTexture knob,
                                     GuiTexture knobHovered,
                                     GuiTexture knobInactive){
        return setBackgroundTexture(background)
                .setKnobTexture(knob)
                .setKnobTextureHovered(knobHovered)
                .setKnobTextureInactive(knobInactive);

    }
    public WidgetInfoSlider textures(GuiTexture background,
                                     GuiTexture knob,
                                     GuiTexture knobHovered){
        return setBackgroundTexture(background)
                .setKnobTexture(knob)
                .setKnobTextureHovered(knobHovered)
                .setKnobTextureInactive(knob);

    }
    public WidgetInfoSlider highlight(AtumColor hovered,
                                      float thickness){
        return setKnobHighlightEnabled(true)
                .setKnobHighlightHovered(hovered)
                .setKnobHighlightThickness(thickness);
    }
    public WidgetInfoSlider highlight(AtumColor hovered){
        return setKnobHighlightEnabled(true)
                .setKnobHighlightHovered(hovered);
    }

    public WidgetInfoSlider setKnobHighlightHovered(AtumColor color) {
        this.knobHighlightHovered = color;
        this.knobHighlightHoveredInt = color.asInt();
        this.knobHighlightCornerHoveredInt = color.lighten(0.25f).asInt();
        return this;
    }


    @Override
    public WidgetInfoSlider pos(int x, int y) {
        return (WidgetInfoSlider) super.pos(x, y);
    }

    @Override
    public WidgetInfoSlider size(int width, int height) {
        return (WidgetInfoSlider) super.size(width, height);
    }



    public void drawHighlight(GuiGraphics guiGraphics,
                              int x, int y, int width, int height,
                              boolean active,
                              boolean hovered) {
        if (!knobHighlightEnabled) return;
        if (!hovered || !active) return;

        // Choose base color based on state
        int baseColor;
        int cornerColor;

        baseColor = knobHighlightHoveredInt;
        cornerColor = knobHighlightCornerHoveredInt;



        float s = Math.max(0f, knobHighlightThickness);
        if (s <= 0f) return;


        float x0 = x;
        float y0 = y;
        float x1 = x + width;
        float y1 = y + height;

        float innerW = Math.max(0f, x1 - x0);
        float innerH = Math.max(0f, y1 - y0);
        if (innerW <= 0f || innerH <= 0f) return;

        float innerHAvail = Math.max(0f, innerH - 2f * s);

        var pose = guiGraphics.pose();

        // Top
        pose.pushPose();
        pose.translate(x0, y0, 0);
        pose.scale(innerW, s, 1.0f);
        guiGraphics.fill(0, 0, 1, 1, baseColor);
        pose.popPose();

        // Bottom
        pose.pushPose();
        pose.translate(x0, y1 - s, 0);
        pose.scale(innerW, s, 1.0f);
        guiGraphics.fill(0, 0, 1, 1, baseColor);
        pose.popPose();

        if (innerHAvail > 0f) {
            // Left (between corners)
            pose.pushPose();
            pose.translate(x0, y0 + s, 0);
            pose.scale(s, innerHAvail, 1.0f);
            guiGraphics.fill(0, 0, 1, 1, baseColor);
            pose.popPose();

            // Right (between corners)
            pose.pushPose();
            pose.translate(x1 - s, y0 + s, 0);
            pose.scale(s, innerHAvail, 1.0f);
            guiGraphics.fill(0, 0, 1, 1, baseColor);
            pose.popPose();
        }

        // Corners
        if(knobHighlightCorners) {
            // Top-left
            pose.pushPose();
            pose.translate(x0, y0, 0);
            pose.scale(s, s, 1.0f);
            guiGraphics.fill(0, 0, 1, 1, cornerColor);
            pose.popPose();

            // Top-right
            pose.pushPose();
            pose.translate(x1 - s, y0, 0);
            pose.scale(s, s, 1.0f);
            guiGraphics.fill(0, 0, 1, 1, cornerColor);
            pose.popPose();

            // Bottom-left
            pose.pushPose();
            pose.translate(x0, y1 - s, 0);
            pose.scale(s, s, 1.0f);
            guiGraphics.fill(0, 0, 1, 1, cornerColor);
            pose.popPose();

            // Bottom-right
            pose.pushPose();
            pose.translate(x1 - s, y1 - s, 0);
            pose.scale(s, s, 1.0f);
            guiGraphics.fill(0, 0, 1, 1, cornerColor);
            pose.popPose();
        }
    }
}
