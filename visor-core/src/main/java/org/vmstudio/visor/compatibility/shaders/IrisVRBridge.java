package org.vmstudio.visor.compatibility.shaders;

import me.phoenixra.atumvr.api.enums.EyeType;

public interface IrisVRBridge {
    boolean isActive();

    boolean sameSizedBuffers();

    int getShaderLightValue();

    void beginFrame(float partialTicks, long frameNanos);

    void beginEye(EyeType eyeType);

    void endEye();

    void endFrame();

    void setIsMainBound(boolean bound);

    void onVisorTargetsRecreated(int eyeRenderWidth, int eyeRenderHeight);

    void onPackChanged();
}