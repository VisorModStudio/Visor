package org.vmstudio.visor.core.client.provider.openxr.render;

import lombok.Getter;
import me.phoenixra.atumvr.api.enums.EyeType;
import me.phoenixra.atumvr.api.input.device.AtumVRDeviceHMD;

import me.phoenixra.atumvr.api.rendering.AtumVRRenderContext;
import me.phoenixra.atumvr.api.utils.GLUtils;
import me.phoenixra.atumvr.core.enums.XRGraphicsApi;
import me.phoenixra.atumvr.core.session.vulkan.XRVulkanBridge;
import me.phoenixra.atumvr.core.utils.XRUtils;
import me.phoenixra.atumvr.core.input.device.XRDeviceHMD;
import org.vmstudio.visor.core.client.provider.VisorScene;
import org.vmstudio.visor.core.client.provider.openxr.XrProvider;
import org.vmstudio.visor.core.client.render.VRRendererBase;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.openxr.*;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;

public class XrRenderer extends VRRendererBase {
    @Getter
    private final XrProvider vrProvider;

    protected final int[] swapIndices = new int[2];

    protected XrEyeTexture[] leftFramebuffers;
    protected XrEyeTexture[] rightFramebuffers;

    protected XrCompositionLayerProjectionView.Buffer projectionLayerViews;

    protected XrPosef.Buffer bridgePrevPoses;
    protected XrFovf.Buffer bridgePrevFovs;

    boolean frameStarted;

    @Getter
    private final VisorScene currentScene;


    protected boolean frameShouldRender;

    /** SteamVR + Linux workaround on GL issue */
    private boolean steamVRLinuxWorkaround;
    private int lastSceneGLError;

    public XrRenderer(XrProvider provider) {
        vrProvider = provider;
        currentScene = new VisorScene(this);
    }

    @Override
    public void init() throws Throwable {
        steamVRLinuxWorkaround = XRUtils.detectSteamVRLinux(vrProvider);

        super.init();
    }

