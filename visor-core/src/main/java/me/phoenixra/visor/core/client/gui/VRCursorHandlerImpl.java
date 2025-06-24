package me.phoenixra.visor.core.client.gui;

import lombok.Getter;
import lombok.Setter;
import me.phoenixra.visor.api.client.data.PoseData;
import me.phoenixra.visor.api.client.data.PoseElement;
import me.phoenixra.visor.api.client.data.PoseType;
import me.phoenixra.visor.api.client.gui.VRCursorHandler;
import me.phoenixra.visor.api.client.gui.overlay.VROverlay;
import me.phoenixra.visor.api.common.ControllerHand;
import me.phoenixra.visor.api.common.utils.VRMathUtils;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.VisorState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4fc;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3fc;


public class VRCursorHandlerImpl implements VRCursorHandler {


    @Getter @Setter
    private ControllerHand cursorHand = ControllerHand.MAIN;

    @Getter @Setter
    private boolean draggingItem;

    @Getter
    private boolean twoHandedCursor;

    private final CursorState mainHandState = new CursorState();
    private final CursorState offhandState = new CursorState();

    public void process() {
        PoseData renderPose = ClientContext.player.getPose(PoseType.RENDER);

        updateCursorState(ControllerHand.MAIN, mainHandState, renderPose);
        updateCursorState(ControllerHand.OFFHAND, offhandState, renderPose);


        updateOverlays();
    }

    private void updateCursorState(@NotNull ControllerHand hand, @NotNull CursorState state, @NotNull PoseData renderPose) {
        VROverlay previouslyFocused = state.focusedOverlay;

        CursorResult result = getCursorResult(hand, renderPose);
        state.update(result.cursorPos, result.collidedOverlay);

        // Clean up previous focus
        if(previouslyFocused != null) {
            previouslyFocused.updateMousePosition(
                    true,
                    -1, -1
            );
            previouslyFocused.updateMousePosition(
                    false,
                    -1, -1
            );

        }
    }



