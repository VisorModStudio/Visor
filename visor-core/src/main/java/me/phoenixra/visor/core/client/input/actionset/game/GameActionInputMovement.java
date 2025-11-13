package me.phoenixra.visor.core.client.input.actionset.game;

import me.phoenixra.atumvr.core.enums.XRInteractionProfile;
import me.phoenixra.atumvr.core.input.action.profileset.types.OculusTouchSet;
import me.phoenixra.atumvr.core.input.action.profileset.types.ValveIndexSet;
import me.phoenixra.visor.api.client.input.action.BindingPath;
import me.phoenixra.visor.api.client.input.action.VisorActionSet;
import me.phoenixra.visor.api.client.input.action.framework.VisorActionVec2;
import me.phoenixra.visor.api.common.ControllerHand;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.settings.VRClientSettings;
import me.phoenixra.visor.core.client.utils.ClientUtils;
import net.minecraft.util.Mth;
import org.joml.Vector2f;

import java.util.Map;

import static me.phoenixra.visor.core.client.VisorClientImpl.MC;

public class GameActionInputMovement extends VisorActionVec2 {
    public static final String ID = "input_movement";

    private boolean wasMovement;
    private boolean wasAutoSprinting;

    public GameActionInputMovement(VisorActionSet actionSet) {
        super(actionSet, ID);
    }

    @Override
    public void preTick() {
        super.preTick();

        //@TODO
        if(ClientContext.cursorHandler.isHandFocused(ControllerHand.OFFHAND)){
            onClear();
            return;
        }

        Vector2f rawMove = getState();

        Vector2f movement = ClientContext.player.getMovement();


        boolean climbing = false;
        boolean moving = ClientContext.player.isMoving();
        float forward = 0F;
        if (/*!KeyboardHandler.SHOWING
                && */!climbing) {
            movement.zero();

            movement.x = applyDeadzone(rawMove.x, 0.05F);
            movement.y = applyDeadzone(rawMove.y, 0.05F);

            moving = (
                    movement.x != 0.0F || movement.y != 0.0F
            );

            forward = movement.y;

            movement.set(toDigital(movement, 0.5f));


            if (moving) {
                ClientUtils.updateKeyMappingState(
                        MC.options.keyUp, movement.y > 0
                );
                ClientUtils.updateKeyMappingState(
                        MC.options.keyDown, movement.y < 0
                );
                ClientUtils.updateKeyMappingState(
                        MC.options.keyRight, movement.x > 0
                );
                ClientUtils.updateKeyMappingState(
                        MC.options.keyLeft, movement.x < 0
                );

                if (!MC.player.isMovingSlowly()) {
                    if (forward >= VRClientSettings.getSprintThreshold()) {
                        MC.player.setSprinting(true);
                        this.wasAutoSprinting = true;
                        movement.y = 1.0F;
                    } else if (movement.y > 0.0F) {
                        movement.y /= VRClientSettings.getSprintThreshold();
                    }
                }
            }
        }

        if (!moving && this.wasMovement) {
            ClientUtils.updateKeyMappingState(MC.options.keyUp, false);
            ClientUtils.updateKeyMappingState(MC.options.keyDown, false);
            ClientUtils.updateKeyMappingState(MC.options.keyLeft, false);
            ClientUtils.updateKeyMappingState(MC.options.keyRight, false);
        }
        this.wasMovement = moving;
        ClientContext.player.setMoving(moving);
        if (this.wasAutoSprinting && forward < VRClientSettings.getSprintThreshold()) {
            MC.player.setSprinting(false);
            this.wasAutoSprinting = false;
        }
    }

    @Override
    protected void onStateChanged(Vector2f newState) {

    }

    @Override
    protected void onClear() {
        Vector2f input = ClientContext.player.getMovement();

        input.x = 0;
        input.y = 0;
        ClientContext.player.setMoving(false);

    }

    @Override
    protected Map<XRInteractionProfile, BindingPath> loadDefaults() {
        return Map.of(
                XRInteractionProfile.VALVE_INDEX,
                new BindingPath(
                        ValveIndexSet.VEC2_THUMBSTICK_LEFT,
                        ValveIndexSet.VEC2_THUMBSTICK_RIGHT
                ),
                XRInteractionProfile.OCULUS_TOUCH,
                new BindingPath(
                        OculusTouchSet.VEC2_THUMBSTICK_LEFT,
                        OculusTouchSet.VEC2_THUMBSTICK_RIGHT
                )
        );
    }


    private float applyDeadzone(float axis, float deadzone) {
        if (Math.abs(axis) > deadzone) {
            float scalar = 1.0F / (1.0F - deadzone);
            return (Math.abs(axis) - deadzone) * scalar * Math.signum(axis);
        } else {
            return 0F;
        }
    }

    private Vector2f toDigital(Vector2f value, float deadzone) {
        Vector2f digital = new Vector2f();
        if (value.length() > deadzone) {
            // get pointing angle, forward 0, back +-PI
            float angle = (float) Math.atan2(value.x, value.y);
            float angleAbs = Math.abs(angle);
            final float PI_8TH = Mth.PI / 8F;
            // left/right
            if (angleAbs >= PI_8TH && angleAbs <= Mth.PI - PI_8TH) {
                digital.x = Math.signum(angle);
            }
            // forward/back
            if (angleAbs < Mth.HALF_PI - PI_8TH) {
                digital.y = 1F;
            } else if (angleAbs > Mth.HALF_PI + PI_8TH) {
                digital.y = -1F;
            }
        }
        return digital;
    }

}
