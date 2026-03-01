package org.vmstudio.visor.core.client.input.mouse;

import org.vmstudio.visor.api.client.ClientFeature;
import org.vmstudio.visor.api.client.gui.overlays.VROverlay;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.core.client.ClientContext;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

public class MouseScrollHandler {
    public static final MouseScrollHandler INSTANCE = new MouseScrollHandler();

    private double deltaSaved;

    private Vector2f mainHandState = new Vector2f(0,0);
    private Vector2f offhandState = new Vector2f(0,0);

    public void updateState(@NotNull HandType handType,
                            @NotNull Vector2f newState){
        switch (handType){
            case MAIN -> mainHandState = newState;
            case OFFHAND -> offhandState = newState;
        }
    }

    public void tick() {
        if(!ClientContext.visor.isFeatureEnabled(ClientFeature.INPUT_MOUSE)){
            return;
        }
        HandType handType;
        if(!ClientContext.cursorHandler.isCursorHandFocused()
                && MC.screen == null && MC.player != null){
            handType = ClientContext.localPlayer.getActiveHand();
        }else {
            handType = ClientContext.cursorHandler.getCursorHand();
        }

        float scrollPos = getState(handType);
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

    public float getState(@NotNull HandType handType){
        return switch (handType){
            case MAIN -> mainHandState.y;
            case OFFHAND -> offhandState.y;
        };
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


}