    private void updateOverlays() {
        twoHandedCursor = offhandState.supportsTwoHandedCursor()
                || mainHandState.supportsTwoHandedCursor();

        CursorState activeState = (cursorHand == ControllerHand.MAIN) ? mainHandState : offhandState;
        CursorState inactiveState = (cursorHand == ControllerHand.MAIN) ? offhandState : mainHandState;

        // Update the overlay for the active hand
        if (activeState.isFocused()) {
            activeState.focusedOverlay.updateMousePosition(
                    true,
                    activeState.cursorPos.x(),
                    activeState.cursorPos.y()
            );
        }

        // Update the overlay for the inactive hand
        if (inactiveState.isFocused()) {
            inactiveState.focusedOverlay.updateMousePosition(
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
            if (!overlay.isVisible() || !overlay.isCursorSupported()) {
                continue;
            }

            double distance = getCursorDistanceToGui(
                    renderPose,
                    cursorElement,
                    overlay.getPosition(),
                    overlay.getRotation()
            );

            //can focus cursor if distance within [0;5] bounds
            if (distance < 0 || distance > 5 || distance > closestDistance) {
                continue;
            }

            boolean notFacingGui = !overlay.ignoreFacingGui()
                    && !isFacingOverlay(cursorElement, overlay, false
            );
            if (notFacingGui) {
                continue;
            }

            Vector3fc newCursorPos = findCursorPosition3D(
                    cursorElement,
                    overlay.getPosition(),
                    overlay.getRotation(),
                    overlay.getOverlayScale()
            );

            if (overlay.isCursorWithinBounds(
                    true,
                    newCursorPos.x(),
                    newCursorPos.y()
            )) {
                finalCursorPos = newCursorPos;
                collidingOverlay = overlay;
                closestDistance = distance;
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


    public boolean isFacingOverlay(PoseElement element,
                                   VROverlay overlay,
                                   boolean checkUpsideDown,
                                   double threshold
    ) {
        Vector3f elementForward = VRMathUtils.extractForwardDir(
                element.getRotation(), true
        );
        Vector3f overlayForward = VRMathUtils.extractForwardDir(
                overlay.getRotation(), true
        );

        Vector3f toOverlayDir = new Vector3f(overlay.getPosition())
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
        Vector3f overlayUp = VRMathUtils.extractUpDir(overlay.getRotation(), true);
        float upDot = overlayUp.dot(VRMathUtils.upVector);
        return upDot > 0.2;
    }




    public float getCursorDistanceToGui(@NotNull PoseData clientPose,
                                        @NotNull PoseElement cursorElement,
                                        @NotNull Vector3fc guiPosition,
                                        @NotNull Matrix4fc guiRotation){
        Vector3fc position = cursorElement.getPosition();
        Vector3fc direction = cursorElement.getDirection();
        Vector3fc aimZ = guiRotation.transformDirection(VRMathUtils.forwardVectorReversed, new Vector3f());
        Vector3fc aimX = guiRotation.transformDirection(VRMathUtils.rightVector, new Vector3f());
        Vector3fc aimY = guiRotation.transformDirection(VRMathUtils.upVector, new Vector3f());
        float point = aimZ.dot(direction);

        if (Math.abs(point) > 1.0E-5F) {
            Vector3f cursorBase = guiPosition
                    .sub(
                            aimY.div(2f, new Vector3f()),
                            new Vector3f()
                    ).sub(
                            aimX.div(2f, new Vector3f())
                    );
            float depthFactor = -aimZ.dot(
                    position.sub(cursorBase, new Vector3f())
            ) / point;

            return depthFactor / clientPose.getWorldScale();
        }
        return -1;
    }

    @Override
    public @NotNull Vector2f findCursorPosition2D(@NotNull PoseElement component,
                                                  @NotNull Vector3fc guiPosition,
                                                  @NotNull Matrix4fc guiRotation,
                                                  float guiScale
    ) {

        var vec3 = findCursorPosition3D(
                component,
                guiPosition, guiRotation,
                guiScale
        );
        return new Vector2f((float) vec3.x, (float) vec3.y);
    }

    @Override
    public Vector3f findCursorPosition3D(@NotNull PoseElement cursorElement,
                                         @NotNull Vector3fc guiPosition,
                                         @NotNull Matrix4fc guiRotation,
                                         float guiScale
    ) {
        // 1) World‐space setup
        PoseData renderPose = ClientContext.player.getPose(PoseType.RENDER);
        float worldScale = renderPose.getWorldScale();
        float effectiveScale = guiScale * worldScale;

        Vector3fc rayOrigin = cursorElement.getPosition();
        Vector3fc rayDirection = cursorElement.getDirection()
                .normalize(new Vector3f());

        // 2) GUI basis vectors (scaled later)
        Vector3fc planeRight = VRMathUtils.extractRightDir(guiRotation, false);
        Vector3fc planeUp = VRMathUtils.extractUpDir(guiRotation,false);
        Vector3fc planeNormal = VRMathUtils.extractForwardDir(guiRotation, true);

        // 3) Intersection: t = dot(P0–O, N) / dot(D, N)
        float denom = planeNormal.dot(rayDirection);
        if (Math.abs(denom) < 1e-5f) {
            return new Vector3f(-1, -1, -1);
        }

        float numerator = planeNormal.dot(guiPosition.sub(rayOrigin, new Vector3f()));
        float t = numerator / denom;
        if (t <= 0) {
            return new Vector3f(-1, -1, -1);
        }

        // 4) Compute hit point on plane
        Vector3f hitPoint = rayOrigin.add(rayDirection.mul(t, new Vector3f()), new Vector3f());

        // 5) Convert to local GUI coords (0..1)
        Vector3f local = hitPoint.sub(
                guiPosition.sub(planeRight.mul(0.5f, new Vector3f()), new Vector3f())
                        .sub(planeUp.mul(0.5f, new Vector3f()))
        );

        float rawU = local.dot(planeRight);
        float rawV = local.dot(planeUp);

        // 6) Normalize by GUI size (1.5 units?) and scale/aspect
        float aspect = ClientContext.guiManager.getScaledAspectRatio();
        float u = (rawU - 0.5f) / (1.5f * effectiveScale) + 0.5f;
        float v = 1f - ((rawV - 0.5f) / (1.5f * effectiveScale * aspect) + 0.5f);

        return new Vector3f(u, v, t / worldScale);
    }




    private record CursorResult(Vector3fc cursorPos, VROverlay collidedOverlay) {
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
        boolean supportsTwoHandedCursor(){
            return focusedOverlay != null && focusedOverlay.supportsTwoHandedCursor();
        }

        double getCursorLength() {
            return isFocused() ? cursorPos.z() : -1;
        }
    }
}
