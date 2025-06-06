package me.phoenixra.visor.core.mixin.client.input;


import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.VisorState;
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
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    public void visor$tick(boolean isSneaking,
                           float sneakSpeed,
                           CallbackInfo ci) {
        if (!VisorState.getStateMode().isActive()) {
            return;
        }
        ci.cancel();


        var vrInput = ClientContext.player.getInputMovement();
        this.leftImpulse = vrInput.leftImpulse;
        this.forwardImpulse = vrInput.forwardImpulse;

        if (isSneaking) {
            this.leftImpulse = (float) ((double) this.leftImpulse * sneakSpeed);
            this.forwardImpulse = (float) ((double) this.forwardImpulse * sneakSpeed);
        }
    }
}
