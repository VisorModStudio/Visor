package org.vmstudio.visor.core.client.input.actions.game;


import me.phoenixra.atumvr.api.input.profile.VRInteractionProfileType;
import me.phoenixra.atumvr.api.input.profile.types.OculusTouchProfile;
import me.phoenixra.atumvr.api.input.profile.types.ValveIndexProfile;
import org.vmstudio.visor.api.client.input.action.ActionBinding;
import org.vmstudio.visor.api.client.input.action.VRActionSet;
import org.vmstudio.visor.api.client.input.action.framework.VRActionButton;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.core.client.tasks.types.TaskHotBar;
import org.jetbrains.annotations.NotNull;


import java.util.Map;

public class GameActionHotBar extends VRActionButton {
    public static final String ID_MAIN = "hotbar_main";
    public static final String ID_OFFHAND = "hotbar_offhand";

    private final HandType handType;
    public GameActionHotBar(@NotNull VRActionSet actionSet,
                            @NotNull HandType handType) {
        super(actionSet, handType == HandType.MAIN ? ID_MAIN : ID_OFFHAND);
        this.handType = handType;
    }


    @Override
    protected void onPress() {
        if(handType == HandType.MAIN) {
            TaskHotBar.getInstance().setInputPressedMain(true);
        }else{
            TaskHotBar.getInstance().setInputPressedOffhand(true);
        }
    }

    @Override
    protected void onRelease() {
        if(handType == HandType.MAIN) {
            TaskHotBar.getInstance().setInputPressedMain(false);
        }else{
            TaskHotBar.getInstance().setInputPressedOffhand(false);
        }
    }



    @Override
    public @NotNull Map<VRInteractionProfileType, ActionBinding> getDefaultBindings() {
        if (getId().equals(ID_MAIN)) {
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
        }else{
            return Map.of(
                    VRInteractionProfileType.VALVE_INDEX,
                    new ActionBinding(
                            ValveIndexProfile.BUTTON_TRACKPAD_FORCE_LEFT,
                            ValveIndexProfile.BUTTON_TRACKPAD_FORCE_RIGHT
                    ),
                    VRInteractionProfileType.OCULUS_TOUCH,
                    new ActionBinding(
                            OculusTouchProfile.BUTTON_GRIP_LEFT,
                            OculusTouchProfile.BUTTON_GRIP_RIGHT
                    )
            );
        }
    }
}
