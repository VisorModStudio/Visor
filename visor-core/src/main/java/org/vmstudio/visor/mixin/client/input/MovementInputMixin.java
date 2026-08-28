package org.vmstudio.visor.mixin.client.input;


import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.core.client.tasks.types.movement.TaskRoomSneak;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(KeyboardInput.class)
public class MovementInputMixin extends Input {


    /* ****************** *\
  //--------MOVEMENT--------\\
    \* ****************** */
    @Inject(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/client/player/KeyboardInput;shiftKeyDown:Z", shift = At.Shift.AFTER))
    public void visor$applyVrInput(boolean isSneaking,
                                   float sneakSpeed,
                                   CallbackInfo ci) {
        if (VisorState.get().isNotActive()) {
            return;
        }

        boolean screenOpen = Minecraft.getInstance().screen != null;
        if (screenOpen) {
            this.jumping = false;
        }

        TaskRoomSneak sneak = TaskRoomSneak.getInstance();
        this.shiftKeyDown = !screenOpen
                && (this.shiftKeyDown || sneak.isSneaking() || sneak.getSneakTimer() > 0);

        if (ClientContext.localPlayer.isMoving()) {
            var movement = ClientContext.localPlayer.getMovement();
            this.leftImpulse = -movement.x;
            this.forwardImpulse = movement.y;
        }
    }
}
