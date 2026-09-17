package org.vmstudio.visor.core.client.render.target;

import com.mojang.blaze3d.pipeline.RenderTarget;
//? if >=1.21.5 {
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
//?}
import org.vmstudio.visor.api.client.render.VRRenderPass;
import org.vmstudio.visor.core.client.render.VRRenderState;

import java.util.EnumMap;


public class MultiCameraRenderTarget extends RenderTarget {

    private final RenderTarget mainTarget;
    private final EnumMap<VRRenderPass, RenderTarget> vrTargets;

    public MultiCameraRenderTarget(RenderTarget mainTarget, EnumMap<VRRenderPass, RenderTarget> vrTargets) {
        //? if >=1.21.5 {
        super("visor_multi_camera", mainTarget.useDepth);
        //?} else {
        /*super(mainTarget.useDepth);
        *///?}

        this.mainTarget = mainTarget;
        this.vrTargets = vrTargets;

        //Defaults from main target
        //? if <1.21.5 {
        /*this.frameBufferId = mainTarget.frameBufferId;
        *///?}
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

    //? if >=1.21.2 {
    @Override
    public void resize(int width, int height) {
        getCurrentTarget().resize(width, height);
    }
    //?} else {
    /*@Override
    public void resize(int width, int height, boolean clearError) {
        getCurrentTarget().resize(width, height, clearError);
    }
    *///?}

    @Override
    public void destroyBuffers() {
        mainTarget.destroyBuffers();
        vrTargets.values().forEach(RenderTarget::destroyBuffers);
    }

    @Override
    public void copyDepthFrom(RenderTarget other) {
        getCurrentTarget().copyDepthFrom(other);
    }

    //? if >=1.21.2 {
    @Override
    public void createBuffers(int width, int height) {
        getCurrentTarget().createBuffers(width, height);
    }
    //?} else {
    /*@Override
    public void createBuffers(int width, int height, boolean clearError) {
        getCurrentTarget().createBuffers(width, height, clearError);
    }
    *///?}

    //? if >=1.21.5 {
    @Override
    public void setFilterMode(FilterMode filterMode) {
        getCurrentTarget().setFilterMode(filterMode);
    }

    @Override
    public void blitToScreen() {
        getCurrentTarget().blitToScreen();
    }

    @Override
    public void blitAndBlendToTexture(GpuTexture texture) {
        getCurrentTarget().blitAndBlendToTexture(texture);
    }

    @Override
    public GpuTexture getColorTexture() {
        return getCurrentTarget().getColorTexture();
    }

    @Override
    public GpuTexture getDepthTexture() {
        return getCurrentTarget().getDepthTexture();
    }
    //?} elif >=1.21.2 {
    /*    @Override
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
    *///?} else {
        /*@Override
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
    public void blitToScreen(int width, int height, boolean disableBlend) {
        getCurrentTarget().blitToScreen(width, height, disableBlend);
    }

    @Override
    public void clear(boolean clearError) {
        getCurrentTarget().clear(clearError);
    }

    @Override
    public int getColorTextureId() {
        return getCurrentTarget().getColorTextureId();
    }

    @Override
    public int getDepthTextureId() {
        return getCurrentTarget().getDepthTextureId();
    }
    *///?}
}
