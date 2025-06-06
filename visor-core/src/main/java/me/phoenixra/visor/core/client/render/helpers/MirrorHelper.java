package me.phoenixra.visor.core.client.render.helpers;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import me.phoenixra.visor.core.client.mcmodified.WindowModified;
import me.phoenixra.visor.core.client.settings.VRClientSettings;
import me.phoenixra.visor.core.client.utils.ClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Matrix4f;

import java.util.List;

import me.phoenixra.visor.core.client.ClientContext;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL30C;

import static com.mojang.blaze3d.platform.GlStateManager._glBindFramebuffer;
import static com.mojang.blaze3d.platform.GlStateManager._glBlitFrameBuffer;
import static me.phoenixra.visor.core.client.VisorClientImpl.MC;

public class MirrorHelper {

    public static void drawMirror() {
        switch (VRClientSettings.getDisplayMirrorMode()){
            case DISABLED -> drawTextMirror("Mirror is DISABLED", true);
            case GUI -> drawGuiMirror();
            case SINGLE -> drawSingleMirror();
            case CROPPED -> drawCroppedMirror();
            case DUAL -> drawDualMirror();
            case FIRST_PERSON -> drawFirstPersonMirror();
            case THIRD_PERSON -> drawThirdPersonMirror();
        }
    }


    private static void drawGuiMirror(){
        RenderTarget source = ClientContext.renderer.guiTarget.getTarget();

        int screenWidth = ((WindowModified) (Object) MC.getWindow()).visor$getScreenWidth();
        int screenHeight = ((WindowModified) (Object) MC.getWindow()).visor$getScreenHeight();
        blit(
                source,
                0,0,
                screenWidth,
                screenHeight
        );
    }
    private static void drawSingleMirror(){
        RenderTarget source;
        if (VRClientSettings.isDisplayMirrorLeftEye()) {
            source = ClientContext.renderer.getTextureLeftEye().getRenderTarget();
        }else {
            source = ClientContext.renderer.getTextureRightEye().getRenderTarget();
        }

        int screenWidth = ((WindowModified) (Object) MC.getWindow()).visor$getScreenWidth();
        int screenHeight = ((WindowModified) (Object) MC.getWindow()).visor$getScreenHeight();
        blit(
                source,
                0,0,
                screenWidth,
                screenHeight
        );
    }

    private static void drawCroppedMirror(){
        RenderTarget source;
        if (VRClientSettings.isDisplayMirrorLeftEye()) {
            source = ClientContext.renderer.getTextureLeftEye().getRenderTarget();
        }else {
            source = ClientContext.renderer.getTextureRightEye().getRenderTarget();
        }

        float xCrop = VRClientSettings.getMirrorCrop();
        float yCrop = VRClientSettings.getMirrorCrop();

        int screenWidth = ((WindowModified) (Object) MC.getWindow()).visor$getScreenWidth();
        int screenHeight = ((WindowModified) (Object) MC.getWindow()).visor$getScreenHeight();

        blitCropped(
                source,
                0,0,
                screenWidth, screenHeight,
                xCrop, yCrop,
                true
        );
    }



    private static void drawFirstPersonMirror(){
        RenderTarget source = ClientContext.renderer.firstPersonTarget.getTarget();

        int screenWidth = ((WindowModified) (Object) MC.getWindow()).visor$getScreenWidth();
        int screenHeight = ((WindowModified) (Object) MC.getWindow()).visor$getScreenHeight();
        blit(
                source,
                0,0,
                screenWidth, screenHeight
        );
    }
    private static void drawThirdPersonMirror(){
        RenderTarget source = ClientContext.renderer.thirdPersonTarget.getTarget();

        int screenWidth = ((WindowModified) (Object) MC.getWindow()).visor$getScreenWidth();
        int screenHeight = ((WindowModified) (Object) MC.getWindow()).visor$getScreenHeight();
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

        int screenWidth = ((WindowModified) (Object) MC.getWindow()).visor$getScreenWidth() / 2;
        int screenHeight = ((WindowModified) (Object) MC.getWindow()).visor$getScreenHeight();

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
        var window  = (WindowModified)(Object)MC.getWindow();
        int vrWidth = window.visor$getScreenWidth();
        int vrHeight= window.visor$getScreenHeight();

        // 2) viewport + projection
        RenderSystem.viewport(0, 0, vrWidth, vrHeight);
        var proj = new Matrix4f().setOrtho(0, vrWidth, vrHeight, 0, NEAR_PLANE, FAR_PLANE);
        RenderSystem.setProjectionMatrix(proj, VertexSorting.ORTHOGRAPHIC_Z);

        // 3) push / configure model-view
        var mv = RenderSystem.getModelViewStack();
        mv.pushPose();
        try {
            mv.setIdentity();
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
            mv.popPose();
            RenderSystem.applyModelViewMatrix();
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
    }

    public static void blitCropped(RenderTarget source,
                                   int left, int top,
                                   int right, int bottom,
                                   float xCropFactor, float yCropFactor,
                                   boolean keepAspect) {
        if (keepAspect) {
            float drawAspect = (float) MC.mainRenderTarget.width / (float) MC.mainRenderTarget.height;
            float bufferAspect = (float) source.viewWidth / (float) source.viewHeight;
            if (drawAspect > bufferAspect) {

                float heightAspect = (bufferAspect / drawAspect) * (0.5F - yCropFactor);

                yCropFactor = 0.5F - heightAspect;
            } else {

                float widthAspect = (drawAspect / bufferAspect) * (0.5F - xCropFactor);

                xCropFactor = 0.5F - widthAspect;
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
    }






}
