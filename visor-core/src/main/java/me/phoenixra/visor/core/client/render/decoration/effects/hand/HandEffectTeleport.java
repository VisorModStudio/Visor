package me.phoenixra.visor.core.client.render.decoration.effects.hand;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import me.phoenixra.atumvr.api.misc.color.AtumColor;
import me.phoenixra.visor.api.client.gui.helpers.TexturesHelper;
import me.phoenixra.visor.api.client.player.pose.PlayerPoseType;
import me.phoenixra.visor.api.client.render.VRCameraType;
import me.phoenixra.visor.api.client.render.decoration.VRDecorator;
import me.phoenixra.visor.api.client.render.decoration.annotations.RegisterVRHandEffect;
import me.phoenixra.visor.api.client.render.decoration.effects.VRHandEffect;
import me.phoenixra.visor.api.common.HandType;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.compatibility.ShadersHelper;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.VisorState;
import me.phoenixra.visor.core.client.render.VRShaders;
import me.phoenixra.visor.core.client.render.helpers.RenderHelper;
import me.phoenixra.visor.core.client.render.helpers.RenderPoseHelper;
import me.phoenixra.visor.core.client.settings.VRClientSettings;
import me.phoenixra.visor.core.client.tasks.movement.TaskTeleport;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static me.phoenixra.visor.core.client.VisorClientImpl.MC;

@RegisterVRHandEffect
public class HandEffectTeleport extends VRHandEffect {
    public static final String ID = "teleport";

    private static final float BEAM_WIDTH = 0.1f;
    private static final float BEAM_ANIMATION_SPEED = 2.4f;


    private final AtumColor tpUnlimitedColor = AtumColor.immutable(
            (AtumColor.CYAN.getRed() * 0.9f),
            (AtumColor.CYAN.getGreen() * 0.9f),
            (AtumColor.CYAN.getBlue() * 0.9f),
            1.0f
    );
    private final AtumColor tpLimitedColor = AtumColor.immutable(
            (AtumColor.CYAN.getRed() * 0.8f),
            (AtumColor.CYAN.getGreen() * 0.8f),
            (AtumColor.CYAN.getBlue() * 0.8f),
            1.0f
    );
    private final AtumColor tpInvalidColor = AtumColor.immutable(
            (AtumColor.CYAN.getRed() * 0.3f),
            (AtumColor.CYAN.getGreen() * 0.3f),
            (AtumColor.CYAN.getBlue() * 0.3f),
            1.0f
    );


    public double lastArcDisplayOffset = 0;

    private float timer;

    public HandEffectTeleport(@NotNull VisorAddon owner) {
        super(owner);
    }

    @Override
    public void render(@NotNull HandType hand,
                       @NotNull VRCameraType cameraType,
                       @NotNull PoseStack poseStack,
                       boolean simpleHand,
                       float partialTicks) {
        timer = getAnimationTick(partialTicks);

        poseStack.pushPose();
        poseStack.setIdentity();
        RenderPoseHelper.applyCameraOrientation(cameraType, poseStack);

        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
        );

        // Render the teleport arc and landing pad effect
        RenderSystem.enableDepthTest();
        renderTeleportArc(cameraType, poseStack);

