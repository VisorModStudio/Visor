package org.vmstudio.visor.api.compatibility.mcversion.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
//? if >=1.21.6 {
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.vmstudio.visor.api.compatibility.mcversion.render.McModelViewStack;
//?}
//? if >=26.2 {
import org.vmstudio.visor.api.compatibility.mcversion.render.McFog;
//?}
//? if >=26.1 {
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.renderer.state.WindowRenderState;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
//?}

/**
 * Cross-mc-version GUI frame: a GuiGraphicsExtractor that draws into the main render target
 * once {@link #end(GuiGraphicsExtractor)} is called
 */
@Environment(EnvType.CLIENT)
public class McGuiRenderer {
    private McGuiRenderer() {
        throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
    }

    public static GuiGraphicsExtractor begin() {
        return begin(-1, -1);
    }

    /**
     * Same with the cursor position, which drives the text hover effects since 1.21.11
     */
    public static GuiGraphicsExtractor begin(int mouseX, int mouseY) {
        Minecraft minecraft = Minecraft.getInstance();
        //? if >=26.1 {
        //? if >=26.2 {
        GuiRenderState guiRenderState = minecraft.gameRenderer.gameRenderState().guiRenderState;
        //?} else {
        /*GuiRenderState guiRenderState = minecraft.gameRenderer.getGameRenderState().guiRenderState;
        *///?}
        guiRenderState.reset();
        return new GuiGraphicsExtractor(minecraft, guiRenderState, mouseX, mouseY);
        //?} elif >=1.21.11 {
        /*GameRenderer gameRenderer = minecraft.gameRenderer;
        gameRenderer.guiRenderState.reset();
        return new GuiGraphics(minecraft, gameRenderer.guiRenderState, mouseX, mouseY);
        *///?} elif >=1.21.6 {
        /*GameRenderer gameRenderer = minecraft.gameRenderer;
        gameRenderer.guiRenderState.reset();
        return new GuiGraphics(minecraft, gameRenderer.guiRenderState);
        *///?} else {
        /*return new GuiGraphics(minecraft, minecraft.renderBuffers().bufferSource());
        *///?}
    }

    public static void end(GuiGraphicsExtractor guiGraphics) {
        //? if >=26.1 {
        Minecraft minecraft = Minecraft.getInstance();
        GameRenderer gameRenderer = minecraft.gameRenderer;
        //? if >=26.2 {
        WindowRenderState windowState = gameRenderer.gameRenderState().windowRenderState;
        McFog.State fog = McFog.save();
        McFog.disable();
        //?} else {
        /*WindowRenderState windowState = gameRenderer.getGameRenderState().windowRenderState;
        *///?}
        Window window = minecraft.getWindow();
        int width = windowState.width;
        int height = windowState.height;
        int guiScale = windowState.guiScale;
        boolean uiLightmap = gameRenderer.useUiLightmap;
        windowState.width = window.getWidth();
        windowState.height = window.getHeight();
        windowState.guiScale = window.getGuiScale();
        gameRenderer.useUiLightmap = true;
        McModelViewStack.push();
        McModelViewStack.identity();
        try {
            //? if >=26.2 {
            gameRenderer.guiRenderer.render();
            //?} else {
            /*gameRenderer.guiRenderer.render(gameRenderer.fogRenderer.getBuffer(FogRenderer.FogMode.NONE));
            *///?}
        } finally {
            //? if >=26.2 {
            McFog.restore(fog);
            //?}
            McModelViewStack.pop();
            gameRenderer.useUiLightmap = uiLightmap;
            windowState.width = width;
            windowState.height = height;
            windowState.guiScale = guiScale;
        }
        gameRenderer.guiRenderer.endFrame();
        //?} elif >=1.21.6 {
        /*GameRenderer gameRenderer = Minecraft.getInstance().gameRenderer;
        McModelViewStack.push();
        McModelViewStack.identity();
        try {
            gameRenderer.guiRenderer.render(gameRenderer.fogRenderer.getBuffer(FogRenderer.FogMode.NONE));
        } finally {
            McModelViewStack.pop();
        }
        gameRenderer.guiRenderer.incrementFrameNumber();
        *///?} else {
        /*guiGraphics.flush();
        *///?}
    }
}
