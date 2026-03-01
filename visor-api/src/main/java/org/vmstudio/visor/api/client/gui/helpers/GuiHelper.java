package org.vmstudio.visor.api.client.gui.helpers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class GuiHelper {
    private GuiHelper() {
        throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
    }


    /**
     * Renders custom scaled text centered within bounds, scrolling if it overflows.
     *
     * @param guiGraphics the gui graphics context
     * @param font        the font to use
     * @param text        the text to render
     * @param color       the text color
     * @param posX        left edge of the text area
     * @param posY        top edge of the text area
     * @param width       width of the text area
     * @param height      height of the text area
     * @param scale       fixed text scale (1.0 = default)
     * @param center      whether to center the text when it fits
     */
    public static void renderScrollableText(@NotNull GuiGraphics guiGraphics,
                                            @NotNull Font font,
                                            @NotNull String text,
                                            int color,
                                            int posX, int posY,
                                            int width, int height,
                                            float scale,
                                            boolean center) {
        if (text.isEmpty()) return;

        if (scale == 1.0f) {
            int textWidth = font.width(text);
            int textHeight = font.lineHeight;

            int x = center ? posX + (width - textWidth) / 2 : posX;
            int y = center ? posY + (height - textHeight) / 2 : posY;

            if (textWidth <= width) {
                guiGraphics.drawString(font, text, x, y, color, false);
            } else {
                int overflow = textWidth - width;
                double d = (double) Util.getMillis() / 1000.0;
                double e = Math.max((double) overflow * 0.5, 3.0);
                double f = Math.sin((Math.PI / 2.0) * Math.cos((Math.PI * 2.0) * d / e)) / 2.0 + 0.5;
                int offset = (int) Mth.lerp(f, 0.0, (double) overflow);

                guiGraphics.enableScissor(posX, posY, posX + width, posY + height);
                guiGraphics.drawString(font, text, posX - offset, y, color, false);
                guiGraphics.disableScissor();
            }
            return;
        }

        float scaledTextWidth = font.width(text) * scale;

        PoseStack poseStack = guiGraphics.pose();

        poseStack.pushPose();
        poseStack.translate(posX, posY, 0);
        poseStack.scale(scale, scale, 1f);
        poseStack.translate(-posX, -posY, 0);

        float areaW = width / scale;
        float areaH = height / scale;
        int textWidth = font.width(text);

        int y = center
                ? posY + Math.round((areaH - font.lineHeight) / 2f)
                : posY;

        if (scaledTextWidth <= width) {
            int x = center
                    ? posX + Math.round((areaW - textWidth) / 2f)
                    : posX;

            guiGraphics.enableScissor(posX, posY, posX + width, posY + height);
            guiGraphics.drawString(font, text, x, y, color, false);
            guiGraphics.disableScissor();
        } else {
            float overflow = scaledTextWidth - width;
            double d = (double) Util.getMillis() / 1000.0;
            double e = Math.max((double) overflow * 0.5, 3.0);
            double f = Math.sin((Math.PI / 2.0) * Math.cos((Math.PI * 2.0) * d / e)) / 2.0 + 0.5;
            int offset = (int) Mth.lerp(f, 0.0, (double) overflow);

            int x = posX - Math.round(offset / scale);

            guiGraphics.enableScissor(posX, posY, posX + width, posY + height);
            guiGraphics.drawString(font, text, x, y, color, false);
            guiGraphics.disableScissor();
        }

        poseStack.popPose();
    }

    public static void renderScalableText(@NotNull GuiGraphics guiGraphics,
                                          @NotNull Font font,
                                          @NotNull String text,
                                          int color,
                                          int posX, int posY,
                                          int width, int height,
                                          boolean center) {
        renderScalableText(
                guiGraphics,
                font,
                text,
                color,
                posX, posY,
                width, height,
                1.0f,
                center
        );
    }

    public static void renderScalableText(@NotNull GuiGraphics guiGraphics,
                                          @NotNull Font font,
                                          @NotNull String text,
                                          int color,
                                          int posX, int posY,
                                          int width, int height,
                                          float maxScale,
                                          boolean center) {
        if (text.isEmpty()) return;

        float textWidth = font.width(text);
        float textHeight = font.lineHeight;

        float heightScale = (float) height / font.lineHeight;

        float scale = Math.min(maxScale, Math.min(width / textWidth, heightScale));


        float dispW = textWidth * scale;
        float dispH = textHeight * scale;

        float drawX = posX;
        float drawY = posY;
        if (center) {
            float targetX = posX + (width  - dispW) * 0.5f;
            float targetY = posY + (height - dispH) * 0.5f;
            drawX = targetX;
            drawY = targetY;
        }

        // Save current transform state
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();

        // Apply the transform FIRST, before scissoring
        poseStack.translate(drawX, drawY, 0);
        poseStack.scale(scale, scale, 1f);
        poseStack.translate(-drawX, -drawY, 0);

        // Calculate text position in the transformed space
        float baseX = drawX;
        float baseY = drawY;

        guiGraphics.drawString(font, text, Math.round(baseX), Math.round(baseY), color, false);


        // Restore transform state
        poseStack.popPose();

    }



}
