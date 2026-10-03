package org.vmstudio.visor.mixin.client.renderer.blaze3d;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.Window;
import org.vmstudio.visor.api.client.gui.overlays.framework.VROverlayScreen;
import org.vmstudio.visor.api.compatibility.mcversion.render.McRenderTarget;
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.core.client.render.VRRenderState;
import org.vmstudio.visor.extensions.client.WindowExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import org.vmstudio.visor.core.client.ClientContext;

@Mixin(Window.class)
public abstract class WindowMixin implements WindowExtension {

    @Shadow
    private int width;

    @Shadow
    private int height;


    /* ********************************** *\
  //--------REPLACING VANILLA VALUES--------\\
    \* ********************************** */

    // Overlay screen draws into a framebuffer of its own size
    // not into the shared gui canvas,
    // so, we need to override sizes here to avoid issues with tooltips
    // (might be more than just tooltips issues)

    @Inject(method = "getWidth", at = @At("HEAD"), cancellable = true, require = 1)
    void visor$vrWidth(CallbackInfoReturnable<Integer> cir) {
        if(VisorState.get().isActive()) {
            VROverlayScreen overlay = VROverlayScreen.getRenderingOverlay();
            var phase = VRRenderState.getPhase();
            if (overlay != null) {
                cir.setReturnValue(
                        visor$overlayPixelWidth(overlay)
                );
            } else if (phase.isVanilla() || phase.isVRGui()) {
                cir.setReturnValue(
                        ClientContext.guiManager.getGuiWidth()
                );
            } else {
                cir.setReturnValue(
                        McRenderTarget.viewWidth(McRenderTarget.mainTarget())
                );
            }
        }
    }

    @Inject(method = "getHeight", at = @At("HEAD"), cancellable = true, require = 1)
    void visor$vrHeight(CallbackInfoReturnable<Integer> cir) {
        if(VisorState.get().isActive()) {
            VROverlayScreen overlay = VROverlayScreen.getRenderingOverlay();
            var phase = VRRenderState.getPhase();
            if (overlay != null) {
                cir.setReturnValue(
                        visor$overlayPixelHeight(overlay)
                );
            } else if (phase.isVanilla() || phase.isVRGui()) {
                cir.setReturnValue(
                        ClientContext.guiManager.getGuiHeight()
                );
            } else {
                cir.setReturnValue(
                        McRenderTarget.viewHeight(McRenderTarget.mainTarget())
                );
            }
        }
    }


    @Inject(method = "getScreenWidth", at = @At("HEAD"), cancellable = true, require = 1)
    void visor$vrScreenWidth(CallbackInfoReturnable<Integer> cir) {
        if (VisorState.get().isActive()) {
            VROverlayScreen overlay = VROverlayScreen.getRenderingOverlay();
            cir.setReturnValue(overlay != null
                    ? visor$overlayPixelWidth(overlay)
                    : ClientContext.guiManager.getGuiWidth()
            );
        }
    }

    @Inject(method = "getScreenHeight", at = @At("HEAD"), cancellable = true, require = 1)
    void visor$vrScreenHeight(CallbackInfoReturnable<Integer> cir) {
        if (VisorState.get().isActive()) {
            VROverlayScreen overlay = VROverlayScreen.getRenderingOverlay();
            cir.setReturnValue(overlay != null
                    ? visor$overlayPixelHeight(overlay)
                    : ClientContext.guiManager.getGuiHeight()
            );
        }
    }


    @Inject(method = "getGuiScaledWidth", at = @At("HEAD"), cancellable = true, require = 1)
    void visor$vrGuiScaledWidth(CallbackInfoReturnable<Integer> cir) {
        if (VisorState.get().isActive()) {
            VROverlayScreen overlay = VROverlayScreen.getRenderingOverlay();
            cir.setReturnValue(overlay != null
                    ? overlay.width
                    : ClientContext.guiManager.getGuiScaledWidth()
            );
        }
    }

    @Inject(method = "getGuiScaledHeight", at = @At("HEAD"), cancellable = true, require = 1)
    void visor$vrGuiScaledHeight(CallbackInfoReturnable<Integer> cir) {
        if (VisorState.get().isActive()) {
            VROverlayScreen overlay = VROverlayScreen.getRenderingOverlay();
            cir.setReturnValue(overlay != null
                    ? overlay.height
                    : ClientContext.guiManager.getGuiScaledHeight()
            );
        }
    }


    //? if >=1.21.6 {
    @Inject(method = "getGuiScale", at = @At("HEAD"), cancellable = true, require = 1)
    void visor$vrScaleFactor(CallbackInfoReturnable<Integer> cir) {
        if (VisorState.get().isActive()) {
            VROverlayScreen overlay = VROverlayScreen.getRenderingOverlay();
            cir.setReturnValue(overlay != null
                    ? overlay.getGuiScaleFactor()
                    : ClientContext.guiManager.getScaleFactor()
            );
        }
    }
    //?} else {
    /*@Inject(method = "getGuiScale", at = @At("HEAD"), cancellable = true, require = 1)
    void visor$vrScaleFactor(CallbackInfoReturnable<Double> cir) {
        if (VisorState.get().isActive()) {
            VROverlayScreen overlay = VROverlayScreen.getRenderingOverlay();
            cir.setReturnValue((double) (overlay != null
                    ? overlay.getGuiScaleFactor()
                    : ClientContext.guiManager.getScaleFactor())
            );
        }
    }
    *///?}


    @Unique
    private static int visor$overlayPixelWidth(VROverlayScreen overlay) {
        RenderTarget target = overlay.getRenderTarget();
        return target != null ? McRenderTarget.viewWidth(target) : overlay.getRequestedWidth();
    }

    @Unique
    private static int visor$overlayPixelHeight(VROverlayScreen overlay) {
        RenderTarget target = overlay.getRenderTarget();
        return target != null ? McRenderTarget.viewHeight(target) : overlay.getRequestedHeight();
    }


    /* ************** *\
  //--------MISC--------\\
    \* ************** */
    //? if >=26.3 {
    @Inject(method = "onResize", at = @At("HEAD"))
    private void visor$onResize(int i, int j, CallbackInfo ci) {
    //?} else {
    /*@Inject(method = "onResize", at = @At("HEAD"))
    private void visor$onResize(long l, int i, int j, CallbackInfo ci) {
    *///?}
        if (VisorState.get().isActive()) {
            ClientContext.renderer.prepareResize(
                    "window resized"
            );
        }
    }

    /**
     * No Vsync in VR
     * @param v s
     * @return s
     */
    //? if <26.2 {
    /*@ModifyVariable(method = "updateVsync", ordinal = 0, at = @At("HEAD"), argsOnly = true)
    boolean visor$noVsync(boolean v) {
        if (VisorState.get().isActive()) {
            return false;
        }
        return v;
    }
    *///?}


    /* ************************ *\
  //--------PUBLIC METHODS--------\\
    \* ************************ */
    @Override
    @Unique
    public int visor$mcScreenHeight() {
        return height;
    }

    @Override
    @Unique
    public int visor$mcScreenWidth() {
        return width;
    }
}
