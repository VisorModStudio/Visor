package me.phoenixra.visor.core.client.input.actionset.game.mouse;

import me.phoenixra.atumvr.core.enums.XRInteractionProfile;
import me.phoenixra.atumvr.core.input.action.profileset.types.ValveIndexSet;
import me.phoenixra.visor.api.client.input.InputHelper;
import me.phoenixra.visor.api.client.input.action.BindingPath;
import me.phoenixra.visor.api.client.input.action.VisorActionSet;
import me.phoenixra.visor.api.client.input.action.types.VisorActionButton;
import me.phoenixra.visor.api.common.ControllerHand;
import me.phoenixra.visor.core.client.ClientContext;

import java.util.Map;

public class ActionLeftMouse extends VisorActionButton {
    public static final String ID = "mouse_left";

    public ActionLeftMouse(VisorActionSet actionSet) {
        super(actionSet, ID);
    }


    @Override
    protected void onPress() {
        InputHelper.pressMouse(0);
        ClientContext.inputHandler
                .triggerHapticPulseClick(
                        ControllerHand.MAIN
                );
    }

    @Override
    protected void onRelease() {
        InputHelper.releaseMouse(0);
    }



    @Override
    protected Map<XRInteractionProfile, BindingPath> loadDefaults() {
        return Map.of(
                XRInteractionProfile.VALVE_INDEX,
                new BindingPath(
                        ValveIndexSet.BUTTON_TRIGGER_RIGHT,
                        ValveIndexSet.BUTTON_TRIGGER_LEFT
                )
        );
    }
}
