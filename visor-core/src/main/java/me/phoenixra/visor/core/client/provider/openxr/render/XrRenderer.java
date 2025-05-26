package me.phoenixra.visor.core.client.provider.openxr.render;

import lombok.Getter;
import me.phoenixra.atumvr.api.enums.EyeType;
import me.phoenixra.atumvr.api.input.device.VRDeviceHMD;
import me.phoenixra.atumvr.api.rendering.IRenderContext;
import me.phoenixra.atumvr.api.utils.GLUtils;
import me.phoenixra.atumvr.core.input.device.OpenXRDeviceHMD;
import me.phoenixra.visor.core.client.provider.VisorScene;
import me.phoenixra.visor.core.client.provider.openxr.XrVRProvider;
import me.phoenixra.visor.core.client.render.VisorRendererBase;
import me.phoenixra.visor.core.common.utils.LoggerUtils;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.openxr.*;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

public class XrRenderer extends VisorRendererBase {
    @Getter
    private final XrVRProvider vrProvider;

    protected int swapIndex;

    protected XrEyeTexture[] leftFramebuffers;
    protected XrEyeTexture[] rightFramebuffers;

    protected XrCompositionLayerProjectionView.Buffer projectionLayerViews;


    boolean frameStarted;
    @Getter
    private final VisorScene currentScene;
    public XrRenderer(XrVRProvider provider) {
        vrProvider = provider;
        currentScene = new VisorScene(this);
    }


