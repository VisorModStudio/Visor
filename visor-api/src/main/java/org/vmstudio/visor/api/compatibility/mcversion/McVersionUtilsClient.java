package org.vmstudio.visor.api.compatibility.mcversion;

import com.mojang.blaze3d.platform.NativeImage;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.GenericMessageScreen;
import net.minecraft.client.gui.screens.ProgressScreen;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import org.jetbrains.annotations.Nullable;


@Environment(EnvType.CLIENT)
public class McVersionUtilsClient {
    private McVersionUtilsClient() {
        throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
    }

    public static boolean isLevelTransitionScreen(@Nullable Screen screen) {
        return screen instanceof ReceivingLevelScreen
                || screen instanceof ProgressScreen
                || screen instanceof GenericMessageScreen;
    }

    //---------- widget hit testing ----------

    /**
     * Bounds test against a widget, honouring active/visible.
     * <p>
     * Replaces the protected AbstractWidget#clicked, removed in 1.21.2. Do not swap this
     * for isMouseOver - subclasses override that to mean "over an interactive part of me",
     * which is not the same question.
     *
     * @param widget the widget to test against
     * @param mouseX mouse X in screen space
     * @param mouseY mouse Y in screen space
     * @return true when the widget is interactive and the point lies inside it
     */
    public static boolean isWithinWidget(AbstractWidget widget,
                                         double mouseX, double mouseY) {
        return widget.active
                && widget.visible
                && mouseX >= widget.getX()
                && mouseY >= widget.getY()
                && mouseX < widget.getX() + widget.getWidth()
                && mouseY < widget.getY() + widget.getHeight();
    }

    //---------- gui blitting (GuiGraphics#blit gained a RenderType factory in 1.21.2) ----------

    /**
     * Draws a texture region scaled to the target size.
     *
     * @param gui           the GuiGraphics instance
     * @param texture       the texture to sample
     * @param x             X coordinate to draw at
     * @param y             Y coordinate to draw at
     * @param targetWidth   width to draw
     * @param targetHeight  height to draw
     * @param srcX          source region X in the texture
     * @param srcY          source region Y in the texture
     * @param srcWidth      source region width
     * @param srcHeight     source region height
     * @param textureWidth  full texture width
     * @param textureHeight full texture height
     */
    public static void blitStretched(GuiGraphics gui, ResourceLocation texture,
                                     int x, int y,
                                     int targetWidth, int targetHeight,
                                     int srcX, int srcY,
                                     int srcWidth, int srcHeight,
                                     int textureWidth, int textureHeight) {
        gui.blit(
                RenderType::guiTextured, texture,
                x, y,
                srcX, srcY,
                targetWidth, targetHeight,
                srcWidth, srcHeight,
                textureWidth, textureHeight
        );
    }

    /**
     * Draws a texture region at 1:1 scale, repeating it when the target area is
     * larger than the texture. The source region is taken to be the target size,
     * so the UVs run past 1.0 and the sampler wraps.
     *
     * @param gui           the GuiGraphics instance
     * @param texture       the texture to sample
     * @param x             X coordinate to draw at
     * @param y             Y coordinate to draw at
     * @param targetWidth   width to fill
     * @param targetHeight  height to fill
     * @param srcX          source region X in the texture
     * @param srcY          source region Y in the texture
     * @param textureWidth  full texture width
     * @param textureHeight full texture height
     */
    public static void blitTiled(GuiGraphics gui, ResourceLocation texture,
                                 int x, int y,
                                 int targetWidth, int targetHeight,
                                 int srcX, int srcY,
                                 int textureWidth, int textureHeight) {
        gui.blit(
                RenderType::guiTextured, texture,
                x, y,
                srcX, srcY,
                targetWidth, targetHeight,
                textureWidth, textureHeight
        );
    }

    //---------- dynamic textures ----------

    /**
     * Writes a single pixel of a RGBA image.
     * <p>
     * 1.21.2 replaced NativeImage#setPixelRGBA, which took an ABGR-packed int, with
     * NativeImage#setPixel, which takes an ARGB-packed one. Callers pass components
     * so the packing order stays an implementation detail.
     *
     * @param image the image to write to; must be RGBA
     * @param x     pixel X
     * @param y     pixel Y
     * @param alpha alpha component, 0-255
     * @param red   red component, 0-255
     * @param green green component, 0-255
     * @param blue  blue component, 0-255
     */
    public static void setPixel(NativeImage image,
                                int x, int y,
                                int alpha, int red, int green, int blue) {
        image.setPixel(x, y, ARGB.color(alpha, red, green, blue));
    }

    /**
     * Binds a texture to the active GL texture unit.
     * <p>
     * Replaces TextureManager#bindForSetup, removed in 1.21.2; it was a thin
     * {@code getTexture(id).bind()} with a render-thread hop, and the lookup and bind
     * are both still public.
     *
     * @param texture id of the texture to bind
     */
    public static void bindTexture(ResourceLocation texture) {
        Minecraft.getInstance().getTextureManager().getTexture(texture).bind();
    }

    /**
     * Registers a runtime-built texture under the given id and returns it.
     * <p>
     * 1.21.2 dropped TextureManager#register(String, DynamicTexture), which used to mint
     * the id itself, so the id is now the caller's to choose - and must be unique.
     *
     * @param namespace namespace of the generated id
     * @param path      path of the generated id, below {@code dynamic/}
     * @param texture   the texture to register
     * @return the id the texture was registered under
     */
    public static ResourceLocation registerDynamicTexture(String namespace,
                                                          String path,
                                                          DynamicTexture texture) {
        ResourceLocation id = McVersionUtils.newResourceLoc(namespace, "dynamic/" + path);
        Minecraft.getInstance().getTextureManager().register(id, texture);
        return id;
    }

}
