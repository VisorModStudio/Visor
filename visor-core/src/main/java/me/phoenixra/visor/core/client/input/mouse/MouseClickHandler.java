package me.phoenixra.visor.core.client.input.mouse;

import me.phoenixra.visor.api.client.ClientFeature;
import me.phoenixra.visor.api.client.gui.overlays.VROverlay;
import me.phoenixra.visor.api.client.gui.overlays.framework.VROverlayScreen;
import me.phoenixra.visor.api.client.input.InputHelper;
import me.phoenixra.visor.api.client.input.MouseButtonType;
import me.phoenixra.visor.api.common.HandType;
import me.phoenixra.visor.core.client.ClientContext;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import static me.phoenixra.visor.core.client.VisorClientImpl.MC;

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
    private boolean wasPressed;
    private boolean ignoreSingleClick;

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
                && wasPressed) {
            previousFocus.mouseReleased(
                    previousFocus.getMouseX(),
                    previousFocus.getMouseY(),
                    buttonType.getId()
            );
            if (previousFocus instanceof VROverlayScreen overlayScreen) {
                overlayScreen.finishDragMouse();
            }
            wasPressed = false;
        } else if (focusedOverlay != null
                && previousFocus != null
                && focusedOverlay != previousFocus
                && wasPressed) {
            previousFocus.mouseReleased(
                    previousFocus.getMouseX(),
                    previousFocus.getMouseY(),
                    buttonType.getId()
            );
            if (previousFocus instanceof VROverlayScreen overlayScreen) {
                overlayScreen.finishDragMouse();
            }
            wasPressed = false;
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
        ClientContext.inputManager.triggerHapticPulseClick(handType);
        if (isLeftClick && ignoreSingleClick) {
            return;
        }
        process(true);
    }

    public void onRelease() {
        if (isLeftClick && ignoreSingleClick) {
            ignoreSingleClick = false;
            return;
        }
        process(false);
    }

    public void onClear() {
        InputHelper.releaseMouse(buttonType);
        if (previousFocus != null && wasPressed) {
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
        wasPressed = false;
        ignoreSingleClick = false;
    }

    private void process(boolean press) {
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
            processGame(press);
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
            wasPressed = true;
        } else if (wasPressed) {
            overlay.mouseReleased(
                    overlay.getMouseX(), overlay.getMouseY(),
                    buttonType.getId()
            );
            if (overlay instanceof VROverlayScreen overlayScreen) {
                overlayScreen.finishDragMouse();
            }
            wasPressed = false;
        }
    }

    private void processScreen(boolean press) {
        if (!isLeftClick) {
            return;
        }
        if (press) {
            if (MC.level != null) {
                InputHelper.pressKey(GLFW.GLFW_KEY_ESCAPE);
                InputHelper.releaseKey(GLFW.GLFW_KEY_ESCAPE);
            }
        }
    }

    private void processGame(boolean press) {
        if (press) {
            InputHelper.pressMouse(buttonType);
        } else {
            InputHelper.releaseMouse(buttonType);
        }
    }
}