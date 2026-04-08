package org.vmstudio.visor.core.client.render.helpers;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import me.phoenixra.atumvr.api.misc.color.AtumColor;
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
                                         float scale
    ) {
        // --- Prepare variables ---
        VRPlayerPoseClient renderPose = ClientContext.localPlayer
                .getPose(PlayerPoseType.RENDER);

        var eye = RenderPoseHelper.getCameraPosition(
                VRRenderState.getRenderPass(),
                renderPose
        );
        scale = scale * renderPose.getWorldScale();


        float fogStartCache = RenderSystem.getShaderFogStart();
        var color = AtumColor.WHITE.asMutable();

        // --- Setup GL ---
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
                        color.getAlpha() * 0.75F
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
            //disable mask to not mess up depth for something
            RenderSystem.depthMask(false);
        } else {
            RenderSystem.depthFunc(GL11C.GL_LEQUAL);
            RenderSystem.depthMask(true);
        }

        RenderSystem.enableDepthTest();

        // --- Setup Pose ---
        poseStack.pushPose();
        poseStack.translate(position.x() - eye.x(), position.y() - eye.y(), position.z() - eye.z());
        poseStack.mulPoseMatrix((Matrix4f) orientation);
        poseStack.scale(scale, scale, scale);


        // --- Render ---
        if (MC.level != null && useLight) {
            if (RenderHelper.isInSolidBlock(position)
                    || ((GameRendererExtension) MC.gameRenderer).visor$isInBlock()) {
                position = ClientContext.localPlayer.getPose(PlayerPoseType.RENDER).getHmd().getPosition();
            }

            int minLight = ShadersHelper.shaderLight();
            int light = ClientUtils.getCombinedLightWithMin(
                    MC.level,
                    BlockPos.containing(new Vec3((Vector3f) position)),
                    minLight
            );
            RenderHelper.renderDisplayQuadWithLight(
                    poseStack.last().pose(),
                    color,
                    (float) overlay.getWidth(),
                    (float) overlay.getHeight(),
                    VROverlayPose.QUAD_SCALE,
                    light,
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

        // --- Restore ---
        RenderSystem.setShaderFogStart(fogStartCache);
        RenderSystem.depthFunc(GL11C.GL_LEQUAL);
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableCull();

        poseStack.popPose();


    }



}
