package me.phoenixra.atumvr.core.rendering;

import lombok.Getter;
import me.phoenixra.atumvr.api.enums.EyeType;
import me.phoenixra.atumvr.api.input.device.VRDeviceHMD;
import me.phoenixra.atumvr.api.rendering.IRenderContext;
import me.phoenixra.atumvr.api.rendering.VRRenderer;
import me.phoenixra.atumvr.api.rendering.VRTexture;
import me.phoenixra.atumvr.api.utils.GLUtils;
import me.phoenixra.atumvr.core.OpenXRProvider;
import me.phoenixra.atumvr.core.input.device.OpenXRDeviceHMD;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL30;
import org.lwjgl.openxr.*;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;
import java.util.HashMap;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;

public abstract class OpenXRRenderer implements VRRenderer {
    @Getter
    protected OpenXRProvider vrProvider;


    @Getter
    protected int resolutionWidth;
    @Getter
    protected int resolutionHeight;

    @Getter
    protected long windowHandle;
    protected final HashMap<EyeType, float[]> hiddenArea = new HashMap<>();



    protected int swapIndex;

    protected OpenXRTexture[] leftFramebuffers;
    protected OpenXRTexture[] rightFramebuffers;

    protected XrCompositionLayerProjectionView.Buffer projectionLayerViews;


    private boolean createdGLContext;
    public OpenXRRenderer(OpenXRProvider vrProvider) {
        this.vrProvider = vrProvider;

    }

    public abstract void onInit() throws Throwable;
    protected abstract OpenXRTexture createTexture(int width, int height, int textureId, int index);

    @Override
    public void init() throws Throwable{
        setupResolution();
        setupEyes();
        setupHiddenArea();
        onInit();
    }

    @Override
    public void preRender(@NotNull IRenderContext context) {
        prepareXrFrame();
    }

    @Override
    public void renderFrame(@NotNull IRenderContext context) {


        if(createdGLContext) {
            GL30.glViewport(0, 0, resolutionWidth, resolutionHeight);
            GL30.glEnable(GL30.GL_DEPTH_TEST);
        }

        getCurrentScene().render(context);

        finishXrFrame();

        if(createdGLContext) {
            GL30.glFlush();
            GL30.glFinish();
        }
    }



    protected void prepareXrFrame(){
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

    protected void finishXrFrame(){
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



    public void setupGLContext() {
        createdGLContext = true;
        GLFWErrorCallback.createPrint(System.out).set();

        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }


        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_DEPTH_BITS, 24);
        glfwWindowHint(GLFW_STENCIL_BITS, 8);

        windowHandle = glfwCreateWindow(640, 480, vrProvider.getAppName(), 0L, 0L);
        if (windowHandle == 0L) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        glfwMakeContextCurrent(windowHandle);
        glfwSwapInterval(1);

        GL.createCapabilities();
        GL30.glEnable(GL30.GL_DEPTH_TEST);

        //texture staff, better be moved to a renderers of objects
        GL30.glEnable(GL30.GL_CULL_FACE);
        GL30.glCullFace(GL30.GL_BACK);

    }

    protected void setupResolution() {

        resolutionWidth = vrProvider.getState().getEyeTexWidth();
        resolutionHeight = vrProvider.getState().getEyeTexHeight();
    }


    protected void setupEyes() {

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

            this.leftFramebuffers = new OpenXRTexture[imageCount];
            this.rightFramebuffers = new OpenXRTexture[imageCount];

            for (int i = 0; i < imageCount; i++) {
                XrSwapchainImageOpenGLKHR openxrImage = swapchainImageBuffer.get(i);
                this.leftFramebuffers[i] = createTexture(
                        resolutionWidth, resolutionHeight,
                        openxrImage.image(),
                        0
                ).init();
                GLUtils.checkGLError("Left Eye " + i + " framebuffer setup");
                this.rightFramebuffers[i] = createTexture(
                        resolutionWidth, resolutionHeight,
                        openxrImage.image(),
                        1
                ).init();
                GLUtils.checkGLError("Right Eye " + i + " framebuffer setup");

            }
        }

    }



    protected void setupHiddenArea(){
        try(MemoryStack stack = MemoryStack.stackPush()) {
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
                IntBuffer          idxBuf = stack.mallocInt(indexCount);

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

    @Override
    public VRTexture getTextureLeftEye() {
        if(leftFramebuffers==null){
            return null;
        }
        return leftFramebuffers[swapIndex];
    }

    @Override
    public VRTexture getTextureRightEye() {
        if(rightFramebuffers==null){
            return null;
        }
        return rightFramebuffers[swapIndex];
    }

    @Override
    public float[] getHiddenAreaVertices(EyeType eyeType) {
        return hiddenArea.get(eyeType);
    }


    public void reinitBuffers(){

    }

    @Override
    public void destroy() {
        getCurrentScene().destroy();
        if(createdGLContext) {
            glfwFreeCallbacks(windowHandle);
            glfwDestroyWindow(windowHandle);

            glfwTerminate();
        }
    }
}
