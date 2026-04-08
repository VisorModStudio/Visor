package org.vmstudio.visor.core.client.gui;

import lombok.Getter;
import lombok.Setter;
import org.vmstudio.visor.api.client.ClientFeature;
import org.vmstudio.visor.api.client.player.pose.VRPlayerPoseClient;
import org.vmstudio.visor.api.common.player.VRPose;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.client.gui.VRCursorHandler;
import org.vmstudio.visor.api.client.gui.overlays.VROverlay;
import org.vmstudio.visor.api.client.gui.overlays.VROverlayPose;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.common.utils.VRMathUtils;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.VisorState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.function.Function;


public class VRCursorHandlerImpl implements VRCursorHandler {


    @Getter @Setter
    private HandType cursorHand = HandType.MAIN;

    @Getter @Setter
    private VROverlay forceFocused;

    @Getter
    private boolean twoHandedCursor;

    private final CursorState mainHandState = new CursorState();
    private final CursorState offhandState = new CursorState();

    public void process() {
        VRPlayerPoseClient renderPose = ClientContext.localPlayer.getPose(PlayerPoseType.RENDER);

        updateCursorState(HandType.MAIN, mainHandState, renderPose);
        updateCursorState(HandType.OFFHAND, offhandState, renderPose);


        updateOverlays();
    }

    private void updateCursorState(@NotNull HandType hand, @NotNull CursorState state, @NotNull VRPlayerPoseClient renderPose) {
        VROverlay previouslyFocused = state.focusedOverlay;

        CursorResult result;
        if(ClientContext.visor.isFeatureEnabled(ClientFeature.GUI_CURSOR)){
            result = getCursorResult(hand, renderPose, null, true);
            if(hand == cursorHand
                    && forceFocused != null
                    && result.focusedOverlay() != forceFocused){
                forceFocused = null;
            }
        }else{
            result = new CursorResult(
                    new Vector3f(-1,-1,-1),
                    null
            );
            forceFocused = null;
        }
        state.update(result.cursorPos(), result.focusedOverlay());

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

        CursorState activeState = (cursorHand == HandType.MAIN) ? mainHandState : offhandState;
        CursorState inactiveState = (cursorHand == HandType.MAIN) ? offhandState : mainHandState;

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


    public @NotNull CursorResult getCursorResult(@NotNull HandType hand,
                                                 @NotNull VRPlayerPoseClient poseData,
                                                 @Nullable Function<VROverlay, Boolean> overlayFilter,
                                                 boolean checkForceFocused) {
        VROverlay collidingOverlay = null;
        Vector3fc finalCursorPos = new Vector3f(0, 0, -1);

        if(VisorState.get().isNotFocused()){
            return new CursorResult(finalCursorPos, collidingOverlay);
        }

        double closestDistance = Double.MAX_VALUE;

        VRPose cursorElement = poseData.getHand(hand);



        for (VROverlay overlay : ClientContext.overlayManager
                .getOverlaysRegistry().getSortedComponents()) {
            if (!overlay.supportsCursor()) {
                continue;
            }
            if(overlay.supportsCursorIgnoreVisible()
                    && !overlay.isEnabled()){
                continue;
            }
            if(!overlay.supportsCursorIgnoreVisible()
                    && !overlay.isVisible()){
                continue;
            }
            if(overlayFilter != null
                    && !overlayFilter.apply(overlay)){
                continue;
            }

            boolean forcedFocus = checkForceFocused
                    && hand == cursorHand && forceFocused == overlay;


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
                    overlay.getPose().getScale(),
                    overlay.getAspectRatio()
            );

            //can focus cursor if distance within [0;5] bounds
            if (cursorPos.z() < 0 || cursorPos.z() > 5) {
                continue;
            }

            //there is a closer overlay (ignore if forced focus)
            if(!forcedFocus && cursorPos.z() > closestDistance){
                continue;
            }

            boolean withinBounds = overlay.isWithinCursorBounds(
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
    public double getCursorLineLength(@NotNull HandType hand) {
        return (hand == HandType.MAIN) ? mainHandState.getCursorLength() : offhandState.getCursorLength();
    }

    @Override
    public @Nullable VROverlay getFocusedOverlay(@NotNull HandType hand) {
        return (hand == HandType.MAIN) ? mainHandState.focusedOverlay : offhandState.focusedOverlay;
    }


    public boolean isFacingOverlay(@NotNull VRPose pose,
                                   @NotNull VROverlay overlay,
                                   boolean checkUpsideDown) {
        // -- basis
        var overlayRot = overlay.getPose().getRotation();

        Vector3f elementForward = VRMathUtils.extractForwardDir(pose.getRotation(), true);

        Vector3f overlayForward = VRMathUtils.extractForwardDir(overlayRot, true);
        Vector3f overlayUp = VRMathUtils.extractUpDir(overlayRot, true);
        Vector3f overlayRight = VRMathUtils.extractRightDir(overlayRot, true);

        var elementPos = pose.getPosition();
        var overlayCenter = overlay.getPose().getPosition();


        var halfWidth = overlay.getPose().getHalfWidth();
        var halfHeight = overlay.getPose().getHalfHeight();

        // -- closest point on overlay (plane -> clamp to rect if size known)
        Vector3f dir = new Vector3f(elementPos).sub(overlayCenter);
        float x = dir.dot(overlayRight);
        float y = dir.dot(overlayUp);
        float px = Math.max(-halfWidth, Math.min(halfWidth, x));
        float py = Math.max(-halfHeight, Math.min(halfHeight, y));
        Vector3f closest = new Vector3f(overlayCenter).fma(px, overlayRight).fma(py, overlayUp);

        // -- direction & facing tests
        Vector3f toOverlayDir = new Vector3f(closest).sub(elementPos);
        float len = toOverlayDir.length();
        if (len < 1e-6f) {
            return false;
        }
        //normalizing
        toOverlayDir.div(len);

        float elementDot = elementForward.dot(toOverlayDir);
        float overlayDot = overlayForward.dot(new Vector3f(toOverlayDir).negate());
        if (elementDot <= 0.2f || overlayDot >= -0.2f) {
            return false;
        }

        // -- upside-down test
        if (!checkUpsideDown) return true;
        float upDot = overlayUp.dot(VRMathUtils.UP_VECTOR);
        return upDot > 0.2f;
    }







    @Override
    public @NotNull Vector3f findCursorPosition3D(@NotNull VRPose pose,
                                                  @NotNull Vector3fc guiPosition,
                                                  @NotNull Matrix4fc guiRotation,
                                                  float guiScale,
                                                  float guiAspectRatio
    ) {
        VRPlayerPoseClient renderPose = ClientContext.localPlayer.getPose(PlayerPoseType.RENDER);
        float worldScale = renderPose.getWorldScale();
        float effectiveScale = VROverlayPose.QUAD_SCALE * guiScale * worldScale;

        Vector3fc rayOrigin = pose.getPosition();
        Vector3fc rayDirection = pose.getDirection()
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

        float xPos = (rawU - 0.5f) / (effectiveScale) + 0.5f;
        float yPos = 1f - ((rawV - 0.5f) / (effectiveScale * guiAspectRatio) + 0.5f);

        return new Vector3f(xPos, yPos, t / worldScale);
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
