package org.vmstudio.visor.core.client.render.decoration.effects;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11C;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.client.player.pose.VRPlayerPoseClient;
import org.vmstudio.visor.api.client.render.VRRenderPass;
import org.vmstudio.visor.api.client.render.decoration.VRDecorator;
import org.vmstudio.visor.api.client.render.decoration.annotations.RegisterVRGameEffect;
import org.vmstudio.visor.api.client.render.decoration.effects.VRGameEffect;
import org.vmstudio.visor.api.client.settings.VRClientSettings;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.api.common.player.VRPose;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.render.VRShaders;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;


@RegisterVRGameEffect
public class GameEffectPumpkin extends VRGameEffect {

    public static final String ID = "pumpkin";
    private static final ResourceLocation PUMPKIN_BLUR_LOCATION = new ResourceLocation("textures/misc/pumpkinblur.png");

    private static final float FACE_DISTANCE = 0.24F;
    private static final float FACE_RADIUS = 0.24F;
    private static final float EYE_LINE = 0.34F;
    private static final float BORDER_SCALE = 6.0F;
    private static final float FADE_SPEED = 0.5F;

    private float opacity;


    public GameEffectPumpkin(@NotNull VisorAddon owner) {
        super(owner);
    }

    @Override
    public void render(@NotNull VRRenderPass renderPass,
                       @NotNull PoseStack poseStack,
                       float partialTicks) {
        if (renderPass == VRRenderPass.worldUpdater()) {
            updateOpacity();
        }
        if (!renderPass.isFirstPerson() || opacity <= 0.0F) {
            return;
        }

        // --- Prepare shader ---
        VRShaders.getPumpkinOverlay().prepare(opacity);
        ShaderInstance shader = VRShaders.getPumpkinOverlay().getHandle();

        // --- GL setup ---
        RenderSystem.setShader(() -> shader);
        RenderSystem.setShaderTexture(0, PUMPKIN_BLUR_LOCATION);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthFunc(GL11C.GL_ALWAYS);
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();

        // --- Render ---
        VRPlayerPoseClient renderPose = ClientContext.localPlayer.getPoseData(PlayerPoseType.RENDER);
        float scale = renderPose.getWorldScale();

        poseStack.pushPose();
        poseStack.setIdentity();
        renderCarvedFace(
                poseStack.last().pose(),
                faceCenter(renderPose, renderPass, scale),
                FACE_RADIUS * BORDER_SCALE * scale
        );
        poseStack.popPose();

        // --- Restore GL ---
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.depthFunc(GL11C.GL_LEQUAL);
        RenderSystem.disableBlend();
    }

    private Vector3f faceCenter(VRPlayerPoseClient renderPose,
                                VRRenderPass renderPass,
                                float scale) {
        VRPose camera = renderPose.getCameraPose(renderPass);

        Vector3f eyeOffset = camera.getPosition()
                .sub(renderPose.getHmd().getPosition(), new Vector3f());

        return camera.inverseTransformDirection(eyeOffset)
                .negate()
                .sub(0.0F, EYE_LINE * FACE_RADIUS * scale, FACE_DISTANCE * scale);
    }

    private void renderCarvedFace(Matrix4f matrix, Vector3f center, float radius) {
        float x0 = center.x - radius;
        float x1 = center.x + radius;
        float y0 = center.y - radius;
        float y1 = center.y + radius;

        float uv0 = 0.5F - 0.5F * BORDER_SCALE;
        float uv1 = 0.5F + 0.5F * BORDER_SCALE;

        BufferBuilder buf = Tesselator.getInstance().getBuilder();
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buf.vertex(matrix, x0, y0, center.z).uv(uv0, uv1).endVertex();
        buf.vertex(matrix, x1, y0, center.z).uv(uv1, uv1).endVertex();
        buf.vertex(matrix, x1, y1, center.z).uv(uv1, uv0).endVertex();
        buf.vertex(matrix, x0, y1, center.z).uv(uv0, uv0).endVertex();
        BufferUploader.drawWithShader(buf.end());
    }

    private void updateOpacity() {
        float target = isMasked() ? 1.0F : 0.0F;

        opacity = Mth.lerp(
                Math.min(1.0F, MC.getDeltaFrameTime() * FADE_SPEED),
                opacity,
                target
        );
        if (Math.abs(target - opacity) < 0.01F) {
            opacity = target;
        }
    }

    private boolean isMasked() {
        if (!VRClientSettings.isPumpkinEffectEnabled()
                || MC.player == null
                || MC.player.isSpectator()) {
            return false;
        }
        ItemStack headItem = MC.player.getInventory().getArmor(3);
        return headItem.getItem() == Blocks.CARVED_PUMPKIN.asItem()
                && (!headItem.hasTag() || headItem.getTag().getInt("CustomModelData") == 0);
    }

    @Override
    public boolean isVisible(@NotNull VRDecorator currentDecorator) {
        return opacity > 0.0F || isMasked();
    }

    @Override
    public @NotNull String getId() {
        return ID;
    }
}
