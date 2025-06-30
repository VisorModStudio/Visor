package me.phoenixra.visor.core.mixin.client.input;

import me.phoenixra.visor.api.common.utils.Vector3fHistory;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.VisorState;
import me.phoenixra.visor.core.client.mcmodified.WindowModified;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


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
    @Inject(at = @At("HEAD"), method = "turnPlayer", cancellable = true)
    public void visor$noTurn(CallbackInfo ci) {
        if (VisorState.getState().isNotActive()) {
            return;
        }

        Vector3fHistory forwardMove = ClientContext.rawPoseHandler
                .getControllerData(ClientContext.player.getActiveHand())
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





}
