package org.vmstudio.visor.core.client.render.helpers;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import me.phoenixra.atumvr.api.enums.EyeType;
import org.vmstudio.visor.extensions.client.WindowExtension;
import org.vmstudio.visor.core.client.render.VRShaders;
import org.vmstudio.visor.api.client.settings.VRClientSettings;
import org.vmstudio.visor.core.client.utils.ClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Matrix4f;

import java.util.List;

import org.vmstudio.visor.core.client.ClientContext;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL30C;

import static com.mojang.blaze3d.platform.GlStateManager._glBindFramebuffer;
import static com.mojang.blaze3d.platform.GlStateManager._glBlitFrameBuffer;
import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

public class MirrorHelper {
    private MirrorHelper() {
        throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
    }


    public static void drawMirror() {
        switch (VRClientSettings.getMirrorMode()){
            case OFF -> drawTextMirror("Mirror is OFF", true);
            case GUI -> drawGuiMirror();
            case CROPPED -> drawCroppedMirror();
            case SINGLE -> drawSingleMirror();
            case DUAL -> drawDualMirror();
            case FIRST_PERSON -> drawFirstPersonMirror();
            case THIRD_PERSON -> drawThirdPersonMirror();
            case MIXED_REALITY -> VRShaders.getMixedReality().drawMirror();
        }
    }


    private static void drawGuiMirror(){
        RenderTarget source = ClientContext.renderer.guiTarget.getTarget();

        int screenWidth = ((WindowExtension) (Object) MC.getWindow()).visor$getActualScreenWidth();
        int screenHeight = ((WindowExtension) (Object) MC.getWindow()).visor$getActualScreenHeight();
        blit(
                source,
                0,0,
                screenWidth,
                screenHeight
        );
    }

    private static void drawCroppedMirror(){
        RenderTarget source;
        if (VRClientSettings.getMirrorEye() == EyeType.LEFT) {
            source = ClientContext.renderer.getTextureLeftEye().getRenderTarget();
        }else {
            source = ClientContext.renderer.getTextureRightEye().getRenderTarget();
        }

        float xCrop = VRClientSettings.getMirrorCrop();
        float yCrop = VRClientSettings.getMirrorCrop();

        int screenWidth = ((WindowExtension) (Object) MC.getWindow()).visor$getActualScreenWidth();
        int screenHeight = ((WindowExtension) (Object) MC.getWindow()).visor$getActualScreenHeight();

        blitCropped(
                source,
                0,0,
                screenWidth, screenHeight,
                xCrop, yCrop,
                true
        );
    }
    private static void drawSingleMirror(){
        RenderTarget source;
        if (VRClientSettings.getMirrorEye() == EyeType.LEFT) {
            source = ClientContext.renderer.getTextureLeftEye().getRenderTarget();
        }else {
            source = ClientContext.renderer.getTextureRightEye().getRenderTarget();
        }

        int screenWidth = ((WindowExtension) (Object) MC.getWindow()).visor$getActualScreenWidth();
        int screenHeight = ((WindowExtension) (Object) MC.getWindow()).visor$getActualScreenHeight();
        blit(
                source,
                0,0,
                screenWidth,
                screenHeight
        );
    }




    private static void drawFirstPersonMirror(){
        RenderTarget source = ClientContext.renderer.firstPersonTarget.getTarget();

        int screenWidth = ((WindowExtension) (Object) MC.getWindow()).visor$getActualScreenWidth();
        int screenHeight = ((WindowExtension) (Object) MC.getWindow()).visor$getActualScreenHeight();
        blit(
                source,
                0,0,
                screenWidth, screenHeight
        );
    }
    private static void drawThirdPersonMirror(){
        RenderTarget source = ClientContext.renderer.thirdPersonTarget.getTarget();

        int screenWidth = ((WindowExtension) (Object) MC.getWindow()).visor$getActualScreenWidth();
        int screenHeight = ((WindowExtension) (Object) MC.getWindow()).visor$getActualScreenHeight();
        blit(
                source,
                0,0,
                screenWidth, screenHeight
        );
    }
    private static void drawDualMirror(){
        RenderTarget leftEye = ClientContext.renderer
                .getTextureLeftEye().getRenderTarget();
        RenderTarget rightEye = ClientContext.renderer
                .getTextureRightEye().getRenderTarget();

        int screenWidth = ((WindowExtension) (Object) MC.getWindow()).visor$getActualScreenWidth() / 2;
        int screenHeight = ((WindowExtension) (Object) MC.getWindow()).visor$getActualScreenHeight();

        blit(
                leftEye,
                0,0,
                screenWidth, screenHeight
        );

        blit(
                rightEye,
                screenWidth,0,
                MC.mainRenderTarget.width, screenHeight
        );

    }



