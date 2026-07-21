package org.vmstudio.visor.api.client.gui.widgets;

import lombok.Getter;
import org.vmstudio.visor.api.client.gui.GuiTexture;
import org.vmstudio.visor.api.client.gui.helpers.GuiHelper;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoButtonImaged;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class ButtonImaged extends AbstractButton {

    @Getter
    private final WidgetInfoButtonImaged widgetInfo;

    private final Consumer<ButtonImaged> onPress;

    @Nullable
    private final Consumer<ButtonImaged> onRelease;

    @Getter
    private boolean selected;

    @Getter
    private boolean pressed;

    public ButtonImaged(WidgetInfoButtonImaged widgetInfo,
                        Consumer<ButtonImaged> onPress) {
        this(widgetInfo, onPress, null);
    }

    public ButtonImaged(WidgetInfoButtonImaged widgetInfo,
                        Consumer<ButtonImaged> onPress,
                        @Nullable Consumer<ButtonImaged> onRelease) {
        super(widgetInfo.getX(), widgetInfo.getY(),
                widgetInfo.getWidth(), widgetInfo.getHeight(),
                widgetInfo.getText()
        );
        this.widgetInfo = widgetInfo;
        this.onPress = onPress;
        this.onRelease = onRelease;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        this.active = !widgetInfo.isInactiveOnSelected()
                || !selected;
    }


    @Override
    public void onPress() {
        pressed = true;
        if (this.onPress != null) {
            this.onPress.accept(this);
        }
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        if (!pressed) return;
        pressed = false;
        if (this.onRelease != null) {
            this.onRelease.accept(this);
        }
    }

    public void forceRelease() {
        onRelease(getX(), getY());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        Tooltip tooltip = widgetInfo.getTooltip();
        if (tooltip == null || !this.visible) {
            return;
        }
        if (this.isHovered || this.isFocused()) {
            Screen screen = Minecraft.getInstance().screen;
            if (screen != null) {
                screen.setTooltipForNextRenderPass(
                        tooltip,
                        ClampedTooltipPositioner.INSTANCE,
                        this.isFocused()
                );
            }
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

        widgetInfo.drawFill(guiGraphics, isHovered);

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



    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }

}