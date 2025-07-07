package me.phoenixra.visor.core.mixin.client.input;


import me.phoenixra.visor.api.client.ClientFeature;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.VisorState;
import me.phoenixra.visor.core.client.settings.VRClientSettings;
import me.phoenixra.visor.core.client.tasks.types.movement.TaskRoomSneak;
import me.phoenixra.visor.core.client.utils.ClientUtils;
import net.minecraft.client.Options;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.KeyboardInput;
import org.joml.Vector2f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.phoenixra.visor.core.client.VisorClientImpl.MC;


@Mixin(KeyboardInput.class)
public class MovementInputMixin extends Input {


    /* ****************** *\
  //--------MOVEMENT--------\\
    \* ****************** */
    @Final
    @Shadow
    private Options options;
    @Unique
    private boolean visor$autoSprintActive = false;
    @Unique
    private boolean visor$movedLastTick = false;

    /* ****************** *\
  //--------MOVEMENT--------\\
    \* ****************** */
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    public void visor$tick(boolean isSneaking,
                           float sneakSpeed,
                           CallbackInfo ci) {
        if (VisorState.getState().isNotActive()) {
            return;
        }
        ci.cancel();

        this.leftImpulse = 0.0F;
        this.forwardImpulse = 0.0F;

        float forward = 0.0F;

        boolean moved = false;

        //analog movement
        if (MC.screen == null
                && !ClientContext.overlayManager
                .getKeyboardAccessor().isVisible()
                && ClientContext.visor.isFeatureEnabled(ClientFeature.INPUT_MOVEMENT)) {
            moved = true;

            Vector2f strafeVec = ClientContext.player.getInputMovement();

            if (strafeVec.x == 0.0F && strafeVec.y == 0.0F) {
                this.forwardImpulse = 0.0F;
                this.leftImpulse = 0.0F;
            } else {
                forward = strafeVec.y;
                this.forwardImpulse = strafeVec.y;
                this.leftImpulse = -strafeVec.x;
            }

            this.visor$movedLastTick = true;
            this.up = this.forwardImpulse > 0f;
            this.down = this.forwardImpulse < 0f;
            this.left = this.leftImpulse > 0f;
            this.right = this.leftImpulse < 0f;
            ClientUtils.updateKeyMappingState(
                    this.options.keyUp, this.up
            );
            ClientUtils.updateKeyMappingState(
                    this.options.keyDown, this.down
            );
            ClientUtils.updateKeyMappingState(
                    this.options.keyLeft, this.left
            );
            ClientUtils.updateKeyMappingState(
                    this.options.keyRight, this.right
            );

            //Sprinting
            if (forward >= VRClientSettings.getSprintThreshold()) {
                MC.player.setSprinting(true);
                this.visor$autoSprintActive = true;
                this.forwardImpulse = 1.0F;
            } else if (this.forwardImpulse > 0.0F) {
                this.forwardImpulse = this.forwardImpulse / VRClientSettings.getSprintThreshold();
            }
        }

        //RESET STATE NEXT TICK
        if (!moved && this.visor$movedLastTick) {
            ClientUtils.updateKeyMappingState(
                    this.options.keyUp, false
            );
            ClientUtils.updateKeyMappingState(
                    this.options.keyDown, false
            );
            ClientUtils.updateKeyMappingState(
                    this.options.keyLeft, false
            );
            ClientUtils.updateKeyMappingState(
                    this.options.keyRight, false
            );
        }
        this.visor$movedLastTick = moved;

        //SPRINTING
        if (this.visor$autoSprintActive
                && forward < VRClientSettings.getSprintThreshold()) {
            MC.player.setSprinting(false);
            this.visor$autoSprintActive = false;
        }

        //JUMP
        boolean canJump = MC.screen == null;
        this.jumping = this.options.keyJump.isDown() && canJump;

        //SHIFT
        boolean canShift = MC.screen == null;
        this.shiftKeyDown = canShift && (
                TaskRoomSneak.getInstance().getSneakTimer() > 0
                        || TaskRoomSneak.getInstance().isSneaking()
                        || this.options.keyShift.isDown()
        );

        //SNEAKING
        if (isSneaking) {
            this.leftImpulse = (float) ((double) this.leftImpulse * sneakSpeed);
            this.forwardImpulse = (float) ((double) this.forwardImpulse * sneakSpeed);
        }
    }
}
