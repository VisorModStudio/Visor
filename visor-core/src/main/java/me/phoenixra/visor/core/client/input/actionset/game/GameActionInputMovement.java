package me.phoenixra.visor.core.client.input.actionset.game;

import me.phoenixra.atumvr.core.enums.XRInteractionProfile;
import me.phoenixra.atumvr.core.input.action.profileset.types.OculusTouchSet;
import me.phoenixra.atumvr.core.input.action.profileset.types.ValveIndexSet;
import me.phoenixra.visor.api.client.ClientFeature;
import me.phoenixra.visor.api.client.input.action.BindingPath;
import me.phoenixra.visor.api.client.input.action.VisorActionSet;
import me.phoenixra.visor.api.client.input.action.framework.VisorActionVec2;
import me.phoenixra.visor.api.common.ControllerHand;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.settings.VRClientSettings;
import me.phoenixra.visor.core.client.tasks.types.movement.TaskRoomSneak;
import me.phoenixra.visor.core.client.utils.ClientUtils;
import net.minecraft.client.player.Input;
import org.joml.Vector2f;

import java.util.Map;

import static me.phoenixra.visor.core.client.VisorClientImpl.MC;

public class GameActionInputMovement extends VisorActionVec2 {
    public static final String ID = "input_movement";


    private boolean movedLastTick, autoSprintActive;


    public GameActionInputMovement(VisorActionSet actionSet) {
        super(actionSet, ID);
    }

    @Override
    public void preTick() {
        super.preTick();
        if(ClientContext.cursorHandler.isHandFocused(ControllerHand.OFFHAND)){
            onClear();
            return;
        }
        Vector2f newState = getState();

        Vector2f input = ClientContext.player.getInputMovement();

        input.x = newState.x;
        input.y = newState.y;


    }

    @Override
    protected void onStateChanged(Vector2f newState) {

    }

    @Override
    protected void onClear() {
        Vector2f input = ClientContext.player.getInputMovement();

        input.x = 0;
        input.y = 0;

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
}
