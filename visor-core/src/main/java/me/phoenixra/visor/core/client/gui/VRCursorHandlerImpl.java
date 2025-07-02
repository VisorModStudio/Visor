package me.phoenixra.visor.core.client.gui;

import lombok.Getter;
import lombok.Setter;
import me.phoenixra.visor.api.client.ClientFeature;
import me.phoenixra.visor.api.client.data.PoseData;
import me.phoenixra.visor.api.client.data.PoseElement;
import me.phoenixra.visor.api.client.data.PoseDataType;
import me.phoenixra.visor.api.client.gui.VRCursorHandler;
import me.phoenixra.visor.api.client.gui.overlay.VROverlay;
import me.phoenixra.visor.api.client.gui.overlay.VROverlayPose;
import me.phoenixra.visor.api.common.ControllerHand;
import me.phoenixra.visor.api.common.utils.VRMathUtils;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.VisorState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;


public class VRCursorHandlerImpl implements VRCursorHandler {


    @Getter @Setter
    private ControllerHand cursorHand = ControllerHand.MAIN;

    @Getter @Setter
    private VROverlay forceFocused;

    @Getter
    private boolean twoHandedCursor;

    private final CursorState mainHandState = new CursorState();
    private final CursorState offhandState = new CursorState();

    public void process() {
        PoseData renderPose = ClientContext.player.getPoseData(PoseDataType.RENDER);

        updateCursorState(ControllerHand.MAIN, mainHandState, renderPose);
        updateCursorState(ControllerHand.OFFHAND, offhandState, renderPose);


        updateOverlays();
    }

    private void updateCursorState(@NotNull ControllerHand hand, @NotNull CursorState state, @NotNull PoseData renderPose) {
        VROverlay previouslyFocused = state.focusedOverlay;

        CursorResult result;
        if(ClientContext.visor.isFeatureEnabled(ClientFeature.GUI_CURSOR)){
            result = getCursorResult(hand, renderPose);
            if(hand == cursorHand
                    && forceFocused != null
                    && result.focusedOverlay != forceFocused){
                forceFocused = null;
            }
        }else{
            result = new CursorResult(
                    new Vector3f(-1,-1,-1),
                    null
            );
            forceFocused = null;
        }
        state.update(result.cursorPos, result.focusedOverlay);

        // Clean up previous focus
        if(previouslyFocused != null) {
            previouslyFocused.updateCursorData(
                    true,
                    -1, -1
            );
            previouslyFocused.updateCursorData(
                    false,
                    -1, -1
            );

        }
    }



    private void updateOverlays() {
        twoHandedCursor = offhandState.supportsTwoCursors()
                || mainHandState.supportsTwoCursors();

        CursorState activeState = (cursorHand == ControllerHand.MAIN) ? mainHandState : offhandState;
        CursorState inactiveState = (cursorHand == ControllerHand.MAIN) ? offhandState : mainHandState;

        // Update the overlay for the active hand
        if (activeState.isFocused()) {
            activeState.focusedOverlay.updateCursorData(
                    true,
                    activeState.cursorPos.x(),
                    activeState.cursorPos.y()
            );
        }

        // Update the overlay for the inactive hand
        if (inactiveState.isFocused()) {
            inactiveState.focusedOverlay.updateCursorData(
                    false,
                    inactiveState.cursorPos.x(),
                    inactiveState.cursorPos.y()
            );
        }
    }


    private CursorResult getCursorResult(@NotNull ControllerHand hand, @NotNull PoseData renderPose) {
        VROverlay collidingOverlay = null;
        Vector3fc finalCursorPos = new Vector3f(0, 0, -1);

        if(VisorState.getState().isNotFocused()){
            return new CursorResult(finalCursorPos, collidingOverlay);
        }

        double closestDistance = Double.MAX_VALUE;

        PoseElement cursorElement = renderPose.getController(hand);



        for (VROverlay overlay : ClientContext.overlayManager
                .getOverlaysRegistry().getSortedElements()) {
            if (!overlay.isVisible() || !overlay.supportsCursor()) {
                continue;
            }
            boolean forcedFocus = hand == cursorHand && forceFocused == overlay;


            boolean facingGui = isFacingOverlay(
                    cursorElement,
                    overlay,
                    false
            );
            if (!facingGui) {
                continue;
            }

            Vector3fc cursorPos = findCursorPosition3D(
                    cursorElement,
                    overlay.getPose().getPosition(),
                    overlay.getPose().getRotation(),
                    overlay.getPose().getScale()
            );

            //can focus cursor if distance within [0;5] bounds
            if (cursorPos.z() < 0 || cursorPos.z() > 5) {
                continue;
            }

            //there is a closer overlay (ignore if forced focus)
            if(!forcedFocus && cursorPos.z() > closestDistance){
                continue;
            }

            boolean withinBounds = overlay.isCursorWithinBounds(
                    true,
                    cursorPos.x(),
                    cursorPos.y()
            );
            if (withinBounds) {
                finalCursorPos = cursorPos;
                collidingOverlay = overlay;
                closestDistance = cursorPos.z();
                if(forcedFocus){
                    break;
                }
            }
        }
        return new CursorResult(finalCursorPos, collidingOverlay);
    }




