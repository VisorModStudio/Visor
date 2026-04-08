package org.vmstudio.visor.core.client.input.mouse;

import org.vmstudio.visor.api.client.ClientFeature;
import org.vmstudio.visor.api.client.gui.overlays.VROverlay;
import org.vmstudio.visor.api.client.gui.overlays.framework.VROverlayScreen;
import org.vmstudio.visor.api.client.input.InputHelper;
import org.vmstudio.visor.api.client.input.MouseButtonType;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.core.client.ClientContext;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

public class MouseClickHandler {

    public final static MouseClickHandler LEFT_HANDLER = new MouseClickHandler(MouseButtonType.LEFT);

    public final static MouseClickHandler RIGHT_HANDLER = new MouseClickHandler(MouseButtonType.RIGHT);

    public final static MouseClickHandler MIDDLE_HANDLER = new MouseClickHandler(MouseButtonType.MIDDLE);

    private final MouseButtonType buttonType;

    private boolean mainHandPressed;
    private boolean offhandPressed;

    private boolean mainHandChanged;
    private boolean offhandChanged;

    private VROverlay previousFocus;
    private boolean wasPressedOverlay;
    private boolean ignoreSingleClick;
    private boolean ignoreSingleRelease;

    // only used by left-click
    private final boolean isLeftClick;

    public MouseClickHandler(MouseButtonType buttonType) {
        this.buttonType = buttonType;
        this.isLeftClick = buttonType == MouseButtonType.LEFT;
    }

    public void updateState(@NotNull HandType handType,
                            boolean pressed,
                            boolean changed) {
        switch (handType) {
            case MAIN -> {
                mainHandPressed = pressed;
                mainHandChanged = changed;
            }
            case OFFHAND -> {
                offhandPressed = pressed;
                offhandChanged = changed;
            }
        }
    }

    public void preTick() {
        if (!ClientContext.visor.isFeatureEnabled(ClientFeature.INPUT_MOUSE)) {
            return;
        }

        VROverlay focusedOverlay = ClientContext.cursorHandler.getFocusedOverlay();

        // --- Cleanup Clicks ---
        if (focusedOverlay != null
                && previousFocus == null
                && InputHelper.isMousePressed(buttonType)) {
            InputHelper.releaseMouse(buttonType);
        } else if (focusedOverlay == null
                && previousFocus != null
                && wasPressedOverlay) {
            previousFocus.mouseReleased(
                    previousFocus.getMouseX(),
                    previousFocus.getMouseY(),
                    buttonType.getId()
            );
            if (previousFocus instanceof VROverlayScreen overlayScreen) {
                overlayScreen.finishDragMouse();
            }
            wasPressedOverlay = false;
        } else if (focusedOverlay != null
                && previousFocus != null
                && focusedOverlay != previousFocus
                && wasPressedOverlay) {
            previousFocus.mouseReleased(
                    previousFocus.getMouseX(),
                    previousFocus.getMouseY(),
                    buttonType.getId()
            );
            if (previousFocus instanceof VROverlayScreen overlayScreen) {
                overlayScreen.finishDragMouse();
            }
            wasPressedOverlay = false;
        }
        previousFocus = focusedOverlay;

        // --- Cursor hand switching (left click only) ---
        if (isLeftClick) {
            processCursorUpdate();
        }
    }

    private void processCursorUpdate() {
        if (!ClientContext.cursorHandler.isAnyHandFocused()) {
            return;
        }

        HandType cursorHand = ClientContext.cursorHandler.getCursorHand();

        boolean offHandClicked = offhandPressed && offhandChanged;
        boolean mainClicked = mainHandPressed && mainHandChanged;
        if (offHandClicked && mainClicked) {
            return;
        }

        if (cursorHand != HandType.OFFHAND
                && offHandClicked
                && !mainHandPressed
                && !mainHandChanged) {

            if (!ClientContext.cursorHandler.isHandFocused(HandType.OFFHAND)) {
                return;
            }
            if (!ClientContext.cursorHandler.isTwoHandedCursor()) {
                ignoreSingleClick = true;
            }
            ClientContext.cursorHandler.setCursorHand(HandType.OFFHAND);
            ClientContext.cursorHandler.process();
            return;
        }

        if (cursorHand != HandType.MAIN
                && mainClicked
                && !offhandPressed
                && !offhandChanged) {

            if (!ClientContext.cursorHandler.isHandFocused(HandType.MAIN)) {
                return;
            }
            if (!ClientContext.cursorHandler.isTwoHandedCursor()) {
                ignoreSingleClick = true;
            }
            ClientContext.cursorHandler.setCursorHand(HandType.MAIN);
            ClientContext.cursorHandler.process();
        }
    }

