package me.phoenixra.atumvr.core.init;

import lombok.Getter;
import me.phoenixra.atumvr.api.exceptions.VRException;
import me.phoenixra.atumvr.core.OpenXRProvider;
import me.phoenixra.atumvr.core.OpenXRState;
import org.lwjgl.PointerBuffer;
import org.lwjgl.openxr.*;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.util.*;

public class OpenXRInstance {
    private final static String GRAPHICS_EXTENSION = KHROpenGLEnable.XR_KHR_OPENGL_ENABLE_EXTENSION_NAME;
    private final OpenXRState xrState;
    @Getter
    protected XrInstance handle;

    @Getter
    protected final XrEventDataBuffer xrEventBuffer;

    @Getter
    private String runtimeName;
    @Getter
    private long runtimeVersion;
    @Getter
    private String runtimeVersionString;

    private XrDebugUtilsMessengerEXT debugMessenger;

    public OpenXRInstance(OpenXRState xrState){
        this.xrState = xrState;
        this.xrEventBuffer = XrEventDataBuffer.calloc();
    }

    public void init() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            OpenXRProvider provider = this.xrState.getVrProvider();



            var extensionsPointer = setupExtensions(
                    provider, stack
            );

            // 1) Fill XrApplicationInfo
            var appInfo = XrApplicationInfo.calloc(stack)
                    .applicationName(stack.UTF8(provider.getAppName()))
                    .applicationVersion(1)
                    .engineName(stack.UTF8("AtumEngine"))
                    .engineVersion(1)
                    .apiVersion(XR10.XR_MAKE_VERSION(1, 0, 40));

            // 2) Create XrInstanceCreateInfo
            var instInfo = XrInstanceCreateInfo.calloc(stack)
                    .type(XR10.XR_TYPE_INSTANCE_CREATE_INFO)
                    .next(0)
                    .applicationInfo(appInfo)
                    .enabledExtensionNames(extensionsPointer)
                    .enabledApiLayerNames(null);

            // 3) Create the instance and handle errors
            var instancePointer = stack.callocPointer(1);
            int result = XR10.xrCreateInstance(instInfo, instancePointer);
            if (result == XR10.XR_ERROR_RUNTIME_FAILURE) {
                throw new VRException("Failed to create XrInstance: runtime failure (is headset connected?)");
            } else if (result == XR10.XR_ERROR_INSTANCE_LOST) {
                throw new VRException("Failed to create XrInstance: instance lost during creation");
            } else if (result != XR10.XR_SUCCESS) {
                provider.checkXRError(result, "xrCreateInstance", "Failed to create XrInstance");
            }

            this.handle = new XrInstance(instancePointer.get(0), instInfo);

            if(handle.getCapabilities().XR_EXT_debug_utils) {
                setupDebugMessenger(provider, stack);
            }

