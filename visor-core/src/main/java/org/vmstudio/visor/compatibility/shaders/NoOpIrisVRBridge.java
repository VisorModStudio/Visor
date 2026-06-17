package org.vmstudio.visor.compatibility.shaders;

import me.phoenixra.atumvr.api.enums.EyeType;

public final class NoOpIrisVRBridge implements IrisVRBridge {
    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public boolean sameSizedBuffers() {
        return false;
    }

    @Override
    public int getShaderLightValue() {
        return 8;
    }

    @Override
    public void beginFrame(float partialTicks, long frameNanos) {
    }

    @Override
    public void beginEye(EyeType eyeType) {
    }

    @Override
    public void endEye() {
    }

    @Override
    public void endFrame() {
    }

    @Override
    public void onVisorTargetsRecreated(int eyeRenderWidth, int eyeRenderHeight) {
    }

    @Override
    public void onPackChanged() {
    }

    @Override
    public void setIsMainBound(boolean bound) {
    }
}
