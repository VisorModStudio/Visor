package org.vmstudio.visor.api.client.gui.helpers;

import com.mojang.blaze3d.platform.NativeImage;
import me.phoenixra.atumvr.api.misc.color.AtumColor;
import org.vmstudio.visor.api.client.gui.GuiTexture;
import org.vmstudio.visor.api.common.utils.LoggerUtils;
import org.vmstudio.visor.api.compatibility.mcversion.render.McRenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class TexturesHelper {
    private TexturesHelper() {
        throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
    }


    private static final Map<Integer, Identifier> CACHE = new ConcurrentHashMap<>();
    private static final Map<Integer, GuiTexture> CACHE_GUI = new ConcurrentHashMap<>();


    private static final int CACHE_WARN_SIZE = 256;

    private static volatile boolean cacheSizeWarned = false;

    private static final AtumColor WHITE_COLOR = AtumColor.WHITE;
    private static final AtumColor BLACK_COLOR = AtumColor.BLACK;



    public static Identifier getWhiteTexture() {
        return getColorTexture(WHITE_COLOR);
    }

    public static Identifier getBlackTexture() {
        return getColorTexture(BLACK_COLOR);
    }



    public static Identifier getColorTexture(@NotNull AtumColor color) {

        Identifier texture = CACHE.computeIfAbsent(
                color.asInt(),
                it -> createAndRegister(color)
        );

        warnIfCacheTooLarge();
        return texture;
    }


    public static GuiTexture getColorGuiTexture(@NotNull AtumColor color) {
        return CACHE_GUI.computeIfAbsent(
                color.asInt(),
                it -> new GuiTexture(
                        getColorTexture(color),
                        0, 0, 1, 1
                )
        );
    }


    private static Identifier createAndRegister(AtumColor color) {
        int red = color.getRedInt();
        int green = color.getGreenInt();
        int blue = color.getBlueInt();
        int alpha = color.getAlphaInt();

        NativeImage img = new NativeImage(NativeImage.Format.RGBA, 1, 1, true);

        McRenderUtils.setPixelArgb(img, 0, 0,
                (alpha << 24) | (red << 16) | (green << 8) | blue
        );

        String name = String.format("visor_%02x%02x%02x%02x",
                red, green, blue, alpha);

        DynamicTexture tex = McRenderUtils.newDynamicTexture(name, img);

        return McRenderUtils.registerDynamicTexture(name, tex);
    }

    private static void warnIfCacheTooLarge() {
        if (cacheSizeWarned || CACHE.size() <= CACHE_WARN_SIZE) return;
        cacheSizeWarned = true;

        LoggerUtils.getLogger().warn(
                "TexturesHelper is holding {} distinct color textures, which are never released. "
                        + "A caller is likely passing runtime-varying colors to getColorTexture - "
                        + "those should use GuiGraphics#fill instead.",
                CACHE.size()
        );
    }

}
