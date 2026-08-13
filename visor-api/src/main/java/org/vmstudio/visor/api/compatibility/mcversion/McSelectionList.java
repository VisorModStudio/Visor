package org.vmstudio.visor.api.compatibility.mcversion;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.narration.NarrationElementOutput;

/**
 * McVersion adapter for AbstractSelectionList
 */
@Environment(EnvType.CLIENT)
public abstract class McSelectionList<E extends McSelectionList.Entry<E>> extends AbstractSelectionList<E> {


    @Environment(EnvType.CLIENT)
    public abstract static class Entry<E extends Entry<E>> extends AbstractSelectionList.Entry<E> {
    }

    protected McSelectionList(Minecraft minecraft,
                              int width, int height,
                              int x, int y,
                              int itemHeight) {
        super(Minecraft.getInstance(),
                width,
                height,
                y,
                y + height,
                itemHeight
        );
        this.setLeftPos(x);
    }


    // ------- STABLE API -------

    protected abstract void renderContents(GuiGraphics guiGraphics,
                                           int mouseX, int mouseY,
                                           float partialTick);

    protected void renderRows(GuiGraphics guiGraphics,
                              int mouseX, int mouseY,
                              float partialTick) {
        super.renderList(guiGraphics, mouseX, mouseY, partialTick);
    }


    protected int scrollbarX() {
        return super.getScrollbarPosition();
    }

    protected boolean onMouseScrolled(double mouseX, double mouseY, double verticalAmount) {
        return super.mouseScrolled(mouseX, mouseY, verticalAmount);
    }

    protected void updateListNarration(NarrationElementOutput narrationElementOutput) {
    }



    protected final int listLeft() {
        return x0;
    }
    protected final int listRight() {
        return x1;
    }

    protected final int listTop() {
        return y0;
    }
    protected final int listBottom() {
        return y1;
    }



    // ------- IMPLEMENTATION -------

    @Override
    public final void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderContents(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected final void renderList(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderRows(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void renderBackground(GuiGraphics guiGraphics) {
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {
        updateListNarration(narrationElementOutput);
    }


    @Override
    protected final int getScrollbarPosition() {
        return scrollbarX();
    }

    @Override
    public final boolean mouseScrolled(double mouseX, double mouseY,  double scrollY) {
        return onMouseScrolled(mouseX, mouseY, scrollY);
    }


}
