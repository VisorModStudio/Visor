package me.phoenixra.atumvr.core.rendering;

import lombok.Getter;
import me.phoenixra.atumvr.api.enums.EyeType;
import me.phoenixra.atumvr.api.rendering.IRenderContext;
import me.phoenixra.atumvr.api.rendering.VRScene;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;

public abstract class OpenXRScene implements VRScene {

    @Getter
    private OpenXRRenderer vrRenderer;



    @Getter
    protected OpenXREyeCamera rightEyeCamera;
    @Getter
    protected OpenXREyeCamera leftEyeCamera;

    public OpenXRScene(OpenXRRenderer vrRenderer) {
        this.vrRenderer = vrRenderer;

    }

    public abstract void updateEyeTexture(@NotNull EyeType eyeType);

    public abstract void onInit();

    @Override
    public void init() {
        leftEyeCamera = new OpenXREyeCamera(
                vrRenderer.getVrProvider()
        );
        rightEyeCamera = new OpenXREyeCamera(
                vrRenderer.getVrProvider()
        );
        setupMvp();
        onInit();
    }

    @Override
    public void render(@NotNull IRenderContext context) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            //stack not used here but necessary for some OpenGL operations,
            // so its pushed then auto-popped together with OpenGL staff
            // that might be there during rendering

            setupMvp();

            for (EyeType eyeType : EyeType.values()) {

                int fbo = (eyeType == EyeType.LEFT)
                        ? vrRenderer.getTextureLeftEye().getFrameBufferId()
                        : vrRenderer.getTextureRightEye().getFrameBufferId();
                GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo);


                GL30.glClearColor(0, 0, 0, 1);
                GL30.glClear(
                        GL30.GL_COLOR_BUFFER_BIT |
                                GL30.GL_DEPTH_BUFFER_BIT |
                                GL30.GL_STENCIL_BUFFER_BIT
                );

                updateEyeTexture(eyeType);
            }

            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        }
    }





    protected void setupMvp() {
        leftEyeCamera.updateProjectionMatrix(EyeType.LEFT,
                0.02f,100f

        );
        leftEyeCamera.updateViewMatrix(EyeType.LEFT);

        rightEyeCamera.updateProjectionMatrix(EyeType.RIGHT,
                0.02f,100f
        );
        rightEyeCamera.updateViewMatrix(EyeType.RIGHT);
    }


}
