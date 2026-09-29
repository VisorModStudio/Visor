package org.vmstudio.visor.mixin.client.input;


//? if >=1.21.2 {
import org.objectweb.asm.Opcodes;
//?}
import org.spongepowered.asm.mixin.Unique;
import org.vmstudio.visor.api.compatibility.mcversion.McPlayerInput;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.core.client.tasks.types.movement.TaskRoomSneak;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(KeyboardInput.class)
public class MovementInputMixin {


    /* ****************** *\
  //--------MOVEMENT--------\\
    \* ****************** */
    //? if >=1.21.5 {
    // javac writes the inherited field through KeyboardInput, and Mixin matches the owner exactly
    @Inject(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/client/player/KeyboardInput;moveVector:Lnet/minecraft/world/phys/Vec2;", opcode = Opcodes.PUTFIELD, ordinal = 0, shift = At.Shift.AFTER))
    public void visor$applyVrInput(CallbackInfo ci) {
        visor$applyVrInputState();
    }
    //?} elif >=1.21.2 {
    /*// 1.21.2 builds the key record first, so the first impulse write is the spot before the sneak multiplier
    @Inject(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/client/player/KeyboardInput;leftImpulse:F", opcode = Opcodes.PUTFIELD, ordinal = 0, shift = At.Shift.AFTER))
    public void visor$applyVrInput(CallbackInfo ci) {
        visor$applyVrInputState();
    }
    *///?} else {
    /*@Inject(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/client/player/KeyboardInput;shiftKeyDown:Z", shift = At.Shift.AFTER))
    public void visor$applyVrInput(CallbackInfo ci) {
        visor$applyVrInputState();
    }
    *///?}

    @Unique
    private void visor$applyVrInputState() {
        if (VisorState.get().isNotActive()) {
            return;
        }
        KeyboardInput input = (KeyboardInput) (Object) this;

        boolean screenOpen = Minecraft.getInstance().screen != null;
        if (screenOpen) {
            McPlayerInput.setJumping(input, false);
        }

        TaskRoomSneak sneak = TaskRoomSneak.getInstance();
        McPlayerInput.setSneaking(input, !screenOpen
                && (McPlayerInput.isSneaking(input) || sneak.isSneaking() || sneak.getSneakTimer() > 0));

        if (ClientContext.localPlayer.isMoving()) {
            var movement = ClientContext.localPlayer.getMovement();
            McPlayerInput.setImpulses(input, -movement.x, movement.y);
        }
    }
}
