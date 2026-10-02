package org.vmstudio.visor.api.compatibility.mcversion.render;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.model.geom.ModelPart;
//? if >=1.21 && <1.21.6 {
/*import net.minecraft.client.gui.GuiGraphicsExtractor;
*///?}
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
//? if >=26.1 {
import net.minecraft.util.LightCoordsUtil;
//?} else {
/*import net.minecraft.client.renderer.LightTexture;
*///?}
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
//? if >=1.21.11 {
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.sprite.AtlasManager;
import net.minecraft.data.AtlasIds;
import net.minecraft.world.level.MoonPhase;
//?}
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.LevelReader;
//? if >=1.21.11 {
import net.minecraft.client.renderer.rendertype.RenderTypes;
//?}
//? if >=1.21.9 {
import net.minecraft.world.entity.player.PlayerModelType;
//?}
import org.jetbrains.annotations.Nullable;
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

    public static Identifier getSkinTexture(AbstractClientPlayer player) {
        //? if >=1.21.9 {
        return player.getSkin().body().texturePath();
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

    // ------- RENDER TYPES -------

    public static RenderType entityTranslucent(Identifier texture) {
        //? if >=1.21.11 {
        return RenderTypes.entityTranslucent(texture);
        //?} else {
        /*return RenderType.entityTranslucent(texture);
        *///?}
    }

    // ------- SPRITES -------

    // how far the UVs are pulled towards the sprite centre against bleeding; 1.21.11 pads sprites instead
    public static float uvShrinkRatio(TextureAtlasSprite sprite) {
        //? if >=1.21.11 {
        return 0.0F;
        //?} else {
        /*return sprite.uvShrinkRatio();
        *///?}
    }

    // ------- CELESTIALS -------

    /**
     * A sun or moon image: the texture and the UV rect in it
     */
    public record CelestialSprite(Identifier texture, float u0, float v0, float u1, float v1) {
    }

    // null while the celestials atlas of 1.21.11 is not stitched yet
    @Nullable
    public static CelestialSprite sunSprite() {
        //? if >=1.21.11 {
        TextureAtlas atlas = celestialsAtlas();
        return atlas == null ? null : celestialSprite(atlas, McVersionUtils.newResourceLoc("sun"));
        //?} else {
        /*return new CelestialSprite(McVersionUtils.newResourceLoc("textures/environment/sun.png"), 0f, 0f, 1f, 1f);
        *///?}
    }

    // phase 0..7, full moon to waxing gibbous
    @Nullable
    public static CelestialSprite moonSprite(int phase) {
        //? if >=1.21.11 {
        TextureAtlas atlas = celestialsAtlas();
        return atlas == null ? null : celestialSprite(atlas,
                McVersionUtils.newResourceLoc("moon/" + MoonPhase.values()[phase & 7].getSerializedName()));
        //?} else {
        /*float u0 = (phase % 4) / 4f;
        float v0 = ((int) (phase / 4f)) / 2f;
        return new CelestialSprite(McVersionUtils.newResourceLoc("textures/environment/moon_phases.png"),
                u0, v0, u0 + 0.25f, v0 + 0.5f);
        *///?}
    }

    // 1.21.11 moved sun.png and moon_phases.png into the celestials atlas
    //? if >=1.21.11 {
    @Nullable
    private static TextureAtlas celestialsAtlas() {
        AtlasManager manager = Minecraft.getInstance().getAtlasManager();
        if (manager == null) {
            return null;
        }
        try {
            return manager.getAtlasOrThrow(AtlasIds.CELESTIALS);
        } catch (RuntimeException notStitched) {
            return null;
        }
    }

    private static CelestialSprite celestialSprite(TextureAtlas atlas, Identifier id) {
        TextureAtlasSprite sprite = atlas.getSprite(id);
        return new CelestialSprite(atlas.location(), sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1());
    }
    //?}

    // ------- LIGHT -------

    public static int fullBrightLight() {
        //? if >=26.1 {
        return LightCoordsUtil.FULL_BRIGHT;
        //?} else {
        /*return LightTexture.FULL_BRIGHT;
        *///?}
    }

    public static int packedLight(LevelReader level, BlockPos pos) {
        //? if >=26.2 {
        return LightCoordsUtil.getLightCoords(level, pos);
        //?} elif >=26.1 {
        /*return LevelRenderer.getLightCoords(level, pos);
        *///?} else {
        /*return LevelRenderer.getLightColor(level, pos);
        *///?}
    }

    // ------- CROSSHAIR -------

    public static Identifier crosshairTexture() {
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

    public static void setShaderTexture(int unit, Identifier texture) {
        McGlState.setShaderTexture(unit, texture);
    }

    public static void updateDisplay(Window window) {
        //? if >=26.1 && <26.2 {
        /*com.mojang.blaze3d.systems.RenderSystem.flipFrame(null);
        *///?} elif >=1.21.2 && <26.1 {
        /*window.updateDisplay(null);
        *///?} elif <1.21.2 {
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

    public static Identifier registerDynamicTexture(String name, DynamicTexture texture) {
        //? if >=1.21.4 {
        Identifier id = McVersionUtils.newResourceLoc("visor",
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
        //? if >=26.2 {
        Minecraft minecraft = Minecraft.getInstance();
        var deltaTracker = minecraft.getDeltaTracker();
        try (var mainThreadGizmos = minecraft.levelExtractor.collectPerFrameMainThreadGizmos()) {
            renderer.update(deltaTracker);
            renderer.extract(deltaTracker, renderLevel);
        }
        try (var renderThreadGizmos = minecraft.levelRenderer.collectPerFrameRenderThreadGizmos()) {
            renderer.render(deltaTracker, renderLevel);
        }
        //?} elif >=26.1 {
        /*var deltaTracker = Minecraft.getInstance().getDeltaTracker();
        renderer.update(deltaTracker, renderLevel);
        renderer.extract(deltaTracker, renderLevel);
        renderer.render(deltaTracker, renderLevel);
        *///?} elif >=1.21.2 {
        /*renderer.render(Minecraft.getInstance().getDeltaTracker(), renderLevel);
        *///?} elif >=1.21 {
        /*renderer.render(Minecraft.getInstance().getTimer(), renderLevel);
        *///?} else {
        /*renderer.render(partialTicks, nanoTime, renderLevel);
        *///?}
    }

    public static void endRenderPass() {
        //? if >=26.2 {
        McFeatureRenderer.endFrame();
        com.mojang.blaze3d.systems.RenderSystem.getDevice().createCommandEncoder().submit();
        //?}
    }

    public static void renderItemActivationAnimation(GameRenderer renderer, float partialTicks) {
        //? if >=1.21.9 {
        // 1.21.9 only queues it: draw it now, under the caller's matrices and the ITEMS_3D lights it just set
        renderer.screenEffectRenderer.renderItemActivationAnimation(
                new PoseStack(), partialTicks, McFeatureRenderer.collector());
        McFeatureRenderer.render();
        //?} elif >=1.21.6 {
        /*// 1.21.6 moved the animation onto ScreenEffectRenderer
        renderer.screenEffectRenderer.renderItemActivationAnimation(new PoseStack(), partialTicks);
        *///?} elif >=1.21 {
        /*Minecraft minecraft = Minecraft.getInstance();
        renderer.renderItemActivationAnimation(
                new GuiGraphicsExtractor(minecraft, minecraft.renderBuffers().bufferSource()),
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
