package org.vmstudio.visor.core.client.input.mouse;

import org.vmstudio.visor.api.client.ClientFeature;
import org.vmstudio.visor.api.client.gui.overlays.VROverlay;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.VisorState;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

public class MouseScrollHandler {
    public static final MouseScrollHandler INSTANCE = new MouseScrollHandler();

    private static final float DEAD_ZONE = 0.1f;

    private static final int REPEAT_DELAY_TICKS = 6;
    private static final double MIN_STEPS_PER_SECOND = 3.0;
    private static final double MAX_STEPS_PER_SECOND = 15.0;

    private int heldDirection;
    private int ticksHeld;
    private double repeatSaved;
    private int lastHandledTick = -1;

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

        if(lastHandledTick == VisorState.TICK_COUNT){
            return;
        }
        lastHandledTick = VisorState.TICK_COUNT;

        HandType handType;
        if(!ClientContext.cursorHandler.isCursorHandFocused()
                && MC.screen == null && MC.player != null){
            handType = ClientContext.localPlayer.getActiveHand();
        }else {
            handType = ClientContext.cursorHandler.getCursorHand();
        }

        float scrollPos = getState(handType);
        if (Math.abs(scrollPos) < DEAD_ZONE) {
            heldDirection = 0;
            return;
        }

        int direction = scrollPos > 0 ? 1 : -1;
        if (direction != heldDirection) {
            heldDirection = direction;
            ticksHeld = 0;
            repeatSaved = 0;
            doScroll(direction);
            return;
        }

        if (++ticksHeld < REPEAT_DELAY_TICKS) {
            return;
        }

        double strength = Math.min(
                1.0,
                (Math.abs(scrollPos) - DEAD_ZONE) / (1.0 - DEAD_ZONE)
        );
        double stepsPerSecond = MIN_STEPS_PER_SECOND
                + (MAX_STEPS_PER_SECOND - MIN_STEPS_PER_SECOND) * strength;

        repeatSaved += stepsPerSecond / 20.0;
        if (repeatSaved < 1) {
            return;
        }
        // keep the remainder, so the repeat speed stays even
        repeatSaved -= 1;
        doScroll(direction);
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
