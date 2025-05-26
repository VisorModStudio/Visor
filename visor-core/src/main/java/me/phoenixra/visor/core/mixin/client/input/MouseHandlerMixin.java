package me.phoenixra.visor.core.mixin.client.input;

import me.phoenixra.visor.api.common.utils.Vec3History;
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

    @Unique
    private double visor$scrollVarD;
    @Unique
    private double visor$scrollVarE;


    /* ****************** *\
  //--------VR MOUSE--------\\
    \* ****************** */
    @Inject(at = @At("HEAD"), method = "turnPlayer", cancellable = true)
    public void visor$noTurn(CallbackInfo ci) {
        if (VisorState.getStateMode().isNotActive()) {
            return;
        }

        ci.cancel();
    }


    @ModifyArg(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/InputConstants;grabOrReleaseMouse(JIDD)V"), index = 2, method = {"grabMouse", "releaseMouse"})
    public double visor$vrMouseXCenter(double x) {
        return VisorState.getStateMode().isActive()
                ? (double) ((WindowModified) (Object) minecraft.getWindow())
                .visor$getScreenWidth() / 2
                : x;
    }
    @ModifyArg(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/InputConstants;grabOrReleaseMouse(JIDD)V"), index = 3, method = {"grabMouse", "releaseMouse"})
    public double visor$vrMouseYCenter(double y) {
        return VisorState.getStateMode().isActive()
                ? (double) ((WindowModified) (Object) minecraft.getWindow())
                .visor$getScreenHeight() / 2
                : y;
    }
    @ModifyVariable(at = @At(value = "HEAD"), ordinal = 0, method = "onMove", argsOnly = true)
    public double visor$vrMouseX(double x) {
        if (VisorState.getStateMode().isActive()) {
            x *= ClientContext.guiManager.getGuiWidth()
                    / (double) ((WindowModified) (Object) minecraft.getWindow())
                    .visor$getScreenWidth();
        }
        return x;
    }
    @ModifyVariable(at = @At(value = "HEAD"), ordinal = 1, method = "onMove", argsOnly = true)
    public double visor$vrMouseY(double y) {
        if (VisorState.getStateMode().isActive()) {
            y *= (double) ClientContext.guiManager.getGuiHeight()
                    / (double) ((WindowModified) (Object) minecraft.getWindow())
                    .visor$getScreenHeight();
        }
        return y;
    }


    /* ********************* *\
  //--------VR OVERLAYS--------\\
    \* ********************* */
    @Inject(method = "onScroll", at = @At(value = "HEAD"))
    public void visor$onScroll(long l, double d, double e, CallbackInfo ci) {
        visor$scrollVarD = d;
        visor$scrollVarE = e;
    }

    @Redirect(method = "onScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getOverlay()Lnet/minecraft/client/gui/screens/Overlay;", ordinal = 0))
    public Overlay visor$scrollForVROverlay(Minecraft instance) {
        if(VisorState.getStateMode().isNotActive()) return instance.getOverlay();

        boolean discrete = this.minecraft.options.discreteMouseScroll().get();
        double wheelSensitivity = this.minecraft.options.mouseWheelSensitivity().get();
        double scrollAmount = (
                discrete
                        ? Math.signum(visor$scrollVarD)
                        : visor$scrollVarD
        ) * wheelSensitivity;
        double scrollDelta = (
                discrete
                        ? Math.signum(visor$scrollVarE)
                        : visor$scrollVarE
        ) * wheelSensitivity;
        /*McOverlayScreen overlayScreen = CLIENT_CONTEXT.cursorHandler != null ?
                CLIENT_CONTEXT.cursorHandler.getFocusedOverlayAsScreen() : null;
        if (overlayScreen != null) {
            overlayScreen.mouseScrolled(
                    overlayScreen.getMouseX(), overlayScreen.getMouseY(),
                    scrollDelta
            );
            overlayScreen.afterMouseAction();
            return null;
        }*/
        return instance.getOverlay();
    }


}
