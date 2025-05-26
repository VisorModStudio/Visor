package me.phoenixra.atumvr.core.init;

import lombok.Getter;

import me.phoenixra.atumvr.core.OpenXRHelper;
import me.phoenixra.atumvr.core.OpenXRProvider;
import me.phoenixra.atumvr.core.OpenXRState;
import org.lwjgl.PointerBuffer;
import org.lwjgl.openxr.*;
import org.lwjgl.system.MemoryStack;

import org.lwjgl.system.Struct;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class OpenXRSession {
    private final OpenXRState xrState;

    @Getter
    protected XrSession handle;

    @Getter
    protected XrSpace xrAppSpace;
    @Getter
    protected XrSpace xrViewSpace;

    public OpenXRSession(OpenXRState xrState){
        this.xrState = xrState;

    }


    public void init() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            OpenXRProvider provider = this.xrState.getVrProvider();
            initSession(provider, stack);
            initSpaces(stack);
            initDisplayRefreshRate(provider, stack);
        }
    }


    private void initSession(OpenXRProvider provider, MemoryStack stack){
        OpenXRInstance xrInstance  = xrState.getVrInstance();
        OpenXRSystem xrSystem  = xrState.getVrSystem();
        long systemId = xrState.getVrSystem().getSystemId();

        Struct<?> graphicsBind = xrSystem.createGraphicsBinding(
                stack,
                xrInstance.getHandle(),
                systemId,
                provider.getRenderer().getWindowHandle()
        );
        var sessionInfo = XrSessionCreateInfo.calloc(stack)
                .type(XR10.XR_TYPE_SESSION_CREATE_INFO)
                .next(graphicsBind.address())
                .systemId(systemId)
                .createFlags(0);

        PointerBuffer sessionBuf = stack.callocPointer(1);
        provider.checkXRError(
                XR10.xrCreateSession(xrInstance.getHandle(), sessionInfo, sessionBuf),
                "xrCreateSession", "Failed to create session"
        );
        handle = new XrSession(sessionBuf.get(0), xrInstance.getHandle());

    }

    private void initSpaces(MemoryStack stack) {
        // Identity pose for all reference spaces
        XrPosef identity = XrPosef.calloc(stack)
                .set(
                        XrQuaternionf.calloc(stack).set(0, 0, 0, 1),
                        XrVector3f.calloc(stack).set(0f, 0f, 0f)
                );

        xrAppSpace  = OpenXRHelper.createReferenceSpace(
                xrState,
                XR10.XR_REFERENCE_SPACE_TYPE_STAGE,
                identity,
                stack
        );

        identity = XrPosef.calloc(stack)
                .set(
                        XrQuaternionf.calloc(stack).set(0, 0, 0, 1),
                        XrVector3f.calloc(stack).set(0f, 0f, 0f)
                );
        xrViewSpace = OpenXRHelper.createReferenceSpace(
                xrState,
                XR10.XR_REFERENCE_SPACE_TYPE_VIEW,
                identity,
                stack
        );
    }



    //@TODO test it
    private void initDisplayRefreshRate(OpenXRProvider provider, MemoryStack stack) {
        if (handle.getCapabilities().XR_FB_display_refresh_rate) {
            IntBuffer refreshRateCount = stack.callocInt(1);
            provider.checkXRError(
                    FBDisplayRefreshRate.xrEnumerateDisplayRefreshRatesFB(
                            handle, refreshRateCount, null
                    ),
                    "xrEnumerateDisplayRefreshRatesFB",
                    "first call"
            );
            FloatBuffer refreshRateBuffer = stack.callocFloat(refreshRateCount.get(0));
            provider.checkXRError(
                    FBDisplayRefreshRate.xrEnumerateDisplayRefreshRatesFB(
                            handle, refreshRateCount, refreshRateBuffer
                    ),
                    "xrEnumerateDisplayRefreshRatesFB",
                    "second call"
            );
            refreshRateBuffer.rewind();
            provider.checkXRError(
                    FBDisplayRefreshRate.xrRequestDisplayRefreshRateFB(
                            handle, refreshRateBuffer.get(refreshRateCount.get(0) -1)
                    ),
                    "xrRequestDisplayRefreshRateFB"
            );
        }
    }

    public void destroy(){
        OpenXRProvider provider = xrState.getVrProvider();

        if (this.xrAppSpace != null) {
            provider.checkXRError(
                    false,
                    XR10.xrDestroySpace(this.xrAppSpace),
                    "xrDestroySpace",
                    "xrAppSpace"
            );
        }
        if (this.xrViewSpace != null) {
            provider.checkXRError(
                    false,
                    XR10.xrDestroySpace(this.xrViewSpace),
                    "xrDestroySpace",
                    "xrViewSpace"
            );
        }
        if (this.handle != null) {
            provider.checkXRError(
                    false,
                    XR10.xrDestroySession(this.handle),
                    "xrDestroySession",
                    "xrAppSpace"
            );
        }
    }
}
