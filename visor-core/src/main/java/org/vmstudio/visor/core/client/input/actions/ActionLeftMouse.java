package org.vmstudio.visor.core.client.input.actions;

import lombok.Getter;
import me.phoenixra.atumvr.api.input.profile.VRInteractionProfileType;
import me.phoenixra.atumvr.api.input.profile.types.*;
import org.vmstudio.visor.api.client.input.action.ActionBinding;
import org.vmstudio.visor.api.client.input.action.VRActionSet;
import org.vmstudio.visor.api.client.input.action.framework.VRActionButton;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.input.mouse.MouseClickHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

public class ActionLeftMouse extends VRActionButton {
    public static final String ID_MAIN = "mouse_left_main";
    public static final String ID_OFFHAND = "mouse_left_offhand";

    private final MouseClickHandler handler = MouseClickHandler.LEFT_HANDLER;

    @Getter
    private final HandType handType;

    public ActionLeftMouse(@NotNull VRActionSet actionSet,
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

        handler.preTick();

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
    public void forcePress() {
        super.forcePress();
        if(handType == HandType.MAIN) {
            handler.setForcedMain(true);
        }else{
            handler.setForcedOffhand(true);
        }
    }

    @Override
    public void forceRelease() {
        super.forceRelease();
        if(handType == HandType.MAIN) {
            handler.setForcedMain(false);
        }else{
            handler.setForcedOffhand(false);
        }
    }

    @Override
    public boolean isCommon() {
        return true;
    }

    @Override
    public boolean isRequired() {
        return true;
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
                    ),
                    VRInteractionProfileType.VIVE,
                    new ActionBinding(
                            ViveProfile.BUTTON_TRIGGER_RIGHT,
                            ViveProfile.BUTTON_TRIGGER_LEFT
                    ),
                    VRInteractionProfileType.VIVE_COSMOS,
                    new ActionBinding(
                            ViveCosmosProfile.BUTTON_TRIGGER_RIGHT,
                            ViveCosmosProfile.BUTTON_TRIGGER_LEFT
                    ),
                    VRInteractionProfileType.HP_MIXED_REALITY,
                    new ActionBinding(
                            HpMixedRealityProfile.BUTTON_TRIGGER_RIGHT,
                            HpMixedRealityProfile.BUTTON_TRIGGER_LEFT
                    ),
                    VRInteractionProfileType.WINDOWS_MOTION,
                    new ActionBinding(
                            WindowsMotionProfile.BUTTON_TRIGGER_RIGHT,
                            WindowsMotionProfile.BUTTON_TRIGGER_LEFT
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
                    ),
                    VRInteractionProfileType.VIVE,
                    new ActionBinding(
                            ViveProfile.BUTTON_TRIGGER_LEFT,
                            ViveProfile.BUTTON_TRIGGER_RIGHT
                    ),
                    VRInteractionProfileType.VIVE_COSMOS,
                    new ActionBinding(
                            ViveCosmosProfile.BUTTON_TRIGGER_LEFT,
                            ViveCosmosProfile.BUTTON_TRIGGER_RIGHT
                    ),
                    VRInteractionProfileType.HP_MIXED_REALITY,
                    new ActionBinding(
                            HpMixedRealityProfile.BUTTON_TRIGGER_LEFT,
                            HpMixedRealityProfile.BUTTON_TRIGGER_RIGHT
                    ),
                    VRInteractionProfileType.WINDOWS_MOTION,
                    new ActionBinding(
                            WindowsMotionProfile.BUTTON_TRIGGER_LEFT,
                            WindowsMotionProfile.BUTTON_TRIGGER_RIGHT
                    )
            );
        }
    }
}