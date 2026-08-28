package org.vmstudio.visor.core.client.render.decoration.effects.hand;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import me.phoenixra.atumvr.api.misc.color.AtumColor;
import org.vmstudio.visor.api.client.gui.helpers.TexturesHelper;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.client.render.VRRenderPass;
import org.vmstudio.visor.api.client.render.decoration.VRDecorator;
import org.vmstudio.visor.api.client.render.decoration.annotations.RegisterVRHandEffect;
import org.vmstudio.visor.api.client.render.decoration.effects.VRHandEffect;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.compatibility.ShaderCompatHelper;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.core.client.render.VRShaders;
import org.vmstudio.visor.core.client.render.helpers.RenderPoseHelper;
import org.vmstudio.visor.core.client.render.helpers.RenderShaderHelper;
import org.vmstudio.visor.api.client.settings.VRClientSettings;
import org.vmstudio.visor.core.client.tasks.types.movement.TaskTeleport;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

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
                       @NotNull VRRenderPass renderPass,
                       @NotNull PoseStack poseStack,
                       boolean guiHand,
                       float partialTicks) {
        timer = getAnimationTick(partialTicks);

        poseStack.pushPose();
        poseStack.setIdentity();
        RenderPoseHelper.applyCameraOrientation(renderPass, poseStack);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // Render the teleport arc and landing pad effect
        RenderSystem.enableDepthTest();

        RenderSystem.depthMask(false);

        renderTeleportArc(renderPass, poseStack);

        RenderSystem.depthMask(true);

        poseStack.popPose();
    }

    private void renderTeleportArc(VRRenderPass renderPass,
                                   PoseStack poseStack) {
        MC.getProfiler().push("visorTeleportArc");

        RenderSystem.enableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        MC.getTextureManager().bindForSetup(TexturesHelper.getWhiteTexture());
        RenderSystem.setShaderTexture(0, TexturesHelper.getWhiteTexture());

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();
        builder.begin(VertexFormat.Mode.QUADS,
                DefaultVertexFormat.POSITION_COLOR_NORMAL);

        Vec3 dest = TaskTeleport.getDestination();
        boolean validLocation = dest != null;

        double scroll = lastArcDisplayOffset;
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
            scroll = timer * BEAM_ANIMATION_SPEED * 0.6D;
            lastArcDisplayOffset = scroll;
        }

        //LIGHT LEVEL
        if (MC.level != null) {
            Vec3 lightProbe = validLocation
                    ? dest
                    : new Vec3((Vector3f) ClientContext.localPlayer
                    .getPoseData(PlayerPoseType.RENDER)
                    .getHmd()
                    .getPosition());
            float light = MC.level.getMaxLocalRawBrightness(BlockPos.containing(lightProbe));
            light = Math.max(light, ShaderCompatHelper.minShaderLight());

            float lightPercent = Math.min(1.0f, light / MC.level.getMaxLightLevel());
            color = AtumColor.immutable(
                    Mth.floor(color.getRedInt() * lightPercent),
                    Mth.floor(color.getGreenInt() * lightPercent),
                    Mth.floor(color.getBlueInt() * lightPercent),
                    color.getAlphaInt()
            );
        }

        float halfWidth = BEAM_WIDTH * 0.15F;
        int segments = TaskTeleport.getInstance().getArcSteps() - 1;
        double segmentStep = 1.0D / segments;

        var cameraPosition = new Vec3((Vector3f) RenderPoseHelper.getCameraPosition(
                renderPass,
                ClientContext.localPlayer
                        .getPoseData(PlayerPoseType.RENDER)
        ));

        Vec3i colorInt = new Vec3i(
                color.getRedInt(),
                color.getGreenInt(),
                color.getBlueInt()
        );
        for (int i = 0; i < segments; i++) {
            double progress = Mth.frac((double) i / segments + scroll * segmentStep);

            Vec3 tail = TaskTeleport.getArcPosInterpolated((float) (progress - segmentStep * 0.4F))
                    .subtract(cameraPosition);
            Vec3 head = TaskTeleport.getArcPosInterpolated((float) progress)
                    .subtract(cameraPosition);

            float rise = (float) progress * 2.0F;
            renderBox(
                    tesselator,
                    tail, head,
                    -halfWidth, halfWidth,
                    (rise - 1.0F) * halfWidth,
                    (rise + 1.0F) * halfWidth * 0.3f,
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
        float halfSize = size / 2.0F;
        Matrix4f matrix = poseStack.last().pose();

        RenderShaderHelper.renderQuad(
                VRShaders.getTeleportPoint().getHandle().getVertexFormat(),
                matrix,
                (float) center.x - halfSize,
                (float) center.y,
                (float) center.z - halfSize,
                (float) center.x + halfSize,
                (float) center.z + halfSize
        );
    }

    private float getAnimationTick(float partialTicks) {
        return (VisorState.TICK_COUNT + partialTicks) / 20.0f;
    }


    public static void renderBox(Tesselator tes, Vec3 start, Vec3 end,
                                 float minX, float maxX,
                                 float minY, float maxY,
                                 Vec3i color, byte alpha,
                                 PoseStack poseStack) {
        Vec3 axis = start.subtract(end).normalize();
        Vec3 side = axis.cross(new Vec3(0.0D, 1.0D, 0.0D));
        Vec3 lift = side.cross(axis);

        Vec3[] corners = new Vec3[8];
        for (int i = 0; i < 8; i++) {
            Vec3 base = (i & 4) == 0 ? start : end;
            Vec3 s = side.scale((i & 2) == 0 ? minX : maxX);
            Vec3 v = lift.scale((i & 1) == 0 ? minY : maxY);
            corners[i] = base.add(s.x + v.x, s.y + v.y, s.z + v.z);
        }

        Vec3 sideNormal = side.scale(maxX).normalize();
        Vec3 liftNormal = lift.scale(maxY).normalize();
        Vec3[] faceNormals = {
                axis, axis.reverse(),
                sideNormal, sideNormal.reverse(),
                liftNormal, liftNormal.reverse()
        };
        int[][] faces = {
                {2, 0, 1, 3},   // start cap
                {4, 6, 7, 5},   // end cap
                {6, 2, 3, 7},   // max-side wall
                {0, 4, 5, 1},   // min-side wall
                {1, 5, 7, 3},   // top
                {4, 0, 2, 6}    // bottom
        };

        BufferBuilder buffer = tes.getBuilder();
        Matrix4f mat = poseStack.last().pose();
        for (int f = 0; f < faces.length; f++) {
            for (int corner : faces[f]) {
                addVertex(buffer, mat, corners[corner], color, alpha, faceNormals[f]);
            }
        }
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
                             boolean guiHand) {
        return TaskTeleport.isAiming()
                && TaskTeleport.getInstance().getUsingHand() == hand
                && TaskTeleport.getInstance().getArcSteps() > 1;
    }


    @Override
    public @NotNull String getId() {
        return ID;
    }
}
