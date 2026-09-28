package org.vmstudio.visor.api.compatibility.mcversion.render;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.model.geom.ModelPart;
//? if >=1.21 && <1.21.6 {
/*import net.minecraft.client.gui.GuiGraphics;
*///?}
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
//? if >=1.21.9 {
import net.minecraft.world.entity.player.PlayerModelType;
//?}
import org.joml.Matrix4f;
import org.vmstudio.visor.api.compatibility.mcversion.McVersionUtils;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Cross-mc-version Utils for rendering methods
 */
public class McRenderUtils {
    private McRenderUtils() {
        throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
    }

    // ------- PLAYER SKIN -------

    public static ResourceLocation getSkinTexture(AbstractClientPlayer player) {
        //? if >=1.21.9 {
        return player.getSkin().body().id();
        //?} elif >=1.20.2 {
        /*return player.getSkin().texture();
        *///?} else {
        /*return player.getSkinTextureLocation();
        *///?}
    }

    public static String getModelName(AbstractClientPlayer player) {
        //? if >=1.21.9 {
        // PlayerModelType.getSerializedName() is "wide", the model registry key stayed "default"
        return player.getSkin().model() == PlayerModelType.SLIM ? "slim" : "default";
        //?} elif >=1.20.2 {
        /*return player.getSkin().model().id();
        *///?} else {
        /*return player.getModelName();
        *///?}
    }

    // ------- CROSSHAIR -------

    public static ResourceLocation crosshairTexture() {
        //? if >=1.20.2 {
        return McVersionUtils.newResourceLoc("minecraft", "textures/gui/sprites/hud/crosshair.png");
        //?} else {
        /*return Gui.GUI_ICONS_LOCATION;
        *///?}
    }

    public static float crosshairUvSize() {
        //? if >=1.20.2 {
        return 1f;
        //?} else {
        /*return 15f / 256f;
        *///?}
    }

    // ------- RENDER STATE -------

    public static void clear(int mask) {
        McGlState.clear(mask);
    }

    public static void setShaderTexture(int unit, ResourceLocation texture) {
        McGlState.setShaderTexture(unit, texture);
    }

    public static void updateDisplay(Window window) {
        //? if >=1.21.2 {
        window.updateDisplay(null);
        //?} else {
        /*window.updateDisplay();
        *///?}
    }

    // ------- TEXTURES -------

    private static final AtomicInteger DYNAMIC_TEXTURE_ID = new AtomicInteger();

    public static DynamicTexture newDynamicTexture(String name, NativeImage image) {
        //? if >=1.21.5 {
        return new DynamicTexture(() -> name, image);
        //?} else {
        /*return new DynamicTexture(image);
        *///?}
    }

    public static ResourceLocation registerDynamicTexture(String name, DynamicTexture texture) {
        //? if >=1.21.4 {
        ResourceLocation id = McVersionUtils.newResourceLoc("visor",
                "dynamic/" + name + "_" + DYNAMIC_TEXTURE_ID.incrementAndGet());
        Minecraft.getInstance().getTextureManager().register(id, texture);
        return id;
        //?} else {
        /*return Minecraft.getInstance().getTextureManager().register(name, texture);
        *///?}
    }

    public static void setPixelArgb(NativeImage image, int x, int y, int argb) {
        //? if >=1.21.2 {
        image.setPixel(x, y, argb);
        //?} else {
        /*int abgr = (argb & 0xFF00FF00) | ((argb >> 16) & 0xFF) | ((argb & 0xFF) << 16);
        image.setPixelRGBA(x, y, abgr);
        *///?}
    }

    // ------- TIMING -------

    public static float deltaFrameTicks() {
        //? if >=1.21.2 {
        return Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaTicks();
        //?} elif >=1.21 {
        /*return Minecraft.getInstance().getTimer().getGameTimeDeltaTicks();
        *///?} else {
        /*return Minecraft.getInstance().getDeltaFrameTime();
        *///?}
    }

    public static float partialTick() {
        //? if >=1.21.2 {
        return Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);
        //?} elif >=1.21 {
        /*return Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
        *///?} else {
        /*return Minecraft.getInstance().getFrameTime();
        *///?}
    }

    // ------- MODEL PARTS -------

    public static void renderModelPart(ModelPart part,
                                       PoseStack poseStack,
                                       VertexConsumer consumer,
                                       int packedLight,
                                       int packedOverlay) {
        //? if >=1.21 {
        part.render(poseStack, consumer, packedLight, packedOverlay, 0xFFFFFFFF);
        //?} else {
        /*part.render(poseStack, consumer, packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
        *///?}
    }

    // ------- GAME RENDERER -------

    public static void renderGame(GameRenderer renderer,
                                  float partialTicks,
                                  long nanoTime,
                                  boolean renderLevel) {
        //? if >=1.21.2 {
        renderer.render(Minecraft.getInstance().getDeltaTracker(), renderLevel);
        //?} elif >=1.21 {
        /*renderer.render(Minecraft.getInstance().getTimer(), renderLevel);
        *///?} else {
        /*renderer.render(partialTicks, nanoTime, renderLevel);
        *///?}
    }

    public static void renderItemActivationAnimation(GameRenderer renderer, float partialTicks) {
        //? if >=1.21.9 {
        // 1.21.9 defers it: the node is drawn by the frame's FeatureRenderDispatcher
        renderer.screenEffectRenderer.renderItemActivationAnimation(
                new PoseStack(), partialTicks, renderer.getSubmitNodeStorage());
        //?} elif >=1.21.6 {
        /*// 1.21.6 moved the animation onto ScreenEffectRenderer
        renderer.screenEffectRenderer.renderItemActivationAnimation(new PoseStack(), partialTicks);
        *///?} elif >=1.21 {
        /*Minecraft minecraft = Minecraft.getInstance();
        renderer.renderItemActivationAnimation(
                new GuiGraphics(minecraft, minecraft.renderBuffers().bufferSource()),
                partialTicks
        );
        *///?} else {
        /*renderer.renderItemActivationAnimation(0, 0, partialTicks);
        *///?}
    }

    // ------- MATRICES -------

    // 1.20.5 renamed PoseStack.mulPoseMatrix to mulPose
    public static void mulPose(PoseStack poseStack, Matrix4f matrix) {
        //? if >=1.20.5 {
        poseStack.mulPose(matrix);
        //?} else {
        /*poseStack.mulPoseMatrix(matrix);
        *///?}
    }
}
