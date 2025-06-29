package me.phoenixra.visor.core.client.provider;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Getter;
import me.phoenixra.atumvr.api.enums.EyeType;
import me.phoenixra.atumvr.api.rendering.IRenderContext;
import me.phoenixra.atumvr.api.rendering.VRRenderer;
import me.phoenixra.atumvr.api.rendering.VRScene;
import me.phoenixra.atumvr.api.utils.GLUtils;
import me.phoenixra.visor.api.client.render.VRDisplay;
import me.phoenixra.visor.api.client.render.context.RenderContext;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.render.VRShaders;
import me.phoenixra.visor.core.client.render.VisorRendererBase;
import me.phoenixra.visor.core.client.render.VRRenderState;
import me.phoenixra.visor.core.client.render.helpers.MirrorHelper;
import me.phoenixra.visor.core.client.settings.VRClientSettings;
import me.phoenixra.visor.core.client.settings.option.enums.MirrorMode;
import me.phoenixra.visor.core.client.utils.ClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;

import static me.phoenixra.visor.core.client.VisorClientImpl.*;


public class VisorScene implements VRScene {

    @Getter
    private VRRenderer vrRenderer;


    public VisorScene(VRRenderer vrRenderer) {
        this.vrRenderer = vrRenderer;
    }

    @Override
    public void init() {

    }

    @Override
    public void render(@NotNull IRenderContext context) {

        var renderContext = (RenderContext) context;
        var profiler =  renderContext.profiler();

        // pop pose pushed in onGameRenderStart method
        RenderSystem.getModelViewStack().popPose();


        RenderSystem.depthMask(true);
        RenderSystem.applyModelViewMatrix();


        profiler.push("VROverlay texturing");
        GuiGraphics guiGraphics = new GuiGraphics(MC, MC.renderBuffers().bufferSource());
        ClientContext.overlayManager.renderOverlayTextures(
                MC.getProfiler(),
                guiGraphics,
                renderContext.partialTicks()
        );
        profiler.pop();
        GLUtils.checkGLError("post VR Overlays texturing");

        for (VRDisplay display : VRRenderState.getVRWorldDisplays()) {
            profiler.push("VR world display: "+display.name());

            renderVRDisplay(
                    display,
                    renderContext
            );
            GLUtils.checkGLError("post VR world display render: " + display.name());


            if (ClientContext.renderer.isAskedForScreenShot()) {
                takeScreenshot(display);
            }
            profiler.pop();
        }


        profiler.push("VR mirror");
        VRRenderState.startVRMirrorPhase();
        MC.mainRenderTarget.bindWrite(true);
        MirrorHelper.drawMirror();
        profiler.pop();
        GLUtils.checkGLError("post mirror");


    }

    private void takeScreenshot(VRDisplay currentStage) {

        boolean flag;
        if (currentStage == VRDisplay.FIRST_PERSON) {
            flag = true;
        } else {
            flag = VRClientSettings.getMirrorEye() == EyeType.LEFT ?
                    currentStage == VRDisplay.EYE_LEFT
                    : currentStage == VRDisplay.EYE_RIGHT;
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

    private void renderVRDisplay(VRDisplay display,
                                 RenderContext context
    ) {
        VRRenderState.startVRWorldPhase(display);

        MC.mainRenderTarget.bindWrite(true);
        RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 1.0F);
        RenderSystem.clear(16384, Minecraft.ON_OSX);
        RenderSystem.enableDepthTest();

        MC.gameRenderer.render(
                context.partialTicks(),
                context.nanoTime(),
                context.renderLevel()
        );

        if (display.isEye()) {

            if (display == VRDisplay.EYE_LEFT) {
                ClientContext.renderer.getTextureLeftEye()
                        .getRenderTarget().bindWrite(true);
            } else {
                ClientContext.renderer.getTextureRightEye()
                        .getRenderTarget().bindWrite(true);
            }

            VRShaders.getPostProcess().finishEye(
                    display == VRDisplay.EYE_LEFT
                            ? EyeType.LEFT : EyeType.RIGHT,
                    MC.mainRenderTarget,
                    context.partialTicks()
            );

        }
    }



}
