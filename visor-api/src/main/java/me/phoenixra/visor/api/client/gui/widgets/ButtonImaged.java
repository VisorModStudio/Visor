package me.phoenixra.visor.api.client.gui.widgets;

import lombok.Getter;
import me.phoenixra.visor.api.client.gui.GuiTexture;
import me.phoenixra.visor.api.client.gui.helpers.GuiHelper;
import me.phoenixra.visor.api.client.gui.widgets.info.WidgetInfoButtonImaged;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.util.Mth;

import java.util.function.Consumer;

public class ButtonImaged extends AbstractButton {

    @Getter
    private final WidgetInfoButtonImaged widgetInfo;

    private final Consumer<ButtonImaged> onPress;

    @Getter
    private boolean selected;

    public ButtonImaged(WidgetInfoButtonImaged widgetInfo,
                        Consumer<ButtonImaged> onPress) {
        super(widgetInfo.getX(), widgetInfo.getY(),
                widgetInfo.getWidth(), widgetInfo.getHeight(),
                widgetInfo.getText()
        );
        this.widgetInfo = widgetInfo;
        this.onPress = onPress;
        this.setTooltip(widgetInfo.getTooltip());
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        this.active = !widgetInfo.isInactiveOnSelected()
                || !selected;
    }


    @Override
    public void onPress() {
        if (this.onPress != null) {
            this.onPress.accept(this);
        }
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {

        GuiTexture texture;
        if(!active){

            texture = selected
                    ? widgetInfo.getTextureSelected()
                    : widgetInfo.getTextureInactive();
        }else {
            if (selected) {
                texture = widgetInfo.getTextureHoveredSelected();
                if (!isHovered || texture == null) {
                    texture = widgetInfo.getTextureSelected();
                }
            } else if (isHovered) {
                texture = widgetInfo.getTextureHovered();
            } else {
                texture = widgetInfo.getTexture();
            }
        }
        if(texture == null){
            texture = widgetInfo.getTexture();
        }

        widgetInfo
                .pos(getX(), getY())
                .size(getWidth(), getHeight());

        if(texture != null) {
            texture.blit(
                    guiGraphics,
                    this.getX(), this.getY(),
                    this.width, this.height
            );
        }

        widgetInfo.drawHighlight(guiGraphics, active, isHovered, selected);


        String text = getMessage().getString();
        int textX = getX() + widgetInfo.getTextPosOffset().x;
        int textY = getY() + widgetInfo.getTextPosOffset().y;
        int textW = getWidth() + widgetInfo.getTextSizeOffset().x;
        int textH = getHeight() + widgetInfo.getTextSizeOffset().y;

        if (!text.isEmpty()) {
            Font font = Minecraft.getInstance().font;
            int color = widgetInfo.getTextColor().asInt();

            if (widgetInfo.isDynamicTextScale()) {
                GuiHelper.renderScalableText(
                        guiGraphics, font, text, color,
                        textX, textY, textW, textH, true
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



    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }

}