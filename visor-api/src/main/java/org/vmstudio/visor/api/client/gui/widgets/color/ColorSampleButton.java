package org.vmstudio.visor.api.client.gui.widgets.color;

import lombok.Getter;
import lombok.Setter;
import me.phoenixra.atumvr.api.misc.color.AtumColor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.api.client.gui.helpers.ColorsHelper;

import java.util.function.Consumer;


public class ColorSampleButton extends AbstractButton {

    private final Consumer<ColorSampleButton> onPress;

    @Getter @Setter
    private @NotNull AtumColor color;

    @Setter
    private boolean showTransparency = true;

    @Setter
    private boolean selected;

    public ColorSampleButton(int x, int y,
                             int width, int height,
                             @NotNull AtumColor color,
                             @NotNull Consumer<ColorSampleButton> onPress) {
        super(x, y, width, height, Component.empty());
        this.color = color;
        this.onPress = onPress;
    }

    @Override
    public void onPress() {
        this.onPress.accept(this);
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

        if (showTransparency && color.getAlphaInt() < 255) {
            ColorsHelper.drawTransparencyChecker(guiGraphics, x, y, width, height);
        }
        guiGraphics.fill(x, y, x + width, y + height, color.asInt());

        int borderColor;
        if (selected) {
            borderColor = 0xFFFFFFFF;
        } else if (isHovered) {
            borderColor = 0xFFBBBBBB;
        } else {
            borderColor = 0xFF000000;
        }
        ColorsHelper.drawBorder(guiGraphics, x, y, width, height, borderColor);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }
}
