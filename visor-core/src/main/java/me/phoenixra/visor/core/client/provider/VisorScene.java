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
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.mcmodified.MinecraftModified;
import me.phoenixra.visor.core.client.render.VRShaders;
import me.phoenixra.visor.core.client.render.VisorRendererBase;
import me.phoenixra.visor.core.client.render.VRRenderState;
import me.phoenixra.visor.core.client.render.helpers.MirrorHelper;
import me.phoenixra.visor.core.client.settings.VRClientSettings;
import me.phoenixra.visor.core.client.utils.ClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;

import static me.phoenixra.visor.core.client.VisorClientImpl.*;


public class VisorScene implements VRScene {

    @Getter
    private VRRenderer vrRenderer;




    private boolean dataRenderLevel;
    private long dataNanoTime;

    public VisorScene(VRRenderer vrRenderer) {
        this.vrRenderer = vrRenderer;
    }

    @Override
    public void init() {

    }

    @Override
    public void render(@NotNull IRenderContext context) {

        Minecraft mc = Minecraft.getInstance();

        RenderSystem.depthMask(true);

        // pop pose pushed in onGameRenderStart method
        RenderSystem.getModelViewStack().popPose();

        RenderSystem.applyModelViewMatrix();

        float partialTicks = ((MinecraftModified) mc).visor$getPartialTicks();

        GuiGraphics guiGraphics = new GuiGraphics(mc, mc.renderBuffers().bufferSource());
        ClientContext.overlayManager.renderOverlayTextures(
                MC.getProfiler(),
                guiGraphics,
                partialTicks
        );
        GLUtils.checkGLError("post overlays");

        for (VRDisplay display : VisorRendererBase.getVRWorldDisplays()) {

            renderVRDisplay(
                    display, partialTicks,
                    dataNanoTime, dataRenderLevel
            );


            if (ClientContext.renderer.isAskedForScreenShot()) {
                takeScreenshot(display);
            }

        }

        VRRenderState.startVRMirrorPhase();
        MC.mainRenderTarget.bindWrite(true);
        MirrorHelper.drawMirror();
        GLUtils.checkGLError("mirror");


    }

    private void takeScreenshot(VRDisplay currentStage) {

        boolean flag;
        if (currentStage == VRDisplay.FIRST_PERSON) {
            flag = true;
        } else {
            flag = VRClientSettings.isDisplayMirrorLeftEye()
                    ? currentStage == VRDisplay.EYE_LEFT
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
                                 float partialTick,
                                 long nanoTime,
                                 boolean renderWorld
    ) {
        VRRenderState.startVRWorldPhase(display);

        MC.mainRenderTarget.bindWrite(true);
        RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 1.0F);
        RenderSystem.clear(16384, Minecraft.ON_OSX);
        RenderSystem.enableDepthTest();

        MC.gameRenderer.render(partialTick, nanoTime, renderWorld);
        GLUtils.checkGLError("post game render " + display.name());


        if (display.isEye()) {

            if (display == VRDisplay.EYE_LEFT) {
                ClientContext.renderer.getTextureLeftEye()
                        .getRenderTarget().bindWrite(true);
            } else {
                ClientContext.renderer.getTextureRightEye()
                        .getRenderTarget().bindWrite(true);
            }

            VRShaders.getPostProcess().renderEye(
                    display == VRDisplay.EYE_LEFT
                            ? EyeType.LEFT : EyeType.RIGHT,
                    MC.mainRenderTarget,
                    partialTick
            );
            GLUtils.checkGLError("post overlay");
        }
    }



    public void updateRenderData(boolean renderLevel, long nanoTime) {
        dataRenderLevel = renderLevel;
        dataNanoTime = nanoTime;
    }


}
