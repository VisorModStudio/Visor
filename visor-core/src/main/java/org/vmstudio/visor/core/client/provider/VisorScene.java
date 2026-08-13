package org.vmstudio.visor.core.client.provider;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import me.phoenixra.atumvr.api.enums.EyeType;
import me.phoenixra.atumvr.api.rendering.AtumVRRenderContext;
import me.phoenixra.atumvr.api.rendering.AtumVRScene;
import me.phoenixra.atumvr.api.utils.GLUtils;
import org.joml.Matrix4fStack;
import org.vmstudio.visor.api.client.render.VRRenderPass;
import org.vmstudio.visor.api.client.render.VRRenderer;
import org.vmstudio.visor.core.client.render.context.RenderContext;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.render.VRShaders;
import org.vmstudio.visor.core.client.render.VRRenderState;
import org.vmstudio.visor.compatibility.ShadersHelper;
import org.vmstudio.visor.core.client.render.helpers.MirrorHelper;
import org.vmstudio.visor.api.client.settings.VRClientSettings;
import org.vmstudio.visor.core.client.utils.ClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;

import static org.vmstudio.visor.core.client.VisorClientImpl.*;


public class VisorScene implements AtumVRScene {

    @Getter
    private VRRenderer renderer;


    public VisorScene(VRRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public void init() {

    }

    @Override
    public void render(@NotNull AtumVRRenderContext context) {

        var renderContext = (RenderContext) context;
        var profiler =  renderContext.profiler();

        // pop pose pushed in onGameRenderStart method
        RenderSystem.getModelViewStack().popMatrix();


        RenderSystem.depthMask(true);
        RenderSystem.applyModelViewMatrix();


        profiler.push("prepare VROverlays and cursor");
        ClientContext.overlayManager.prepareOverlaysAndCursor(
                context.partialTicks()
        );
        profiler.pop();

        profiler.push("VROverlay texturing");
        GuiGraphics guiGraphics = new GuiGraphics(MC, MC.renderBuffers().bufferSource());
        ClientContext.overlayManager.renderOverlayTextures(
                MC.getProfiler(),
                guiGraphics,
                renderContext.partialTicks()
        );
        profiler.pop();
        GLUtils.checkGLError("post VR Overlays texturing");

        ShadersHelper.bridge().beginFrame(
                renderContext.partialTicks(),
                renderContext.nanoTime()
        );

        for (VRRenderPass renderPass : VRRenderState.getActivePasses()) {
            profiler.push("VR render pass: "+renderPass.name());

            renderPass(
                    renderPass,
                    renderContext
            );
            GLUtils.checkGLError("post VR render pass: " + renderPass.name());


            if (ClientContext.renderer.isAskedForScreenShot()) {
                takeScreenshot(renderPass);
            }
            profiler.pop();
        }


        ShadersHelper.bridge().endFrame();

        profiler.push("VR mirror");
        VRRenderState.startVRMirrorPhase();
        MC.mainRenderTarget.bindWrite(true);
        MirrorHelper.drawMirror();
        profiler.pop();
        GLUtils.checkGLError("post mirror");


    }

    private void takeScreenshot(VRRenderPass currentStage) {

        boolean flag;
        if (currentStage == VRRenderPass.CENTER) {
            flag = true;
        } else {
            flag = VRClientSettings.getMirrorEye() == EyeType.LEFT ?
                    currentStage == VRRenderPass.EYE_LEFT
                    : currentStage == VRRenderPass.EYE_RIGHT;
        }

        if (flag) {
            RenderTarget rendertarget = MC.mainRenderTarget;

            MC.mainRenderTarget.unbindWrite();
            ClientUtils.takeScreenshot(rendertarget);
            MC.getWindow().updateDisplay();
            ClientContext.renderer.setAskedForScreenShot(false);
        }
    }

    @Override
    public void destroy() {

    }

    private void renderPass(VRRenderPass renderPass,
                            RenderContext context
    ) {
        VRRenderState.startVRWorldPhase(renderPass);

        if (MC.mainRenderTarget == null) {
            LOGGER.warn("Visor: no render target for pass {}; requesting renderer reinit.", renderPass);
            VRRenderState.startVanillaPhase();
            ClientContext.renderer.prepareReinit("Missing target for pass " + renderPass);
            return;
        }

        MC.mainRenderTarget.bindWrite(true);
        RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 1.0F);
        RenderSystem.clear(16384, Minecraft.ON_OSX);
        RenderSystem.enableDepthTest();

        ShadersHelper.bridge().beginEye(renderPass.getEyeOrLeft());

        if (ShadersHelper.isShaderActive()) {
            RenderSystem.setShaderTexture(0, 0);
            RenderSystem.setShaderTexture(1, 0);
            RenderSystem.setShaderTexture(2, 0);
        }

        MC.gameRenderer.render(
                MC.getTimer(),
                context.renderLevel()
        );

        if (ShadersHelper.isShaderActive()) {
            MC.mainRenderTarget.bindWrite(true);
            Matrix4fStack modelView = RenderSystem.getModelViewStack();
            modelView.pushMatrix();
            modelView.identity();
            RenderSystem.applyModelViewMatrix();
            ClientContext.decorationRenderer.renderShaderUi(new PoseStack(), context.partialTicks());
            modelView.popMatrix();
            RenderSystem.applyModelViewMatrix();
        }

        if (renderPass.isEye()) {

            if (renderPass == VRRenderPass.EYE_LEFT) {
                ClientContext.renderer.getTextureLeftEye()
                        .getRenderTarget().bindWrite(true);
            } else {
                ClientContext.renderer.getTextureRightEye()
                        .getRenderTarget().bindWrite(true);
            }

            VRShaders.getPostProcess().finishEye(
                    renderPass == VRRenderPass.EYE_LEFT
                            ? EyeType.LEFT : EyeType.RIGHT,
                    MC.mainRenderTarget,
                    context.partialTicks()
            );

        }

        ShadersHelper.bridge().endEye();
    }



}
