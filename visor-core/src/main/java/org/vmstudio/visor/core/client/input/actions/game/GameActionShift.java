package org.vmstudio.visor.core.client.input.actions.game;

import me.phoenixra.atumvr.api.input.profile.VRInteractionProfileType;
import me.phoenixra.atumvr.api.input.profile.types.OculusTouchProfile;
import me.phoenixra.atumvr.api.input.profile.types.ValveIndexProfile;
import org.vmstudio.visor.api.client.input.InputHelper;
import org.vmstudio.visor.api.client.input.action.ActionBinding;
import org.vmstudio.visor.api.client.input.action.VisorActionSet;
import org.vmstudio.visor.api.client.input.action.framework.VisorActionButton;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.Map;

public class GameActionShift extends VisorActionButton {
    public static final String ID = "shift";



    public GameActionShift(VisorActionSet actionSet) {
        super(actionSet, ID);
    }

    @Override
    protected void onPress() {
        InputHelper.pressKey(GLFW.GLFW_KEY_LEFT_SHIFT);
    }

    @Override
    protected void onRelease() {
        InputHelper.releaseKey(GLFW.GLFW_KEY_LEFT_SHIFT);
    }

    @Override
    public @NotNull Map<VRInteractionProfileType, ActionBinding> getDefaultBindings() {
        return Map.of(
                VRInteractionProfileType.VALVE_INDEX,
                new ActionBinding(
                        ValveIndexProfile.BUTTON_THUMBSTICK_RIGHT,
                        ValveIndexProfile.BUTTON_THUMBSTICK_LEFT
                ),
                VRInteractionProfileType.OCULUS_TOUCH,
                new ActionBinding(
                        OculusTouchProfile.BUTTON_THUMBSTICK_RIGHT,
                        OculusTouchProfile.BUTTON_THUMBSTICK_LEFT
                )
        );
    }


}
