package me.phoenixra.visor.core.mixin.client.input;

import me.phoenixra.visor.api.client.gui.overlay.types.VROverlayScreen;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.VisorState;
import me.phoenixra.visor.core.client.mcmodified.WindowModified;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.Overlay;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(MouseHandler.class)
public class MouseHandlerMixin {

    @Final
    @Shadow
    private Minecraft minecraft;



    /* ****************** *\
  //--------VR MOUSE--------\\
    \* ****************** */
    @Inject(at = @At("HEAD"), method = "turnPlayer", cancellable = true)
    public void visor$noTurn(CallbackInfo ci) {
        if (VisorState.getState().isNotActive()) {
            return;
        }

        ci.cancel();
    }


    @ModifyArg(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/InputConstants;grabOrReleaseMouse(JIDD)V"), index = 2, method = {"grabMouse", "releaseMouse"})
    public double visor$vrMouseXCenter(double x) {
        return VisorState.getState().isActive()
                ? (double) ((WindowModified) (Object) minecraft.getWindow())
                .visor$getActualWidth() / 2
                : x;
    }
    @ModifyArg(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/InputConstants;grabOrReleaseMouse(JIDD)V"), index = 3, method = {"grabMouse", "releaseMouse"})
    public double visor$vrMouseYCenter(double y) {
        return VisorState.getState().isActive()
                ? (double) ((WindowModified) (Object) minecraft.getWindow())
                .visor$getActualHeight() / 2
                : y;
    }
    @ModifyVariable(at = @At(value = "HEAD"), ordinal = 0, method = "onMove", argsOnly = true)
    public double visor$vrMouseX(double x) {
        if (VisorState.getState().isActive()) {
            x *= ClientContext.guiManager.getGuiWidth()
                    / (double) ((WindowModified) (Object) minecraft.getWindow())
                    .visor$getActualWidth();
        }
        return x;
    }
    @ModifyVariable(at = @At(value = "HEAD"), ordinal = 1, method = "onMove", argsOnly = true)
    public double visor$vrMouseY(double y) {
        if (VisorState.getState().isActive()) {
            y *= (double) ClientContext.guiManager.getGuiHeight()
                    / (double) ((WindowModified) (Object) minecraft.getWindow())
                    .visor$getActualHeight();
        }
        return y;
    }



}
