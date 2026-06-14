package org.vmstudio.visor.core.client.input.actions;

import lombok.Getter;
import me.phoenixra.atumvr.api.input.profile.VRInteractionProfileType;
import me.phoenixra.atumvr.api.input.profile.types.OculusTouchProfile;
import me.phoenixra.atumvr.api.input.profile.types.ValveIndexProfile;
import org.vmstudio.visor.api.client.input.action.ActionBinding;
import org.vmstudio.visor.api.client.input.action.VRActionSet;
import org.vmstudio.visor.api.client.input.action.framework.VRActionVec2;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.core.client.input.mouse.MouseScrollHandler;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;

import java.util.Map;

public class ActionScrollMouse extends VRActionVec2 {
    public static final String ID_MAIN = "mouse_scroll_main";
    public static final String ID_OFFHAND = "mouse_scroll_offhand";


    private final MouseScrollHandler handler = MouseScrollHandler.INSTANCE;

    @Getter
    private final HandType handType;

    public ActionScrollMouse(@NotNull VRActionSet actionSet,
                             @NotNull HandType handType) {
        super(actionSet, handType == HandType.MAIN ? ID_MAIN : ID_OFFHAND);
        this.handType = handType;
    }


    @Override
    public void preTick() {
        onStateChanged(getState());
        handler.tick();
    }

    @Override
    protected void onStateChanged(@NotNull Vector2f newState) {
        handler.updateState(handType, newState);
    }

    @Override
    public boolean isCommon() {
        return true;
    }

    @Override
    public @NotNull Map<VRInteractionProfileType, ActionBinding> getDefaultBindings() {
        if(getId().equals(ID_MAIN)){
            return Map.of(
                    VRInteractionProfileType.VALVE_INDEX,
                    new ActionBinding(
                            ValveIndexProfile.VEC2_THUMBSTICK_RIGHT,
                            ValveIndexProfile.VEC2_THUMBSTICK_LEFT
                    ),
                    VRInteractionProfileType.OCULUS_TOUCH,
                    new ActionBinding(
                            OculusTouchProfile.VEC2_THUMBSTICK_RIGHT,
                            OculusTouchProfile.VEC2_THUMBSTICK_LEFT
                    )
            );
        }else{
           return Map.of(
                    VRInteractionProfileType.VALVE_INDEX,
                    new ActionBinding(
                            ValveIndexProfile.VEC2_THUMBSTICK_LEFT,
                            ValveIndexProfile.VEC2_THUMBSTICK_RIGHT
                    ),
                    VRInteractionProfileType.OCULUS_TOUCH,
                    new ActionBinding(
                            OculusTouchProfile.VEC2_THUMBSTICK_LEFT,
                            OculusTouchProfile.VEC2_THUMBSTICK_RIGHT
                    )
            );
        }
    }
}
