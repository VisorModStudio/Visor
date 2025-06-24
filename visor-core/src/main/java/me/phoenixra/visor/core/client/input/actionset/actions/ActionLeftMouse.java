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
import me.phoenixra.visor.api.client.input.action.framework.VisorActionButton;
import me.phoenixra.visor.api.common.ControllerHand;
import me.phoenixra.visor.core.client.ClientContext;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static me.phoenixra.visor.core.client.VisorClientImpl.MC;

public class ActionLeftMouse extends VisorActionButton {
    public static final String ID = "mouse_left";

    private static final int BUTTON_TYPE = 0;

    private ControllerHand lastUsedHand = ControllerHand.MAIN;

    private VROverlay previousFocused;

    private boolean wasPressed;
    private boolean ignoreSingleClick;

    public ActionLeftMouse(VisorActionSet actionSet) {
        super(actionSet, ID);
    }

    @Override
    public void preTick() {

        VROverlay focusedOverlay = ClientContext.cursorHandler.getFocusedOverlay();

        //-------CLEANUP CLICKS-------
        if(focusedOverlay != null
                && InputHelper.isMousePressed(BUTTON_TYPE)) {
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


    }

    @Override
    protected void onPress() {
        if(!ClientContext.visor.isFeatureEnabled(ClientFeature.INPUT_VR_MOUSE)){
            return;
        }
        ClientContext.inputManager.triggerHapticPulseClick(lastUsedHand);
        if(ignoreSingleClick){
            return;
        }

        process(true);
    }

    @Override
    protected void onRelease() {
        if(ignoreSingleClick){
            ignoreSingleClick = false;
            return;
        }
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
        ignoreSingleClick = false;

    }


    @Override
    public void updateState(@NotNull OpenXRProfileSet currentProfile,
                            boolean leftHanded) {
        if(!ClientContext.cursorHandler.isAnyHandFocused()){
            super.updateState(currentProfile, leftHanded);
            return;
        }
        BindingPath bindingPath = bindings.get(currentProfile.getType());

        if(bindingPath == null){
            active = false;
            if(pressed){
                releaseDelayed = true;
                pressDelayed = false;
            }
            return;
        }

        var buttonDataOffhand = bindingPath.getButton(currentProfile, !leftHanded);
        var buttonDataMain = bindingPath.getButton(currentProfile, leftHanded);

        if(buttonDataOffhand == null
                || buttonDataMain == null){
            return;
        }
        processCursorUpdate(
                buttonDataOffhand,
                buttonDataMain
        );

        super.updateState(currentProfile, leftHanded);
    }

    private void processCursorUpdate(VRActionDataButton buttonDataOffhand,
                                     VRActionDataButton buttonDataMain){

        ControllerHand cursorHand = ClientContext.cursorHandler.getCursorHand();

        boolean offHandClicked = buttonDataOffhand.isPressed()
                && buttonDataOffhand.isButtonChanged();
        boolean mainClicked = buttonDataMain.isPressed()
                && buttonDataMain.isButtonChanged();
        if(offHandClicked && mainClicked){
            return;
        }

        //We make sure that button was clicked on one hand
        //and other hand button is not pressed or just released
        if (cursorHand != ControllerHand.OFFHAND
                && offHandClicked
                && !buttonDataMain.isPressed()
                && !buttonDataMain.isButtonChanged()) {

            if(!ClientContext.cursorHandler.isHandFocused(ControllerHand.OFFHAND)){
                return;
            }
            if(!ClientContext.cursorHandler.isTwoHandedCursor()){
                ignoreSingleClick = true;
            }
            ClientContext.cursorHandler.setCursorHand(
                    ControllerHand.OFFHAND
            );
            ClientContext.cursorHandler.process();
            return;
        }

        if (cursorHand != ControllerHand.MAIN
                && mainClicked
                && !buttonDataOffhand.isPressed()
                && !buttonDataOffhand.isButtonChanged()) {

            if(!ClientContext.cursorHandler.isHandFocused(ControllerHand.MAIN)){
                return;
            }
            if(!ClientContext.cursorHandler.isTwoHandedCursor()){
                ignoreSingleClick = true;
            }
            ClientContext.cursorHandler.setCursorHand(
                    ControllerHand.MAIN
            );
            ClientContext.cursorHandler.process();
        }
    }

    private void process(boolean press){
        VROverlay focusedOverlay = ClientContext.cursorHandler.getFocusedOverlay();

        if(focusedOverlay != null){
            processOverlay(focusedOverlay, press);
            return;
        }
        if(MC.screen != null){
            processScreen(press);
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

    private void processScreen(boolean press){
        if (press) {
            //closing the screen if clicked out of its bounds
            if(MC.level != null){
                MC.setScreen(null);
            }
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

        lastUsedHand = mainHand ? ControllerHand.MAIN : ControllerHand.OFFHAND;
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
                        ValveIndexSet.BUTTON_TRIGGER_RIGHT,
                        ValveIndexSet.BUTTON_TRIGGER_LEFT
                )
        );
    }
}
