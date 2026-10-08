// #!MC-VERSION:: 1.20.1-26.2
package org.vmstudio.visor.mixin.client.input;

import org.vmstudio.visor.api.common.utils.Vector3fHistory;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.extensions.client.WindowExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//? if >=1.20.5 {
/*import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
*///?}


@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {

    @Shadow
    private boolean mouseGrabbed;

    @Final
    @Shadow
    private Minecraft minecraft;


    /* ****************** *\
      //--------VR MOUSE--------\\
        \* ****************** */
    // 1.20.5 split this into handleAccumulatedMovement (which also dispatches the screen
    // mouseMoved/mouseDragged events - cancelling THAT kills slider dragging) + turnPlayer(double)
    @Inject(at = @At("HEAD"), method = "turnPlayer", cancellable = true)
    public void visor$noTurn(CallbackInfo ci) {
        if (VisorState.get().isNotActive()) {
            return;
        }

        Vector3fHistory forwardMove = ClientContext.rawPoseHandler
                .getControllerData(ClientContext.localPlayer.getActiveHand())
                .getForwardHistory();
        this.minecraft.getTutorial().onMouse(
                1.0 - forwardMove
                        .averagePosition(0.2f)
                        .normalize()
                        .dot(
                                forwardMove
                                        .averagePosition(1.0f)
                                        .normalize()
                        ),
                0
        );
        ci.cancel();
    }

    //? if >=1.20.5 {
    /*@WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;isWindowActive()Z"), method = {"onMove", "handleAccumulatedMovement"})
    public boolean visor$mouseMoveIfWindowNotFocused(Minecraft instance, Operation<Boolean> original) {
        return VisorState.get().isActive() || original.call(instance);
    }
    *///?}

    //here we use ActualScreenWidth, to support mouse usage in GUI mirror mode
    //? if >=1.21.9 {
    /*@ModifyArg(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/InputConstants;grabOrReleaseMouse(Lcom/mojang/blaze3d/platform/Window;IDD)V"), index = 2, method = {"grabMouse", "releaseMouse"})
    *///?} else {
    @ModifyArg(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/InputConstants;grabOrReleaseMouse(JIDD)V"), index = 2, method = {"grabMouse", "releaseMouse"})
    //?}
    public double visor$vrMouseXCenter(double x) {
        return VisorState.get().isActive()
                ? (double) ((WindowExtension) (Object) minecraft.getWindow())
                .visor$mcScreenWidth() / 2
                : x;
    }
    //? if >=1.21.9 {
    /*@ModifyArg(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/InputConstants;grabOrReleaseMouse(Lcom/mojang/blaze3d/platform/Window;IDD)V"), index = 3, method = {"grabMouse", "releaseMouse"})
    *///?} else {
    @ModifyArg(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/InputConstants;grabOrReleaseMouse(JIDD)V"), index = 3, method = {"grabMouse", "releaseMouse"})
    //?}
    public double visor$vrMouseYCenter(double y) {
        return VisorState.get().isActive()
                ? (double) ((WindowExtension) (Object) minecraft.getWindow())
                .visor$mcScreenHeight() / 2
                : y;
    }
    @ModifyVariable(at = @At(value = "HEAD"), ordinal = 0, method = "onMove", argsOnly = true)
    public double visor$vrMouseX(double x) {
        if (VisorState.get().isActive()) {
            x *= ClientContext.guiManager.getGuiWidth()
                    / (double) ((WindowExtension) (Object) minecraft.getWindow())
                    .visor$mcScreenWidth();
        }
        return x;
    }
    @ModifyVariable(at = @At(value = "HEAD"), ordinal = 1, method = "onMove", argsOnly = true)
    public double visor$vrMouseY(double y) {
        if (VisorState.get().isActive()) {
            y *= (double) ClientContext.guiManager.getGuiHeight()
                    / (double) ((WindowExtension) (Object) minecraft.getWindow())
                    .visor$mcScreenHeight();
        }
        return y;
    }



}
