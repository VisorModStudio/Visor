package me.phoenixra.atumvr.core.init;

import lombok.Getter;
import me.phoenixra.atumvr.api.VRLogger;
import me.phoenixra.atumvr.core.OpenXRState;
import org.lwjgl.PointerBuffer;
import org.lwjgl.openxr.*;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.List;

import static org.lwjgl.openxr.XR10.*;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.system.MemoryUtil.memCalloc;

public class OpenXRSwapChain {


    private static final int VIEW_TYPE = XR_VIEW_CONFIGURATION_TYPE_PRIMARY_STEREO;


    private final OpenXRState xrState;
    private final VRLogger LOG;
    private final List<Integer> desiredSwapChainFormats;


    @Getter
    private XrSwapchain handle;

    @Getter
    private XrView.Buffer xrViewBuffer;

    @Getter
    private int eyeMaxWidth, eyeMaxHeight;

    public OpenXRSwapChain(OpenXRState xrState) {
        this.xrState = xrState;
        LOG = xrState.getVrProvider().getLogger();
        desiredSwapChainFormats = xrState.getVrProvider().getSwapChainFormats();
    }



    public void init() {
        try (MemoryStack stack = MemoryStack.stackPush()) {

            XrInstance    instance = xrState.getVrInstance().getHandle();
            XrSession     session  = xrState.getVrSession().getHandle();
            long          systemId = xrState.getVrSystem().getSystemId();

            int viewCount = enumerateViewCount(instance, systemId, stack);

            var viewConfigs = enumerateViewConfigs(instance, systemId, viewCount, stack);

            long chosenFormat = pickSwapchainFormat(session, stack);

            this.handle       = createSwapchain(session, viewConfigs.get(0), chosenFormat, stack);
            this.eyeMaxWidth  = viewConfigs.get(0).recommendedImageRectWidth();
            this.eyeMaxHeight = viewConfigs.get(0).recommendedImageRectHeight();

            // persistent view buffer on heap
            this.xrViewBuffer = XrView.calloc(viewCount);
            for (int i = 0; i < viewCount; i++) {
                xrViewBuffer.get(i).type(XR_TYPE_VIEW).next(NULL);
            }

            LOG.logInfo(String.format("Swapchain created: %s×%s pixels, format 0x%s",
                    eyeMaxWidth, eyeMaxHeight, Long.toHexString(chosenFormat))
            );
        }
    }

    private int enumerateViewCount(XrInstance instance, long systemId, MemoryStack stack) {
        IntBuffer countBuf = stack.callocInt(1);
        xrState.getVrProvider().checkXRError(
                xrEnumerateViewConfigurationViews(
                        instance, systemId, VIEW_TYPE, countBuf, null
                ),
                "xrEnumerateViewConfigurationViews",
                "count"
        );
        return countBuf.get(0);
    }

    private XrViewConfigurationView.Buffer enumerateViewConfigs(
            XrInstance instance,
            long systemId,
            int viewCount,
            MemoryStack stack)
    {
        XrViewConfigurationView.Buffer configs = XrViewConfigurationView.calloc(viewCount, stack);
        for (int i = 0; i < viewCount; i++) {
            configs.get(i).type(XR_TYPE_VIEW_CONFIGURATION_VIEW).next(NULL);
        }
        IntBuffer countBuf = stack.ints(viewCount);
        int err = xrEnumerateViewConfigurationViews(instance, systemId, VIEW_TYPE, countBuf, configs);
        xrState.getVrProvider().checkXRError(err, "xrEnumerateViewConfigurationViews", "views");
        return configs;
    }

    private long pickSwapchainFormat(XrSession session, MemoryStack stack) {
        IntBuffer fmtCount = stack.callocInt(1);
        xrState.getVrProvider().checkXRError(
                xrEnumerateSwapchainFormats(session, fmtCount, null),
                "xrEnumerateSwapchainFormats", "count"
        );

        LongBuffer available = stack.callocLong(fmtCount.get(0));
        xrState.getVrProvider().checkXRError(
                xrEnumerateSwapchainFormats(session, fmtCount, available),
                "xrEnumerateSwapchainFormats", "values"
        );

        for (long want : desiredSwapChainFormats) {
            for (int i = 0; i < available.capacity(); i++) {
                if (available.get(i) == want) {
                    LOG.logDebug(String.format(
                            "Selected swapchain format: 0x%s", Long.toHexString(want)
                    ));
                    return want;
                }
            }
        }

        throw new IllegalStateException("No compatible swapchain format found: " + available);
    }

    private XrSwapchain createSwapchain(
            XrSession session,
            XrViewConfigurationView viewConfig,
            long format,
            MemoryStack stack)
    {
        XrSwapchainCreateInfo info = XrSwapchainCreateInfo.calloc(stack)
                .type(XR_TYPE_SWAPCHAIN_CREATE_INFO)
                .next(NULL)
                .usageFlags(XR_SWAPCHAIN_USAGE_COLOR_ATTACHMENT_BIT)
                .format(format)
                .sampleCount(1)
                .width(viewConfig.recommendedImageRectWidth())
                .height(viewConfig.recommendedImageRectHeight())
                .faceCount(1)
                .arraySize(2)    // stereo
                .mipCount(1);

        PointerBuffer handlePtr = stack.callocPointer(1);
        xrState.getVrProvider().checkXRError(
                xrCreateSwapchain(session, info, handlePtr),
                "xrCreateSwapchain", "format: "+Long.toHexString(format)
        );
        return new XrSwapchain(handlePtr.get(0), session);
    }



    public XrSwapchainImageOpenGLKHR.Buffer createImageBuffers(int imageCount, MemoryStack stack) {
        var swapchainImageBuffer = XrSwapchainImageOpenGLKHR.calloc(imageCount, stack);
        for (XrSwapchainImageOpenGLKHR image : swapchainImageBuffer) {
            image.type(KHROpenGLEnable.XR_TYPE_SWAPCHAIN_IMAGE_OPENGL_KHR);
        }

        return swapchainImageBuffer;
    }



    public void destroy() {
        destroySwapchainQuietly();
        destroyViewBuffer();
    }

    private void destroySwapchainQuietly() {
        if (handle == null) return;
        int err = xrDestroySwapchain(handle);
        xrState.getVrProvider().checkXRError(false, err, "xrDestroySwapchain", "ignoring on teardown");
        handle = null;
        LOG.logDebug("Swapchain destroyed");
    }

    private void destroyViewBuffer() {
        if (xrViewBuffer != null) {
            xrViewBuffer.close();
            xrViewBuffer = null;
            LOG.logDebug("View buffer destroyed");
        }
    }
}
