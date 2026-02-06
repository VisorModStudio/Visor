package me.phoenixra.visor.core.client.input.actions;

import lombok.Getter;
import me.phoenixra.atumvr.api.input.action.data.VRActionDataVec2;
import me.phoenixra.atumvr.core.input.profile.XRInteractionProfile;
import me.phoenixra.atumvr.api.input.profile.VRInteractionProfileType;
import me.phoenixra.atumvr.api.input.profile.types.OculusTouchProfile;
import me.phoenixra.atumvr.api.input.profile.types.ValveIndexProfile;
import me.phoenixra.visor.api.client.ClientFeature;
import me.phoenixra.visor.api.client.gui.overlays.VROverlay;
import me.phoenixra.visor.api.client.input.action.ActionBinding;
import me.phoenixra.visor.api.client.input.action.VisorActionSet;
import me.phoenixra.visor.api.client.input.action.framework.VisorActionVec2;
import me.phoenixra.visor.api.common.HandType;
import me.phoenixra.visor.core.client.ClientContext;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;

import java.util.Map;

import static me.phoenixra.visor.core.client.VisorClientImpl.MC;

public class ActionScrollMouse extends VisorActionVec2 {
    public static final String ID = "mouse_scroll";

    @Getter
    private final boolean required = true;


    private double deltaSaved;


    public ActionScrollMouse(VisorActionSet actionSet) {
        super(actionSet, ID);
    }


    @Override
    public void preTick() {
        onStateChanged(getState());
    }

    @Override
    protected void onStateChanged(Vector2f newState) {
        if(!ClientContext.visor.isFeatureEnabled(ClientFeature.INPUT_MOUSE)){
            return;
        }
        float scrollPos = newState.y;
        if (Math.abs(scrollPos) < 1) {
            if ((deltaSaved > 0 && scrollPos < 0)
                    || (deltaSaved < 0 && scrollPos > 0)) {
                deltaSaved = 0;
            }
            deltaSaved += scrollPos;
            if (Math.abs(deltaSaved) < 1) {
                return;
            }
            doScroll(deltaSaved);
            deltaSaved = 0;
            return;
        }
        deltaSaved = 0;
        doScroll(scrollPos);

    }

    private void doScroll(double scrollOffset){
        VROverlay focusedOverlay = ClientContext.cursorHandler.getFocusedOverlay();
        if (focusedOverlay == null) {
            return;
        }
        boolean discrete = MC.options.discreteMouseScroll().get();
        double wheelSensitivity = MC.options.mouseWheelSensitivity().get();
        double scrollDelta = (
                discrete
                        ? Math.signum(scrollOffset)
                        : scrollOffset
        ) * wheelSensitivity;

        if(scrollDelta == 0){
            return;
        }

        focusedOverlay.mouseScrolled(
                focusedOverlay.getMouseX(), focusedOverlay.getMouseY(),
                scrollDelta
        );
    }


    @Override
    protected VRActionDataVec2 getVec2Data(@NotNull ActionBinding actionBinding,
                                           @NotNull XRInteractionProfile currentProfile, boolean leftHanded) {
        boolean mainHand;

        if(!ClientContext.cursorHandler.isCursorHandFocused()
                && MC.screen == null && MC.player != null){
            mainHand = ClientContext.localPlayer.getActiveHand() == HandType.MAIN;
        }else {
            var cursorHand = ClientContext.cursorHandler.getCursorHand();
            mainHand = cursorHand == HandType.MAIN;
        }

        //Here we change leftHanded parameter for method call,
        //to match used hand
        if(leftHanded){
            //left hand as main (leftHanded parameter = true)
            return actionBinding.getVec2(currentProfile, mainHand);
        }else{
            //right hand as main (leftHanded parameter = false)
            return actionBinding.getVec2(currentProfile, !mainHand);
        }
    }

    @Override
    protected Map<VRInteractionProfileType, ActionBinding> loadDefaults() {
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
    }
}
