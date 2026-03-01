package org.vmstudio.visor.core.client.input.actions;

import me.phoenixra.atumvr.api.input.profile.VRInteractionProfileType;
import me.phoenixra.atumvr.api.input.profile.types.OculusTouchProfile;
import me.phoenixra.atumvr.api.input.profile.types.ValveIndexProfile;
import org.vmstudio.visor.api.client.input.action.ActionBinding;
import org.vmstudio.visor.api.client.input.action.VisorActionSet;
import org.vmstudio.visor.api.client.input.action.framework.VisorActionButton;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.input.mouse.MouseClickHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

public class ActionLeftMouse extends VisorActionButton {
    public static final String ID_MAIN = "mouse_left_main";
    public static final String ID_OFFHAND = "mouse_left_offhand";

    private final MouseClickHandler handler = MouseClickHandler.LEFT_HANDLER;

    private final HandType handType;

    public ActionLeftMouse(@NotNull VisorActionSet actionSet,
                           @NotNull HandType handType) {
        super(actionSet, handType == HandType.MAIN ? ID_MAIN : ID_OFFHAND);
        this.handType = handType;
    }

    @Override
    public void preTick() {
        handler.updateState(handType, pressed, changed);

        if (handType == HandType.MAIN) {
            handler.preTick();
        }

        super.preTick();
    }

    @Override
    protected void onPress() {
        HandType activeHand;
        if (!ClientContext.cursorHandler.isCursorHandFocused()
                && MC.screen == null && MC.player != null) {
            activeHand = ClientContext.localPlayer.getActiveHand();
        } else {
            activeHand = ClientContext.cursorHandler.getCursorHand();
        }
        if (handType != activeHand) {
            return;
        }
        handler.onPress(handType);
    }

    @Override
    protected void onRelease() {
        HandType activeHand;
        if (!ClientContext.cursorHandler.isCursorHandFocused()
                && MC.screen == null && MC.player != null) {
            activeHand = ClientContext.localPlayer.getActiveHand();
        } else {
            activeHand = ClientContext.cursorHandler.getCursorHand();
        }
        if (handType != activeHand) {
            return;
        }
        handler.onRelease();
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
                            ValveIndexProfile.BUTTON_TRIGGER_RIGHT,
                            ValveIndexProfile.BUTTON_TRIGGER_LEFT
                    ),
                    VRInteractionProfileType.OCULUS_TOUCH,
                    new ActionBinding(
                            OculusTouchProfile.BUTTON_TRIGGER_RIGHT,
                            OculusTouchProfile.BUTTON_TRIGGER_LEFT
                    )
            );
        } else {
            return Map.of(
                    VRInteractionProfileType.VALVE_INDEX,
                    new ActionBinding(
                            ValveIndexProfile.BUTTON_TRIGGER_LEFT,
                            ValveIndexProfile.BUTTON_TRIGGER_RIGHT
                    ),
                    VRInteractionProfileType.OCULUS_TOUCH,
                    new ActionBinding(
                            OculusTouchProfile.BUTTON_TRIGGER_LEFT,
                            OculusTouchProfile.BUTTON_TRIGGER_RIGHT
                    )
            );
        }
    }
}