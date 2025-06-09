package me.phoenixra.visor.core.client.render.helpers;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import me.phoenixra.visor.api.client.data.PoseData;
import me.phoenixra.visor.api.client.data.PoseType;
import me.phoenixra.visor.api.client.render.VRDisplay;
import me.phoenixra.visor.compatibility.ShadersHelper;
import me.phoenixra.visor.core.client.mcmodified.render.GameRendererModified;
import me.phoenixra.visor.core.client.mcmodified.render.RenderTargetModified;
import me.phoenixra.visor.core.client.render.VRRenderState;
import me.phoenixra.visor.core.client.settings.VRClientSettings;
import me.phoenixra.visor.core.client.settings.option.enums.ShaderGUIRenderMode;
import me.phoenixra.visor.core.client.utils.ClientUtils;
import me.phoenixra.visor.core.mixin.client.accessors.RenderSystemAccessor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import me.phoenixra.visor.core.client.ClientContext;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11C;

import java.util.function.Supplier;

import static me.phoenixra.visor.core.client.VisorClientImpl.MC;

public class VRScreenHelper {

    public static void drawScreen(float partialTick,
                                  Screen screen,
                                  GuiGraphics guiGraphics,
                                  int mouseX, int mouseY) {

        //saves previous projection and sets compatible with minecraft screens
        RenderSystem.backupProjectionMatrix();
        Matrix4f matrix4f = new Matrix4f().setOrtho(0, screen.width, screen.height, 0, 1000.0F, 22000.0F);
        RenderSystem.setProjectionMatrix(matrix4f, VertexSorting.ORTHOGRAPHIC_Z);


        PoseStack posestack = RenderSystem.getModelViewStack();
        posestack.pushPose();
        posestack.setIdentity();
        posestack.translate(0.0D, 0.0D, -11000.0D);
        RenderSystem.applyModelViewMatrix();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ONE
        );

