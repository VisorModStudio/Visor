package me.phoenixra.visor.core.client.render.helpers;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static me.phoenixra.visor.core.client.VisorClientImpl.MC;

public class TexturesHelper {
    private TexturesHelper() {
        throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
    }

    private static final Map<Color, ResourceLocation> CACHE = new ConcurrentHashMap<>();

    private static final Color WHITE_COLOR = Color.WHITE;
    private static final Color BLACK_COLOR = Color.BLACK;



    public static ResourceLocation getWhiteTexture() {
        return getSolidColorTexture(WHITE_COLOR);
    }

    public static ResourceLocation getBlackTexture() {
        return getSolidColorTexture(BLACK_COLOR);
    }


    public static ResourceLocation getSolidColorTexture(Color color) {
        return CACHE.computeIfAbsent(color, TexturesHelper::createAndRegister);
    }

    private static ResourceLocation createAndRegister(Color color) {

        NativeImage img = new NativeImage(NativeImage.Format.RGBA, 1, 1, true);

        img.setPixelRGBA(0, 0, color.getRGB());

        DynamicTexture tex = new DynamicTexture(img);

        String name = String.format("visor_%02x%02x%02x%02x",
                color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());

        return MC.getTextureManager().register(name, tex);
    }

}
