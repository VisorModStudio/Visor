package org.vmstudio.visor.api.compatibility.mcversion.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ObjectSelectionList;

/**
 * Cross-mc-version adapter for ObjectSelectionList
 */
@Environment(EnvType.CLIENT)
public abstract class McObjectSelectionList<E extends ObjectSelectionList.Entry<E>> extends ObjectSelectionList<E> {

    protected McObjectSelectionList(Minecraft minecraft,
                                    int width, int height,
                                    int x, int y,
                                    int itemHeight) {
        //? if >=1.20.3 {
        super(minecraft, width, height, y, itemHeight);
        this.setX(x);
        //?} else {
        /*super(minecraft, width, height, y, y + height, itemHeight);
        this.setLeftPos(x);
        *///?}
    }


    // ------- STABLE API -------

    protected void renderContents(GuiGraphicsExtractor guiGraphics,
                                  int mouseX, int mouseY,
                                  float partialTick) {
        renderDefault(guiGraphics, mouseX, mouseY, partialTick);
    }

    /**
     * The vanilla list body - background, header, rows, scrollbar and decorations.
     */
    protected final void renderDefault(GuiGraphicsExtractor guiGraphics,
                                       int mouseX, int mouseY,
                                       float partialTick) {
        //? if >=26.1 {
        super.extractWidgetRenderState(guiGraphics, mouseX, mouseY, partialTick);
        //?} elif >=1.20.3 {
        /*super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        *///?} else {
        /*super.render(guiGraphics, mouseX, mouseY, partialTick);
        *///?}
    }


    protected int scrollbarX() {
        //? if >=1.21.4 {
        return super.scrollBarX();
        //?} else {
        /*return super.getScrollbarPosition();
        *///?}
    }

    // 1.20.5 dropped the flag, the background moved to renderListBackground
    public void setRenderBackground(boolean render) {
        //? if <1.20.5 {
        /*super.setRenderBackground(render);
        *///?}
    }


    protected final int listLeft() {
        //? if >=1.20.3 {
        return getX();
        //?} else {
        /*return x0;
        *///?}
    }
    protected final int listRight() {
        //? if >=1.20.3 {
        return getRight();
        //?} else {
        /*return x1;
        *///?}
    }

    protected final int listTop() {
        //? if >=1.20.3 {
        return getY();
        //?} else {
        /*return y0;
        *///?}
    }
    protected final int listBottom() {
        //? if >=1.20.3 {
        return getBottom();
        //?} else {
        /*return y1;
        *///?}
    }


    // ------- MC-VERSION SPECIFIC IMPLEMENTATION -------

    //? if >=26.1 {
    @Override
    public final void extractWidgetRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderContents(guiGraphics, mouseX, mouseY, partialTick);
    }
    //?} elif >=1.20.3 {
    /*@Override
    public final void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderContents(guiGraphics, mouseX, mouseY, partialTick);
    }
    *///?} else {
    /*@Override
    public final void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderContents(guiGraphics, mouseX, mouseY, partialTick);
    }
    *///?}

    //? if >=1.21.4 {
    @Override
    protected final int scrollBarX() {
        return scrollbarX();
    }
    //?} else {
    /*@Override
    protected final int getScrollbarPosition() {
        return scrollbarX();
    }
    *///?}

}
