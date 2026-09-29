package org.vmstudio.visor.api.compatibility.mcversion.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
//? if >=1.21.6 {
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.fog.FogRenderer;
//?}

/**
 * Cross-mc-version GUI frame: a GuiGraphics that draws into the main render target
 * once {@link #end(GuiGraphics)} is called
 */
@Environment(EnvType.CLIENT)
public class McGuiRenderer {
    private McGuiRenderer() {
        throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
    }

    public static GuiGraphics begin() {
        return begin(-1, -1);
    }

    /**
     * Same with the cursor position, which drives the text hover effects since 1.21.11
     */
    public static GuiGraphics begin(int mouseX, int mouseY) {
        Minecraft minecraft = Minecraft.getInstance();
        //? if >=1.21.11 {
        GameRenderer gameRenderer = minecraft.gameRenderer;
        gameRenderer.guiRenderState.reset();
        return new GuiGraphics(minecraft, gameRenderer.guiRenderState, mouseX, mouseY);
        //?} elif >=1.21.6 {
        /*GameRenderer gameRenderer = minecraft.gameRenderer;
        gameRenderer.guiRenderState.reset();
        return new GuiGraphics(minecraft, gameRenderer.guiRenderState);
        *///?} else {
        /*return new GuiGraphics(minecraft, minecraft.renderBuffers().bufferSource());
        *///?}
    }

    public static void end(GuiGraphics guiGraphics) {
        //? if >=1.21.6 {
        GameRenderer gameRenderer = Minecraft.getInstance().gameRenderer;
        gameRenderer.guiRenderer.render(gameRenderer.fogRenderer.getBuffer(FogRenderer.FogMode.NONE));
        gameRenderer.guiRenderer.incrementFrameNumber();
        //?} else {
        /*guiGraphics.flush();
        *///?}
    }
}