    public void onPress(@NotNull HandType handType) {

        if (!ClientContext.visor.isFeatureEnabled(ClientFeature.INPUT_MOUSE)) {
            return;
        }

        if (ClientContext.cursorHandler.isCursorHandFocused()
                || MC.screen != null
                || MC.player == null) {
            var activeHand = ClientContext.cursorHandler.getCursorHand();
            if (handType != activeHand) {
                return;
            }
        }

        ClientContext.inputManager.triggerHapticPulseClick(handType);
        if (isLeftClick && ignoreSingleClick) {
            return;
        }
        process(handType,true);
    }

    public void onRelease(@NotNull HandType handType) {
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

        if (isLeftClick && ignoreSingleClick) {
            ignoreSingleClick = false;
            return;
        }
        process(handType,false);
    }

    public void onClear() {
        InputHelper.releaseMouse(buttonType);
        if (previousFocus != null && wasPressedOverlay) {
            previousFocus.mouseReleased(
                    previousFocus.getMouseX(),
                    previousFocus.getMouseY(),
                    buttonType.getId()
            );
            if (previousFocus instanceof VROverlayScreen overlayScreen) {
                overlayScreen.finishDragMouse();
            }
        }
        previousFocus = null;
        wasPressedOverlay = false;
        ignoreSingleClick = false;
    }

    private void process(@NotNull HandType handType, boolean press) {
        VROverlay focusedOverlay = ClientContext.cursorHandler.getFocusedOverlay();

        if (focusedOverlay != null) {
            processOverlay(focusedOverlay, press);
            return;
        }
        if (MC.screen != null) {
            processScreen(press);
            return;
        }
        if (MC.player != null) {
            processGame(handType, press);
        }
    }

    private void processOverlay(VROverlay overlay, boolean press) {
        if (press) {
            overlay.mouseClicked(
                    overlay.getMouseX(), overlay.getMouseY(),
                    buttonType.getId()
            );
            if (isLeftClick && overlay instanceof VROverlayScreen overlayScreen) {
                overlayScreen.startDragMouse();
            }
            wasPressedOverlay = true;
        } else if (wasPressedOverlay) {
            overlay.mouseReleased(
                    overlay.getMouseX(), overlay.getMouseY(),
                    buttonType.getId()
            );
            if (overlay instanceof VROverlayScreen overlayScreen) {
                overlayScreen.finishDragMouse();
            }
            wasPressedOverlay = false;
        }
    }

    private void processScreen(boolean press) {
        if (!isLeftClick) {
            return;
        }
        if (press) {
            if (MC.level != null) {
                //clicked outside of overlay screen, close the screen
                InputHelper.pressKey(GLFW.GLFW_KEY_ESCAPE);
                InputHelper.releaseKey(GLFW.GLFW_KEY_ESCAPE);
            }
        }
    }

    private void processGame(@NotNull HandType handType, boolean press) {


        if (press) {
            //update active hand if only one hand is pressed
            var activeHand = ClientContext.localPlayer.getActiveHand();
            if(activeHand != handType) {
                if ((mainHandPressed && !offhandPressed)
                        || (!mainHandPressed && offhandPressed)) {
                    ClientContext.localPlayer.setActiveHand(handType);
                    ignoreSingleRelease = true;
                    return;
                }
                if (ignoreSingleRelease) {
                    return;
                }
            }
            InputHelper.pressMouse(buttonType);
        } else {
            if(ignoreSingleRelease){
                ignoreSingleRelease = false;
                return;
            }
            InputHelper.releaseMouse(buttonType);
        }
    }
}