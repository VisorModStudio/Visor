package me.phoenixra.visor.core.client.input.actionset.actions;

import me.phoenixra.atumvr.api.input.action.VRActionDataButton;
import me.phoenixra.atumvr.core.enums.XRInteractionProfile;
import me.phoenixra.atumvr.core.input.action.profileset.OpenXRProfileSet;
import me.phoenixra.atumvr.core.input.action.profileset.types.ValveIndexSet;
import me.phoenixra.visor.api.client.ClientFeature;
import me.phoenixra.visor.api.client.gui.overlay.VROverlay;
import me.phoenixra.visor.api.client.input.InputHelper;
import me.phoenixra.visor.api.client.input.action.BindingPath;
import me.phoenixra.visor.api.client.input.action.VisorActionSet;
import me.phoenixra.visor.api.client.input.action.types.VisorActionButton;
import me.phoenixra.visor.api.common.ControllerHand;
import me.phoenixra.visor.core.client.ClientContext;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static me.phoenixra.visor.core.client.VisorClientImpl.MC;

public class ActionRightMouse extends VisorActionButton {
    public static final String ID = "mouse_right";

    private static final int BUTTON_TYPE = 1;


    private VROverlay previousFocused;
    private boolean wasPressed;
    private boolean canDrag;

    public ActionRightMouse(VisorActionSet actionSet) {
        super(actionSet, ID);
    }

    @Override
    public void preTick() {
        VROverlay focusedOverlay = ClientContext.cursorHandler.getFocusedOverlay();

        //-------CLEANUP CLICKS-------
        if(focusedOverlay != null
                && InputHelper.isKeyDown(BUTTON_TYPE)) {
            InputHelper.releaseMouse(BUTTON_TYPE);
        }
        if(focusedOverlay == null
                && previousFocused != null
                && wasPressed){
            previousFocused.mouseReleased(
                    previousFocused.getMouseX(),
                    previousFocused.getMouseY(),
                    BUTTON_TYPE
            );
            wasPressed = false;
        }
        previousFocused = focusedOverlay;
        //--------------

        super.preTick();

        //-------DRAG-------
        if(!ClientContext.visor.isFeatureEnabled(ClientFeature.INPUT_VR_MOUSE)){
            return;
        }
        if(focusedOverlay != null
                && wasPressed && pressed){
            if(!canDrag){
                canDrag = true;
                return;
            }
            focusedOverlay.mouseDragged(
                    focusedOverlay.getMouseX(), focusedOverlay.getMouseY(),
                    BUTTON_TYPE,
                    0,0 //ignore it for now
            );
        }

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
        if(previousFocused != null
                && wasPressed){
            previousFocused.mouseReleased(
                    previousFocused.getMouseX(),
                    previousFocused.getMouseY(),
                    BUTTON_TYPE
            );

        }
        previousFocused = null;

        wasPressed = false;
        canDrag = false;
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
            canDrag = false;
        }else if(wasPressed){
            overlay.mouseReleased(
                    overlay.getMouseX(),overlay.getMouseY(),
                    BUTTON_TYPE
            );
            wasPressed = false;
            canDrag = false;
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
                )
        );
    }
}
