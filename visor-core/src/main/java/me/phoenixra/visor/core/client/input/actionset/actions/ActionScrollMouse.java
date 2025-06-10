package me.phoenixra.visor.core.client.input.actionset.actions;

import me.phoenixra.atumvr.api.input.action.VRActionDataVec2;
import me.phoenixra.atumvr.core.enums.XRInteractionProfile;
import me.phoenixra.atumvr.core.input.action.profileset.OpenXRProfileSet;
import me.phoenixra.atumvr.core.input.action.profileset.types.ValveIndexSet;
import me.phoenixra.visor.api.client.ClientFeature;
import me.phoenixra.visor.api.client.gui.overlay.VROverlay;
import me.phoenixra.visor.api.client.input.InputHelper;
import me.phoenixra.visor.api.client.input.action.BindingPath;
import me.phoenixra.visor.api.client.input.action.VisorActionSet;
import me.phoenixra.visor.api.client.input.action.types.VisorActionVec2;
import me.phoenixra.visor.api.common.ControllerHand;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.gui.overlays.builtin.VROverlayGameScreen;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;

import java.util.Map;

import static me.phoenixra.visor.core.client.VisorClientImpl.MC;

public class ActionScrollMouse extends VisorActionVec2 {
    public static final String ID = "mouse_scroll";

    private double deltaSaved;

    public ActionScrollMouse(VisorActionSet actionSet) {
        super(actionSet, ID);
    }


    @Override
    protected void onStateChanged(Vector2f newState) {
        if(!ClientContext.visor.isFeatureEnabled(ClientFeature.INPUT_VR_MOUSE)){
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

    private void doScroll(double scrollDelta){
        VROverlay focusedOverlay = ClientContext.cursorHandler.getFocusedOverlay();
        if (focusedOverlay == null) {
            return;
        }
        if(MC.screen != null
                && focusedOverlay.getId().equals(VROverlayGameScreen.ID)){
            InputHelper.scrollMouse(0, scrollDelta);
            return;
        }
        boolean discrete = MC.options.discreteMouseScroll().get();
        double wheelSensitivity = MC.options.mouseWheelSensitivity().get();

        scrollDelta = (
                discrete
                        ? Math.signum(scrollDelta)
                        : scrollDelta
        ) * wheelSensitivity;


        focusedOverlay.mouseScrolled(
                focusedOverlay.getMouseX(), focusedOverlay.getMouseY(),
                scrollDelta
        );
    }


    @Override
    protected VRActionDataVec2 getVec2Data(@NotNull BindingPath bindingPath,
                                           @NotNull OpenXRProfileSet currentProfile, boolean leftHanded) {
        boolean mainHand;

        if(!ClientContext.cursorHandler.isFocused()
                && MC.screen == null && MC.player != null){
            mainHand = ClientContext.player.getActiveHand() == ControllerHand.MAIN;
        }else {
            var cursorHand = ClientContext.cursorHandler.getActiveCursorHand();
            mainHand = cursorHand == ControllerHand.MAIN;
        }

        //Here we change leftHanded parameter for method call,
        //to match used hand
        if(leftHanded){
            //left hand as main (leftHanded parameter = true)
            return bindingPath.getVec2(currentProfile, mainHand);
        }else{
            //right hand as main (leftHanded parameter = false)
            return bindingPath.getVec2(currentProfile, !mainHand);
        }
    }

    @Override
    protected Map<XRInteractionProfile, BindingPath> loadDefaults() {
        return Map.of(
                XRInteractionProfile.VALVE_INDEX,
                new BindingPath(
                        ValveIndexSet.VEC2_THUMBSTICK_RIGHT,
                        ValveIndexSet.VEC2_THUMBSTICK_LEFT
                )
        );
    }
}