    private static void drawTextMirror(String text, boolean clearBackground) {
        final int CLEAR_DEPTH_FLAG = 256;
        final int CLEAR_COLOR_FLAG = 16384;
        final int TEXT_COLOR       = 0xFFFFFF;
        final int CHAR_WIDTH       = 22;
        final int LINE_HEIGHT      = 5;
        final int TEXT_X_OFFSET    = 1;
        final float NEAR_PLANE     = 1000f;
        final float FAR_PLANE      = 3000f;
        final float CAMERA_Z       = 2000f;
        final float TEXT_SCALE     = 2f;

        // 1) get the VR mirror dimensions
        var window  = (WindowExtension)(Object)MC.getWindow();
        int vrWidth = window.visor$getActualScreenWidth();
        int vrHeight= window.visor$getActualScreenHeight();

        // 2) viewport + projection
        RenderSystem.backupProjectionMatrix();
        RenderSystem.viewport(0, 0, vrWidth, vrHeight);
        var proj = new Matrix4f().setOrtho(0, vrWidth, vrHeight, 0, NEAR_PLANE, FAR_PLANE);
        RenderSystem.setProjectionMatrix(proj, VertexSorting.ORTHOGRAPHIC_Z);

        // 3) push / configure model-view
        var mv = RenderSystem.getModelViewStack();
        mv.pushMatrix();
        try {
            mv.identity();
            mv.translate(0, 0, -CAMERA_Z);
            RenderSystem.applyModelViewMatrix();

            // 4) disable fog + clear
            RenderSystem.setShaderFogStart(Float.MAX_VALUE);
            int flags = CLEAR_DEPTH_FLAG | (clearBackground ? CLEAR_COLOR_FLAG : 0);
            RenderSystem.clear(flags, Minecraft.ON_OSX);
            if (clearBackground) {
                RenderSystem.clearColor(0, 0, 0, 0);
            }

            // 5) prepare GuiGraphics with scaled text
            var gui = new GuiGraphics(MC, MC.renderBuffers().bufferSource());
            gui.pose().scale(TEXT_SCALE, TEXT_SCALE, TEXT_SCALE);

            // 6) wrap & draw text lines
            int wrapWidth = vrWidth / CHAR_WIDTH;
            var lines    = (text == null)
                    ? List.<String>of()
                    : ClientUtils.wrapText(text, wrapWidth);

            int y = LINE_HEIGHT;
            for (String line : lines) {
                gui.drawString(MC.font, line, TEXT_X_OFFSET, y, TEXT_COLOR);
                y += LINE_HEIGHT;
            }

            gui.flush();
        } finally {
            mv.popMatrix();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.restoreProjectionMatrix();
            RenderStateHelper.restoreAfterExternalRender();
        }
    }


    public static void blit(RenderTarget source,
                            int left, int top,
                            int right, int bottom) {
        _glBindFramebuffer(GL30C.GL_READ_FRAMEBUFFER, source.frameBufferId);
        _glBlitFrameBuffer(
                0, 0, source.width, source.height,
                left, top, right, bottom,
                GL11C.GL_COLOR_BUFFER_BIT, GL11C.GL_LINEAR);
        _glBindFramebuffer(GL30C.GL_READ_FRAMEBUFFER, 0);
        RenderStateHelper.restoreAfterExternalRender();
    }

    public static void blitCropped(RenderTarget source,
                                   int left, int top,
                                   int right, int bottom,
                                   float xCropFactor, float yCropFactor,
                                   boolean keepAspect) {
        if (keepAspect) {
            float targetAspect = (float) MC.mainRenderTarget.width / (float) MC.mainRenderTarget.height;
            float sourceAspect = (float) source.viewWidth / (float) source.viewHeight;
            if (targetAspect > sourceAspect) {
                yCropFactor = 0.5F
                        - (sourceAspect / targetAspect) * (0.5F - yCropFactor);
            } else {
                xCropFactor = 0.5F
                        - (targetAspect / sourceAspect) * (0.5F - xCropFactor);
            }
        }

        int xMin = (int) (xCropFactor * source.width);
        int yMin = (int) (yCropFactor * source.height);
        int xMax = source.width - xMin;
        int yMax = source.height - yMin;

        _glBindFramebuffer(GL30C.GL_READ_FRAMEBUFFER, source.frameBufferId);
        _glBlitFrameBuffer(
                xMin, yMin, xMax, yMax,
                left, top, right, bottom,
                GL11C.GL_COLOR_BUFFER_BIT, GL11C.GL_LINEAR);
        _glBindFramebuffer(GL30C.GL_READ_FRAMEBUFFER, 0);
        RenderStateHelper.restoreAfterExternalRender();
    }






}
