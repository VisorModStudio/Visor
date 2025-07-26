package me.phoenixra.visor.core.client.render.decoration.effects;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import me.phoenixra.visor.api.client.data.PoseData;
import me.phoenixra.visor.api.client.data.PoseDataType;
import me.phoenixra.visor.api.client.render.VRDisplay;
import me.phoenixra.visor.api.client.render.decoration.VRDecorator;
import me.phoenixra.visor.api.client.render.decoration.annotations.RegisterVRGameEffect;
import me.phoenixra.visor.api.client.render.decoration.effects.VRGameEffect;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.modified.client.render.GameRendererModified;
import me.phoenixra.visor.core.client.render.helpers.RenderPoseHelper;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11C;

import static me.phoenixra.visor.core.client.VisorClientImpl.MC;


@RegisterVRGameEffect
public class GameEffectOnFire extends VRGameEffect {

    public static final String ID = "on_fire";

    private static final float  FIRE_HALF_WIDTH  = 0.3f;
    private static final float  FIRE_ALPHA       = 0.9f;


    public GameEffectOnFire(@NotNull VisorAddon owner) {
        super(owner);
    }

    @Override
    public void render(@NotNull VRDisplay display,
                       @NotNull PoseStack stack,
                       float partialTicks) {
        // --- Prepare variables ---
        PoseData renderPose = ClientContext.player.getPoseData(PoseDataType.RENDER);
        float fireHeight = (float)(renderPose.getHeadPivot().y()
                - ((GameRendererModified)MC.gameRenderer)
                .visor$getCameraEntityCache()
                .getY());

        TextureAtlasSprite sprite = ModelBakery.FIRE_1.sprite();
        ResourceLocation atlas = sprite.atlasLocation();
        float uMin = sprite.getU0();
        float uMax = sprite.getU1();
        float vMin = sprite.getV0();
        float vMax = sprite.getV1();
        float midU = (uMin + uMax) * 0.5f;
        float midV = (vMin + vMax) * 0.5f;
        float shrink = sprite.uvShrinkRatio();

        float u0 = Mth.lerp(shrink, uMin, midU);
        float u1 = Mth.lerp(shrink, uMax, midU);
        float v0 = Mth.lerp(shrink, vMin, midV);
        float v1 = Mth.lerp(shrink, vMax, midV);

        // --- GL setup ---
        RenderSystem.depthFunc(
                display == VRDisplay.THIRD_PERSON
                        ? GL11C.GL_LEQUAL
                        : GL11C.GL_ALWAYS
        );
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, atlas);

        // --- Pose setup ---
        stack.pushPose();
        stack.setIdentity();
        RenderPoseHelper.applyDisplayPose(display, stack);

        // --- Render ---
        BufferBuilder buf = Tesselator.getInstance().getBuilder();
        for (int i = 0; i < 4; i++) {
            stack.pushPose();
            // spin quad around player
            stack.mulPose(Axis.YP.rotation(
                    i * (float)Math.PI/2 - renderPose.getBodyYaw()
            ));
            stack.translate(0, -fireHeight, 0);

            Matrix4f mat = stack.last().pose();
            buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            buf.vertex(mat, -FIRE_HALF_WIDTH,0, -FIRE_HALF_WIDTH)
                    .uv(u1, v1).color(1,1,1,FIRE_ALPHA).endVertex();
            buf.vertex(mat,  FIRE_HALF_WIDTH,0, -FIRE_HALF_WIDTH)
                    .uv(u0, v1).color(1,1,1,FIRE_ALPHA).endVertex();
            buf.vertex(mat,  FIRE_HALF_WIDTH, fireHeight,  -FIRE_HALF_WIDTH)
                    .uv(u0, v0).color(1,1,1,FIRE_ALPHA).endVertex();
            buf.vertex(mat, -FIRE_HALF_WIDTH, fireHeight,  -FIRE_HALF_WIDTH)
                    .uv(u1, v0).color(1,1,1,FIRE_ALPHA).endVertex();
            BufferUploader.drawWithShader(buf.end());

            stack.popPose();
        }

        // --- Restore GL & pose ---
        RenderSystem.depthFunc(GL11C.GL_LEQUAL);
        RenderSystem.disableBlend();
        stack.popPose();
    }

    @Override
    public boolean isVisible(@NotNull VRDecorator currentDecorator) {
        return ((GameRendererModified) MC.gameRenderer).visor$isOnFire();
    }

    @Override
    public @NotNull String getId() {
        return ID;
    }
}
