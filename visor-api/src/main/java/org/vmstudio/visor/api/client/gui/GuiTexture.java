package org.vmstudio.visor.api.client.gui;

import lombok.Getter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

@Getter
public class GuiTexture {

    private final @NotNull ResourceLocation resourceLocation;
    private final int x, y;
    private final int width, height;
    private final int textureWidth, textureHeight;
    private final @NotNull DrawMode drawMode;


    /**
     * Constructor for {@link DrawMode#STRETCH} texture,
     * drawing whole image
     *
     * @param resourceLocation the resource location
     */
    public GuiTexture(@NotNull ResourceLocation resourceLocation) {
        this.resourceLocation = resourceLocation;
        this.x = 0;
        this.y = 0;
        this.width = 1;
        this.height = 1;
        this.textureWidth = 1;
        this.textureHeight = 1;
        this.drawMode = DrawMode.STRETCH;
    }

    /**
     * Constructor for {@link DrawMode#TILE} texture
     *
     * @param resourceLocation the resource location
     * @param x                source region X in the texture
     * @param y                source region Y in the texture
     * @param width            source region width
     * @param height           source region height
     */
    public GuiTexture(@NotNull ResourceLocation resourceLocation,
                      int x, int y,
                      int width, int height) {
        this.resourceLocation = resourceLocation;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.textureWidth = width;
        this.textureHeight = height;
        this.drawMode = DrawMode.TILE;
    }
    /**
     * Constructor for {@link DrawMode#STRETCH} texture
     * with custom source region
     *
     * @param resourceLocation the resource location
     * @param x                source region X in the texture
     * @param y                source region Y in the texture
     * @param width            source region width
     * @param height           source region height
     * @param textureWidth     full texture width
     * @param textureHeight    full texture height
     */
    public GuiTexture(
            @NotNull ResourceLocation resourceLocation,
            int x, int y,
            int width, int height,
            int textureWidth, int textureHeight) {
        this.resourceLocation = resourceLocation;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.drawMode = DrawMode.STRETCH;
    }


    /**
     * Draws this texture region with specified position and size.
     *
     * @param gui          the GuiGraphics instance
     * @param xPos         X coordinate to draw at
     * @param yPos         Y coordinate to draw at
     * @param targetWidth  desired width
     * @param targetHeight desired height
     */
    public void blit(@NotNull GuiGraphics gui,
                     int xPos, int yPos,
                     int targetWidth, int targetHeight) {

        if (drawMode == DrawMode.STRETCH) {
            gui.blit(
                    resourceLocation,
                    xPos, yPos,
                    targetWidth, targetHeight,
                    x, y,
                    width, height,
                    textureWidth, textureHeight
            );
        } else {
            gui.blit(
                    resourceLocation,
                    xPos, yPos,
                    x, y,
                    targetWidth, targetHeight,
                    width, height
            );
        }
    }

    /**
     * Draws this texture region with specified position and original size
     *
     * @param gui          the GuiGraphics instance
     * @param xPos         X coordinate to draw at
     * @param yPos         Y coordinate to draw at
     */
    public void blit(@NotNull GuiGraphics gui,
                     int xPos, int yPos) {
        blit(gui, xPos, yPos, textureWidth, textureHeight);
    }

    /**
     * Returns {@link DrawMode#STRETCH} texture,
     * drawing whole image
     * @param location the resource location
     * @return the texture
     */
    public static GuiTexture of(@NotNull ResourceLocation location){
        return new GuiTexture(location);
    }

    public enum DrawMode {
        /**
         * scale the texture to image's width×height
         */
        STRETCH,
        /**
         * repeats the texture
         * if image's width×height exceeds its bounds
         */
        TILE
    }
}