        screen.renderWithTooltip(guiGraphics, mouseX, mouseY, partialTick);

        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ONE
        );

        RenderSystem.restoreProjectionMatrix();

        posestack.popPose();
        RenderSystem.applyModelViewMatrix();

        RenderTarget main = MC.getMainRenderTarget();
        main.bindRead();
        ((RenderTargetModified) main).visor$genMipMaps();
        main.unbindRead();
    }


    public static void drawSizedQuad(float displayWidth,
                                     float displayHeight,
                                     float size,
                                     float[] color,
                                     Matrix4f pMatrix) {
        float aspect = displayHeight / displayWidth;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(color[0], color[1], color[2], color[3]);
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder
                .vertex(pMatrix, (-(size / 2.0F)), (-(size * aspect) / 2.0F), 0)
                .uv(0.0F, 0.0F)
                .endVertex();
        bufferbuilder
                .vertex(pMatrix, (size / 2.0F), (-(size * aspect) / 2.0F), 0)
                .uv(1.0F, 0.0F)
                .endVertex();
        bufferbuilder
                .vertex(pMatrix, (size / 2.0F), (size * aspect / 2.0F), 0)
                .uv(1.0F, 1.0F)
                .endVertex();
        bufferbuilder
                .vertex(pMatrix, (-(size / 2.0F)), (size * aspect / 2.0F), 0)
                .uv(0.0F, 1.0F)
                .endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    public static void drawSizedQuadWithLightmapCutout(float displayWidth,
                                                       float displayHeight,
                                                       float size,
                                                       int lighti,
                                                       Matrix4f pMatrix,
                                                       boolean flipY) {
        drawSizedQuadWithLightmapCutout(displayWidth, displayHeight, size, lighti, new float[]{1, 1, 1, 1}, pMatrix, flipY);
    }

    public static void drawSizedQuadWithLightmapCutout(float displayWidth,
                                                       float displayHeight,
                                                       float size,
                                                       int lighti,
                                                       float[] color,
                                                       Matrix4f pMatrix,
                                                       boolean flipY) {
        drawSizedQuadWithLightmap(displayWidth, displayHeight, size, lighti, color, pMatrix, GameRenderer::getRendertypeEntityCutoutNoCullShader, flipY);
    }

    public static void drawSizedQuadSolid(float displayWidth,
                                          float displayHeight, float size,
                                          float[] color, Matrix4f pMatrix) {
        drawSizedQuadWithLightmap(displayWidth, displayHeight, size, LightTexture.pack(15, 15), color, pMatrix, GameRenderer::getRendertypeEntitySolidShader, false);
    }

    public static void drawSizedQuadWithLightmap(float displayWidth,
                                                 float displayHeight,
                                                 float size, int lighti,
                                                 float[] color, Matrix4f pMatrix, Supplier<ShaderInstance> shader, boolean flipY) {
        float aspect = displayHeight / displayWidth;
        RenderSystem.setShader(shader);
        MC.gameRenderer.lightTexture().turnOnLightLayer();
        MC.gameRenderer.overlayTexture().setupOverlayColor();
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.NEW_ENTITY);

        // store old lights
        Vector3f light0Old = RenderSystemAccessor.getShaderLightDirections()[0];
        Vector3f light1Old = RenderSystemAccessor.getShaderLightDirections()[1];

        // set lights to front
        RenderSystem.setShaderLights(new Vector3f(0, 0, 1), new Vector3f(0, 0, 1));
        RenderSystem.setupShaderLights(RenderSystem.getShader());

        bufferbuilder.vertex(pMatrix, (-(size / 2.0F)), (-(size * aspect) / 2.0F), 0)
                .color(color[0], color[1], color[2], color[3])
                .uv(0.0F, flipY ? 1.0F : 0.0F)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(lighti)
                .normal(0, 0, 1)
                .endVertex();
        bufferbuilder.vertex(pMatrix, (size / 2.0F), (-(size * aspect) / 2.0F), 0)
                .color(color[0], color[1], color[2], color[3])
                .uv(1.0F, flipY ? 1.0F : 0.0F)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(lighti)
                .normal(0, 0, 1)
                .endVertex();
        bufferbuilder.vertex(pMatrix, (size / 2.0F), (size * aspect / 2.0F), 0)
                .color(color[0], color[1], color[2], color[3])
                .uv(1.0F, flipY ? 0.0F : 1.0F)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(lighti)
                .normal(0, 0, 1)
                .endVertex();
        bufferbuilder.vertex(pMatrix, (-(size / 2.0F)), (size * aspect / 2.0F), 0)
                .color(color[0], color[1], color[2], color[3])
                .uv(0.0F, flipY ? 0.0F : 1.0F)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(lighti)
                .normal(0, 0, 1)
                .endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());

        MC.gameRenderer.lightTexture().turnOffLightLayer();

        // reset lights
        if (light0Old != null && light1Old != null) {
            RenderSystem.setShaderLights(light0Old, light1Old);
            RenderSystem.setupShaderLights(RenderSystem.getShader());
        }
    }

    public static void renderFlatQuad(Vec3 pos,
                                      float width, float height,
                                      float yaw,
                                      int r, int g, int b, int a,
                                      PoseStack poseStack) {
        Tesselator tesselator = Tesselator.getInstance();
        tesselator.getBuilder().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_NORMAL);

        Vec3 offset = (new Vec3((width / 2.0F), 0.0, height / 2.0F))
                .yRot((float) Math.toRadians(-yaw));

        Matrix4f mat = poseStack.last().pose();
        tesselator.getBuilder().vertex(mat, (float) (pos.x + offset.x), (float) pos.y, (float) (pos.z + offset.z))
                .color(r, g, b, a).normal(0.0F, 1.0F, 0.0F).endVertex();
        tesselator.getBuilder().vertex(mat, (float) (pos.x + offset.x), (float) pos.y, (float) (pos.z - offset.z))
                .color(r, g, b, a).normal(0.0F, 1.0F, 0.0F).endVertex();
        tesselator.getBuilder().vertex(mat, (float) (pos.x - offset.x), (float) pos.y, (float) (pos.z - offset.z))
                .color(r, g, b, a).normal(0.0F, 1.0F, 0.0F).endVertex();
        tesselator.getBuilder().vertex(mat, (float) (pos.x - offset.x), (float) pos.y, (float) (pos.z + offset.z))
                .color(r, g, b, a).normal(0.0F, 1.0F, 0.0F).endVertex();
        tesselator.end();
    }

    public static void applyGameScreenView(PoseStack poseStack,
                                           float partialTicks
    ) {

        ((GameRendererModified) MC.gameRenderer).visor$resetProjectionMatrix(partialTicks);
        poseStack.pushPose();
        poseStack.setIdentity();
        RenderHelper.applyDisplayOrientation(
                VRRenderState.getCurrentVRDisplay(),
                poseStack
        );

        PoseStack modelView = RenderSystem.getModelViewStack();
        modelView.pushPose();
        modelView.setIdentity();
        RenderSystem.applyModelViewMatrix();
    }

    public static void finishScreenRendering(PoseStack poseStack) {
        poseStack.popPose();
        RenderSystem.getModelViewStack().popPose();
        RenderSystem.applyModelViewMatrix();
    }

    public static void renderScreen(PoseStack poseStack,
                                    RenderTarget screenFramebuffer,
                                    boolean depthAlways, boolean noFog,
                                    Vec3 screenRenderPos
    ) {
        screenFramebuffer.bindRead();
        RenderSystem.disableCull();
        RenderSystem.setShaderTexture(0, screenFramebuffer.getColorTextureId());

        // cache fog distance
        float fogStart = RenderSystem.getShaderFogStart();
        float[] color = new float[]{1.0F, 1.0F, 1.0F, 1.0F};
        if (!VRRenderState.isInMainMenu()) {

            if (noFog || MC.screen != null) {
                // disable fog for menus
                RenderSystem.setShaderFogStart(Float.MAX_VALUE);
            }

            if (MC.player != null && MC.player.isShiftKeyDown()) {
                color[3] *= 0.75F;
            }

            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE_MINUS_DST_ALPHA, GlStateManager.DestFactor.ONE);
            if (VRClientSettings.getShaderGUIRender() == ShaderGUIRenderMode.BEFORE_TRANSLUCENT_SOLID && ShadersHelper.isShaderActive()) {
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

        if (MC.level != null) {
            if (isInsideOpaqueBlock(screenRenderPos) || ((GameRendererModified) MC.gameRenderer).visor$isInBlock() > 0.0F) {
                screenRenderPos = ClientContext.player.getPose(PoseType.RENDER).getHmd().getPosition();
            }

            int minLight = ShadersHelper.shaderLight();
            int light = ClientUtils.getCombinedLightWithMin(MC.level, BlockPos.containing(screenRenderPos), minLight);
            drawSizedQuadWithLightmapCutout(
                    (float) MC.getWindow().getGuiScaledWidth(),
                    (float) MC.getWindow().getGuiScaledHeight(),
                    1.5F, light, color,
                    poseStack.last().pose(), false
            );
        } else {
            drawSizedQuad(
                    (float) MC.getWindow().getGuiScaledWidth(),
                    (float) MC.getWindow().getGuiScaledHeight(),
                    1.5F, color,
                    poseStack.last().pose()
            );
        }

        // reset fog
        RenderSystem.setShaderFogStart(fogStart);
        RenderSystem.depthFunc(GL11C.GL_LEQUAL);
        RenderSystem.enableDepthTest();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableCull();
    }


    public static void renderOverlay2D(float partialTicks,
                                       RenderTarget framebuffer,
                                       Vec3 pos, Matrix4fc rot,
                                       boolean depthAlways,
                                       PoseStack poseStack,
                                       float scale
    ) {
        PoseData renderPose = ClientContext.player
                .getPose(PoseType.RENDER);

        applyGameScreenView(poseStack, partialTicks);

        MC.getProfiler().push("apply2DModelView");

        Vec3 eye = RenderHelper.getCameraPosition(
                VRRenderState.getCurrentVRDisplay(),
                renderPose
        );

        poseStack.translate((float) (pos.x - eye.x), (float) (pos.y - eye.y), (float) (pos.z - eye.z));
        poseStack.mulPoseMatrix(new Matrix4f(rot));

        scale = scale * renderPose.getWorldScale();
        poseStack.scale(scale, scale, scale);

        MC.getProfiler().pop();

        renderScreen(poseStack, framebuffer, depthAlways, true, pos);

        finishScreenRendering(poseStack);

    }


    public static boolean shouldOccludeGui() {
        if(VRRenderState.getCurrentVRDisplay() == VRDisplay.THIRD_PERSON){
            return true;
        }
        Vec3 pos = ClientContext.player
                .getPose(PoseType.RENDER)
                .getElementForDisplay(VRRenderState.getCurrentVRDisplay())
                .getPosition();

        return !VRRenderState.isInMainMenu()
                && MC.screen == null
                && !ClientContext.overlayManager.isShowingKeyboard()
                && !ClientContext.overlayManager.isEnabledAtLeastOne()
                && !isInsideOpaqueBlock(pos);
    }

    public static boolean isInsideOpaqueBlock(Vec3 in) {
        if (MC.level == null) {
            return false;
        } else {
            BlockPos blockpos = BlockPos.containing(in);
            return MC.level.getBlockState(blockpos).isSolidRender(MC.level, blockpos);
        }
    }
}
