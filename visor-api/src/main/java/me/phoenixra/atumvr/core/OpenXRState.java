package me.phoenixra.atumvr.core;

import lombok.Getter;
import me.phoenixra.atumvr.api.VRState;
import me.phoenixra.atumvr.core.enums.XREvent;
import me.phoenixra.atumvr.core.enums.XRSessionStateChange;
import me.phoenixra.atumvr.core.init.OpenXRInstance;
import me.phoenixra.atumvr.core.init.OpenXRSession;
import me.phoenixra.atumvr.core.init.OpenXRSwapChain;
import me.phoenixra.atumvr.core.init.OpenXRSystem;
import org.lwjgl.openxr.*;
import org.lwjgl.system.MemoryStack;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.system.MemoryUtil.NULL;

@Getter
public class OpenXRState implements VRState {
    private final OpenXRProvider vrProvider;

    protected OpenXRInstance vrInstance;
    protected OpenXRSystem vrSystem;
    protected OpenXRSession vrSession;
    protected OpenXRSwapChain vrSwapChain;



    protected final List<XREvent> xrEventsReceived = new ArrayList<>();



    protected boolean running = false;
    protected boolean active = false;
    protected boolean focused = false;

    protected boolean initialized = false;

    public OpenXRState(OpenXRProvider vrProvider){
        this.vrProvider = vrProvider;

    }

    public void init() throws Throwable{
        vrInstance = new OpenXRInstance(this);
        vrSystem = new OpenXRSystem(this);
        vrSession = new OpenXRSession(this);
        vrSwapChain = new OpenXRSwapChain(this);

        vrInstance.init();
        vrSystem.init();
        vrSession.init();

        vrSwapChain.init();

        while (!running){
            vrProvider.getLogger().logInfo("Waiting for OpenXR session to start...");
            pollVREvents();
        }
        vrProvider.inputHandler.init();
        vrProvider.renderer.init();

        initialized = true;
    }



    protected void pollVREvents() {
        xrEventsReceived.clear();
        XrEventDataBuffer eventBuffer = vrInstance.getXrEventBuffer();
        while (true) {
            eventBuffer.clear();
            eventBuffer.type(XR10.XR_TYPE_EVENT_DATA_BUFFER);
            int error = XR10.xrPollEvent(vrInstance.getHandle(), eventBuffer);
            vrProvider.checkXRError(error, "xrPollEvent", "");
            if (error != XR10.XR_SUCCESS) {
                //no more events available
                break;
            }
            var event = XrEventDataBaseHeader.create(eventBuffer.address());
            var vrEvent = XREvent.fromId(event.type());
            if (vrEvent == XREvent.SESSION_STATE_CHANGED) {
                xrStateChanged(
                        XrEventDataSessionStateChanged.create(event.address())
                );
            }else {
                xrEventsReceived.add(vrEvent);
            }
        }
    }
    private void xrStateChanged(XrEventDataSessionStateChanged event) {
        var stateChange = XRSessionStateChange.fromId(event.state());
        vrProvider.getLogger().logDebug("VR Session State changed to: "+stateChange);


        switch (stateChange) {
            case READY -> {
                try (MemoryStack stack = MemoryStack.stackPush()) {
                    XrSessionBeginInfo sessionBeginInfo = XrSessionBeginInfo.calloc(stack)
                            .type(XR10.XR_TYPE_SESSION_BEGIN_INFO)
                            .next(NULL)
                            .primaryViewConfigurationType(XR10.XR_VIEW_CONFIGURATION_TYPE_PRIMARY_STEREO);

                    vrProvider.checkXRError(
                            XR10.xrBeginSession(vrSession.getHandle(), sessionBeginInfo),
                            "xrBeginSession", "XRStateChangeREADY"
                    );
                }
                running = true;
                active = true;
                vrProvider.getLogger().logInfo("OpenXR session is READY");

            }

            case STOPPING -> {
                running = false;
                active = false;
            }

            case VISIBLE, FOCUSED -> active = true;

            case EXITING, IDLE, SYNCHRONIZED -> active = false;
        }

        focused = stateChange == XRSessionStateChange.FOCUSED;

        vrProvider.onStateChanged(stateChange);
    }



    public int getEyeTexWidth() {
        return vrSwapChain.getEyeMaxWidth();
    }

    public int getEyeTexHeight() {
        return vrSwapChain.getEyeMaxHeight();
    }




    public void destroy(){

        if(vrSwapChain != null){
            vrSwapChain.destroy();
        }
        if(vrSession != null){
            vrSession.destroy();
        }
        if(vrSystem != null){
            vrSystem.destroy();
        }
        if(vrInstance != null){
            vrInstance.destroy();
        }
        xrEventsReceived.clear();

        running = false;
        active = false;
        focused = false;
        initialized = false;
    }
}
