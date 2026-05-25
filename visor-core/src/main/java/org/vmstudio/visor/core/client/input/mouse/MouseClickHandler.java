package org.vmstudio.visor.core.client.input.mouse;

import lombok.Setter;
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
    private VROverlay pressedOverlay;
    private boolean wasPressedOverlay;
    private boolean ignoreSingleClick;
    private boolean ignoreSingleRelease;

    private boolean gamePressed;

    // only used by left-click
    private final boolean isLeftClick;
    @Setter
    private boolean forcedMain, forcedOffhand;

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
                && pressedOverlay != null
                && wasPressedOverlay) {
            pressedOverlay.mouseReleased(
                    pressedOverlay.getMouseX(),
                    pressedOverlay.getMouseY(),
                    buttonType.getId()
            );
            if (pressedOverlay instanceof VROverlayScreen overlayScreen) {
                overlayScreen.finishDragMouse(buttonType.getId());
            }
            wasPressedOverlay = false;
            pressedOverlay = null;
        } else if (focusedOverlay != null
                && pressedOverlay != null
                && focusedOverlay != pressedOverlay
                && wasPressedOverlay) {
            pressedOverlay.mouseReleased(
                    pressedOverlay.getMouseX(),
                    pressedOverlay.getMouseY(),
                    buttonType.getId()
            );
            if (pressedOverlay instanceof VROverlayScreen overlayScreen) {
                overlayScreen.finishDragMouse(buttonType.getId());
            }
            wasPressedOverlay = false;
            pressedOverlay = null;
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
        process(handType);
    }

    public void onRelease(@NotNull HandType handType) {
        if (isLeftClick && ignoreSingleClick) {
            ignoreSingleClick = false;
            if (!gamePressed && !wasPressedOverlay) {
                return;
            }
        }

        if (wasPressedOverlay) {
            VROverlay target = pressedOverlay;
            if (target == null) {
                target = ClientContext.cursorHandler.getFocusedOverlay();
            }
            if (target != null) {
                target.mouseReleased(
                        target.getMouseX(),
                        target.getMouseY(),
                        buttonType.getId()
                );
                if (target instanceof VROverlayScreen overlayScreen) {
                    overlayScreen.finishDragMouse(buttonType.getId());
                }
            }
            wasPressedOverlay = false;
            pressedOverlay = null;
        }

        if (gamePressed) {
            InputHelper.releaseMouse(buttonType);
            gamePressed = false;
        }

        ignoreSingleRelease = false;
    }

    public void onClear() {
        InputHelper.releaseMouse(buttonType);
        if (pressedOverlay != null && wasPressedOverlay) {
            pressedOverlay.mouseReleased(
                    pressedOverlay.getMouseX(),
                    pressedOverlay.getMouseY(),
                    buttonType.getId()
            );
            if (pressedOverlay instanceof VROverlayScreen overlayScreen) {
                overlayScreen.finishDragMouse(buttonType.getId());
            }
        }
        previousFocus = null;
        pressedOverlay = null;
        wasPressedOverlay = false;
        ignoreSingleClick = false;
        ignoreSingleRelease = false;
        gamePressed = false;
    }

    private void process(@NotNull HandType handType) {
        VROverlay focusedOverlay = ClientContext.cursorHandler.getFocusedOverlay();

        if (focusedOverlay != null) {
            processOverlay(focusedOverlay);
            return;
        }
        if (MC.screen != null) {
            processScreen();
            return;
        }
        if (MC.player != null) {
            processGame(handType);
        }
    }

    private void processOverlay(VROverlay overlay) {
        overlay.mouseClicked(
                overlay.getMouseX(), overlay.getMouseY(),
                buttonType.getId()
        );
        if (overlay instanceof VROverlayScreen overlayScreen) {
            overlayScreen.startDragMouse(buttonType.getId());
        }
        wasPressedOverlay = true;
        pressedOverlay = overlay;
    }

    private void processScreen() {
        if (!isLeftClick) {
            return;
        }
        if (MC.level != null) {
            //clicked outside of overlay screen, close the screen
            InputHelper.pressKey(GLFW.GLFW_KEY_ESCAPE);
            InputHelper.releaseKey(GLFW.GLFW_KEY_ESCAPE);
        }
    }

    private void processGame(@NotNull HandType handType) {
        // update active hand if only one hand is pressed
        var activeHand = ClientContext.localPlayer.getActiveHand();
        if (activeHand != handType) {
            if ((mainHandPressed && !offhandPressed)
                    || (!mainHandPressed && offhandPressed)) {
                ClientContext.localPlayer.setActiveHand(handType);
                if(!(forcedMain && mainHandPressed)
                        && !(forcedOffhand && offhandPressed)) {
                    ignoreSingleRelease = true;
                    return;
                }
            }
            if (ignoreSingleRelease) {
                ignoreSingleRelease = false;
                return;
            }
        }
        InputHelper.pressMouse(buttonType);
        gamePressed = true;
    }
}