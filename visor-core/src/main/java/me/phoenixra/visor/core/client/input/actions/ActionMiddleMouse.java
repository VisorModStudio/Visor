package me.phoenixra.visor.core.client.input.actions;


import lombok.Getter;
import me.phoenixra.atumvr.api.input.action.data.VRActionDataButton;
import me.phoenixra.atumvr.core.input.profile.XRInteractionProfile;
import me.phoenixra.atumvr.api.input.profile.VRInteractionProfileType;
import me.phoenixra.atumvr.api.input.profile.types.ValveIndexProfile;
import me.phoenixra.visor.api.client.ClientFeature;
import me.phoenixra.visor.api.client.gui.overlays.VROverlay;
import me.phoenixra.visor.api.client.gui.overlays.framework.VROverlayScreen;
import me.phoenixra.visor.api.client.input.InputHelper;
import me.phoenixra.visor.api.client.input.action.ActionBinding;
import me.phoenixra.visor.api.client.input.action.VisorActionSet;
import me.phoenixra.visor.api.client.input.action.framework.VisorActionButton;
import me.phoenixra.visor.api.common.HandType;
import me.phoenixra.visor.core.client.ClientContext;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static me.phoenixra.visor.core.client.VisorClientImpl.MC;

public class ActionMiddleMouse extends VisorActionButton {
    public static final String ID = "mouse_middle";

    private static final int BUTTON_TYPE = 2;

    @Getter
    private final boolean required = false;


    private HandType lastUsedHand = HandType.MAIN;

    private VROverlay previousFocus;
    private boolean wasPressed;


    public ActionMiddleMouse(VisorActionSet actionSet) {
        super(actionSet, ID);
    }

    @Override
    public void preTick() {
        VROverlay focusedOverlay = ClientContext.cursorHandler.getFocusedOverlay();

        // --- Cleanup Clicks ---
        if(focusedOverlay != null
                && previousFocus == null
                && InputHelper.isMousePressed(BUTTON_TYPE)){
            InputHelper.releaseMouse(BUTTON_TYPE);
        }
        else if(focusedOverlay == null
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
        if(!ClientContext.visor.isFeatureEnabled(ClientFeature.INPUT_MOUSE)){
            return;
        }
        ClientContext.inputManager.triggerHapticPulseClick(lastUsedHand);
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
    protected VRActionDataButton getButtonData(@NotNull ActionBinding actionBinding, @NotNull XRInteractionProfile currentProfile, boolean leftHanded) {
        boolean mainHand;

        if(!ClientContext.cursorHandler.isCursorHandFocused()
                && MC.screen == null && MC.player != null){
            mainHand = ClientContext.localPlayer.getActiveHand() == HandType.MAIN;
        }else {
            var cursorHand = ClientContext.cursorHandler.getCursorHand();
            mainHand = cursorHand == HandType.MAIN;
        }
        lastUsedHand = mainHand ? HandType.MAIN : HandType.OFFHAND;
        //Here we change leftHanded parameter for method call,
        //to match used hand
        if(leftHanded){
            //left hand as main (leftHanded parameter = true)
            return actionBinding.getButton(currentProfile, mainHand);
        }else{
            //right hand as main (leftHanded parameter = false)
            return actionBinding.getButton(currentProfile, !mainHand);
        }
    }

    @Override
    protected Map<VRInteractionProfileType, ActionBinding> loadDefaults() {
        return Map.of(
                VRInteractionProfileType.VALVE_INDEX,
                new ActionBinding(
                        ValveIndexProfile.BUTTON_GRIP_FORCE_RIGHT,
                        ValveIndexProfile.BUTTON_GRIP_FORCE_LEFT
                ),
                VRInteractionProfileType.OCULUS_TOUCH,
                new ActionBinding(
                        ActionBinding.EMPTY_ID,
                        ActionBinding.EMPTY_ID
                )
        );
    }
}
