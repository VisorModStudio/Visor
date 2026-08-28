package org.vmstudio.visor.core.client.render.decoration.effects.hand;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import org.vmstudio.visor.api.client.ClientFeature;
import org.vmstudio.visor.api.client.player.pose.VRPlayerPoseClient;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.client.render.VRRenderPass;
import org.vmstudio.visor.api.client.render.decoration.VRDecorator;
import org.vmstudio.visor.api.client.render.decoration.annotations.RegisterVRHandEffect;
import org.vmstudio.visor.api.client.render.decoration.effects.VRHandEffect;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.api.server.VRServerSettings;
import org.vmstudio.visor.compatibility.ShaderCompatHelper;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.extensions.client.render.GameRendererExtension;
import org.vmstudio.visor.core.client.render.helpers.RenderPoseHelper;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GameRenderer;
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

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

@RegisterVRHandEffect
public class HandEffectCrosshair extends VRHandEffect {
    public static final String ID = "crosshair";

    private static final ResourceLocation ICONS_LOC = Gui.GUI_ICONS_LOCATION;
    private static final float BASE_SCALE = 0.125f;
    private static final float UV_SIZE = 15f / 256f;
    private static final float LIGHT_OFFSET = -0.01f;
    private static final float FULL_BRIGHTNESS = 1.0f;
    private static final float MISS_BRIGHTNESS = 0.5f;
    private static final float INACTIVE_BRIGHTNESS = 0.4f;

    public HandEffectCrosshair(@NotNull VisorAddon owner) {
        super(owner);
    }

    @Override
    public void render(@NotNull HandType hand,
                       @NotNull VRRenderPass renderPass,
                       @NotNull PoseStack poseStack,
                       boolean guiHand,
                       float partialTicks) {

        // --- Prepare variables ---
        VRPlayerPoseClient pose = ClientContext.localPlayer.getPoseData(PlayerPoseType.RENDER);
        var renderer = (GameRendererExtension) MC.gameRenderer;
        var crossVec = renderer.visor$getCrossVec(hand);
        if (crossVec == null) {
            return;
        }
        HitResult handHit = renderer.visor$getHandHitResult(hand);
        var rawCross = crossVec.toVector3f();
        var aim = rawCross.sub(pose.getHand(hand).getPosition(), new Vector3f());
        float worldScale = (float)Math.sqrt(pose.getWorldScale());
        float scale = BASE_SCALE * worldScale;

        // nudge back for correct lighting
        var crossPos = rawCross.add(aim.normalize().mul(LIGHT_OFFSET));

        float baseBrightness = (handHit == null || handHit.getType() == HitResult.Type.MISS)
                ? MISS_BRIGHTNESS
                : FULL_BRIGHTNESS;
        if (hand != ClientContext.localPlayer.getActiveHand()) {
            baseBrightness *= INACTIVE_BRIGHTNESS;
        }
        float brightness = getBrightness(crossPos) * baseBrightness;

        BufferBuilder buf = Tesselator.getInstance().getBuilder();

        // --- GL setup ---
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.depthFunc(GL11C.GL_ALWAYS);

        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR,
                GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO
        );

        RenderSystem.setShaderTexture(0, ICONS_LOC);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);

        // --- Pose setup ---
        poseStack.pushPose();
        poseStack.setIdentity();
        RenderPoseHelper.applyCameraOrientation(renderPass, poseStack);

        Vector3f camPos = MC.getCameraEntity().position().toVector3f();
        Vector3f translate = crossPos.sub(camPos);
        poseStack.translate(translate.x, translate.y, translate.z);

        applyCrossHairRotation(poseStack, hand, pose, handHit);

        poseStack.scale(scale, scale, scale);

        // --- Render ---
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        Matrix4f mat = poseStack.last().pose();

        buf.vertex(mat, -1f,1f,0f)
                .uv(UV_SIZE, 0f)
                .color(brightness, brightness, brightness, 1f)
                .endVertex();
        buf.vertex(mat,1f,1f,0f)
                .uv(0f,0f)
                .color(brightness, brightness, brightness, 1f)
                .endVertex();
        buf.vertex(mat,1f, -1f, 0f)
                .uv(0f, UV_SIZE)
                .color(brightness, brightness, brightness, 1f)
                .endVertex();
        buf.vertex(mat,-1f, -1f,0f)
                .uv(UV_SIZE, UV_SIZE)
                .color(brightness, brightness, brightness, 1f)
                .endVertex();

        BufferUploader.drawWithShader(buf.end());

        // --- Restore GL & pose ---
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GL11C.GL_LEQUAL);
        poseStack.popPose();
    }

    private void applyCrossHairRotation(PoseStack poseStack,
                                        HandType hand,
                                        VRPlayerPoseClient pose,
                                        HitResult hit) {
        if (hit instanceof BlockHitResult bhr && bhr.getType() != HitResult.Type.MISS) {
            switch (bhr.getDirection()) {
                case DOWN -> {
                    rotateInDegrees(poseStack, pose.getHand(hand).getYawDegrees(), 0, 1, 0);
                    rotateInDegrees(poseStack, -90, 1, 0, 0);
                }
                case UP -> {
                    rotateInDegrees(poseStack, -pose.getHand(hand).getYawDegrees(), 0, 1, 0);
                    rotateInDegrees(poseStack,  90, 1, 0, 0);
                }
                case WEST -> rotateInDegrees(poseStack,  90, 0, 1, 0);
                case EAST -> rotateInDegrees(poseStack, -90, 0, 1, 0);
                case SOUTH -> rotateInDegrees(poseStack, 180, 0, 1, 0);
                default -> {}
            }
        } else {
            rotateInDegrees(poseStack, -pose.getHand(hand).getYawDegrees(),   0, 1, 0);
            rotateInDegrees(poseStack, -pose.getHand(hand).getPitchDegrees(), 1, 0, 0);
        }
    }

    private void rotateInDegrees(PoseStack pose, float angle, float x, float y, float z) {
        pose.mulPose(new Quaternionf(new AxisAngle4f(
                angle * Mth.DEG_TO_RAD, x, y, z
        )));
    }

    private float getBrightness(Vector3f crossPos) {
        if (MC.level == null) return 1.0f; // how you can get this? idk, just notnull check for myself =)

        float rawLight = MC.level.getMaxLocalRawBrightness(
                BlockPos.containing(new Vec3(crossPos))
        );
        float light =Math.max(rawLight, ShaderCompatHelper.minShaderLight());
        return light / (float) MC.level.getMaxLightLevel();
    }

    @Override
    public boolean isVisible(@NotNull VRDecorator currentDecorator,
                             @NotNull HandType hand,
                             boolean guiHand) {
        if(guiHand){
            return false;
        }
        if(hand != ClientContext.localPlayer.getActiveHand()){
            if(!VRServerSettings.isTwoHandedVR()){
                return false;
            }
            if(((GameRendererExtension) MC.gameRenderer).visor$getCrossVec(hand) == null){
                return false;
            }
        }
        boolean insideBlock = ((GameRendererExtension) MC.gameRenderer).visor$isInBlock();
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
