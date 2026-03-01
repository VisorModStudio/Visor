package org.vmstudio.visor.api.client.gui.widgets;

import lombok.Getter;
import org.vmstudio.visor.api.client.gui.GuiTexture;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoValueDrag;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;


public class ValueDragWidget extends AbstractWidget {

    @Getter
    private final WidgetInfoValueDrag widgetInfo;

    private double dragStartMouseX = 0.0;
    private double startValue = 0.0;

    private long lastDragCall = -1;
    private boolean dragging = false;
    public ValueDragWidget(WidgetInfoValueDrag widgetInfo) {
        super(widgetInfo.getX(),
                widgetInfo.getY(),
                widgetInfo.getWidth(),
                widgetInfo.getHeight(),
                Component.empty()
        );
        this.widgetInfo = widgetInfo;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        this.dragging = true;
        this.lastDragCall = System.currentTimeMillis();
        this.dragStartMouseX = mouseX;
        this.startValue = widgetInfo.getAdapter().get();
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        this.dragging = false;
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
        if (!dragging) return;

        lastDragCall = System.currentTimeMillis();

        double dx = mouseX - dragStartMouseX;
        double delta = dx * widgetInfo.getSensitivity();

        double step = widgetInfo.getStep();

        double newValue = startValue + (delta * step);

        if((widgetInfo.getDirection().isPositive() && newValue < startValue)
                || (widgetInfo.getDirection().isNegative() && newValue > startValue) ){
            newValue = startValue;
        }

        var adapter = widgetInfo.getAdapter();
        if (newValue != adapter.get()) {
            adapter.set(newValue);
        }
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics,
                                int mouseX, int mouseY,
                                float partialTick) {
        if(dragging && lastDragCall + 150 < System.currentTimeMillis()){
            dragging = false;
        }
        GuiTexture texture;
        if(!active){

            texture = widgetInfo.getTextureInactive();
        }else {
            if (dragging) {
                texture = widgetInfo.getTextureDragged();
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

        texture.blit(
                guiGraphics,
                this.getX(), this.getY(),
                this.width, this.height
        );

        widgetInfo.drawHighlight(
                guiGraphics,
                getX(), getY(),
                getWidth(), getHeight(),
                active, isHovered,
                dragging
        );


    }

    @Override
    protected void updateWidgetNarration(net.minecraft.client.gui.narration.NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }


}