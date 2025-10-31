package me.phoenixra.visor.api.client.gui.widgets.info;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import me.phoenixra.atumvr.api.misc.color.AtumColor;
import me.phoenixra.visor.api.client.gui.GuiTexture;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;


public class WidgetInfoValueDrag extends WidgetInfoImage{

    @Setter @Getter
    @Accessors(chain = true)
    private GuiTexture textureHovered;
    @Setter @Getter
    @Accessors(chain = true)
    private GuiTexture textureDragged;
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

    @Setter @Getter
    @Accessors(chain = true)
    private float highlightThickness = 0.65f;



    @Accessors(chain = true)
    @Setter @Getter
    private Direction direction;

    @Accessors(chain = true)
    @Setter @Getter
    private NumericAdapter adapter;

    @Accessors(chain = true)
    @Setter @Getter
    private double step = 1;

    @Accessors(chain = true)
    @Setter @Getter
    private double sensitivity = 1;



    private int highlightHoveredInt = AtumColor.WHITE.toInt();

    private int highlightCornerHoveredInt = AtumColor.WHITE.toInt();

    public WidgetInfoValueDrag(@NotNull WidgetInfoValueDrag copyFrom) {
        super(copyFrom);
        direction = copyFrom.direction;
        adapter = copyFrom.adapter;
        step = copyFrom.step;
        sensitivity = copyFrom.sensitivity;
    }

    public WidgetInfoValueDrag() {

    }



    @Override
    public WidgetInfoValueDrag pos(int x, int y) {
        return (WidgetInfoValueDrag) super.pos(x, y);
    }

    @Override
    public WidgetInfoValueDrag size(int width, int height) {
        return (WidgetInfoValueDrag) super.size(width, height);
    }

    @Override
    public WidgetInfoValueDrag setTexture(GuiTexture texture) {
        return (WidgetInfoValueDrag) super.setTexture(texture);
    }


    public WidgetInfoValueDrag highlight(AtumColor hovered,
                                      float thickness){
        return setHighlightEnabled(true)
                .setHighlightHovered(hovered)
                .setHighlightThickness(thickness);
    }
    public WidgetInfoValueDrag highlight(AtumColor hovered){
        return setHighlightEnabled(true)
                .setHighlightHovered(hovered);
    }

    public WidgetInfoValueDrag setHighlightHovered(AtumColor color) {
        this.highlightHovered = color;
        this.highlightHoveredInt = color.toInt();
        this.highlightCornerHoveredInt = color.lighten(0.25f).toInt();
        return this;
    }

    public void drawHighlight(GuiGraphics guiGraphics,
                              int x, int y, int width, int height,
                              boolean active,
                              boolean hovered) {
        if (!highlightEnabled) return;
        if (!hovered || !active) return;

        // Choose base color based on state
        int baseColor;
        int cornerColor;

        baseColor = highlightHoveredInt;
        cornerColor = highlightCornerHoveredInt;



        float s = Math.max(0f, highlightThickness);
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

    public enum Direction {
        LEFT, RIGHT;
        public boolean isPositive(){
         return this == RIGHT;
        }
        public boolean isNegative(){
            return this == LEFT;
        }
    }

    public interface NumericAdapter {
        double get();
        void set(double value);

        static NumericAdapter of(@NotNull DoubleSupplier getter, @NotNull DoubleConsumer setter) {
            Objects.requireNonNull(getter, "getter");
            Objects.requireNonNull(setter, "setter");
            return new NumericAdapter() {
                @Override
                public double get() { return getter.getAsDouble(); }
                @Override
                public void set(double value) { setter.accept(value); }
            };
        }
    }
}
