package me.phoenixra.atumvr.core.init;

import lombok.Getter;
import me.phoenixra.atumvr.api.exceptions.VRException;
import me.phoenixra.atumvr.core.OpenXRProvider;
import me.phoenixra.atumvr.core.OpenXRState;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWNativeGLX;
import org.lwjgl.glfw.GLFWNativeWGL;
import org.lwjgl.glfw.GLFWNativeWin32;
import org.lwjgl.glfw.GLFWNativeX11;
import org.lwjgl.openxr.*;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.Platform;
import org.lwjgl.system.Struct;
import org.lwjgl.system.linux.X11;
import org.lwjgl.system.windows.User32;

import java.nio.LongBuffer;
import java.util.Objects;

import static org.lwjgl.opengl.GLX13.*;
import static org.lwjgl.system.MemoryStack.stackInts;
import static org.lwjgl.system.MemoryUtil.*;

public class OpenXRSystem {
    private final OpenXRState xrState;

    @Getter
    private long systemId;

    public OpenXRSystem(OpenXRState xrState){
        this.xrState = xrState;

    }
    public void init(){
        try (MemoryStack stack = MemoryStack.stackPush()) {
            OpenXRProvider provider = this.xrState.getVrProvider();

            // 1) Acquire system ID for HMD
            var sysGetInfo = XrSystemGetInfo.calloc(stack)
                    .type(XR10.XR_TYPE_SYSTEM_GET_INFO)
                    .next(NULL)
                    .formFactor(XR10.XR_FORM_FACTOR_HEAD_MOUNTED_DISPLAY);

            LongBuffer sysIdBuf = stack.callocLong(1);
            provider.checkXRError(
                    XR10.xrGetSystem(
                            xrState.getVrInstance().getHandle(),
                            sysGetInfo,
                            sysIdBuf
                    ),
                    "xrGetSystem", "fetch HMD system ID"
            );

            systemId = sysIdBuf.get(0);
            if (systemId == XR10.XR_NULL_SYSTEM_ID) {
                throw new VRException("No compatible HMD detected (system ID == 0)");
            }

            // 2) Query system properties
            var sysProps = XrSystemProperties.calloc(stack)
                    .type(XR10.XR_TYPE_SYSTEM_PROPERTIES);
            provider.checkXRError(
                    XR10.xrGetSystemProperties(
                            xrState.getVrInstance().getHandle(),
                            systemId,
                            sysProps
                    ),
                    "xrGetSystemProperties", "id=" + systemId
            );

            String name = memUTF8(memAddress(sysProps.systemName()));
            var track = sysProps.trackingProperties();
            var gfx = sysProps.graphicsProperties();

            provider.getLogger().logInfo(
                    String.format(
                            "Found HMD [%s] (vendor=%d): orientTrack=%b, posTrack=%b, maxRes=%dx%d, maxLayers=%d",
                            name,
                            sysProps.vendorId(),
                            track.orientationTracking(),
                            track.positionTracking(),
                            gfx.maxSwapchainImageWidth(),
                            gfx.maxSwapchainImageHeight(),
                            gfx.maxLayerCount()
                    )
            );
        }
    }

    public Struct<?> createGraphicsBinding(MemoryStack stack,
                                            XrInstance instance,
                                            long systemID,
                                            long windowHandle) {

        var graphicsRequirements = XrGraphicsRequirementsOpenGLKHR.calloc(stack)
                .type(KHROpenGLEnable.XR_TYPE_GRAPHICS_REQUIREMENTS_OPENGL_KHR);
        KHROpenGLEnable.xrGetOpenGLGraphicsRequirementsKHR(
                instance, systemID, graphicsRequirements
        );
        //Bind the OpenGL context to the OpenXR instance and create the session
        if (Platform.get() == Platform.WINDOWS) {
            return XrGraphicsBindingOpenGLWin32KHR.calloc(stack).set(
                    KHROpenGLEnable.XR_TYPE_GRAPHICS_BINDING_OPENGL_WIN32_KHR,
                    NULL,
                    User32.GetDC(GLFWNativeWin32.glfwGetWin32Window(windowHandle)),
                    GLFWNativeWGL.glfwGetWGLContext(windowHandle)
            );
        } else if (Platform.get() == Platform.LINUX) {
            long xDisplay = GLFWNativeX11.glfwGetX11Display();

            long glXContext = GLFWNativeGLX.glfwGetGLXContext(windowHandle);
            long glXWindowHandle = GLFWNativeGLX.glfwGetGLXWindow(windowHandle);

            int fbXID = glXQueryDrawable(xDisplay, glXWindowHandle, GLX_FBCONFIG_ID);
            PointerBuffer fbConfigBuf = glXChooseFBConfig(
                    xDisplay, X11.XDefaultScreen(xDisplay),
                    stackInts(GLX_FBCONFIG_ID, fbXID, 0)
            );
            if (fbConfigBuf == null) {
                throw new VRException("Framebuffer config is null");
            }
            long fbConfig = fbConfigBuf.get();

            return XrGraphicsBindingOpenGLXlibKHR.calloc(stack).set(
                    KHROpenGLEnable.XR_TYPE_GRAPHICS_BINDING_OPENGL_XLIB_KHR,
                    NULL,
                    xDisplay,
                    (int) Objects.requireNonNull(glXGetVisualFromFBConfig(xDisplay, fbConfig)).visualid(),
                    fbConfig,
                    glXWindowHandle,
                    glXContext
            );
        } else {
            throw new VRException("MacOS not supported");
        }
    }
    public void destroy(){

    }
}
