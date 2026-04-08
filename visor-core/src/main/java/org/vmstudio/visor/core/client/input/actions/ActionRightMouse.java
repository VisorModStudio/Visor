package org.vmstudio.visor.core.client.input.actions;

import me.phoenixra.atumvr.api.input.profile.VRInteractionProfileType;
import me.phoenixra.atumvr.api.input.profile.types.OculusTouchProfile;
import me.phoenixra.atumvr.api.input.profile.types.ValveIndexProfile;
import org.vmstudio.visor.api.client.input.action.ActionBinding;
import org.vmstudio.visor.api.client.input.action.VRActionSet;
import org.vmstudio.visor.api.client.input.action.framework.VRActionButton;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.input.mouse.MouseClickHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

public class ActionRightMouse extends VRActionButton {
    public static final String ID_MAIN = "mouse_right_main";
    public static final String ID_OFFHAND = "mouse_right_offhand";

    private final MouseClickHandler handler = MouseClickHandler.RIGHT_HANDLER;

    private final HandType handType;

    public ActionRightMouse(@NotNull VRActionSet actionSet,
                            @NotNull HandType handType) {
        super(actionSet, handType == HandType.MAIN ? ID_MAIN : ID_OFFHAND);
        this.handType = handType;
    }

    @Override
    public void preTick() {
        handler.updateState(handType,
                pressDelayed || (pressed && !releaseDelayed),
                (pressDelayed && !pressed) || (releaseDelayed && pressed)
        );

        if (handType == HandType.MAIN) {
            handler.preTick();
        }

        super.preTick();
    }

    @Override
    protected void onPress() {
        handler.onPress(handType);
    }

    @Override
    protected void onRelease() {
        handler.onRelease(handType);
    }

    @Override
    protected void onClear() {
        handler.onClear();
    }

    @Override
    public @NotNull Map<VRInteractionProfileType, ActionBinding> getDefaultBindings() {
        if (getId().equals(ID_MAIN)) {
            return Map.of(
                    VRInteractionProfileType.VALVE_INDEX,
                    new ActionBinding(
                            ValveIndexProfile.BUTTON_B_RIGHT,
                            ValveIndexProfile.BUTTON_B_LEFT
                    ),
                    VRInteractionProfileType.OCULUS_TOUCH,
                    new ActionBinding(
                            OculusTouchProfile.BUTTON_A,
                            OculusTouchProfile.BUTTON_X
                    )
            );
        } else {
            return Map.of(
                    VRInteractionProfileType.VALVE_INDEX,
                    new ActionBinding(
                            ValveIndexProfile.BUTTON_B_LEFT,
                            ValveIndexProfile.BUTTON_B_RIGHT
                    ),
                    VRInteractionProfileType.OCULUS_TOUCH,
                    new ActionBinding(
                            OculusTouchProfile.BUTTON_X,
                            OculusTouchProfile.BUTTON_A
                    )
            );
        }
    }
}