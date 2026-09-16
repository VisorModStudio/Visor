package org.vmstudio.visor.core.client.render.decoration.effects;

import org.vmstudio.visor.api.compatibility.mcversion.render.McGlState;
import org.vmstudio.visor.api.compatibility.mcversion.render.McShaders;
import org.vmstudio.visor.api.compatibility.mcversion.render.McVertexBuilder;
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
import org.vmstudio.visor.core.client.render.camera.VRCameraEntitySwap;
import org.vmstudio.visor.core.client.render.camera.VRCameraOverlaps;
import org.vmstudio.visor.core.client.render.helpers.RenderPoseHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11C;


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
                - VRCameraEntitySwap.getCameraEntityCache()
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
        McGlState.depthFunc(
                renderPass == VRRenderPass.THIRD_PERSON
                        ? GL11C.GL_LEQUAL
                        : GL11C.GL_ALWAYS
        );
        McGlState.enableBlend();
        McGlState.defaultBlendFunc();
        McGlState.enableDepthTest();

        McShaders.use(McShaders.Core.POSITION_TEX_COLOR);
        McGlState.setShaderTexture(0, atlas);

        // --- Pose setup ---
        stack.pushPose();
        stack.setIdentity();
        RenderPoseHelper.applyCameraPose(renderPass, stack);

        // --- Render ---
        McVertexBuilder buf = McVertexBuilder.get();
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
            buf.draw();

            stack.popPose();
        }

        // --- Restore GL & pose ---
        McGlState.depthFunc(GL11C.GL_LEQUAL);
        McGlState.disableBlend();
        stack.popPose();
    }

    @Override
    public boolean isVisible(@NotNull VRDecorator currentDecorator) {
        return VRCameraOverlaps.isOnFire();
    }

    @Override
    public @NotNull String getId() {
        return ID;
    }
}
