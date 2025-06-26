package me.phoenixra.visor.core.client.input.actionset.game;

import me.phoenixra.atumvr.core.enums.XRInteractionProfile;
import me.phoenixra.atumvr.core.input.action.profileset.types.OculusTouchSet;
import me.phoenixra.atumvr.core.input.action.profileset.types.ValveIndexSet;

import me.phoenixra.visor.api.client.input.action.BindingPath;
import me.phoenixra.visor.api.client.input.action.VisorActionSet;
import me.phoenixra.visor.api.client.input.action.framework.VisorActionButton;
import me.phoenixra.visor.core.client.tasks.types.TaskHotBar;


import java.util.Map;

public class GameActionHotBar extends VisorActionButton {
    public static final String ID = "hotbar";

    public GameActionHotBar(VisorActionSet actionSet) {
        super(actionSet, ID);
    }


    @Override
    protected void onPress() {
        TaskHotBar.getInstance().setInputPressedMain(true);
    }

    @Override
    protected void onRelease() {
        TaskHotBar.getInstance().setInputPressedMain(false);
    }



    @Override
    protected Map<XRInteractionProfile, BindingPath> loadDefaults() {
        return Map.of(
                XRInteractionProfile.VALVE_INDEX,
                new BindingPath(
                        ValveIndexSet.BUTTON_TRACKPAD_FORCE_RIGHT,
                        ValveIndexSet.BUTTON_TRACKPAD_FORCE_LEFT
                ),
                XRInteractionProfile.OCULUS_TOUCH,
                new BindingPath(
                        OculusTouchSet.BUTTON_GRIP_RIGHT,
                        OculusTouchSet.BUTTON_GRIP_LEFT
                )
        );
    }
}
