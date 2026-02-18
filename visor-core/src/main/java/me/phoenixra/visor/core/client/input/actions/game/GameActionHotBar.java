package me.phoenixra.visor.core.client.input.actions.game;


import lombok.Getter;
import me.phoenixra.atumvr.api.input.profile.VRInteractionProfileType;
import me.phoenixra.atumvr.api.input.profile.types.OculusTouchProfile;
import me.phoenixra.atumvr.api.input.profile.types.ValveIndexProfile;
import me.phoenixra.visor.api.client.input.action.ActionBinding;
import me.phoenixra.visor.api.client.input.action.VisorActionSet;
import me.phoenixra.visor.api.client.input.action.framework.VisorActionButton;
import me.phoenixra.visor.core.client.tasks.types.TaskHotBar;
import org.jetbrains.annotations.NotNull;


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
    public @NotNull Map<VRInteractionProfileType, ActionBinding> getDefaultBindings() {
        return Map.of(
                VRInteractionProfileType.VALVE_INDEX,
                new ActionBinding(
                        ValveIndexProfile.BUTTON_TRACKPAD_FORCE_RIGHT,
                        ValveIndexProfile.BUTTON_TRACKPAD_FORCE_LEFT
                ),
                VRInteractionProfileType.OCULUS_TOUCH,
                new ActionBinding(
                        OculusTouchProfile.BUTTON_GRIP_RIGHT,
                        OculusTouchProfile.BUTTON_GRIP_LEFT
                )
        );
    }
}
