package org.vmstudio.visor.core.client.provider;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Getter;
import me.phoenixra.atumvr.api.enums.EyeType;
import me.phoenixra.atumvr.api.rendering.AtumVRRenderContext;
import me.phoenixra.atumvr.api.rendering.AtumVRRenderer;
import me.phoenixra.atumvr.api.rendering.AtumVRScene;
import me.phoenixra.atumvr.api.utils.GLUtils;
import org.vmstudio.visor.api.client.render.VRCameraType;
import org.vmstudio.visor.api.client.render.VRRenderer;
import org.vmstudio.visor.core.client.render.context.RenderContext;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.render.VRShaders;
import org.vmstudio.visor.core.client.render.VRRenderState;
import org.vmstudio.visor.core.client.render.helpers.MirrorHelper;
import org.vmstudio.visor.core.client.settings.VRClientSettings;
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
        RenderSystem.getModelViewStack().popPose();


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

        for (VRCameraType cameraType : VRRenderState.getActiveCameraTypes()) {
            profiler.push("VR camera type: "+cameraType.name());

            renderCamera(
                    cameraType,
                    renderContext
            );
            GLUtils.checkGLError("post VR camera type render: " + cameraType.name());


            if (ClientContext.renderer.isAskedForScreenShot()) {
                takeScreenshot(cameraType);
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

    private void takeScreenshot(VRCameraType currentStage) {

        boolean flag;
        if (currentStage == VRCameraType.FIRST_PERSON) {
            flag = true;
        } else {
            flag = VRClientSettings.getMirrorEye() == EyeType.LEFT ?
                    currentStage == VRCameraType.EYE_LEFT
                    : currentStage == VRCameraType.EYE_RIGHT;
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

    private void renderCamera(VRCameraType cameraType,
                              RenderContext context
    ) {
        VRRenderState.startVRWorldPhase(cameraType);

        MC.mainRenderTarget.bindWrite(true);
        RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 1.0F);
        RenderSystem.clear(16384, Minecraft.ON_OSX);
        RenderSystem.enableDepthTest();

        MC.gameRenderer.render(
                context.partialTicks(),
                context.nanoTime(),
                context.renderLevel()
        );

        if (cameraType.isEye()) {

            if (cameraType == VRCameraType.EYE_LEFT) {
                ClientContext.renderer.getTextureLeftEye()
                        .getRenderTarget().bindWrite(true);
            } else {
                ClientContext.renderer.getTextureRightEye()
                        .getRenderTarget().bindWrite(true);
            }

            VRShaders.getPostProcess().finishEye(
                    cameraType == VRCameraType.EYE_LEFT
                            ? EyeType.LEFT : EyeType.RIGHT,
                    MC.mainRenderTarget,
                    context.partialTicks()
            );

        }
    }



}
