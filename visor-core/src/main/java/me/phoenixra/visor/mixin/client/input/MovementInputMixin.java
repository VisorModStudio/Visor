package me.phoenixra.visor.mixin.client.input;


import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.VisorState;
import me.phoenixra.visor.core.client.tasks.movement.TaskRoomSneak;
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
    public void visor$movement(boolean isSneaking,
                               float sneakSpeed,
                               CallbackInfo ci,
                               @Share("climbing") LocalBooleanRef climbing) {
        if (VisorState.getState().isNotActive()) {
            return;
        }

        this.jumping = this.jumping
                && Minecraft.getInstance().screen == null
                && !climbing.get();

        this.shiftKeyDown = Minecraft.getInstance().screen == null
                && (TaskRoomSneak.getInstance().getSneakTimer() > 0
                || TaskRoomSneak.getInstance().isSneaking()
                || this.shiftKeyDown);

        if (ClientContext.player.isMoving()) {
            var movement = ClientContext.player.getMovement();
            this.leftImpulse = -movement.x;
            this.forwardImpulse = movement.y;
        }
    }
}
