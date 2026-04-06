package org.vmstudio.visor.core.client.input.actions.game;

import me.phoenixra.atumvr.core.input.profile.XRInteractionProfile;
import me.phoenixra.atumvr.api.input.profile.VRInteractionProfileType;
import me.phoenixra.atumvr.api.input.profile.types.OculusTouchProfile;
import me.phoenixra.atumvr.api.input.profile.types.ValveIndexProfile;
import org.vmstudio.visor.api.client.ClientFeature;
import org.vmstudio.visor.api.client.input.action.ActionBinding;
import org.vmstudio.visor.api.client.input.action.VRActionSet;
import org.vmstudio.visor.api.client.input.action.framework.VRActionVec2;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.settings.VRClientSettings;
import org.vmstudio.visor.core.client.settings.options.enums.MovementMode;
import org.vmstudio.visor.core.client.tasks.types.movement.TaskTeleport;
import org.vmstudio.visor.core.client.utils.ClientUtils;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;

import java.util.Map;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

public class GameActionMovement extends VRActionVec2 {
    public static final String ID = "movement";




    private boolean wasMovement;
    private boolean wasAutoSprinting;

    private static HandType handType = HandType.OFFHAND;

    public GameActionMovement(VRActionSet actionSet) {
        super(actionSet, ID);
    }

    @Override
    public void preTick() {
        super.preTick();


        if(ClientContext.cursorHandler.isHandFocused(handType)
                || ClientContext.visor.isFeatureDisabled(ClientFeature.INPUT_MOVEMENT)){
            onClear();
            return;
        }

        Vector2f rawMove = getState();

        Vector2f movement = ClientContext.localPlayer.getMovement();

        if(VRClientSettings.getMoveMode(MC.player) == MovementMode.TELEPORT){
            resetMovementState();
            movement.set(rawMove);
            TaskTeleport.getInstance().updateInputState(
                    handType,
                    rawMove
            );
            return;
        }


        boolean climbing = ClientContext.localPlayer.isClimbing();
        boolean moving = ClientContext.localPlayer.isMoving();
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

            var digital = toDigital(movement, 0.5f);


            if (moving) {
                ClientUtils.updateKeyMappingState(
                        MC.options.keyUp, digital.y > 0
                );
                ClientUtils.updateKeyMappingState(
                        MC.options.keyDown, digital.y < 0
                );
                ClientUtils.updateKeyMappingState(
                        MC.options.keyRight, digital.x > 0
                );
                ClientUtils.updateKeyMappingState(
                        MC.options.keyLeft, digital.x < 0
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
        }else {
            moving = false;
        }

        if (!moving && this.wasMovement) {
            ClientUtils.updateKeyMappingState(MC.options.keyUp, false);
            ClientUtils.updateKeyMappingState(MC.options.keyDown, false);
            ClientUtils.updateKeyMappingState(MC.options.keyLeft, false);
            ClientUtils.updateKeyMappingState(MC.options.keyRight, false);
        }
        this.wasMovement = moving;
        ClientContext.localPlayer.setMoving(moving);
        if (this.wasAutoSprinting && forward < VRClientSettings.getSprintThreshold()) {
            MC.player.setSprinting(false);
            this.wasAutoSprinting = false;
        }
    }

    @Override
    public void updateState(@NotNull XRInteractionProfile currentProfile, boolean leftHanded) {
        super.updateState(currentProfile, leftHanded);
        ActionBinding actionBinding = bindings.get(currentProfile.getType());
        handType = null;
        if(actionBinding == null){
            return;
        }

        var vec2Data = getVec2Data(
                actionBinding,
                currentProfile,
                leftHanded
        );
        if(vec2Data == null){
            return;
        }
        if(leftHanded) {
            handType = vec2Data.getId().isControllerLeft()
                    ? HandType.MAIN
                    : HandType.OFFHAND;
        }else{
            handType = vec2Data.getId().isControllerLeft()
                    ? HandType.OFFHAND
                    : HandType.MAIN;
        }
    }

    @Override
    protected void onStateChanged(@NotNull Vector2f newState) {

    }

    @Override
    protected void onClear() {
        Vector2f input = ClientContext.localPlayer.getMovement();

        input.x = 0;
        input.y = 0;
        resetMovementState();

    }

    @Override
    public @NotNull Map<VRInteractionProfileType, ActionBinding> getDefaultBindings() {
        return Map.of(
                VRInteractionProfileType.VALVE_INDEX,
                new ActionBinding(
                        ValveIndexProfile.VEC2_THUMBSTICK_LEFT,
                        ValveIndexProfile.VEC2_THUMBSTICK_RIGHT
                ),
                VRInteractionProfileType.OCULUS_TOUCH,
                new ActionBinding(
                        OculusTouchProfile.VEC2_THUMBSTICK_LEFT,
                        OculusTouchProfile.VEC2_THUMBSTICK_RIGHT
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

    private void resetMovementState() {
        ClientUtils.updateKeyMappingState(MC.options.keyUp, false);
        ClientUtils.updateKeyMappingState(MC.options.keyDown, false);
        ClientUtils.updateKeyMappingState(MC.options.keyLeft, false);
        ClientUtils.updateKeyMappingState(MC.options.keyRight, false);

        this.wasMovement = false;

        if (this.wasAutoSprinting && MC.player != null) {
            MC.player.setSprinting(false);
        }
        this.wasAutoSprinting = false;
        ClientContext.localPlayer.setMoving(false);
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
