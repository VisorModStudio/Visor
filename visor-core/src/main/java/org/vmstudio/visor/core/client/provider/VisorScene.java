package org.vmstudio.visor.core.client.provider;

import org.vmstudio.visor.api.compatibility.mcversion.render.McGlState;
import org.vmstudio.visor.api.compatibility.mcversion.render.McProjection;
import org.vmstudio.visor.api.compatibility.mcversion.McVersionClientUtils;
import org.vmstudio.visor.api.compatibility.mcversion.render.McRenderUtils;
import org.vmstudio.visor.api.compatibility.mcversion.render.McModelViewStack;
import org.vmstudio.visor.api.compatibility.mcversion.render.McRenderTarget;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import me.phoenixra.atumvr.api.enums.EyeType;
import me.phoenixra.atumvr.api.rendering.AtumVRRenderContext;
import me.phoenixra.atumvr.api.rendering.AtumVRScene;
import me.phoenixra.atumvr.api.utils.GLUtils;
import org.vmstudio.visor.api.client.render.VRRenderPass;
import org.vmstudio.visor.api.client.render.VRRenderer;
import org.vmstudio.visor.core.client.render.context.RenderContext;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.render.VRShaders;
import org.vmstudio.visor.core.client.render.VRRenderState;
import org.vmstudio.visor.compatibility.ShaderCompatHelper;
//? if >=26.1 {
import org.vmstudio.visor.core.client.render.camera.VRCameraEntitySwap;
import org.vmstudio.visor.core.client.render.camera.VRCameraOverlaps;
//?}
import org.vmstudio.visor.core.client.render.helpers.RenderStateHelper;
import org.vmstudio.visor.api.client.settings.VRClientSettings;
import org.vmstudio.visor.core.client.utils.ClientUtils;
import org.joml.Matrix4f;
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

        McGlState.depthMask(true);
        McModelViewStack.apply();


        profiler.push("prepare VROverlays and cursor");
        ClientContext.overlayManager.prepareOverlaysAndCursor(
                context.partialTicks()
        );
        profiler.pop();

        profiler.push("VROverlay texturing");
        ClientContext.overlayManager.renderOverlayTextures(
                McVersionClientUtils.profiler(),
                renderContext.partialTicks()
        );
        profiler.pop();

        ShaderCompatHelper.bridge().beginFrame(
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
            McRenderUtils.endRenderPass();
            profiler.pop();
        }


        ShaderCompatHelper.bridge().endFrame();


    }

    private void renderOverlaysAfterPostProcessing(RenderContext context) {
        McModelViewStack.push();
        McModelViewStack.identity();
        McModelViewStack.apply();

        McProjection.State projection = McProjection.save();
        try{
            ClientContext.decorationRenderer.renderAfterPostProcessing(new PoseStack(), context.partialTicks());
        } finally {
            McProjection.restore(projection);
            McModelViewStack.pop();
            McModelViewStack.apply();
        }

        GLUtils.checkGLError("post VROverlays skipping post processing");
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
            RenderTarget rendertarget = McRenderTarget.mainTarget();

            McRenderTarget.unbindWrite(rendertarget);
            ClientUtils.takeScreenshot(rendertarget);
            McRenderUtils.updateDisplay(MC.getWindow());
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

        if (McRenderTarget.mainTarget() == null) {
            LOGGER.warn("Visor: no render target for pass {}; requesting renderer reinit.", renderPass);
            VRRenderState.startVanillaPhase();
            ClientContext.renderer.prepareReinit("Missing target for pass " + renderPass);
            return;
        }

        McRenderTarget.bindWrite(McRenderTarget.mainTarget());
        McGlState.clearColor(0.0F, 0.0F, 0.0F, 1.0F);
        McGlState.clear(16384);
        McGlState.enableDepthTest();

        ShaderCompatHelper.bridge().beginEye(renderPass.getEyeOrLeft());

        if (ShaderCompatHelper.isShaderActive()) {
            McGlState.clearShaderTexture(0);
            McGlState.clearShaderTexture(1);
            McGlState.clearShaderTexture(2);
        }

        //? if >=26.1 {
        boolean levelPass = context.renderLevel() && MC.level != null && MC.player != null
                && MC.isGameLoadFinished();
        if (levelPass) {
            VRCameraEntitySwap.cacheCameraEntity(MC.getCameraEntity());
            VRCameraEntitySwap.setupCameraEntityAsVRCamera();
            VRCameraOverlaps.updateCameraOverlaps();
        }
        try {
            McRenderUtils.renderGame(
                    MC.gameRenderer,
                    context.partialTicks(),
                    context.nanoTime(),
                    context.renderLevel()
            );
        } finally {
            if (levelPass) {
                VRCameraEntitySwap.restoreCameraEntity(MC.getCameraEntity());
            }
        }
        //?} else {
        /*McRenderUtils.renderGame(
                MC.gameRenderer,
                context.partialTicks(),
                context.nanoTime(),
                context.renderLevel()
        );
        *///?}
        //render game is outside of VR control. many mods might interfere
        //so, we drain GL errors instead of crash
        RenderStateHelper.drainExternalGLErrors("VR level render");

        if (ShaderCompatHelper.isShaderActive()) {
            McRenderTarget.bindWrite(McRenderTarget.mainTarget());
            McModelViewStack.push();
            McModelViewStack.identity();
            McModelViewStack.apply();
            ClientContext.decorationRenderer.renderShaderUi(new PoseStack(), context.partialTicks());
            McModelViewStack.pop();
            McModelViewStack.apply();
        }

        if (renderPass.isEye()) {
            if (renderPass == VRRenderPass.EYE_LEFT) {
                McRenderTarget.bindWrite(
                        ClientContext.renderer.getTextureLeftEye().getRenderTarget()
                );
            } else {
                McRenderTarget.bindWrite(
                        ClientContext.renderer.getTextureRightEye().getRenderTarget()
                );
            }

            VRShaders.getPostProcess().finishEye(
                    renderPass == VRRenderPass.EYE_LEFT
                            ? EyeType.LEFT : EyeType.RIGHT,
                    McRenderTarget.mainTarget(),
                    context.partialTicks()
            );
        }

        renderOverlaysAfterPostProcessing(context);

        ShaderCompatHelper.bridge().endEye();
    }



}
