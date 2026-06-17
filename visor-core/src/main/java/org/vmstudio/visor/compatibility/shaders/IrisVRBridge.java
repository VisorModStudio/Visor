package org.vmstudio.visor.compatibility.shaders;

public interface IrisVRBridge {
    int EYE_NONE = -1;
    int EYE_LEFT = 0;
    int EYE_RIGHT = 1;

    boolean isActive();

    boolean sameSizedBuffers();

    int getShaderLightValue();

    void beginFrame(float partialTicks, long frameNanos);

    void beginEye(int eyeIndex);

    void endEye();

    void endFrame();

    void setIsMainBound(boolean bound);

    void onVisorTargetsRecreated(int eyeRenderWidth, int eyeRenderHeight);

    void onPackChanged();
}