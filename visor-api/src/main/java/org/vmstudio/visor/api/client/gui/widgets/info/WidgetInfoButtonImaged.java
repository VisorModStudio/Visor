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
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;


public class WidgetInfoButtonImaged extends WidgetInfoImage {

    @Setter @Getter
    @Accessors(chain = true)
    private GuiTexture textureHovered;

    @Setter @Getter
    @Accessors(chain = true)
    private GuiTexture textureSelected;

    @Setter @Getter
    @Accessors(chain = true)
    private GuiTexture textureHoveredSelected;

    @Setter @Getter
    @Accessors(chain = true)
    private GuiTexture textureInactive;

    @Setter @Getter
    @Accessors(chain = true)
    private boolean highlightEnabled = false;
    @Setter @Getter
    @Accessors(chain = true)
    private boolean highlightCorners = true;
    @Getter
    @Accessors(chain = true)
    private AtumColor highlightHovered = AtumColor.WHITE;
    @Getter
    @Accessors(chain = true)
    private AtumColor highlightSelected = AtumColor.GRAY;
    @Getter
    @Accessors(chain = true)
    private AtumColor highlightHoveredSelected = AtumColor.LIGHT_GRAY;
    @Setter @Getter
    @Accessors(chain = true)
    private float highlightThickness = 0.65f;

    @Setter @Getter
    @Accessors(chain = true)
    private Component text = Component.empty();

    @Setter @Getter
    @Accessors(chain = true)
    private Font textFont = Minecraft.getInstance().font;

    @Setter @Getter
    @Accessors(chain = true)
    private AtumColor textColor = AtumColor.WHITE;

    @Setter @Getter
    @Accessors(chain = true)
    private Vector2i textPosOffset = new Vector2i(2,2);

    @Setter @Getter
    @Accessors(chain = true)
    private Vector2i textSizeOffset = new Vector2i(-4,-4);

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

    /**
     * If button becomes inactive once selected
     */
    @Setter @Getter
    @Accessors(chain = true)
    private boolean inactiveOnSelected = true;

    @Setter @Getter
    @Accessors(chain = true)
    private Tooltip tooltip;


    private int highlightHoveredInt = AtumColor.WHITE.asInt();
    private int highlightSelectedInt = AtumColor.GRAY.asInt();
    private int highlightHoveredSelectedInt = AtumColor.LIGHT_GRAY.asInt();

    private int highlightCornerHoveredInt = AtumColor.WHITE.asInt();
    private int highlightCornerSelectedInt = AtumColor.GRAY.asInt();
    private int highlightCornerHoveredSelectedInt = AtumColor.LIGHT_GRAY.asInt();

    public WidgetInfoButtonImaged(@NotNull WidgetInfoButtonImaged copyFrom) {
        super(copyFrom);
        textureHovered = copyFrom.textureHovered;
        textureSelected = copyFrom.textureSelected;
        textureHoveredSelected = copyFrom.textureHoveredSelected;
        textureInactive = copyFrom.textureInactive;
        highlightEnabled = copyFrom.highlightEnabled;
        highlightCorners = copyFrom.highlightCorners;
        highlightHovered = copyFrom.highlightHovered;
        highlightSelected = copyFrom.highlightSelected;
        highlightHoveredSelected = copyFrom.highlightHoveredSelected;
        highlightThickness = copyFrom.highlightThickness;
        highlightHoveredInt = copyFrom.highlightHoveredInt;
        highlightSelectedInt = copyFrom.highlightSelectedInt;
        highlightHoveredSelectedInt = copyFrom.highlightHoveredSelectedInt;
        highlightCornerHoveredInt = copyFrom.highlightCornerHoveredInt;
        highlightCornerSelectedInt = copyFrom.highlightCornerSelectedInt;
        highlightCornerHoveredSelectedInt = copyFrom.highlightCornerHoveredSelectedInt;
        textFont = copyFrom.textFont;
        text = copyFrom.text;
        textColor = copyFrom.textColor;
        textPosOffset = copyFrom.textPosOffset;
        textSizeOffset = copyFrom.textSizeOffset;
        inactiveOnSelected = copyFrom.inactiveOnSelected;
        dynamicTextScale = copyFrom.dynamicTextScale;
        tooltip = copyFrom.tooltip;

    }

