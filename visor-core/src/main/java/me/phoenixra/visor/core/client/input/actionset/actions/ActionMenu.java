package me.phoenixra.visor.core.client.input.actionset.actions;

import me.phoenixra.atumvr.core.enums.XRInteractionProfile;
import me.phoenixra.atumvr.core.input.action.profileset.types.OculusTouchSet;
import me.phoenixra.atumvr.core.input.action.profileset.types.ValveIndexSet;
import me.phoenixra.visor.api.client.input.InputHelper;
import me.phoenixra.visor.api.client.input.action.BindingPath;
import me.phoenixra.visor.api.client.input.action.VisorActionSet;
import me.phoenixra.visor.api.client.input.action.framework.VisorActionButton;
import me.phoenixra.visor.core.client.gui.screens.GameMenuScreen;
import org.lwjgl.glfw.GLFW;

import java.util.Map;

import static me.phoenixra.visor.core.client.VisorClientImpl.MC;

public class ActionMenu extends VisorActionButton {
    public static final String ID = "menu";

    public ActionMenu(VisorActionSet actionSet) {
        super(actionSet, ID);
    }


    @Override
    protected void onPress() {
        if (MC.screen != null) {
            InputHelper.pressKey(GLFW.GLFW_KEY_ESCAPE);
            InputHelper.releaseKey(GLFW.GLFW_KEY_ESCAPE);
        } else {
            MC.setScreen(new GameMenuScreen());
        }
    }

    @Override
    protected void onRelease() {

    }


    @Override
    protected Map<XRInteractionProfile, BindingPath> loadDefaults() {
        return Map.of(
                XRInteractionProfile.VALVE_INDEX,
                new BindingPath(
                        ValveIndexSet.BUTTON_A_LEFT,
                        ValveIndexSet.BUTTON_A_RIGHT
                ),
                XRInteractionProfile.OCULUS_TOUCH,
                new BindingPath(
                        OculusTouchSet.BUTTON_Y,
                        OculusTouchSet.BUTTON_B
                )
        );
    }

}
