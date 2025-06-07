package me.phoenixra.visor.core.client.input.actionset.game;

import me.phoenixra.atumvr.core.enums.XRInteractionProfile;
import me.phoenixra.atumvr.core.input.action.profileset.types.ValveIndexSet;
import me.phoenixra.visor.api.client.input.action.BindingPath;
import me.phoenixra.visor.api.client.input.action.VisorActionSet;
import me.phoenixra.visor.api.client.input.action.types.VisorActionVec2;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.settings.VRClientSettings;
import me.phoenixra.visor.core.client.tasks.types.game.movement.TaskRoomSneak;
import me.phoenixra.visor.core.client.utils.ClientUtils;
import net.minecraft.client.player.Input;
import org.joml.Vector2f;

import java.util.Map;

import static me.phoenixra.visor.core.client.VisorClientImpl.MC;

public class ActionInputMovement extends VisorActionVec2 {
    public static final String ID = "input_movement";


    private boolean movedLastTick, autoSprintActive;


    public ActionInputMovement(VisorActionSet actionSet) {
        super(actionSet, ID);
    }

    @Override
    public void preTick() {
        super.preTick();
        Vector2f newState = getState();

        Input input = ClientContext.player.getInputMovement();

        input.leftImpulse = 0.0F;
        input.forwardImpulse = 0.0F;

        float forward = 0.0F;

        boolean moved = false;

        if (ClientContext.properties.isInputMovementAllowed()) {
            moved = true;

            if (newState.x == 0.0F && newState.y == 0.0F) {
                input.forwardImpulse = 0.0F;
                input.leftImpulse = 0.0F;
            } else {
                forward = newState.y;
                input.forwardImpulse = newState.y;
                input.leftImpulse = -newState.x;
            }

            movedLastTick = true;
            input.up = input.forwardImpulse > 0f;
            input.down = input.forwardImpulse < 0f;
            input.left = input.leftImpulse > 0f;
            input.right = input.leftImpulse < 0f;
            ClientUtils.updateKeyMappingState(
                    MC.options.keyUp, input.up
            );
            ClientUtils.updateKeyMappingState(
                    MC.options.keyDown, input.down
            );
            ClientUtils.updateKeyMappingState(
                    MC.options.keyLeft, input.left
            );
            ClientUtils.updateKeyMappingState(
                    MC.options.keyRight, input.right
            );

            //Sprinting
            if (forward >= VRClientSettings.getSprintThreshold()) {
                MC.player.setSprinting(true);
                this.autoSprintActive = true;
                input.forwardImpulse = 1.0F;
            } else if (input.forwardImpulse > 0.0F) {
                input.forwardImpulse = input.forwardImpulse / VRClientSettings.getSprintThreshold();
            }
        }

        //RESET STATE NEXT TICK
        if (!moved && movedLastTick) {
            ClientUtils.updateKeyMappingState(
                    MC.options.keyUp, false
            );
            ClientUtils.updateKeyMappingState(
                    MC.options.keyDown, false
            );
            ClientUtils.updateKeyMappingState(
                    MC.options.keyLeft, false
            );
            ClientUtils.updateKeyMappingState(
                    MC.options.keyRight, false
            );
        }
        movedLastTick = moved;

        //SPRINTING
        if (this.autoSprintActive
                && forward < VRClientSettings.getSprintThreshold()) {
            MC.player.setSprinting(false);
            this.autoSprintActive = false;
        }

        //JUMP
        //LEGACY makes no sense, since game actionSet is active only when no screen
        boolean canJump = MC.screen == null;

        input.jumping = MC.options.keyJump.isDown() && canJump;

        //SHIFT
        //LEGACY makes no sense, since game actionSet is active only when no screen
        boolean canShift = MC.screen == null;
        input.shiftKeyDown = canShift && (
                TaskRoomSneak.getInstance().getSneakTimer() > 0
                        || TaskRoomSneak.getInstance().isSneaking()
                        || MC.options.keyShift.isDown()
        );
    }

    @Override
    protected void onStateChanged(Vector2f newState) {

    }

    @Override
    protected void onClear() {
        Input input = ClientContext.player.getInputMovement();

        input.leftImpulse = 0.0F;
        input.forwardImpulse = 0.0F;
        input.up = false;
        input.down = false;
        input.left = false;
        input.right = false;
        input.jumping = false;
        input.shiftKeyDown = false;

        if (movedLastTick) {
            ClientUtils.updateKeyMappingState(
                    MC.options.keyUp, false
            );
            ClientUtils.updateKeyMappingState(
                    MC.options.keyDown, false
            );
            ClientUtils.updateKeyMappingState(
                    MC.options.keyLeft, false
            );
            ClientUtils.updateKeyMappingState(
                    MC.options.keyRight, false
            );
            movedLastTick = false;
        }
        if (autoSprintActive) {
            MC.player.setSprinting(false);
            autoSprintActive = false;
        }

    }

    @Override
    protected Map<XRInteractionProfile, BindingPath> loadDefaults() {
        return Map.of(
                XRInteractionProfile.VALVE_INDEX,
                new BindingPath(
                        ValveIndexSet.VEC2_THUMBSTICK_LEFT,
                        ValveIndexSet.VEC2_THUMBSTICK_RIGHT
                )
        );
    }
}
