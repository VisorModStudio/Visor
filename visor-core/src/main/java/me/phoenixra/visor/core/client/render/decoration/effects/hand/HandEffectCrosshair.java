package me.phoenixra.visor.core.client.render.decoration.effects.hand;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import me.phoenixra.visor.api.client.ClientFeature;
import me.phoenixra.visor.api.client.player.pose.PlayerPoseClient;
import me.phoenixra.visor.api.client.player.pose.PlayerPoseType;
import me.phoenixra.visor.api.client.render.VRCameraType;
import me.phoenixra.visor.api.client.render.decoration.VRDecorator;
import me.phoenixra.visor.api.client.render.decoration.annotations.RegisterVRHandEffect;
import me.phoenixra.visor.api.client.render.decoration.effects.VRHandEffect;
import me.phoenixra.visor.api.common.HandType;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.modified.client.render.GameRendererModified;
import me.phoenixra.visor.core.client.render.helpers.RenderPoseHelper;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11C;

import static me.phoenixra.visor.core.client.VisorClientImpl.MC;


@RegisterVRHandEffect
public class HandEffectCrosshair extends VRHandEffect {
    public static final String ID = "crosshair";

    private static final ResourceLocation ICONS_LOC = Gui.GUI_ICONS_LOCATION;
    private static final float BASE_SCALE = 0.125f;
    private static final float UV_SIZE = 15f / 256f;
    private static final float LIGHT_OFFSET = -0.01f;
    private static final float FULL_BRIGHTNESS = 1.0f;
    private static final float MISS_BRIGHTNESS = 0.5f;

    public HandEffectCrosshair(@NotNull VisorAddon owner) {
        super(owner);
    }

    @Override
    public void render(@NotNull HandType hand,
                       @NotNull VRCameraType cameraType,
                       @NotNull PoseStack poseStack,
                       boolean simpleHand,
                       float partialTicks) {

        // --- Prepare variables ---
        PlayerPoseClient pose = ClientContext.localPlayer.getPoseData(PlayerPoseType.RENDER);
        var rawCross = ((GameRendererModified)MC.gameRenderer).visor$getCrossVec().toVector3f();
        var aim = rawCross.sub(pose.getHand(hand).getPosition(), new Vector3f());
        float worldScale = (float)Math.sqrt(pose.getWorldScale());
        float scale = BASE_SCALE * worldScale;

        // nudge back for correct lighting
        var crossPos = rawCross.add(aim.normalize().mul(LIGHT_OFFSET));

        // light & brightness
        BlockPos lightPos = BlockPos.containing(new Vec3(crossPos));
        int lightCoords  = LevelRenderer.getLightColor(MC.level, lightPos);
        float brightness = (MC.hitResult == null || MC.hitResult.getType() == HitResult.Type.MISS)
                ? MISS_BRIGHTNESS
                : FULL_BRIGHTNESS;

        BufferBuilder buf = Tesselator.getInstance().getBuilder();

        // --- GL setup ---
        RenderSystem.setShaderColor(1, 1, 1, 1);
        MC.gameRenderer.lightTexture().turnOnLightLayer();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GL11C.GL_LEQUAL);

        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR,
                GlStateManager.DestFactor.ZERO,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
        );

        RenderSystem.setShaderTexture(0, ICONS_LOC);
        RenderSystem.setShader(GameRenderer::getRendertypeEntityCutoutNoCullShader);

        // --- Pose setup ---
        poseStack.pushPose();
        poseStack.setIdentity();
        RenderPoseHelper.applyCameraOrientation(cameraType, poseStack);

        Vector3f camPos = MC.getCameraEntity().position().toVector3f();
        Vector3f translate = crossPos.sub(camPos);
        poseStack.translate(translate.x, translate.y, translate.z);

        applyCrossHairRotation(poseStack, hand, pose);

        poseStack.scale(scale, scale, scale);

        // --- Render ---
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.NEW_ENTITY);
        Matrix4f mat = poseStack.last().pose();

        buf.vertex(mat, -1f,  1f, 0f)
                .color(brightness, brightness, brightness, 1f)
                .uv(UV_SIZE, 0f)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(lightCoords)
                .normal(0f, 0f, 1f).endVertex();
        buf.vertex(mat,  1f,  1f, 0f)
                .color(brightness, brightness, brightness, 1f)
                .uv(0f,       0f)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(lightCoords)
                .normal(0f, 0f, 1f).endVertex();
        buf.vertex(mat,  1f, -1f, 0f)
                .color(brightness, brightness, brightness, 1f)
                .uv(0f,       UV_SIZE)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(lightCoords)
                .normal(0f, 0f, 1f).endVertex();
        buf.vertex(mat, -1f, -1f, 0f)
                .color(brightness, brightness, brightness, 1f)
                .uv(UV_SIZE, UV_SIZE)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(lightCoords)
                .normal(0f, 0f, 1f).endVertex();

        BufferUploader.drawWithShader(buf.end());

        // --- Restore GL & pose ---
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.depthFunc(GL11C.GL_LEQUAL);
        poseStack.popPose();
    }

    private void applyCrossHairRotation(PoseStack poseStack,
                                        HandType hand,
                                        PlayerPoseClient pose) {
        if (MC.hitResult instanceof BlockHitResult bhr) {
            switch (bhr.getDirection()) {
                case DOWN -> {
                    rotateInDegrees(poseStack, pose.getHand(hand).getYaw(), 0, 1, 0);
                    rotateInDegrees(poseStack, -90, 1, 0, 0);
                }
                case UP -> {
                    rotateInDegrees(poseStack, -pose.getHand(hand).getYaw(), 0, 1, 0);
                    rotateInDegrees(poseStack,  90, 1, 0, 0);
                }
                case WEST -> rotateInDegrees(poseStack,  90, 0, 1, 0);
                case EAST -> rotateInDegrees(poseStack, -90, 0, 1, 0);
                case SOUTH -> rotateInDegrees(poseStack, 180, 0, 1, 0);
                default -> {}
            }
        } else {
            rotateInDegrees(poseStack, -pose.getHand(hand).getYaw(),   0, 1, 0);
            rotateInDegrees(poseStack, -pose.getHand(hand).getPitch(), 1, 0, 0);
        }
    }

    private void rotateInDegrees(PoseStack pose, float angle, float x, float y, float z) {
        pose.mulPose(new Quaternionf(new AxisAngle4f(
                angle * Mth.DEG_TO_RAD, x, y, z
        )));
    }

    @Override
    public boolean isVisible(@NotNull VRDecorator currentDecorator,
                             @NotNull HandType hand,
                             boolean simpleHand) {
        if(simpleHand){
            return false;
        }
        if(hand != ClientContext.localPlayer.getActiveHand()){
            return false;
        }
        boolean insideBlock = ((GameRendererModified) MC.gameRenderer).visor$isInBlock() > 0.0F;
        if(insideBlock){
            return false;
        }
        return ClientContext.visor.isFeatureEnabled(ClientFeature.AIM_EFFECTS);
    }


    @Override
    public @NotNull String getId() {
        return ID;
    }

}
