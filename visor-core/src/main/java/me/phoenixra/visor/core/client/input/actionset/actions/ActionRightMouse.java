package me.phoenixra.visor.core.client.input.actionset.actions;

import me.phoenixra.atumvr.api.input.action.VRActionDataButton;
import me.phoenixra.atumvr.core.enums.XRInteractionProfile;
import me.phoenixra.atumvr.core.input.action.profileset.OpenXRProfileSet;
import me.phoenixra.atumvr.core.input.action.profileset.types.OculusTouchSet;
import me.phoenixra.atumvr.core.input.action.profileset.types.ValveIndexSet;
import me.phoenixra.visor.api.client.gui.overlay.VROverlay;
import me.phoenixra.visor.api.client.gui.overlay.framework.VROverlayScreen;
import me.phoenixra.visor.api.client.input.InputHelper;
import me.phoenixra.visor.api.client.input.action.BindingPath;
import me.phoenixra.visor.api.client.input.action.VisorActionSet;
import me.phoenixra.visor.api.client.input.action.framework.VisorActionButton;
import me.phoenixra.visor.api.common.ControllerHand;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.gui.overlays.builtin.VROverlayGameScreen;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static me.phoenixra.visor.core.client.VisorClientImpl.MC;

public class ActionRightMouse extends VisorActionButton {
    public static final String ID = "mouse_right";

    private static final int BUTTON_TYPE = 1;


    private VROverlay previousFocus;
    private boolean wasPressed;

    public ActionRightMouse(VisorActionSet actionSet) {
        super(actionSet, ID);
    }

    @Override
    public void preTick() {
        VROverlay focusedOverlay = ClientContext.cursorHandler.getFocusedOverlay();

        // --- Cleanup Clicks ---
        if(focusedOverlay == null
                && previousFocus != null
                && wasPressed){
            previousFocus.mouseReleased(
                    previousFocus.getMouseX(),
                    previousFocus.getMouseY(),
                    BUTTON_TYPE
            );
            if(previousFocus instanceof VROverlayScreen overlayScreen){
                overlayScreen.finishDragMouse();
            }
            wasPressed = false;
        }else if(focusedOverlay != null
                && previousFocus != null
                && focusedOverlay != previousFocus
                && wasPressed){
            previousFocus.mouseReleased(
                    previousFocus.getMouseX(),
                    previousFocus.getMouseY(),
                    BUTTON_TYPE
            );
            if(previousFocus instanceof VROverlayScreen overlayScreen){
                overlayScreen.finishDragMouse();
            }
            wasPressed = false;
        }
        previousFocus = focusedOverlay;

        super.preTick();


    }

    @Override
    protected void onPress() {
        process(true);
    }

    @Override
    protected void onRelease() {
        process(false);
    }

    @Override
    protected void onClear() {
        InputHelper.releaseMouse(BUTTON_TYPE);
        if(previousFocus != null
                && wasPressed){
            previousFocus.mouseReleased(
                    previousFocus.getMouseX(),
                    previousFocus.getMouseY(),
                    BUTTON_TYPE
            );

        }
        previousFocus = null;

        wasPressed = false;
    }

    private void process(boolean press){
        VROverlay focusedOverlay = ClientContext.cursorHandler.getFocusedOverlay();

        if(focusedOverlay != null){
            processOverlay(focusedOverlay, press);
            return;
        }
        if(MC.screen != null){
            return;
        }

        if(MC.player != null){
            processGame(press);
        }
    }

    private void processOverlay(VROverlay overlay, boolean press){

        if (press) {
            overlay.mouseClicked(
                    overlay.getMouseX(),overlay.getMouseY(),
                    BUTTON_TYPE
            );
            wasPressed = true;
        }else if(wasPressed){
            overlay.mouseReleased(
                    overlay.getMouseX(),overlay.getMouseY(),
                    BUTTON_TYPE
            );
            wasPressed = false;
        }
    }


    private void processGame(boolean press){
        if (press) {
            InputHelper.pressMouse(BUTTON_TYPE);
        } else {
            InputHelper.releaseMouse(BUTTON_TYPE);
        }
    }


    @Override
    protected VRActionDataButton getButtonData(@NotNull BindingPath bindingPath, @NotNull OpenXRProfileSet currentProfile, boolean leftHanded) {
        boolean mainHand;

        if(!ClientContext.cursorHandler.isCursorHandFocused()
                && MC.screen == null && MC.player != null){
            mainHand = ClientContext.player.getActiveHand() == ControllerHand.MAIN;
        }else {
            var cursorHand = ClientContext.cursorHandler.getCursorHand();
            mainHand = cursorHand == ControllerHand.MAIN;
        }

        //Here we change leftHanded parameter for method call,
        //to match used hand
        if(leftHanded){
            //left hand as main (leftHanded parameter = true)
            return bindingPath.getButton(currentProfile, mainHand);
        }else{
            //right hand as main (leftHanded parameter = false)
            return bindingPath.getButton(currentProfile, !mainHand);
        }
    }


    @Override
    protected Map<XRInteractionProfile, BindingPath> loadDefaults() {
        return Map.of(
                XRInteractionProfile.VALVE_INDEX,
                new BindingPath(
                        ValveIndexSet.BUTTON_B_RIGHT,
                        ValveIndexSet.BUTTON_B_LEFT
                ),
                XRInteractionProfile.OCULUS_TOUCH,
                new BindingPath(
                        OculusTouchSet.BUTTON_A,
                        OculusTouchSet.BUTTON_X
                )
        );
    }
}
