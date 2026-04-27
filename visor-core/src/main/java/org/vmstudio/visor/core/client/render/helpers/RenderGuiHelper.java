package org.vmstudio.visor.core.client.render.helpers;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import me.phoenixra.atumvr.api.misc.color.AtumColor;
import net.minecraft.client.renderer.GameRenderer;
import org.vmstudio.visor.api.client.player.pose.VRPlayerPoseClient;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.client.gui.overlays.VROverlay;
import org.vmstudio.visor.api.client.gui.overlays.VROverlayPose;
import org.vmstudio.visor.compatibility.ShadersHelper;
import org.vmstudio.visor.extensions.client.render.GameRendererExtension;
import org.vmstudio.visor.core.client.render.VRRenderState;
import org.vmstudio.visor.core.client.settings.VRClientSettings;
import org.vmstudio.visor.core.client.settings.options.enums.ShaderGUIRenderMode;
import org.vmstudio.visor.core.client.utils.ClientUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import org.vmstudio.visor.core.client.ClientContext;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.lwjgl.opengl.GL11C;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

public class RenderGuiHelper {
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

        float fogStartCache = RenderSystem.getShaderFogStart();
        var color = AtumColor.WHITE.asMutable();

        boolean dragging = overlay.isBeingDragged();
        var barColor = (dragging
                ? AtumColor.immutable(220, 220, 220, 230)
                : AtumColor.immutable(190, 190, 190, 150)).asMutable();

        var renderTarget = overlay.getRenderTarget();
        assert renderTarget != null;
        renderTarget.bindRead();

        RenderSystem.disableCull();
        RenderSystem.setShaderTexture(0, renderTarget.getColorTextureId());

        if (!VRRenderState.isInMainMenu()) {
            RenderSystem.setShaderFogStart(Float.MAX_VALUE);

            if (MC.player != null && MC.player.isShiftKeyDown()) {
                color.setRGBA(
                        color.getRed(), color.getGreen(), color.getBlue(),
                        (int) (color.getAlpha() * 0.75F)
                );
                barColor.setRGBA(
                        barColor.getRed(), barColor.getGreen(), barColor.getBlue(),
                        (int) (barColor.getAlpha() * 0.75F)
                );
            }

            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(
                    GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                    GlStateManager.SourceFactor.ONE_MINUS_DST_ALPHA,
                    GlStateManager.DestFactor.ONE
            );
            if (VRClientSettings.getShaderGUIRender() == ShaderGUIRenderMode.BEFORE_TRANSLUCENT_SOLID
                    && ShadersHelper.isShaderActive()) {
                RenderSystem.disableBlend();
            }
        } else {
            RenderSystem.enableBlend();
        }

        if (depthAlways) {
            RenderSystem.depthFunc(GL11C.GL_ALWAYS);
            RenderSystem.depthMask(false);
        } else {
            RenderSystem.depthFunc(GL11C.GL_LEQUAL);
            RenderSystem.depthMask(true);
        }
        RenderSystem.enableDepthTest();

        // --- Pose ---
        poseStack.pushPose();
        poseStack.translate(position.x() - eye.x(), position.y() - eye.y(), position.z() - eye.z());
        poseStack.mulPoseMatrix((Matrix4f) orientation);
        poseStack.scale(scale, scale, scale);

        // --- Quad + light ---
        int packedLight = -1;
        if (MC.level != null && useLight) {
            Vector3fc lightPos = position;
            if (RenderHelper.isInSolidBlock(position)
                    || ((GameRendererExtension) MC.gameRenderer).visor$isInBlock()) {
                lightPos = ClientContext.localPlayer
                        .getPoseData(PlayerPoseType.RENDER)
                        .getHmd()
                        .getPosition();
            }
            int minLight = ShadersHelper.shaderLight();
            packedLight = ClientUtils.getCombinedLightWithMin(
                    MC.level,
                    BlockPos.containing(new Vec3((Vector3f) lightPos)),
                    minLight
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

        // --- Drag handle bar
        if (drawDragHandle && overlay.supportsDragging()) {
            float brightness = 1f;
            if (packedLight >= 0) {
                int blockLight = (packedLight >> 4) & 0xF;
                int skyLight   = (packedLight >> 20) & 0xF;
                brightness = Math.max(0.2f, Math.max(blockLight, skyLight) / 15f);
            }
            drawDragHandleBar(overlay, poseStack, barColor, brightness);
        }

        // --- Restore ---
        RenderSystem.setShaderFogStart(fogStartCache);
        RenderSystem.depthFunc(GL11C.GL_LEQUAL);
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableCull();

        poseStack.popPose();
    }

    private static void drawDragHandleBar(VROverlay overlay,
                                          PoseStack poseStack,
                                          AtumColor barColor,
                                          float brightness) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        float aspect = overlay.getAspectRatio();
        float halfWidth  = VROverlayPose.QUAD_SCALE * 0.5f;
        float halfHeight = halfWidth * aspect;

        float barCenterX   = 0f;
        float barHalfWidth = halfWidth * 0.18f;
        float regionBottom = -halfHeight;

        int edgeX = overlay.getCursorBoundsX();
        int edgeY = overlay.getCursorBoundsY();
        int edgeWidth = overlay.getCursorBoundsWidth();
        int edgeHeight = overlay.getCursorBoundsHeight();
        int width = overlay.getWidth();
        int height = overlay.getHeight();
        if (width > 0 && height > 0
                && edgeX >= 0 && edgeY >= 0
                && edgeWidth >= 0 && edgeHeight >= 0) {
            float nx0 = -halfWidth + ((float) edgeX / width) * (2f * halfWidth);
            float nx1 = -halfWidth + ((float) (edgeX + edgeWidth) / width) * (2f * halfWidth);
            regionBottom = halfHeight - ((float) (edgeY + edgeHeight) / height) * (2f * halfHeight);
            barCenterX = (nx0 + nx1) * 0.5f;
            barHalfWidth = (nx1 - nx0) * 0.18f;
        }

        float barHalfHeight = halfHeight * 0.025f;
        float barGap        = halfHeight * 0.04f;
        float barCenterY    = regionBottom - barGap - barHalfHeight;

        var pose = poseStack.last().pose();
        BufferBuilder buf = Tesselator.getInstance().getBuilder();
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        float r = barColor.getRed()   * brightness;
        float g = barColor.getGreen() * brightness;
        float b = barColor.getBlue()  * brightness;
        float a = barColor.getAlpha();
        float left   = barCenterX - barHalfWidth;
        float right  = barCenterX + barHalfWidth;
        float top    = barCenterY + barHalfHeight;
        float bottom = barCenterY - barHalfHeight;
        buf.vertex(pose, left,  bottom, 0f).color(r, g, b, a).endVertex();
        buf.vertex(pose, right, bottom, 0f).color(r, g, b, a).endVertex();
        buf.vertex(pose, right, top,    0f).color(r, g, b, a).endVertex();
        buf.vertex(pose, left,  top,    0f).color(r, g, b, a).endVertex();

        BufferUploader.drawWithShader(buf.end());
    }

}
