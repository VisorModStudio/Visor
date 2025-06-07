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

public class ActionRightMouse extends VisorActionButton {
    public static final String ID = "mouse_right";

    public ActionRightMouse(VisorActionSet actionSet) {
        super(actionSet, ID);
    }


    @Override
    protected void onPress() {
        InputHelper.pressMouse(1);
    }

    @Override
    protected void onRelease() {
        InputHelper.releaseMouse(1);
    }



    @Override
    protected Map<XRInteractionProfile, BindingPath> loadDefaults() {
        return Map.of(
                XRInteractionProfile.VALVE_INDEX,
                new BindingPath(
                        ValveIndexSet.BUTTON_B_RIGHT,
                        ValveIndexSet.BUTTON_B_LEFT
                )
        );
    }
}