    public WidgetInfoButtonImaged() {

    }

    public WidgetInfoButtonImaged textures(GuiTexture texture,
                                           GuiTexture textureHovered,
                                           GuiTexture textureSelected,
                                           GuiTexture textureHoveredSelected,
                                           GuiTexture textureInactive){
        return setTexture(texture)
                .setTextureHovered(textureHovered)
                .setTextureSelected(textureSelected)
                .setTextureHoveredSelected(textureHoveredSelected)
                .setTextureInactive(textureInactive);

    }
    public WidgetInfoButtonImaged textures(GuiTexture texture,
                                           GuiTexture textureHovered,
                                           GuiTexture textureSelected){
        return setTexture(texture)
                .setTextureHovered(textureHovered)
                .setTextureSelected(textureSelected)
                .setTextureHoveredSelected(textureSelected)
                .setTextureInactive(texture);

    }
    public WidgetInfoButtonImaged highlight(AtumColor highlightHovered,
                                            AtumColor highlightSelected,
                                            AtumColor highlightHoveredSelected,
                                            float highlightThickness){
        return setHighlightEnabled(true)
                .setHighlightHovered(highlightHovered)
                .setHighlightSelected(highlightSelected)
                .setHighlightHoveredSelected(highlightHoveredSelected)
                .setHighlightThickness(highlightThickness);

    }
    public WidgetInfoButtonImaged highlight(AtumColor highlightHovered,
                                            AtumColor highlightSelected){
        return setHighlightEnabled(true)
                .setHighlightHovered(highlightHovered)
                .setHighlightSelected(highlightSelected)
                .setHighlightHoveredSelected(highlightSelected);

    }

    public WidgetInfoButtonImaged setHighlightHovered(AtumColor color) {
        this.highlightHovered = color;
        this.highlightHoveredInt = color.asInt();
        this.highlightCornerHoveredInt = color.lighten(0.25f).asInt();
        return this;
    }

    public WidgetInfoButtonImaged setHighlightSelected(AtumColor color) {
        this.highlightSelected = color;
        this.highlightSelectedInt = color.asInt();
        this.highlightCornerSelectedInt = color.lighten(0.25f).asInt();
        return this;
    }
    public WidgetInfoButtonImaged setHighlightHoveredSelected(AtumColor color) {
        this.highlightHoveredSelected = color;
        this.highlightHoveredSelectedInt = color.asInt();
        this.highlightCornerHoveredSelectedInt = color.lighten(0.25f).asInt();
        return this;
    }

    @Override
    public WidgetInfoButtonImaged setTexture(GuiTexture texture) {
        return (WidgetInfoButtonImaged) super.setTexture(texture);
    }

    @Override
    public WidgetInfoButtonImaged pos(int x, int y) {
        return (WidgetInfoButtonImaged) super.pos(x, y);
    }

    @Override
    public WidgetInfoButtonImaged size(int width, int height) {
        return (WidgetInfoButtonImaged) super.size(width, height);
    }


    public void drawHighlight(GuiGraphics guiGraphics,
                              boolean active,
                              boolean hovered,
                              boolean selected) {
        if (!highlightEnabled) return;
        if (!selected && !hovered) return;


        // Choose base color based on state
        int baseColor;
        int cornerColor;
        if(!active){
            if (!selected) return;
            baseColor = highlightSelectedInt;
            cornerColor = highlightCornerSelectedInt;
        } else if (selected) {
            baseColor = hovered
                    ? highlightHoveredSelectedInt
                    : highlightSelectedInt;
            cornerColor = hovered
                    ? highlightCornerHoveredSelectedInt
                    : highlightCornerSelectedInt;
        } else {
            baseColor = highlightHoveredInt;
            cornerColor = highlightCornerHoveredInt;
        }



        float s = Math.max(0f, highlightThickness);
        if (s <= 0f) return;


        float x0 = this.getX();
        float y0 = this.getY();
        float x1 = this.getX() + getWidth();
        float y1 = this.getY() + getHeight();

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
        if(highlightCorners) {
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