    @Override
    public void prepareFrame() {
        if(frameStarted) return;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            XrFrameState frameState = XrFrameState.calloc(stack).type(XR10.XR_TYPE_FRAME_STATE);

            vrProvider.checkXRError(
                    XR10.xrWaitFrame(
                            vrProvider.getSession().getHandle(),
                            XrFrameWaitInfo.calloc(stack)
                                    .type(XR10.XR_TYPE_FRAME_WAIT_INFO),
                            frameState
                    ),
                    "xrWaitFrame", ""
            );

            vrProvider.setXrDisplayTime(frameState.predictedDisplayTime());

            vrProvider.checkXRError(
                    XR10.xrBeginFrame(
                            vrProvider.getSession().getHandle(),
                            XrFrameBeginInfo.calloc(stack)
                                    .type(XR10.XR_TYPE_FRAME_BEGIN_INFO)
                    ),
                    "xrBeginFrame", ""
            );


            frameShouldRender = frameState.shouldRender();

            if (frameShouldRender) {
                XrViewState viewState = XrViewState.calloc(stack).type(XR10.XR_TYPE_VIEW_STATE);
                IntBuffer intBuf = stack.callocInt(1);

                XrViewLocateInfo viewLocateInfo = XrViewLocateInfo.calloc(stack);
                viewLocateInfo.set(
                        XR10.XR_TYPE_VIEW_LOCATE_INFO,
                        0,
                        XR10.XR_VIEW_CONFIGURATION_TYPE_PRIMARY_STEREO,
                        frameState.predictedDisplayTime(),
                        vrProvider.getSession().getXrAppSpace()
                );

                vrProvider.checkXRError(
                        XR10.xrLocateViews(
                                vrProvider.getSession().getHandle(),
                                viewLocateInfo, viewState,
                                intBuf, vrProvider.getSession().getSwapChain().getXrViewBuffer()
                        ),
                        "xrLocateViews", ""
                );

                long requiredView = XR10.XR_VIEW_STATE_ORIENTATION_VALID_BIT
                        | XR10.XR_VIEW_STATE_POSITION_VALID_BIT;
                if ((viewState.viewStateFlags() & requiredView) != requiredView) {
                    frameShouldRender = false;
                }
            }

            if (steamVRLinuxWorkaround) {
                restoreGLContext();
                GLUtils.drainGLErrors();
            }
        }
        frameStarted = true;
    }

    @Override
    public void renderFrame(@NotNull AtumVRRenderContext context) {
        if(!frameStarted) return;


        try {
            if (frameShouldRender) {
                prepareSwapChains();

                if (isVulkanBridge()) {
                    vrProvider.getSession().getVulkanBridge().beginFrameGL();
                }
                getCurrentScene().render(context);
            }
        }finally {
            frameStarted = false;
            finishFrame();
        }




    }
    private void prepareSwapChains(){
        this.projectionLayerViews = XrCompositionLayerProjectionView.calloc(2);
        try (MemoryStack stack = MemoryStack.stackPush()) {

            IntBuffer intBuf2 = stack.callocInt(1);

            boolean bridge = isVulkanBridge();
            boolean delayedContent = bridge && vrProvider.getSession()
                    .getVulkanBridge().isContentOneFrameDelayed();
            if (bridge && bridgePrevPoses == null) {
                bridgePrevPoses = XrPosef.calloc(2);
                bridgePrevFovs = XrFovf.calloc(2);
            }

            for (EyeType eyeType : EyeType.values()) {
                int index = eyeType.getIndex();
                XrSwapchain xrSwapchain = vrProvider.getSession()
                        .getSwapChain().getHandle(index);

                vrProvider.checkXRError(
                        XR10.xrAcquireSwapchainImage(
                                xrSwapchain,
                                XrSwapchainImageAcquireInfo
                                        .calloc(stack)
                                        .type(XR10.XR_TYPE_SWAPCHAIN_IMAGE_ACQUIRE_INFO),
                                intBuf2
                        ),
                        "xrAcquireSwapchainImage", eyeType.name()
                );

                vrProvider.checkXRError(
                        XR10.xrWaitSwapchainImage(xrSwapchain,
                                XrSwapchainImageWaitInfo.calloc(stack)
                                        .type(XR10.XR_TYPE_SWAPCHAIN_IMAGE_WAIT_INFO)
                                        .timeout(XR10.XR_INFINITE_DURATION)
                        ),
                        "xrWaitSwapchainImage", eyeType.name()
                );

                this.swapIndices[index] = intBuf2.get(0);

                XrView xrView = vrProvider.getInputHandler()
                        .getDevice(AtumVRDeviceHMD.ID, XRDeviceHMD.class)
                        .getXrView(eyeType);
                var projectionView = this.projectionLayerViews.get(index)
                        .type(XR10.XR_TYPE_COMPOSITION_LAYER_PROJECTION_VIEW);
                if (delayedContent) {
                    projectionView.pose(bridgePrevPoses.get(index))
                            .fov(bridgePrevFovs.get(index));
                } else {
                    projectionView.pose(xrView.pose())
                            .fov(xrView.fov());
                }
                if (bridge) {
                    bridgePrevPoses.get(index).set(xrView.pose());
                    bridgePrevFovs.get(index).set(xrView.fov());
                }
                XrSwapchainSubImage subImage = projectionView.subImage();
                subImage.swapchain(xrSwapchain);
                subImage.imageRect().offset().set(0, 0);
                subImage.imageRect().extent().set(resolutionWidth, resolutionHeight);
                subImage.imageArrayIndex(0);
            }

        }
    }

    public void finishFrame(){
        if (steamVRLinuxWorkaround) {
            int sceneErr = GLUtils.drainGLErrors();
            if (sceneErr != lastSceneGLError) {
                lastSceneGLError = sceneErr;
                if (sceneErr != 0) {
                    vrProvider.getLogger().logError(
                            "OpenGL error generated by application/scene rendering: "
                                    + sceneErr + " (0x" + Integer.toHexString(sceneErr)
                                    + ") - this is an app-side bug, not the"
                                    + " SteamVR/Linux interop artifact"
                    );
                }
            }
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            XrFrameEndInfo frameEndInfo = XrFrameEndInfo.calloc(stack)
                    .type(XR10.XR_TYPE_FRAME_END_INFO)
                    .displayTime(vrProvider.getXrDisplayTime())
                    .environmentBlendMode(XR10.XR_ENVIRONMENT_BLEND_MODE_OPAQUE);

            if (frameShouldRender) {
                if (isVulkanBridge()) {
                    //must be submitted before the images are released
                    vrProvider.getSession().getVulkanBridge().transferFrame(
                            swapIndices[EyeType.LEFT.getIndex()],
                            swapIndices[EyeType.RIGHT.getIndex()]
                    );
                }
                for (EyeType eyeType : EyeType.values()) {
                    vrProvider.checkXRError(
                            XR10.xrReleaseSwapchainImage(
                                    vrProvider.getSession().getSwapChain()
                                            .getHandle(eyeType.getIndex()),
                                    XrSwapchainImageReleaseInfo.calloc(stack)
                                            .type(XR10.XR_TYPE_SWAPCHAIN_IMAGE_RELEASE_INFO)),
                            "xrReleaseSwapchainImage", eyeType.name()
                    );
                }

                XrCompositionLayerProjection compositionLayerProjection = XrCompositionLayerProjection.calloc(stack)
                        .type(XR10.XR_TYPE_COMPOSITION_LAYER_PROJECTION)
                        .space(vrProvider.getSession().getXrAppSpace())
                        .views(this.projectionLayerViews);

                PointerBuffer layers = stack.callocPointer(1);
                layers.put(compositionLayerProjection);
                layers.flip();

                frameEndInfo.layers(layers);
            }

            vrProvider.checkXRError(
                    XR10.xrEndFrame(vrProvider.getSession().getHandle(), frameEndInfo),
                    "xrEndFrame", ""
            );

            if (frameShouldRender) {
                this.projectionLayerViews.close();
            }
        }

        if (steamVRLinuxWorkaround) {
            restoreGLContext();
            GLUtils.drainGLErrors();
        }
    }



    @Override
    public Matrix4f getProjectionMatrix(EyeType eyeType, float nearClip, float farClip) {
        XrFovf fov = vrProvider.getInputHandler()
                .getDevice(AtumVRDeviceHMD.ID, XRDeviceHMD.class)
                .getXrView(eyeType).fov();

        return new Matrix4f()
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
        resolutionWidth = vrProvider.getSession().getSwapChain().getEyeWidth();
        resolutionHeight = vrProvider.getSession().getSwapChain().getEyeHeight();
    }


    @Override
    protected void setupEyes() {
        if (isVulkanBridge()) {
            setupEyesVulkanBridge();
            return;
        }
        try (MemoryStack stack = MemoryStack.stackPush()) {
            for (EyeType eyeType : EyeType.values()) {
                int eyeIndex = eyeType.getIndex();
                XrSwapchain xrSwapchain = vrProvider.getSession()
                        .getSwapChain().getHandle(eyeIndex);

                IntBuffer intBuffer = stack.ints(0); //Set value to 0
                int error = XR10.xrEnumerateSwapchainImages(xrSwapchain, intBuffer, null);
                vrProvider.checkXRError(error, "xrEnumerateSwapchainImages", "get count");

                int imageCount = intBuffer.get(0);
                XrSwapchainImageOpenGLKHR.Buffer swapchainImageBuffer = vrProvider
                        .getSession().getSwapChain().createImageBuffers(imageCount,
                                stack);

                error = XR10.xrEnumerateSwapchainImages(xrSwapchain, intBuffer,
                        XrSwapchainImageBaseHeader.create(swapchainImageBuffer.address(), swapchainImageBuffer.capacity()));
                vrProvider.checkXRError(error, "xrEnumerateSwapchainImages", "get images");

                XrEyeTexture[] framebuffers = new XrEyeTexture[imageCount];
                for (int i = 0; i < imageCount; i++) {
                    XrSwapchainImageOpenGLKHR openxrImage = swapchainImageBuffer.get(i);
                    framebuffers[i] = new XrEyeTexture(
                            resolutionWidth, resolutionHeight,
                            openxrImage.image(),
                            eyeIndex
                    ).init();
                    GLUtils.checkGLError(eyeType.name() + " " + i + " framebuffer setup");
                }
                if (eyeType == EyeType.LEFT) {
                    this.leftFramebuffers = framebuffers;
                } else {
                    this.rightFramebuffers = framebuffers;
                }
            }
        }

    }

    protected void setupEyesVulkanBridge() {
        XRVulkanBridge bridge = vrProvider.getSession().getVulkanBridge();
        try (MemoryStack stack = MemoryStack.stackPush()) {
            for (EyeType eyeType : EyeType.values()) {
                int eyeIndex = eyeType.getIndex();
                XrSwapchain xrSwapchain = vrProvider.getSession()
                        .getSwapChain().getHandle(eyeIndex);

                IntBuffer intBuffer = stack.ints(0);
                int error = XR10.xrEnumerateSwapchainImages(xrSwapchain, intBuffer, null);
                vrProvider.checkXRError(error, "xrEnumerateSwapchainImages", "get count");

                int imageCount = intBuffer.get(0);
                XrSwapchainImageVulkanKHR.Buffer swapchainImageBuffer =
                        bridge.createImageBuffers(imageCount, stack);
                error = XR10.xrEnumerateSwapchainImages(xrSwapchain, intBuffer,
                        XrSwapchainImageBaseHeader.create(swapchainImageBuffer.address(), swapchainImageBuffer.capacity()));
                vrProvider.checkXRError(error, "xrEnumerateSwapchainImages", "get images");

                int textureId = bridge.setupEye(
                        eyeIndex, swapchainImageBuffer,
                        resolutionWidth, resolutionHeight
                );
                XrEyeTexture texture = new XrEyeTexture(
                        resolutionWidth, resolutionHeight,
                        textureId,
                        eyeIndex
                ).init();
                GLUtils.checkGLError(eyeType.name() + " bridge framebuffer setup");

                if (eyeType == EyeType.LEFT) {
                    this.leftFramebuffers = new XrEyeTexture[]{texture};
                } else {
                    this.rightFramebuffers = new XrEyeTexture[]{texture};
                }
            }
        }
    }

    private boolean isVulkanBridge() {
        return vrProvider.getSession().getGraphicsApi() == XRGraphicsApi.VULKAN;
    }

    @Override
    protected void setupHiddenArea(MemoryStack stack) {
        if (!getVrProvider().getSession().getInstance()
                .getHandle().getCapabilities().XR_KHR_visibility_mask) {
            getVrProvider().getLogger().logInfo(
                    "XR_KHR_visibility_mask not supported by runtime, skipping hidden-area mesh"
            );
            return;
        }
        try {
            loadHiddenAreaMesh(stack);
        } catch (Throwable e) {
            hiddenArea.clear();
            getVrProvider().getLogger().logError(
                    "Failed to load hidden-area mesh, continuing without it: "
                            + e.getClass().getSimpleName() + ": " + e.getMessage()
            );
        }
    }

    private void loadHiddenAreaMesh(MemoryStack stack) {
        XrSession xrSession = getVrProvider().getSession().getHandle();
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

            hiddenArea.put(EyeType.fromIndex(eye), area);
            System.out.println("Hidden-area mesh loaded for eye " + eye);
        }
    }


    @Override
    public XrEyeTexture getTextureLeftEye() {
        if(leftFramebuffers==null){
            return null;
        }
        //the bridge has a single intermediate target per eye
        return leftFramebuffers[isVulkanBridge() ? 0 : swapIndices[EyeType.LEFT.getIndex()]];
    }

    @Override
    public XrEyeTexture getTextureRightEye() {
        if(rightFramebuffers==null){
            return null;
        }
        return rightFramebuffers[isVulkanBridge() ? 0 : swapIndices[EyeType.RIGHT.getIndex()]];
    }

    protected void restoreGLContext() {
        if (steamVRLinuxWorkaround) {
            glfwMakeContextCurrent(getWindowHandle());
        }
    }
}
