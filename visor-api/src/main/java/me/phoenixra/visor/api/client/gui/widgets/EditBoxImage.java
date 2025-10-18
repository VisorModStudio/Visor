package me.phoenixra.visor.api.client.gui.widgets;

import me.phoenixra.visor.api.client.gui.GuiTexture;
import me.phoenixra.visor.api.client.gui.widgets.info.WidgetInfoEditBox;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;


public class EditBoxImage extends EditBox {
    private final GuiTexture texture;

    private final int imageX;
    private final int imageY;
    private final int imageWidth;
    private final int imageHeight;
    public EditBoxImage(@NotNull WidgetInfoEditBox widgetInfo) {
        super(widgetInfo.getTextFont(),
                widgetInfo.getX() + 4,
                widgetInfo.getY() + (widgetInfo.getHeight() - 8) / 2,
                widgetInfo.getWidth() - 8,
                widgetInfo.getHeight(),
                Component.empty()
        );
        this.texture = widgetInfo.getTexture();
        imageX = widgetInfo.getX();
        imageY = widgetInfo.getY();
        imageWidth = widgetInfo.getWidth();
        imageHeight = widgetInfo.getHeight();
        setBordered(false);
        setTextColor(widgetInfo.getTextColor().toInt());
        setHint(widgetInfo.getHint());
        setTooltip(widgetInfo.getTooltip());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        texture.blit(
                guiGraphics,
                imageX, imageY,
                imageWidth, imageHeight
        );
        // draw text, cursor, selection
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

}
