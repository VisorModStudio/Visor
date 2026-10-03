package org.vmstudio.visor.core.client.render.decoration.effects.hand;

import org.vmstudio.visor.api.compatibility.mcversion.render.McGlState;
import org.vmstudio.visor.api.compatibility.mcversion.render.McShaders;
import org.joml.*;
import org.vmstudio.visor.api.compatibility.mcversion.render.McVertexBuilder;
import org.vmstudio.visor.api.compatibility.mcversion.render.McRenderUtils;
import com.mojang.blaze3d.vertex.*;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
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
import org.vmstudio.visor.compatibility.sable.SableCompatHelper;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.utils.ClientUtils;
import org.vmstudio.visor.core.client.player.VRAimPicker;
import org.vmstudio.visor.core.client.render.camera.VRCameraOverlaps;
import org.vmstudio.visor.core.client.render.helpers.RenderPoseHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11C;

import java.lang.Math;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

@RegisterVRHandEffect
public class HandEffectCrosshair extends VRHandEffect {
    public static final String ID = "crosshair";

    private static final Identifier ICONS_LOC = McRenderUtils.crosshairTexture();
    private static final float BASE_SCALE = 0.125f;
    private static final float UV_SIZE = McRenderUtils.crosshairUvSize();
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
        var aimHitPos = VRAimPicker.getAimHitPos(hand);
        if (aimHitPos == null) {
            return;
        }
        HitResult handHit = VRAimPicker.getHandHitResult(hand);
        var rawCross = aimHitPos.toVector3f();
        var aim = rawCross.sub(pose.getHand(hand).getPosition(), new Vector3f());
        float worldScale = (float)Math.sqrt(pose.getWorldScale());
        float scale = BASE_SCALE * worldScale;

        // nudge back for correct lighting
        var crossPos = rawCross.add(aim.normalize().mul(LIGHT_OFFSET));

        float brightness = (handHit == null || handHit.getType() == HitResult.Type.MISS)
                ? MISS_BRIGHTNESS
                : FULL_BRIGHTNESS;
        if (hand != ClientContext.localPlayer.getActiveHand()) {
            brightness *= INACTIVE_BRIGHTNESS;
        }
        int light = MC.level == null
                ? McRenderUtils.fullBrightLight()
                : ClientUtils.packedLightWithFloor(
                        MC.level,
                        BlockPos.containing(crossPos.x, crossPos.y, crossPos.z),
                        ShaderCompatHelper.minShaderLight());

        McVertexBuilder buf = McVertexBuilder.get();

        // --- GL setup ---
        McGlState.setShaderColor(1f, 1f, 1f, 1f);

        McGlState.enableDepthTest();
        McGlState.depthMask(true);
        McGlState.depthFunc(GL11C.GL_ALWAYS);

        McGlState.enableBlend();
        McGlState.blendFuncSeparate(
                McGlState.Blend.ONE_MINUS_DST_COLOR,
                McGlState.Blend.ONE_MINUS_SRC_COLOR,
                McGlState.Blend.ONE,
                McGlState.Blend.ZERO
        );

        McGlState.setShaderTexture(0, ICONS_LOC);
        McShaders.use(McShaders.Core.RENDERTYPE_TEXT);
        McGlState.turnOnLightLayer();

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
        buf.begin(PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP);
        Matrix4f mat = poseStack.last().pose();

        buf.vertex(mat, -1f, 1f, 0f)
                .color(brightness, brightness, brightness, 1f)
                .uv(UV_SIZE, 0f)
                .uv2(light)
                .endVertex();
        buf.vertex(mat, 1f, 1f, 0f)
                .color(brightness, brightness, brightness, 1f)
                .uv(0f, 0f)
                .uv2(light)
                .endVertex();
        buf.vertex(mat, 1f, -1f, 0f)
                .color(brightness, brightness, brightness, 1f)
                .uv(0f, UV_SIZE)
                .uv2(light)
                .endVertex();
        buf.vertex(mat, -1f, -1f, 0f)
                .color(brightness, brightness, brightness, 1f)
                .uv(UV_SIZE, UV_SIZE)
                .uv2(light)
                .endVertex();

        buf.draw();

        // --- Restore GL & pose ---
        McGlState.turnOffLightLayer();
        McGlState.setShaderColor(1f, 1f, 1f, 1f);
        McGlState.defaultBlendFunc();
        McGlState.disableBlend();
        McGlState.enableDepthTest();
        McGlState.depthFunc(GL11C.GL_LEQUAL);
        poseStack.popPose();
    }

    private void applyCrossHairRotation(PoseStack poseStack,
                                        HandType hand,
                                        VRPlayerPoseClient pose,
                                        HitResult hit) {
        float yaw = pose.getHand(hand).getYawDegrees();

        if (hit instanceof BlockHitResult bhr && bhr.getType() != HitResult.Type.MISS) {
            if (SableCompatHelper.isLoaded()) {
                Quaterniond subLevelOrientation = SableCompatHelper.getSubLevelOrientation(MC.level, hit.getLocation());
                if (subLevelOrientation != null) {
                    yaw = 0; // otherwise vertical alignment on block would be broken
                    McRenderUtils.rotate(poseStack,
                            new Quaternionf(
                                    (float) subLevelOrientation.x,
                                    (float) subLevelOrientation.y,
                                    (float) subLevelOrientation.z,
                                    (float) subLevelOrientation.w
                            )
                    );
                }
            }

            switch (bhr.getDirection()) {
                case DOWN -> {
                    rotateInDegrees(poseStack, yaw, 0, 1, 0);
                    rotateInDegrees(poseStack, -90, 1, 0, 0);
                }
                case UP -> {
                    rotateInDegrees(poseStack, -yaw, 0, 1, 0);
                    rotateInDegrees(poseStack,  90, 1, 0, 0);
                }
                case WEST -> rotateInDegrees(poseStack,  90, 0, 1, 0);
                case EAST -> rotateInDegrees(poseStack, -90, 0, 1, 0);
                case SOUTH -> rotateInDegrees(poseStack, 180, 0, 1, 0);
                default -> {}
            }
        } else {
            rotateInDegrees(poseStack, -yaw,   0, 1, 0);
            rotateInDegrees(poseStack, -pose.getHand(hand).getPitchDegrees(), 1, 0, 0);
        }
    }

    private void rotateInDegrees(PoseStack pose, float angle, float x, float y, float z) {
        McRenderUtils.rotate(pose, new Quaternionf(new AxisAngle4f(
                angle * Mth.DEG_TO_RAD, x, y, z
        )));
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
            if(VRAimPicker.getAimHitPos(hand) == null){
                return false;
            }
        }
        boolean insideBlock = VRCameraOverlaps.isInBlock();
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