        RenderSystem.defaultBlendFunc();
        poseStack.popPose();
    }

    private void renderTeleportArc(VRCameraType cameraType,
                                   PoseStack poseStack) {
        MC.getProfiler().push("teleportArc");

        RenderSystem.enableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        MC.getTextureManager().bindForSetup(TexturesHelper.getWhiteTexture());
        RenderSystem.setShaderTexture(0, TexturesHelper.getWhiteTexture());

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();
        builder.begin(VertexFormat.Mode.QUADS,
                DefaultVertexFormat.POSITION_COLOR_NORMAL);

        double VOffset = lastArcDisplayOffset;
        Vec3 dest = TaskTeleport.getDestination();
        boolean validLocation = dest != null;

        byte alpha = -1;
        AtumColor color;
        if (!validLocation) {
            alpha = -128;
            color = tpInvalidColor;
        } else {
            if (VRClientSettings.isLimitedSurvivalTeleport() && !MC.player.getAbilities().mayfly) {
                color = tpLimitedColor;
            } else {
                color = tpUnlimitedColor;
            }
            VOffset = timer * BEAM_ANIMATION_SPEED * 0.6D;
            lastArcDisplayOffset = VOffset;
        }

        //LIGHT LEVEL
        if (MC.level != null) {
            float light = (float) MC.level.getMaxLocalRawBrightness(
                    BlockPos.containing(
                            validLocation
                                    ? dest
                                    : new Vec3((Vector3f) ClientContext.localPlayer
                                    .getPoseData(PlayerPoseType.RENDER)
                                    .getHmd()
                                    .getPosition())
                    )
            );

            int minLight = ShadersHelper.shaderLight();

            if (light < (float) minLight) {
                light = (float) minLight;
            }

            float lightPercent = Math.min(1.0f, light / (float) MC.level.getMaxLightLevel());
            color = AtumColor.immutable(
                    Mth.floor(color.getRedInt() * lightPercent),
                    Mth.floor(color.getGreenInt() * lightPercent),
                    Mth.floor(color.getBlueInt() * lightPercent),
                    color.getAlphaInt()
            );
        }

        float segmentHalfWidth = BEAM_WIDTH * 0.15F;
        int segments = TaskTeleport.getInstance().getArcSteps() - 1;
        double segmentProgress = 1.0D / segments;

        var cameraPosition = new Vec3((Vector3f) RenderPoseHelper.getCameraPosition(
                cameraType,
                ClientContext.localPlayer
                        .getPoseData(PlayerPoseType.RENDER)
        ));

        Vec3i colorInt = new Vec3i(
                color.getRedInt(),
                color.getGreenInt(),
                color.getBlueInt()
        );
        for (int i = 0; i < segments; i++) {
            double progress = (double) i / segments + VOffset * segmentProgress;
            int progressBase = Mth.floor(progress);
            progress -= progressBase;

            Vec3 start = TaskTeleport.getArcPosInterpolated((float) (progress - segmentProgress * 0.4F))
                    .subtract(cameraPosition);
            Vec3 end = TaskTeleport.getArcPosInterpolated((float) progress)
                    .subtract(cameraPosition);

            float shift = (float) progress * 2.0F;
            renderBox(
                    tesselator,
                    start, end,
                    -segmentHalfWidth, segmentHalfWidth,
                    (-1.0F + shift) * segmentHalfWidth,
                    (1.0F + shift) * segmentHalfWidth * 0.3f,
                    colorInt,
                    alpha,
                    poseStack
            );
        }
        tesselator.end();

        // Custom Shader Landing Pad Effect using our own shader
        if (validLocation && TaskTeleport.getInstance().isArcActive()) {

            RenderSystem.disableCull();

            VRShaders.getTeleportPoint().prepare(
                    RenderSystem.getModelViewMatrix(),
                    RenderSystem.getProjectionMatrix(),
                    timer,
                    color
            );
            ShaderInstance shaderInstance = VRShaders.getTeleportPoint().getHandle();


            // Calculate destination relative to camera and add slight offset to avoid z-fighting
            Vec3 destinationRelative = new Vec3(dest.x, dest.y, dest.z)
                    .subtract(cameraPosition).add(0, 0.01, 0);
            // Draw a single quad centered at destinationRelative with fixed size.
            float quadSize = 0.8F;

            drawQuad(destinationRelative, quadSize, poseStack);


            shaderInstance.clear();
            RenderSystem.enableCull();
        }

        MC.getProfiler().pop();

    }


    private void drawQuad(Vec3 center, float size, PoseStack poseStack) {
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();
        float halfSize = size / 2.0F;
        // Get the current transformation matrix from the pose stack.
        Matrix4f matrix = poseStack.last().pose();

        buffer.begin(VertexFormat.Mode.QUADS,
                VRShaders.getTeleportPoint().getHandle().getVertexFormat()
        );


        // Lower-left vertex (with texture coordinates (0, 0))
        buffer.vertex(matrix, (float) center.x - halfSize, (float) center.y, (float) center.z - halfSize)
                .uv(0.0F, 0.0F)
                .endVertex();

        // Lower-right vertex (with texture coordinates (1, 0))
        buffer.vertex(matrix, (float) center.x + halfSize, (float) center.y, (float) center.z - halfSize)
                .uv(1.0F, 0.0F)
                .endVertex();

        // Upper-right vertex (with texture coordinates (1, 1))
        buffer.vertex(matrix, (float) center.x + halfSize, (float) center.y, (float) center.z + halfSize)
                .uv(1.0F, 1.0F)
                .endVertex();

        // Upper-left vertex (with texture coordinates (0, 1))
        buffer.vertex(matrix, (float) center.x - halfSize, (float) center.y, (float) center.z + halfSize)
                .uv(0.0F, 1.0F)
                .endVertex();

        BufferUploader.draw(buffer.end());
    }

    private float getAnimationTick(float partialTicks) {
        return (VisorState.TICK_COUNT + partialTicks) / 20.0f;
    }


    public static void renderBox(Tesselator tes, Vec3 start, Vec3 end,
                                 float minX, float maxX,
                                 float minY, float maxY,
                                 Vec3i color, byte alpha,
                                 PoseStack poseStack) {
        Vec3 forward = start.subtract(end).normalize();
        Vec3 right = forward.cross(new Vec3(0.0D, 1.0D, 0.0D));
        Vec3 up = right.cross(forward);

        Vec3 left = right.scale(minX);
        right = right.scale(maxX);

        Vec3 down = up.scale(minY);
        up = up.scale(maxY);

        Vec3 upNormal = up.normalize();
        Vec3 rightNormal = right.normalize();

        Vec3 backRightBottom = start.add(right.x + down.x, right.y + down.y, right.z + down.z);
        Vec3 backRightTop = start.add(right.x + up.x, right.y + up.y, right.z + up.z);
        Vec3 backLeftBottom = start.add(left.x + down.x, left.y + down.y, left.z + down.z);
        Vec3 backLeftTop = start.add(left.x + up.x, left.y + up.y, left.z + up.z);

        Vec3 frontRightBottom = end.add(right.x + down.x, right.y + down.y, right.z + down.z);
        Vec3 frontRightTop = end.add(right.x + up.x, right.y + up.y, right.z + up.z);
        Vec3 frontLeftBottom = end.add(left.x + down.x, left.y + down.y, left.z + down.z);
        Vec3 frontLeftTop = end.add(left.x + up.x, left.y + up.y, left.z + up.z);

        BufferBuilder bufferbuilder = tes.getBuilder();
        Matrix4f mat = poseStack.last().pose();

        addVertex(bufferbuilder, mat, backRightBottom, color, alpha, forward);
        addVertex(bufferbuilder, mat, backLeftBottom, color, alpha, forward);
        addVertex(bufferbuilder, mat, backLeftTop, color, alpha, forward);
        addVertex(bufferbuilder, mat, backRightTop, color, alpha, forward);

        forward.reverse();
        addVertex(bufferbuilder, mat, frontLeftBottom, color, alpha, forward);
        addVertex(bufferbuilder, mat, frontRightBottom, color, alpha, forward);
        addVertex(bufferbuilder, mat, frontRightTop, color, alpha, forward);
        addVertex(bufferbuilder, mat, frontLeftTop, color, alpha, forward);

        addVertex(bufferbuilder, mat, frontRightBottom, color, alpha, rightNormal);
        addVertex(bufferbuilder, mat, backRightBottom, color, alpha, rightNormal);
        addVertex(bufferbuilder, mat, backRightTop, color, alpha, rightNormal);
        addVertex(bufferbuilder, mat, frontRightTop, color, alpha, rightNormal);

        rightNormal.reverse();
        addVertex(bufferbuilder, mat, backLeftBottom, color, alpha, rightNormal);
        addVertex(bufferbuilder, mat, frontLeftBottom, color, alpha, rightNormal);
        addVertex(bufferbuilder, mat, frontLeftTop, color, alpha, rightNormal);
        addVertex(bufferbuilder, mat, backLeftTop, color, alpha, rightNormal);

        addVertex(bufferbuilder, mat, backLeftTop, color, alpha, upNormal);
        addVertex(bufferbuilder, mat, frontLeftTop, color, alpha, upNormal);
        addVertex(bufferbuilder, mat, frontRightTop, color, alpha, upNormal);
        addVertex(bufferbuilder, mat, backRightTop, color, alpha, upNormal);

        upNormal.reverse();
        addVertex(bufferbuilder, mat, frontLeftBottom, color, alpha, upNormal);
        addVertex(bufferbuilder, mat, backLeftBottom, color, alpha, upNormal);
        addVertex(bufferbuilder, mat, backRightBottom, color, alpha, upNormal);
        addVertex(bufferbuilder, mat, frontRightBottom, color, alpha, upNormal);
    }

    private static void addVertex(BufferBuilder buff,
                                  Matrix4f mat, Vec3 pos, Vec3i color,
                                  int alpha, Vec3 normal) {
        buff.vertex(mat, (float) pos.x, (float) pos.y, (float) pos.z)
                .color(color.getX(), color.getY(), color.getZ(), alpha)
                .normal((float) normal.x, (float) normal.y, (float) normal.z)
                .endVertex();
    }

    @Override
    public boolean isVisible(@NotNull VRDecorator currentDecorator,
                             @NotNull HandType hand,
                             boolean simpleHand) {
        return TaskTeleport.isAiming()
                && TaskTeleport.getInstance().getUsingHand() == hand
                && TaskTeleport.getInstance().getArcSteps() > 1;
    }


    @Override
    public @NotNull String getId() {
        return ID;
    }
}