            var properties = XrInstanceProperties.calloc(stack).type$Default();
            provider.checkXRError(
                    XR10.xrGetInstanceProperties(handle, properties),
                    "xrGetInstanceProperties"
            );
            runtimeName = properties.runtimeNameString();
            runtimeVersion = properties.runtimeVersion();
            runtimeVersionString = XR10.XR_VERSION_MAJOR(runtimeVersion)
                    + "." + XR10.XR_VERSION_MINOR(runtimeVersion)
                    + "." + XR10.XR_VERSION_PATCH(runtimeVersion);
        }
    }

    private PointerBuffer setupExtensions(OpenXRProvider provider, MemoryStack stack){


        // 1) Enumerate available instance extensions
        var extCountBuf = stack.callocInt(1);
        provider.checkXRError(
                XR10.xrEnumerateInstanceExtensionProperties((ByteBuffer)null, extCountBuf, null),
                "xrEnumerateInstanceExtensionProperties", "count"
        );

        int extCount = extCountBuf.get(0);
        var extProperties = XrExtensionProperties
                .calloc(extCount, stack);
        extProperties.forEach(
                prop -> prop.type(XR10.XR_TYPE_EXTENSION_PROPERTIES)
        );

        provider.checkXRError(
                XR10.xrEnumerateInstanceExtensionProperties((ByteBuffer)null, extCountBuf, extProperties),
                "xrEnumerateInstanceExtensionProperties", "properties"
        );

        // Collect supported extension names
        Set<String> availableExtensions = new HashSet<>(extCount);
        for (XrExtensionProperties prop : extProperties) {
            availableExtensions.add(prop.extensionNameString());
        }


        // 2) Define desired extensions in priority order
        List<String> desiredExtensions = new ArrayList<>(List.of(
                GRAPHICS_EXTENSION,

                EXTDebugUtils.XR_EXT_DEBUG_UTILS_EXTENSION_NAME,
                FBDisplayRefreshRate.XR_FB_DISPLAY_REFRESH_RATE_EXTENSION_NAME,
                KHRVisibilityMask.XR_KHR_VISIBILITY_MASK_EXTENSION_NAME //@TODO test mask for performance improvements
        ));
        desiredExtensions.addAll(provider.getXRAppExtensions());

        // Ensure graphics extension is present
        if (!availableExtensions.contains(GRAPHICS_EXTENSION)) {
            throw new VRException(
                    "Missing required graphics extension: " + GRAPHICS_EXTENSION
            );
        }

        // Build PointerBuffer of only the extensions actually supported
        var enabledExtensions = stack.mallocPointer(desiredExtensions.size());
        for (String extName : desiredExtensions) {
            if (availableExtensions.contains(extName)) {
                enabledExtensions.put(stack.UTF8(extName));
            }
        }
        enabledExtensions.flip();
        return enabledExtensions;
    }

    private void setupDebugMessenger(OpenXRProvider provider, MemoryStack stack) {
        var createInfo = XrDebugUtilsMessengerCreateInfoEXT
                .calloc(stack)
                .type$Default()  // XR_TYPE_DEBUG_UTILS_MESSENGER_CREATE_INFO_EXT :contentReference[oaicite:0]{index=0}
                .messageSeverities(
                        EXTDebugUtils.XR_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT
                                | EXTDebugUtils.XR_DEBUG_UTILS_MESSAGE_SEVERITY_INFO_BIT_EXT
                                | EXTDebugUtils.XR_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT
                                | EXTDebugUtils.XR_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT
                )
                // catch every message type:
                .messageTypes(
                        EXTDebugUtils.XR_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT
                                | EXTDebugUtils.XR_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT
                                | EXTDebugUtils.XR_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT
                                | EXTDebugUtils.XR_DEBUG_UTILS_MESSAGE_TYPE_CONFORMANCE_BIT_EXT
                );


        XrDebugUtilsMessengerCallbackEXTI debugCallback = (messageSeverity, messageTypes, pCallbackData, pUserData) -> {

            var data = XrDebugUtilsMessengerCallbackDataEXT.create(pCallbackData);

            provider.getLogger().logDebug(
                    String.format(
                            "[OpenXR][%s] %s%n",
                            data.functionNameString(),
                            data.messageString()
                    )
            );
            return XR10.XR_FALSE; // don't abort the call that triggered this
        };

        createInfo
                .userCallback(debugCallback)
                .userData(0);

        PointerBuffer pMessenger = stack.callocPointer(1);
        int err = EXTDebugUtils.xrCreateDebugUtilsMessengerEXT(
                handle, createInfo, pMessenger
        );
        provider.checkXRError(
                err, "xrCreateDebugUtilsMessengerEXT", ""
        );
        debugMessenger = new XrDebugUtilsMessengerEXT(pMessenger.get(0), handle);
    }
    public void destroy(){
        if (debugMessenger != null) {
            xrState.getVrProvider().checkXRError(
                    false,
                    EXTDebugUtils.xrDestroyDebugUtilsMessengerEXT(debugMessenger),
                    "xrDestroyDebugUtilsMessengerEXT",
                    ""
            );
        }
        if (handle != null) {
            xrState.getVrProvider().checkXRError(
                    false,
                    XR10.xrDestroyInstance(handle),
                    "xrDestroyInstance",
                    ""
            );
        }
        xrEventBuffer.close();
    }
}
