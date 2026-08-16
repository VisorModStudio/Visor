package org.vmstudio.visor.mixin.client.input;


import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.core.client.tasks.types.movement.TaskRoomSneak;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.entity.player.Input;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


// 1.21.2: Input moved to net.minecraft.world.entity.player as an immutable record and
// KeyboardInput now extends ClientInput, which holds it in the keyPresses field.
@Mixin(KeyboardInput.class)
public class MovementInputMixin extends ClientInput {


    /* ****************** *\
  //--------MOVEMENT--------\\
    \* ****************** */

    // 1.21.2: KeyboardInput#tick lost its (isSneaking, sneakSpeedMultiplier) parameters,
    // and the shiftKeyDown field it used to be injected after no longer exists - tick now
    // builds one Input record in a single assignment. TAIL runs after the record and both
    // impulses have been set, which is the same point the old FIELD injection reached.
    @Inject(method = "tick", at = @At("TAIL"))
    public void visor$movement(CallbackInfo ci,
                               @Share("climbing") LocalBooleanRef climbing) {
        if (VisorState.get().isNotActive()) {
            return;
        }

        boolean noScreen = Minecraft.getInstance().screen == null;

        boolean jumping = this.keyPresses.jump()
                && noScreen
                && !climbing.get();

        boolean shiftKeyDown = noScreen
                && (TaskRoomSneak.getInstance().getSneakTimer() > 0
                || TaskRoomSneak.getInstance().isSneaking()
                || this.keyPresses.shift());

        // the record is immutable, so the whole thing is rebuilt
        this.keyPresses = new Input(
                this.keyPresses.forward(),
                this.keyPresses.backward(),
                this.keyPresses.left(),
                this.keyPresses.right(),
                jumping,
                shiftKeyDown,
                this.keyPresses.sprint()
        );

        if (ClientContext.localPlayer.isMoving()) {
            var movement = ClientContext.localPlayer.getMovement();
            this.leftImpulse = -movement.x;
            this.forwardImpulse = movement.y;
        }
    }
}
