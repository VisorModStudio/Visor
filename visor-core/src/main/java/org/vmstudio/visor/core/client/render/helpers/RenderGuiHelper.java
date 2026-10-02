package org.vmstudio.visor.core.client.render.helpers;

import org.vmstudio.visor.api.compatibility.mcversion.render.McFog;
import org.vmstudio.visor.api.compatibility.mcversion.render.McGlState;
import org.vmstudio.visor.api.compatibility.mcversion.render.McShaders;
import org.vmstudio.visor.api.compatibility.mcversion.render.McVertexBuilder;
import org.vmstudio.visor.api.compatibility.mcversion.render.McRenderUtils;
import org.vmstudio.visor.api.compatibility.mcversion.render.McRenderTarget;

import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.PrimitiveTopology;
import me.phoenixra.atumvr.api.misc.color.AtumColor;
import org.vmstudio.visor.api.client.player.pose.VRPlayerPoseClient;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.client.gui.overlays.VROverlay;
import org.vmstudio.visor.api.client.gui.overlays.VROverlayPose;
import org.vmstudio.visor.api.client.gui.helpers.TexturesHelper;
import org.vmstudio.visor.compatibility.ShaderCompatHelper;
import org.vmstudio.visor.core.client.render.camera.VRCameraOverlaps;
import org.vmstudio.visor.core.client.render.VRRenderState;
import org.vmstudio.visor.core.client.utils.ClientUtils;
import net.minecraft.core.BlockPos;

import org.vmstudio.visor.core.client.ClientContext;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3fc;
import org.lwjgl.opengl.GL11C;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

public class RenderGuiHelper {
    // depth-layer overlays sit in the world, so they may get darker than HUD ones; Vivecraft's no-shader GUI floor
    private static final int DEPTH_OVERLAY_MIN_LIGHT = 4;

    private RenderGuiHelper() {
        throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
    }







    public static void renderOverlayQuad(VROverlay overlay,
                                         PoseStack poseStack,
                                         Vector3fc position,
                                         Matrix4fc orientation,
                                         boolean depthAlways,
                                         boolean useLight,
                                         boolean drawDragHandle,
                                         float scale
    ) {
        VRPlayerPoseClient renderPose = ClientContext.localPlayer
                .getPoseData(PlayerPoseType.RENDER);

        var eye = RenderPoseHelper.getCameraPosition(
                VRRenderState.getRenderPass(),
                renderPose
        );
        scale = scale * renderPose.getWorldScale();

        McFog.State fogState = McFog.save();
        var color = AtumColor.WHITE.asMutable();

        boolean dragging = overlay.isBeingDragged();
        boolean resizing = overlay.isBeingResized();
        var barColor = (resizing
                ? AtumColor.immutable(120, 220, 255, 160)
                : (dragging
                ? AtumColor.immutable(220, 220, 220, 110)
                : AtumColor.immutable(190, 190, 190, 85))).asMutable();

        var renderTarget = overlay.getRenderTarget();
        assert renderTarget != null;
        McRenderTarget.bindRead(renderTarget);

        McGlState.disableCull();
        McGlState.setShaderTexture(0, renderTarget);

        McGlState.enableBlend();
        if (VRRenderState.getSceneType().isWorld()) {
            // keep fog away from the overlay, and let its alpha accumulate
            McFog.disable();
            McGlState.blendFuncSeparate(
                    McGlState.Blend.SRC_ALPHA,
                    McGlState.Blend.ONE_MINUS_SRC_ALPHA,
                    McGlState.Blend.ONE_MINUS_DST_ALPHA,
                    McGlState.Blend.ONE
            );
        }

        McGlState.depthFunc(depthAlways ? GL11C.GL_ALWAYS : GL11C.GL_LEQUAL);
        McGlState.depthMask(!depthAlways);
        McGlState.enableDepthTest();

        // --- Pose ---
        poseStack.pushPose();
        poseStack.translate(position.x() - eye.x(), position.y() - eye.y(), position.z() - eye.z());
        McRenderUtils.mulPose(poseStack, (Matrix4f) orientation);
        poseStack.scale(scale, scale, scale);

        // --- Quad + light ---
        int packedLight = -1;
        boolean useLitPath = MC.level != null && useLight && !ShaderCompatHelper.isShaderActive();
        if (useLitPath) {

            boolean overlayInBlock = RenderHelper.isInSolidBlock(position)
                    || VRCameraOverlaps.isInBlock();
            Vector3fc light = overlayInBlock
                    ? renderPose.getHmd().getPosition()
                    : position;
            packedLight = ClientUtils.packedLightWithFloor(
                    MC.level,
                    BlockPos.containing(light.x(), light.y(), light.z()),
                    depthAlways ? ShaderCompatHelper.minShaderLight() : DEPTH_OVERLAY_MIN_LIGHT
            );
            RenderHelper.renderDisplayQuadWithLight(
                    poseStack.last().pose(),
                    color,
                    (float) overlay.getWidth(),
                    (float) overlay.getHeight(),
                    VROverlayPose.QUAD_SCALE,
                    packedLight,
                    false
            );
        } else {
            RenderHelper.renderDisplayQuad(
                    poseStack.last().pose(),
                    color,
                    (float) overlay.getWidth(),
                    (float) overlay.getHeight(),
                    VROverlayPose.QUAD_SCALE
            );
        }

        // --- Drag handle bar + resize handle
        if (drawDragHandle && overlay.supportsDragging()) {
            drawHandles(overlay, poseStack, barColor, packedLight, resizing);
        }

        // --- Restore ---
        McFog.restore(fogState);
        McGlState.depthFunc(GL11C.GL_LEQUAL);
        McGlState.depthMask(true);
        McGlState.enableDepthTest();
        McGlState.defaultBlendFunc();
        McGlState.enableCull();

        poseStack.popPose();
    }

