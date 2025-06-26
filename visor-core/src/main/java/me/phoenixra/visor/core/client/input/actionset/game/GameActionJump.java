package me.phoenixra.visor.core.client.input.actionset.game;

import me.phoenixra.atumvr.core.enums.XRInteractionProfile;
import me.phoenixra.atumvr.core.input.action.profileset.types.OculusTouchSet;
import me.phoenixra.atumvr.core.input.action.profileset.types.ValveIndexSet;
import me.phoenixra.visor.api.client.input.InputHelper;
import me.phoenixra.visor.api.client.input.action.BindingPath;
import me.phoenixra.visor.api.client.input.action.VisorActionSet;
import me.phoenixra.visor.api.client.input.action.framework.VisorActionButton;
import org.lwjgl.glfw.GLFW;

import java.util.Map;

public class GameActionJump extends VisorActionButton {
    public static final String ID = "jump";

    public GameActionJump(VisorActionSet actionSet) {
        super(actionSet, ID);
    }


    @Override
    protected void onPress() {
        InputHelper.pressKey(GLFW.GLFW_KEY_SPACE);
    }

    @Override
    protected void onRelease() {
        InputHelper.releaseKey(GLFW.GLFW_KEY_SPACE);
    }



    @Override
    protected Map<XRInteractionProfile, BindingPath> loadDefaults() {
        return Map.of(
                XRInteractionProfile.VALVE_INDEX,
                new BindingPath(
                        ValveIndexSet.BUTTON_A_RIGHT,
                        ValveIndexSet.BUTTON_A_LEFT
                ),
                XRInteractionProfile.OCULUS_TOUCH,
                new BindingPath(
                        OculusTouchSet.BUTTON_B,
                        OculusTouchSet.BUTTON_Y
                )
        );
    }

}