    @Override
    public double getCursorLineLength(@NotNull ControllerHand hand) {
        return (hand == ControllerHand.MAIN) ? mainHandState.getCursorLength() : offhandState.getCursorLength();
    }

    @Override
    public @Nullable VROverlay getFocusedOverlay(@NotNull ControllerHand hand) {
        return (hand == ControllerHand.MAIN) ? mainHandState.focusedOverlay : offhandState.focusedOverlay;
    }


    public boolean isFacingOverlay(@NotNull PoseElement element,
                                   @NotNull VROverlay overlay,
                                   boolean checkUpsideDown,
                                   double threshold
    ) {
        Vector3f elementForward = VRMathUtils.extractForwardDir(
                element.getRotation(), true
        );
        Vector3f overlayForward = VRMathUtils.extractForwardDir(
                overlay.getPose().getRotation(), true
        );

        Vector3f toOverlayDir = new Vector3f(overlay.getPose().getPosition())
                .sub(element.getPosition()).normalize();

        //  - Element must face Overlay.
        //  - Overlay must face Element.
        float elementDot = elementForward.dot(toOverlayDir);
        float overlayDot = overlayForward.dot(toOverlayDir.negate());
        if (elementDot <= threshold || overlayDot >= -threshold) {
            return false;
        }

        if (!checkUpsideDown) return true;

        //Ensure is not upside down
        Vector3f overlayUp = VRMathUtils.extractUpDir(overlay.getPose().getRotation(), true);
        float upDot = overlayUp.dot(VRMathUtils.UP_VECTOR);
        return upDot > 0.2;
    }







    @Override
    public @NotNull Vector3f findCursorPosition3D(@NotNull PoseElement element,
                                                  @NotNull Vector3fc guiPosition,
                                                  @NotNull Matrix4fc guiRotation,
                                                  float guiScale
    ) {
        PoseData renderPose = ClientContext.player.getPoseData(PoseDataType.RENDER);
        float worldScale = renderPose.getWorldScale();
        float effectiveScale = VROverlayPose.QUAD_SCALE * guiScale * worldScale;

        Vector3fc rayOrigin = element.getPosition();
        Vector3fc rayDirection = element.getDirection()
                .normalize(new Vector3f());

        Vector3fc planeRight = VRMathUtils.extractRightDir(guiRotation, false);
        Vector3fc planeUp = VRMathUtils.extractUpDir(guiRotation,false);
        Vector3fc planeNormal = VRMathUtils.extractForwardDir(guiRotation, true);

        float denom = planeNormal.dot(rayDirection);
        if (Math.abs(denom) < 1e-5f) {
            return new Vector3f(-1, -1, -1);
        }

        float numerator = planeNormal.dot(guiPosition.sub(rayOrigin, new Vector3f()));
        float t = numerator / denom;
        if (t <= 0) {
            return new Vector3f(-1, -1, -1);
        }

        Vector3f hitPoint = rayOrigin.add(rayDirection.mul(t, new Vector3f()), new Vector3f());

        Vector3f local = hitPoint.sub(
                guiPosition.sub(planeRight.mul(0.5f, new Vector3f()), new Vector3f())
                        .sub(planeUp.mul(0.5f, new Vector3f()))
        );

        float rawU = local.dot(planeRight);
        float rawV = local.dot(planeUp);

        float aspect = ClientContext.guiManager.getScaledAspectRatio();
        float xPos = (rawU - 0.5f) / (effectiveScale) + 0.5f;
        float yPos = 1f - ((rawV - 0.5f) / (effectiveScale * aspect) + 0.5f);

        return new Vector3f(xPos, yPos, t / worldScale);
    }




    private record CursorResult(@NotNull Vector3fc cursorPos, @Nullable VROverlay focusedOverlay) {
    }

    private static class CursorState {
        private VROverlay focusedOverlay;
        private Vector3fc cursorPos = new Vector3f(-1, -1, -1);

        void update(@NotNull Vector3fc newCursorPos, @Nullable VROverlay newFocusedOverlay) {
            this.cursorPos = newCursorPos;
            this.focusedOverlay = newFocusedOverlay;
        }

        boolean isFocused() {
            return focusedOverlay != null;
        }
        boolean supportsTwoCursors(){
            return focusedOverlay != null && focusedOverlay.supportsTwoCursors();
        }

        double getCursorLength() {
            return isFocused() ? cursorPos.z() : -1;
        }
    }
}