    private static void drawHandles(VROverlay overlay,
                                    PoseStack poseStack,
                                    AtumColor color,
                                    int packedLight,
                                    boolean resizing) {
        int width = overlay.getWidth();
        int height = overlay.getHeight();
        if (width <= 0 || height <= 0) {
            return;
        }
        float aspect = overlay.getAspectRatio();
        float halfWidth  = VROverlayPose.QUAD_SCALE * 0.5f;
        float halfHeight = halfWidth * aspect;

        int edgeX = overlay.getCursorBoundsX();
        int edgeY = overlay.getCursorBoundsY();
        int edgeWidth = overlay.getCursorBoundsWidth();
        int edgeHeight = overlay.getCursorBoundsHeight();
        // -1 in any bound means "use the full overlay"
        if (edgeX < 0) edgeX = 0;
        if (edgeY < 0) edgeY = 0;
        if (edgeWidth < 0) edgeWidth = width;
        if (edgeHeight < 0) edgeHeight = height;

        float nx0 = -halfWidth + ((float) edgeX / width) * (2f * halfWidth);
        float nx1 = -halfWidth + ((float) (edgeX + edgeWidth) / width) * (2f * halfWidth);
        float regionBottom = halfHeight - ((float) (edgeY + edgeHeight) / height) * (2f * halfHeight);
        float barCenterX   = (nx0 + nx1) * 0.5f;
        float barHalfWidth = (nx1 - nx0) * 0.18f;
        float barHalfHeight = halfHeight * 0.025f;
        float barGap        = halfHeight * 0.04f;
        float barCenterY    = regionBottom - barGap - barHalfHeight;

        var pose = poseStack.last().pose();
        McVertexBuilder buf = McVertexBuilder.get();
        beginFlatQuads(buf, packedLight);

        // drag bar
        emitRect(buf, pose,
                barCenterX - barHalfWidth, barCenterY - barHalfHeight,
                barCenterX + barHalfWidth, barCenterY + barHalfHeight,
                color, packedLight);

        if (overlay.supportsResizing()) {
            float gap  = barHalfWidth * 0.20f;
            float side = barHalfHeight * 1.3f;
            float left = barCenterX + barHalfWidth + gap;
            emitRect(buf, pose, left, barCenterY - side, left + side * 2f, barCenterY + side, color, packedLight);
        }

        if (resizing) {
            float thickness = halfWidth * 0.012f;
            // top edge
            emitRect(buf, pose, -halfWidth, halfHeight - thickness, halfWidth, halfHeight, color, packedLight);
            // bottom edge
            emitRect(buf, pose, -halfWidth, -halfHeight, halfWidth, -halfHeight + thickness, color, packedLight);
            // left edge
            emitRect(buf, pose, -halfWidth, -halfHeight + thickness, -halfWidth + thickness, halfHeight - thickness, color, packedLight);
            // right edge
            emitRect(buf, pose, halfWidth - thickness, -halfHeight + thickness, halfWidth, halfHeight - thickness, color, packedLight);
        }

        buf.draw();
        if (packedLight >= 0) {
            McGlState.turnOffLightLayer();
        }
    }

    private static void beginFlatQuads(McVertexBuilder buf, int packedLight) {
        if (packedLight >= 0) {
            McShaders.use(McShaders.Core.RENDERTYPE_TEXT);
            McGlState.setShaderTexture(0, TexturesHelper.getWhiteTexture());
            McGlState.turnOnLightLayer();
            buf.begin(PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP);
        } else {
            McShaders.use(McShaders.Core.POSITION_COLOR);
            buf.begin(PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_COLOR);
        }
    }

    private static void emitRect(McVertexBuilder buf, Matrix4f pose,
                                 float left, float bottom, float right, float top,
                                 AtumColor color, int packedLight) {
        flatVertex(buf, pose, left,  bottom, color, packedLight);
        flatVertex(buf, pose, right, bottom, color, packedLight);
        flatVertex(buf, pose, right, top,    color, packedLight);
        flatVertex(buf, pose, left,  top,    color, packedLight);
    }

    private static void flatVertex(McVertexBuilder buf, Matrix4f pose, float x, float y,
                                   AtumColor color, int packedLight) {
        buf.vertex(pose, x, y, 0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        if (packedLight >= 0) {
            buf.uv(0.5f, 0.5f).uv2(packedLight);
        }
        buf.endVertex();
    }

}
