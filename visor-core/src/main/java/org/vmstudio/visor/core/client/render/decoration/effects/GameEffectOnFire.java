package org.vmstudio.visor.core.client.render.decoration.effects;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import org.vmstudio.visor.api.client.player.pose.VRPlayerPoseClient;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.client.render.VRRenderPass;
import org.vmstudio.visor.api.client.render.decoration.VRDecorator;
import org.vmstudio.visor.api.client.render.decoration.annotations.RegisterVRGameEffect;
import org.vmstudio.visor.api.client.render.decoration.effects.VRGameEffect;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.extensions.client.render.GameRendererExtension;
import org.vmstudio.visor.core.client.render.helpers.RenderPoseHelper;
import net.minecraft.client.renderer.CoreShaders;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11C;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;


@RegisterVRGameEffect
public class GameEffectOnFire extends VRGameEffect {

    public static final String ID = "on_fire";

    private static final float  FIRE_HALF_WIDTH  = 0.3f;
    private static final float  FIRE_ALPHA       = 0.9f;


    public GameEffectOnFire(@NotNull VisorAddon owner) {
        super(owner);
    }

    @Override
    public void render(@NotNull VRRenderPass renderPass,
                       @NotNull PoseStack stack,
                       float partialTicks) {
        // --- Prepare variables ---
        VRPlayerPoseClient renderPose = ClientContext.localPlayer.getPoseData(PlayerPoseType.RENDER);
        float fireHeight = (float)(renderPose.getHeadPivot().y()
                - ((GameRendererExtension)MC.gameRenderer)
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
                renderPass == VRRenderPass.THIRD_PERSON
                        ? GL11C.GL_LEQUAL
                        : GL11C.GL_ALWAYS
        );
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();

        RenderSystem.setShader(CoreShaders.POSITION_TEX_COLOR);
        RenderSystem.setShaderTexture(0, atlas);

        // --- Pose setup ---
        stack.pushPose();
        stack.setIdentity();
        RenderPoseHelper.applyCameraPose(renderPass, stack);

        // --- Render ---
        BufferBuilder buf;
        for (int i = 0; i < 4; i++) {
            stack.pushPose();
            // spin quad around player
            stack.mulPose(Axis.YP.rotation(
                    i * (float)Math.PI/2 - renderPose.getBodyYaw()
            ));
            stack.translate(0, -fireHeight, 0);

            Matrix4f mat = stack.last().pose();
            buf = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            buf.addVertex(mat, -FIRE_HALF_WIDTH,0, -FIRE_HALF_WIDTH)
                    .setUv(u1, v1).setColor(1,1,1,FIRE_ALPHA);
            buf.addVertex(mat,  FIRE_HALF_WIDTH,0, -FIRE_HALF_WIDTH)
                    .setUv(u0, v1).setColor(1,1,1,FIRE_ALPHA);
            buf.addVertex(mat,  FIRE_HALF_WIDTH, fireHeight,  -FIRE_HALF_WIDTH)
                    .setUv(u0, v0).setColor(1,1,1,FIRE_ALPHA);
            buf.addVertex(mat, -FIRE_HALF_WIDTH, fireHeight,  -FIRE_HALF_WIDTH)
                    .setUv(u1, v0).setColor(1,1,1,FIRE_ALPHA);
            BufferUploader.drawWithShader(buf.buildOrThrow());

            stack.popPose();
        }

        // --- Restore GL & pose ---
        RenderSystem.depthFunc(GL11C.GL_LEQUAL);
        RenderSystem.disableBlend();
        stack.popPose();
    }

    @Override
    public boolean isVisible(@NotNull VRDecorator currentDecorator) {
        return ((GameRendererExtension) MC.gameRenderer).visor$isOnFire();
    }

    @Override
    public @NotNull String getId() {
        return ID;
    }
}
