package me.phoenixra.visor.api.client.gui.helpers;

import com.mojang.blaze3d.platform.NativeImage;
import me.phoenixra.atumvr.api.misc.color.AtumColor;
import me.phoenixra.visor.api.client.gui.GuiTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class TexturesHelper {
    private TexturesHelper() {
        throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
    }

    private static final Map<AtumColor, ResourceLocation> CACHE = new ConcurrentHashMap<>();
    private static final Map<AtumColor, GuiTexture> CACHE_GUI = new ConcurrentHashMap<>();

    private static final AtumColor WHITE_COLOR = AtumColor.WHITE;
    private static final AtumColor BLACK_COLOR = AtumColor.BLACK;



    public static ResourceLocation getWhiteTexture() {
        return getColorTexture(WHITE_COLOR);
    }

    public static ResourceLocation getBlackTexture() {
        return getColorTexture(BLACK_COLOR);
    }


    public static ResourceLocation getColorTexture(AtumColor color) {
        return CACHE.computeIfAbsent(color, TexturesHelper::createAndRegister);
    }

    public static GuiTexture getColorGuiTexture(AtumColor color) {
        return CACHE_GUI.computeIfAbsent(color,
                it-> new GuiTexture(
                        getColorTexture(color),
                        0, 0, 1, 1
                )
        );
    }

    private static ResourceLocation createAndRegister(AtumColor color) {

        NativeImage img = new NativeImage(NativeImage.Format.RGBA, 1, 1, true);

        img.setPixelRGBA(0, 0, color.asInt());

        DynamicTexture tex = new DynamicTexture(img);

        String name = String.format("visor_%02x%02x%02x%02x",
                color.getRedInt(), color.getGreenInt(), color.getBlueInt(), color.getAlphaInt());

        return Minecraft.getInstance().getTextureManager().register(name, tex);
    }

}
