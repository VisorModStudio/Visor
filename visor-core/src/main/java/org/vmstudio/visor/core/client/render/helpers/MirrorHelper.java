package org.vmstudio.visor.core.client.render.helpers;

import org.vmstudio.visor.api.compatibility.mcversion.render.McGlState;
import org.vmstudio.visor.api.compatibility.mcversion.render.McProjection;
import org.vmstudio.visor.api.compatibility.mcversion.render.McFog;
import org.vmstudio.visor.api.compatibility.mcversion.render.McModelViewStack;
import org.vmstudio.visor.api.compatibility.mcversion.render.McRenderTarget;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import me.phoenixra.atumvr.api.enums.EyeType;
import org.vmstudio.visor.extensions.client.WindowExtension;
import org.vmstudio.visor.core.client.render.VRShaders;
import org.vmstudio.visor.api.client.settings.VRClientSettings;
import org.vmstudio.visor.core.client.utils.ClientUtils;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Matrix4f;

import java.util.List;

import org.vmstudio.visor.core.client.ClientContext;

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

        int screenWidth = ((WindowExtension) (Object) MC.getWindow()).visor$mcScreenWidth();
        int screenHeight = ((WindowExtension) (Object) MC.getWindow()).visor$mcScreenHeight();
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

        int screenWidth = ((WindowExtension) (Object) MC.getWindow()).visor$mcScreenWidth();
        int screenHeight = ((WindowExtension) (Object) MC.getWindow()).visor$mcScreenHeight();

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

        int screenWidth = ((WindowExtension) (Object) MC.getWindow()).visor$mcScreenWidth();
        int screenHeight = ((WindowExtension) (Object) MC.getWindow()).visor$mcScreenHeight();
        blit(
                source,
                0,0,
                screenWidth,
                screenHeight
        );
    }




    private static void drawFirstPersonMirror(){
        RenderTarget source = ClientContext.renderer.firstPersonTarget.getTarget();

        int screenWidth = ((WindowExtension) (Object) MC.getWindow()).visor$mcScreenWidth();
        int screenHeight = ((WindowExtension) (Object) MC.getWindow()).visor$mcScreenHeight();
        blit(
                source,
                0,0,
                screenWidth, screenHeight
        );
    }
    private static void drawThirdPersonMirror(){
        RenderTarget source = ClientContext.renderer.thirdPersonTarget.getTarget();

        int screenWidth = ((WindowExtension) (Object) MC.getWindow()).visor$mcScreenWidth();
        int screenHeight = ((WindowExtension) (Object) MC.getWindow()).visor$mcScreenHeight();
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

        int screenWidth = ((WindowExtension) (Object) MC.getWindow()).visor$mcScreenWidth() / 2;
        int screenHeight = ((WindowExtension) (Object) MC.getWindow()).visor$mcScreenHeight();

        blit(
                leftEye,
                0,0,
                screenWidth, screenHeight
        );

        blit(
                rightEye,
                screenWidth,0,
                McRenderTarget.mainTarget().width, screenHeight
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
        int vrWidth = window.visor$mcScreenWidth();
        int vrHeight= window.visor$mcScreenHeight();

        // 2) viewport + projection
        RenderSystem.backupProjectionMatrix();
        McGlState.viewport(0, 0, vrWidth, vrHeight);
        var proj = new Matrix4f().setOrtho(0, vrWidth, vrHeight, 0, NEAR_PLANE, FAR_PLANE);
        McProjection.setOrthographic(proj);

        // 3) push / configure model-view
        McModelViewStack.push();
        try {
            McModelViewStack.identity();
            McModelViewStack.translate(0, 0, -CAMERA_Z);
            McModelViewStack.apply();

            // 4) disable fog + clear
            McFog.disable();
            int flags = CLEAR_DEPTH_FLAG | (clearBackground ? CLEAR_COLOR_FLAG : 0);
            McGlState.clear(flags);
            if (clearBackground) {
                McGlState.clearColor(0, 0, 0, 0);
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
            McModelViewStack.pop();
            McModelViewStack.apply();
            RenderSystem.restoreProjectionMatrix();
            RenderStateHelper.restoreAfterExternalRender();
        }
    }


    public static void blit(RenderTarget source,
                            int left, int top,
                            int right, int bottom) {
        McRenderTarget.blit(
                source, 0, 0, source.width, source.height,
                McRenderTarget.mainTarget(), left, top, right, bottom,
                true);
        RenderStateHelper.restoreAfterExternalRender();
    }

    public static void blitCropped(RenderTarget source,
                                   int left, int top,
                                   int right, int bottom,
                                   float cropX, float cropY,
                                   boolean keepAspect) {
        RenderTarget destination = McRenderTarget.mainTarget();
        if (keepAspect) {
            float dstAspect = (float) destination.width / (float) destination.height;
            float srcAspect = (float) McRenderTarget.viewWidth(source) / (float) McRenderTarget.viewHeight(source);
            if (dstAspect > srcAspect) {
                float ratio = srcAspect / dstAspect;
                cropY = 0.5F * (1F - ratio) + ratio * cropY;
            } else {
                float ratio = dstAspect / srcAspect;
                cropX = 0.5F * (1F - ratio) + ratio * cropX;
            }
        }

        int srcX0 = (int) (cropX * source.width);
        int srcY0 = (int) (cropY * source.height);
        int srcX1 = source.width - srcX0;
        int srcY1 = source.height - srcY0;

        McRenderTarget.blit(
                source, srcX0, srcY0, srcX1, srcY1,
                destination, left, top, right, bottom,
                true);
        RenderStateHelper.restoreAfterExternalRender();
    }






}
