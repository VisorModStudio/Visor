package org.vmstudio.visor.core.client.render.target;

import com.mojang.blaze3d.pipeline.RenderTarget;
import org.vmstudio.visor.api.client.render.VRRenderPass;
import org.vmstudio.visor.core.client.render.VRRenderState;

import java.util.EnumMap;


public class MultiCameraRenderTarget extends RenderTarget {

    private final RenderTarget mainTarget;
    private final EnumMap<VRRenderPass, RenderTarget> vrTargets;

    public MultiCameraRenderTarget(RenderTarget mainTarget, EnumMap<VRRenderPass, RenderTarget> vrTargets) {
        super(mainTarget.useDepth);

        this.mainTarget = mainTarget;
        this.vrTargets = vrTargets;

        //Defaults from main target
        this.frameBufferId = mainTarget.frameBufferId;
        this.filterMode = mainTarget.filterMode;

        this.width = mainTarget.width;
        this.height = mainTarget.height;
        this.viewWidth = mainTarget.viewWidth;
        this.viewHeight = mainTarget.viewHeight;
    }

    private RenderTarget getCurrentTarget() {
        if (VRRenderState.getPhase().isVanilla()) {
            return mainTarget;
        }
        VRRenderPass renderPass = VRRenderState.getRenderPass();
        if (renderPass.isNull()) {
            return mainTarget;
        }
        RenderTarget target = vrTargets.get(renderPass);
        return target != null ? target : mainTarget;
    }

    @Override
    public void resize(int width, int height) {
        getCurrentTarget().resize(width, height);
    }

    @Override
    public void destroyBuffers() {
        mainTarget.destroyBuffers();
        vrTargets.values().forEach(RenderTarget::destroyBuffers);
    }

    @Override
    public void copyDepthFrom(RenderTarget other) {
        getCurrentTarget().copyDepthFrom(other);
    }

    @Override
    public void createBuffers(int width, int height) {
        getCurrentTarget().createBuffers(width, height);
    }

    @Override
    public void setFilterMode(int filterMode) {
        getCurrentTarget().setFilterMode(filterMode);
    }

    @Override
    public void checkStatus() {
        getCurrentTarget().checkStatus();
    }

    @Override
    public void bindRead() {
        getCurrentTarget().bindRead();
    }

    @Override
    public void unbindRead() {
        getCurrentTarget().unbindRead();
    }

    @Override
    public void bindWrite(boolean setViewport) {
        getCurrentTarget().bindWrite(setViewport);
    }

    @Override
    public void unbindWrite() {
        getCurrentTarget().unbindWrite();
    }

    @Override
    public void setClearColor(float red, float green, float blue, float alpha) {
        getCurrentTarget().setClearColor(red, green, blue, alpha);
    }

    @Override
    public void blitToScreen(int width, int height) {
        getCurrentTarget().blitToScreen(width, height);
    }

    // 1.21.2 dropped the blitToScreen(w, h, disableBlend) overload; the blend-enabled
    // variant is now a separate method.
    @Override
    public void blitAndBlendToScreen(int width, int height) {
        getCurrentTarget().blitAndBlendToScreen(width, height);
    }

    @Override
    public void clear() {
        getCurrentTarget().clear();
    }

    @Override
    public int getColorTextureId() {
        return getCurrentTarget().getColorTextureId();
    }

    @Override
    public int getDepthTextureId() {
        return getCurrentTarget().getDepthTextureId();
    }
}
