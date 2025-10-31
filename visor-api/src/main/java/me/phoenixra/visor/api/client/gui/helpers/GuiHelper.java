package me.phoenixra.visor.api.client.gui.helpers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class GuiHelper {
    private GuiHelper() {
        throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
    }

    public static void renderScalableText(@NotNull GuiGraphics guiGraphics,
                                          @NotNull Font font,
                                          @NotNull String text,
                                          int color,
                                          int posX, int posY,
                                          int width, int height,
                                          boolean center) {
        if (text.isEmpty()) return;

        float textWidth = font.width(text);
        float textHeight = font.lineHeight;

        float heightScale = (float) height / font.lineHeight;

        float scale = Math.min(1f, Math.min(width / textWidth, heightScale));


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

        // Calculate scissor in original coordinate space
        guiGraphics.enableScissor(posX, posY, posX + width, posY + height);

        // Calculate text position in the transformed space
        float baseX = drawX;
        float baseY = drawY;

        guiGraphics.drawString(font, text, Math.round(baseX), Math.round(baseY), color, false);


        // Restore transform state
        poseStack.popPose();
        guiGraphics.disableScissor();
    }
}
