package me.phoenixra.visor.core.mixin.client.renderer.blaze3d;

import com.mojang.blaze3d.platform.Window;
import me.phoenixra.visor.core.client.VisorState;
import me.phoenixra.visor.core.client.mcmodified.WindowModified;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import me.phoenixra.visor.core.client.ClientContext;
import static me.phoenixra.visor.core.client.VisorClientImpl.MC;

@Mixin(Window.class)
public abstract class WindowMixin implements WindowModified {

    @Shadow
    private int width;

    @Shadow
    private int height;


    /* ********************************** *\
  //--------REPLACING VANILLA VALUES--------\\
    \* ********************************** */
    @Inject(method = "getWidth", at = @At("HEAD"), cancellable = true)
    void visor$vrWidth(CallbackInfoReturnable<Integer> cir) {
        if (VisorState.getState().isActive()) {
            cir.setReturnValue(
                    MC.mainRenderTarget
                            .viewWidth
            );
        }
    }
    @Inject(method = "getHeight", at = @At("HEAD"), cancellable = true)
    void visor$vrHeight(CallbackInfoReturnable<Integer> cir) {
        if (VisorState.getState().isActive()) {
            cir.setReturnValue(
                    MC.mainRenderTarget
                    .viewHeight
            );
        }
    }
    @Inject(method = "getScreenWidth", at = @At("HEAD"), cancellable = true)
    void visor$vrScreenWidth(CallbackInfoReturnable<Integer> cir) {
        if (VisorState.getState().isActive()) {
            cir.setReturnValue(
                    ClientContext
                            .guiManager
                            .getGuiWidth()
            );
        }
    }
    @Inject(method = "getScreenHeight", at = @At("HEAD"), cancellable = true)
    void visor$vrScreenHeight(CallbackInfoReturnable<Integer> cir) {
        if (VisorState.getState().isActive()) {
            cir.setReturnValue(
                    ClientContext
                            .guiManager
                            .getGuiHeight()
            );
        }
    }
    @Inject(method = "getGuiScaledWidth", at = @At("HEAD"), cancellable = true)
    void visor$vrGuiScaledWidth(CallbackInfoReturnable<Integer> cir) {
        if (VisorState.getState().isActive()) {
            cir.setReturnValue(
                    ClientContext
                            .guiManager
                            .getGuiScaledWidth()
            );
        }
    }
    @Inject(method = "getGuiScaledHeight", at = @At("HEAD"), cancellable = true)
    void visor$vrGuiScaledHeight(CallbackInfoReturnable<Integer> cir) {
        if (VisorState.getState().isActive()) {
            cir.setReturnValue(
                    ClientContext
                            .guiManager
                            .getGuiScaledHeight()
            );
        }
    }
    @Inject(method = "getGuiScale", at = @At("HEAD"), cancellable = true)
    void visor$vrScaleFactor(CallbackInfoReturnable<Double> cir) {
        if (VisorState.getState().isActive()) {
            cir.setReturnValue(
                    (double) ClientContext
                            .guiManager
                            .getScaleFactor()
            );
        }
    }


    /* ************** *\
  //--------MISC--------\\
    \* ************** */
    @Inject(method = "onResize", at = @At("HEAD"))
    private void visor$onResize(long l, int i, int j, CallbackInfo ci) {
        if (VisorState.getState().isActive()) {
            ClientContext.renderer.prepareResize(
                    "Main Window Resized"
            );
        }
    }

    /**
     * No Vsync in VR
     * @param v s
     * @return s
     */
    @ModifyVariable(method = "updateVsync", ordinal = 0, at = @At("HEAD"), argsOnly = true)
    boolean visor$noVsync(boolean v) {
        if (VisorState.getState().isActive()) {
            return false;
        }
        return v;
    }


    /* ************************ *\
  //--------PUBLIC METHODS--------\\
    \* ************************ */
    @Override
    @Unique
    public int visor$getActualScreenHeight() {
        return height;
    }

    @Override
    @Unique
    public int visor$getActualScreenWidth() {
        return width;
    }
}
