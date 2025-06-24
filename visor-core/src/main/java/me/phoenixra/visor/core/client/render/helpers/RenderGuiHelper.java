package me.phoenixra.visor.core.client.render.helpers;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import me.phoenixra.atumvr.api.misc.color.AtumColor;
import me.phoenixra.visor.api.client.data.PoseData;
import me.phoenixra.visor.api.client.data.PoseType;
import me.phoenixra.visor.api.client.render.VRDisplay;
import me.phoenixra.visor.compatibility.ShadersHelper;
import me.phoenixra.visor.core.client.mcmodified.render.GameRendererModified;
import me.phoenixra.visor.core.client.render.VRRenderState;
import me.phoenixra.visor.core.client.settings.VRClientSettings;
import me.phoenixra.visor.core.client.settings.option.enums.ShaderGUIRenderMode;
import me.phoenixra.visor.core.client.utils.ClientUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import me.phoenixra.visor.core.client.ClientContext;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.lwjgl.opengl.GL11C;

import static me.phoenixra.visor.core.client.VisorClientImpl.MC;

public class RenderGuiHelper {
    private RenderGuiHelper() {
        throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
    }



    public static boolean shouldOccludeGui() {
        if(VRRenderState.getCurrentVRDisplay() == VRDisplay.THIRD_PERSON){
            return true;
        }

        return !VRRenderState.isInMainMenu()
                && MC.screen == null
                && !ClientContext.overlayManager.isShowingKeyboard()
                && !ClientContext.overlayManager.isEnabledAtLeastOne()
                &&
                !RenderHelper.isInSolidBlock(
                        ClientContext.player
                                .getPose(PoseType.RENDER)
                                .getElementForDisplay(VRRenderState.getCurrentVRDisplay())
                                .getPosition()
                );
    }



    public static void renderOverlayQuad(RenderTarget renderTarget,
                                         PoseStack poseStack,
                                         Vector3fc position,
                                         Matrix4fc orientation,
                                         boolean depthAlways,
                                         float scale
    ) {
        // --- Prepare variables ---
        PoseData renderPose = ClientContext.player
                .getPose(PoseType.RENDER);

        var eye = RenderPoseHelper.getCameraPosition(
                VRRenderState.getCurrentVRDisplay(),
                renderPose
        );
        scale = scale * renderPose.getWorldScale();


        float fogStartCache = RenderSystem.getShaderFogStart();
        var color = AtumColor.WHITE.asMutable();

        // --- Setup GL ---
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
        } else {
            RenderSystem.depthFunc(GL11C.GL_LEQUAL);
        }

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();

        // --- Setup Pose ---
        poseStack.pushPose();
        poseStack.translate(position.x() - eye.x(), position.y() - eye.y(), position.z() - eye.z());
        poseStack.mulPoseMatrix((Matrix4f) orientation);
        poseStack.scale(scale, scale, scale);


        // --- Render ---
        if (MC.level != null) {
            if (RenderHelper.isInSolidBlock(position)
                    || ((GameRendererModified) MC.gameRenderer).visor$isInBlock() > 0.0F) {
                position = ClientContext.player.getPose(PoseType.RENDER).getHmd().getPosition();
            }

            int minLight = ShadersHelper.shaderLight();
            int light = ClientUtils.getCombinedLightWithMin(MC.level, BlockPos.containing(new Vec3((Vector3f) position)), minLight);
            RenderHelper.renderDisplayQuadWithLight(
                    poseStack.last().pose(),
                    color,
                    (float) MC.getWindow().getGuiScaledWidth(),
                    (float) MC.getWindow().getGuiScaledHeight(),
                    1.5F,
                    light,
                    false
            );
        } else {
            RenderHelper.renderDisplayQuad(
                    poseStack.last().pose(),
                    color,
                    (float) MC.getWindow().getGuiScaledWidth(),
                    (float) MC.getWindow().getGuiScaledHeight(),
                    1.5F
            );
        }

        // --- Restore ---
        RenderSystem.setShaderFogStart(fogStartCache);
        RenderSystem.depthFunc(GL11C.GL_LEQUAL);
        RenderSystem.enableDepthTest();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableCull();

        poseStack.popPose();


    }



}