    @Override
    public void preRender(@NotNull IRenderContext context) {
        if(frameStarted) return;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            XrFrameState frameState = XrFrameState.calloc(stack).type(XR10.XR_TYPE_FRAME_STATE);

            vrProvider.checkXRError(
                    XR10.xrWaitFrame(
                            vrProvider.getState().getVrSession().getHandle(),
                            XrFrameWaitInfo.calloc(stack)
                                    .type(XR10.XR_TYPE_FRAME_WAIT_INFO),
                            frameState
                    ),
                    "xrWaitFrame", ""
            );

            vrProvider.setXrDisplayTime(frameState.predictedDisplayTime());

            vrProvider.checkXRError(
                    XR10.xrBeginFrame(
                            vrProvider.getState().getVrSession().getHandle(),
                            XrFrameBeginInfo.calloc(stack)
                                    .type(XR10.XR_TYPE_FRAME_BEGIN_INFO)
                    ),
                    "xrBeginFrame", ""
            );


            XrViewState viewState = XrViewState.calloc(stack).type(XR10.XR_TYPE_VIEW_STATE);
            IntBuffer intBuf = stack.callocInt(1);

            XrViewLocateInfo viewLocateInfo = XrViewLocateInfo.calloc(stack);
            viewLocateInfo.set(
                    XR10.XR_TYPE_VIEW_LOCATE_INFO,
                    0,
                    XR10.XR_VIEW_CONFIGURATION_TYPE_PRIMARY_STEREO,
                    frameState.predictedDisplayTime(),
                    vrProvider.getState().getVrSession().getXrAppSpace()
            );

            vrProvider.checkXRError(
                    XR10.xrLocateViews(
                            vrProvider.getState().getVrSession().getHandle(),
                            viewLocateInfo, viewState,
                            intBuf, vrProvider.getState().getVrSwapChain().getXrViewBuffer()
                    ),
                    "xrLocateViews", ""
            );


        }
        frameStarted = true;
    }

    @Override
    public void renderFrame(@NotNull IRenderContext context) {
        if(!frameStarted) return;
        frameStarted = false;

        prepareSwapChains();

        try {
            getCurrentScene().render(context);
        }catch (Throwable e){
            LoggerUtils.printError(e);
        }


        finishFrame();

    }
    private void prepareSwapChains(){
        XrSwapchain xrSwapchain = vrProvider.getState().getVrSwapChain().getHandle();
        this.projectionLayerViews = XrCompositionLayerProjectionView.calloc(2);
        try (MemoryStack stack = MemoryStack.stackPush()) {

            IntBuffer intBuf2 = stack.callocInt(1);

            vrProvider.checkXRError(
                    XR10.xrAcquireSwapchainImage(
                            xrSwapchain,
                            XrSwapchainImageAcquireInfo
                                    .calloc(stack)
                                    .type(XR10.XR_TYPE_SWAPCHAIN_IMAGE_ACQUIRE_INFO),
                            intBuf2
                    ),
                    "xrAcquireSwapchainImage", ""
            );

            vrProvider.checkXRError(
                    XR10.xrWaitSwapchainImage(xrSwapchain,
                            XrSwapchainImageWaitInfo.calloc(stack)
                                    .type(XR10.XR_TYPE_SWAPCHAIN_IMAGE_WAIT_INFO)
                                    .timeout(XR10.XR_INFINITE_DURATION)
                    ),
                    "xrWaitSwapchainImage", ""
            );

            this.swapIndex = intBuf2.get(0);

            // Render view to the appropriate part of the swapchain image.
            for (EyeType eyeType : EyeType.values()) {
                int index = eyeType.getIndex();
                XrView xrView = vrProvider.getInputHandler()
                        .getDevice(VRDeviceHMD.ID, OpenXRDeviceHMD.class)
                        .getXrView(eyeType);
                XrSwapchainSubImage subImage = this.projectionLayerViews.get(index)
                        .type(XR10.XR_TYPE_COMPOSITION_LAYER_PROJECTION_VIEW)
                        .pose(xrView.pose())
                        .fov(xrView.fov())
                        .subImage();
                subImage.swapchain(xrSwapchain);
                subImage.imageRect().offset().set(0, 0);
                subImage.imageRect().extent().set(resolutionWidth, resolutionHeight);
                subImage.imageArrayIndex(index);
            }

        }
    }

    public void finishFrame(){
        XrSwapchain xrSwapchain = vrProvider.getState().getVrSwapChain().getHandle();

        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer layers = stack.callocPointer(1);
            int error;

            error = XR10.xrReleaseSwapchainImage(
                    xrSwapchain,
                    XrSwapchainImageReleaseInfo.calloc(stack)
                            .type(XR10.XR_TYPE_SWAPCHAIN_IMAGE_RELEASE_INFO));
            vrProvider.checkXRError(error, "xrReleaseSwapchainImage", "");

            XrCompositionLayerProjection compositionLayerProjection = XrCompositionLayerProjection.calloc(stack)
                    .type(XR10.XR_TYPE_COMPOSITION_LAYER_PROJECTION)
                    .space(vrProvider.getState().getVrSession().getXrAppSpace())
                    .views(this.projectionLayerViews);

            layers.put(compositionLayerProjection);

            layers.flip();

            error = XR10.xrEndFrame(
                    vrProvider.getState().getVrSession().getHandle(),
                    XrFrameEndInfo.calloc(stack)
                            .type(XR10.XR_TYPE_FRAME_END_INFO)
                            .displayTime(vrProvider.getXrDisplayTime())
                            .environmentBlendMode(XR10.XR_ENVIRONMENT_BLEND_MODE_OPAQUE)
                            .layers(layers));
            vrProvider.checkXRError(error, "xrEndFrame", "");

            this.projectionLayerViews.close();
        }
    }

    protected void setupEyes() {
        if(leftFramebuffers != null) return;
        try (MemoryStack stack = MemoryStack.stackPush()) {

            // Get amount of views in the swapchain
            IntBuffer intBuffer = stack.ints(0); //Set value to 0
            int error = XR10.xrEnumerateSwapchainImages(vrProvider.getState().getVrSwapChain().getHandle(), intBuffer, null);
            vrProvider.checkXRError(error, "xrEnumerateSwapchainImages", "get count");

            // Now we know the amount, create the image buffer
            int imageCount = intBuffer.get(0);
            XrSwapchainImageOpenGLKHR.Buffer swapchainImageBuffer = vrProvider
                    .getState().getVrSwapChain().createImageBuffers(imageCount,
                            stack);

            error = XR10.xrEnumerateSwapchainImages(vrProvider.getState().getVrSwapChain().getHandle(), intBuffer,
                    XrSwapchainImageBaseHeader.create(swapchainImageBuffer.address(), swapchainImageBuffer.capacity()));
            vrProvider.checkXRError(error, "xrEnumerateSwapchainImages", "get images");

            this.leftFramebuffers = new XrEyeTexture[imageCount];
            this.rightFramebuffers = new XrEyeTexture[imageCount];

            for (int i = 0; i < imageCount; i++) {
                XrSwapchainImageOpenGLKHR openxrImage = swapchainImageBuffer.get(i);
                this.leftFramebuffers[i] = new XrEyeTexture(
                        resolutionWidth, resolutionHeight,
                        openxrImage.image(),
                        0
                ).init();
                GLUtils.checkGLError("Left Eye " + i + " framebuffer setup");
                this.rightFramebuffers[i] = new XrEyeTexture(
                        resolutionWidth, resolutionHeight,
                        openxrImage.image(),
                        1
                ).init();
                GLUtils.checkGLError("Right Eye " + i + " framebuffer setup");

            }
        }

    }


    @Override
    public Matrix4f getProjectionMatrix(EyeType eyeType, float nearClip, float farClip) {
        XrFovf fov = vrProvider.getInputHandler()
                .getDevice(VRDeviceHMD.ID, OpenXRDeviceHMD.class)
                .getXrView(eyeType).fov();

        return  new Matrix4f()
                .setPerspectiveOffCenterFov(
                        fov.angleLeft(),
                        fov.angleRight(),
                        fov.angleDown(),
                        fov.angleUp(),
                        nearClip,
                        farClip
                );
    }

    @Override
    protected void setupResolution(MemoryStack stack) {
        resolutionWidth = vrProvider.getState().getEyeTexWidth();
        resolutionHeight = vrProvider.getState().getEyeTexHeight();
    }

    @Override
    public XrEyeTexture getTextureLeftEye() {
        if(leftFramebuffers==null){
            return null;
        }
        return leftFramebuffers[swapIndex];
    }

    @Override
    public XrEyeTexture getTextureRightEye() {
        if(rightFramebuffers==null){
            return null;
        }
        return rightFramebuffers[swapIndex];
    }


    @Override
    protected void setupHiddenArea(MemoryStack stack) {
        XrSession xrSession = getVrProvider().getState().getVrSession().getHandle();
        for (int eye = 0; eye < 2; ++eye) {
            // 1) Allocate the mask struct
            XrVisibilityMaskKHR mask = XrVisibilityMaskKHR
                    .calloc(stack)
                    .type(KHRVisibilityMask.XR_TYPE_VISIBILITY_MASK_KHR)
                    .next(0);

            // 2) First call: get counts
            getVrProvider().checkXRError(
                    KHRVisibilityMask.xrGetVisibilityMaskKHR(
                            xrSession,
                            XR10.XR_VIEW_CONFIGURATION_TYPE_PRIMARY_STEREO,
                            eye,
                            KHRVisibilityMask.XR_VISIBILITY_MASK_TYPE_HIDDEN_TRIANGLE_MESH_KHR,
                            mask
                    ),
                    "xrGetVisibilityMaskKHR",
                    "query counts"
            );
            int vertCount  = mask.vertexCountOutput();
            int indexCount = mask.indexCountOutput();

            if (indexCount <= 0) {
                getVrProvider().getLogger().logInfo("No hidden-area mesh found for eye " + eye);
                continue;
            }

            // 3) Allocate buffers for the data
            XrVector2f.Buffer verts  = XrVector2f.calloc(vertCount, stack);
            IntBuffer idxBuf = stack.mallocInt(indexCount);

            mask
                    .vertexCapacityInput(vertCount)
                    .indexCapacityInput(indexCount)
                    .vertices(verts)
                    .indices(idxBuf);

            // 4) Second call: actually fill verts & indices
            getVrProvider().checkXRError(
                    KHRVisibilityMask.xrGetVisibilityMaskKHR(
                            xrSession,
                            XR10.XR_VIEW_CONFIGURATION_TYPE_PRIMARY_STEREO,
                            eye,
                            KHRVisibilityMask.XR_VISIBILITY_MASK_TYPE_HIDDEN_TRIANGLE_MESH_KHR,
                            mask
                    ),
                    "xrGetVisibilityMaskKHR",
                    "retrieve mesh"
            );

            // 5) Flatten into your float[] format (tri-list: x,y,x,y,…)
            float[] area = new float[indexCount * 2];
            for (int i = 0; i < indexCount; i++) {
                XrVector2f v = verts.get(idxBuf.get(i));
                // If your runtime gives coords in [-1..1], map them to [0..1]:
                float ux = (v.x() * 0.5f) + 0.5f;
                float uy = (v.y() * 0.5f) + 0.5f;
                // then to pixels:
                area[i*2    ] = ux * getResolutionWidth();
                area[i*2 + 1] = uy * getResolutionHeight();
            }

            hiddenArea.put(EyeType.asIndex(eye), area);
            System.out.println("Hidden-area mesh loaded for eye " + eye);
        }
    }

}
