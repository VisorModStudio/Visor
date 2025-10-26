package me.phoenixra.visor.api.client.gui.widgets;

import me.phoenixra.visor.api.client.gui.GuiTexture;
import me.phoenixra.visor.api.client.gui.widgets.info.WidgetInfoEditBox;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;


public class EditBoxImage extends EditBox {
    private final GuiTexture texture;

    public EditBoxImage(@NotNull WidgetInfoEditBox widgetInfo) {
        super(widgetInfo.getTextFont(),
                widgetInfo.getX(),
                widgetInfo.getY(),
                widgetInfo.getWidth(),
                widgetInfo.getHeight(),
                Component.empty()
        );
        this.texture = widgetInfo.getTexture();
        setTextColor(widgetInfo.getTextColor().toInt());
        setHint(widgetInfo.getHint());
        setMaxLength(widgetInfo.getTextMaxLength());

        setFilter(widgetInfo.getFilter());

        setTooltip(widgetInfo.getTooltip());

        setBordered(true);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if(visible) {
            texture.blit(
                    guiGraphics,
                    getX(), getY(),
                    getWidth(), getHeight()
            );
        }

        // draw text, cursor, selection
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }



    //---------
    //silly way to make no border drawing, but have the small padding for text

    @Override
    public boolean isBordered() {
        return false;
    }

    public int getInnerWidth() {
        return this.width - 8;
    }
}
